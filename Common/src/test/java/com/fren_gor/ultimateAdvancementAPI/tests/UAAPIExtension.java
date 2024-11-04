package com.fren_gor.ultimateAdvancementAPI.tests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils.AbstractMockedServer;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class UAAPIExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Namespace UAAPI_NAMESPACE = Namespace.create(UAAPIExtension.class);
    private static final String ADV_KEY_NAMESPACE = "a-namespace_";
    private static final String ADV_KEY_PREFIX = "a_-key/";

    private static final AtomicInteger ADV_KEYS_UNIQUE_KEY = new AtomicInteger();
    private static final Set<UUID> GENERATED_UUIDS = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        boolean isNoAdvMain = isNoAdvancementMain(extensionContext);

        initServerMock(extensionContext, extensionContext.getRequiredTestMethod());
        if (!isNoAdvMain) {
            initStore(extensionContext);
        }

        var testClass = extensionContext.getRequiredTestClass();
        var testInstance = extensionContext.getRequiredTestInstance();
        var fields = FieldUtils.getFieldsListWithAnnotation(testClass, AutoInject.class)
                .stream()
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .toList();
        if (fields.isEmpty()) {
            return;
        }

        fields.stream()
                .filter(field -> shouldInject(field.getType(), isNoAdvMain))
                .forEach(field -> inject(field, testInstance, extensionContext));

        fields.stream()
                .filter(field -> field.getType() == AdvancementKey.class)
                .forEach(field -> injectField(field, testInstance, createAdvKey()));

        fields.stream()
                .filter(field -> field.getType() == UUID.class)
                .forEach(field -> injectField(field, testInstance, createUUID()));
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        var store = extensionContext.getStore(UAAPI_NAMESPACE);
        store.remove(AdvancementKey.class, AtomicInteger.class);
        store.remove(DatabaseManager.class, DatabaseManager.class);
        var dbManagerMock = store.remove(DatabaseManagerMock.class, DatabaseManagerMock.class);
        var main = store.remove(AdvancementMain.class, AdvancementMain.class);
        store.remove(ServerMock.class, ServerMock.class);
        try {
            if (dbManagerMock != null) {
                dbManagerMock.disable();
            }
        } finally {
            try {
                if (main != null) {
                    main.disable();
                }
            } finally {
                if (MockBukkit.isMocked()) {
                    MockBukkit.unmock();
                }
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();
        return type == AdvancementKey.class || type == UUID.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();
        return type == AdvancementKey.class ? createAdvKey() : createUUID();
    }

    private boolean shouldInject(Class<?> type, boolean isNoAdvMain) {
        return type == ServerMock.class || (
                !isNoAdvMain && (
                        type == AdvancementMain.class ||
                        type == DatabaseManagerMock.class ||
                        type == DatabaseManager.class
                )
        );
    }

    private void inject(Field field, Object testInstance, ExtensionContext extensionContext) {
        var store = extensionContext.getStore(UAAPI_NAMESPACE);
        Object val = Objects.requireNonNull(store.get(field.getType()), "Cannot find stored object for " + field.getType().getName());
        injectField(field, testInstance, val);
    }

    private AdvancementKey createAdvKey() {
        return new AdvancementKey(ADV_KEY_NAMESPACE, ADV_KEY_PREFIX + ADV_KEYS_UNIQUE_KEY.incrementAndGet());
    }

    private UUID createUUID() {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (!GENERATED_UUIDS.add(uuid)); // Although improbable, make sure the generated uuid is truly unique
        return uuid;
    }

    private void injectField(Field field, Object testInstance, Object value) {
        try {
            field.setAccessible(true);
            field.set(testInstance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initServerMock(ExtensionContext extensionContext, Method testMethod) throws Exception {
        var store = extensionContext.getStore(UAAPI_NAMESPACE);
        if (MockBukkit.isMocked() || store.get(ServerMock.class) != null) {
            throw new IllegalStateException("Already mocked.");
        }
        @Nullable MockedServerClass serverClassAnnotation = testMethod.getAnnotation(MockedServerClass.class);
        AbstractMockedServer server;
        if (serverClassAnnotation == null) {
            server = Utils.mockServer();
        } else {
            var serverClass = serverClassAnnotation.serverClass();
            server = Utils.mockServerWith(serverClass.getConstructor().newInstance());
        }
        store.put(ServerMock.class, Objects.requireNonNull(server));
    }

    private void initStore(ExtensionContext extensionContext) {
        Preconditions.checkArgument(MockBukkit.isMocked(), "Server is not mocked");
        var store = extensionContext.getStore(UAAPI_NAMESPACE);
        if (store.get(AdvancementMain.class) != null) {
            throw new IllegalStateException("Duplicated init of AdvancementMain");
        }
        store.put(AdvancementMain.class, Utils.newAdvancementMain(MockBukkit.createMockPlugin("testPlugin"), main -> {
            var dbManagerMock = new DatabaseManagerMock(main, MockBukkit.getMock());
            if (store.get(DatabaseManagerMock.class) != null) {
                throw new IllegalStateException("Duplicated init of DatabaseManagerMock");
            }
            store.put(DatabaseManagerMock.class, dbManagerMock);
            if (store.get(DatabaseManager.class) != null) {
                throw new IllegalStateException("Duplicated init of DatabaseManager");
            }
            store.put(DatabaseManager.class, dbManagerMock.getDatabaseManager());
            return dbManagerMock.getDatabaseManager();
        }));
    }

    private boolean isNoAdvancementMain(ExtensionContext extensionContext) {
        return extensionContext.getRequiredTestClass().getAnnotation(NoAdvancementMain.class) != null ||
                extensionContext.getRequiredTestMethod().getAnnotation(NoAdvancementMain.class) != null;
    }
}
