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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class UAAPIExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Namespace UAAPI_NAMESPACE = Namespace.create(UAAPIExtension.class);
    private static final String ADV_KEY_NAMESPACE = "a-namespace_";
    private static final String ADV_KEY_PREFIX = "a_-key/";

    private static final AtomicInteger ADV_KEYS_UNIQUE_KEY = new AtomicInteger();

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        var testMethod = extensionContext.getRequiredTestMethod();
        initServerMock(extensionContext, testMethod);

        var testClass = extensionContext.getRequiredTestClass();
        var testInstance = extensionContext.getRequiredTestInstance();
        var fields = FieldUtils.getFieldsListWithAnnotation(testClass, AutoInject.class)
                .stream()
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .toList();
        if (fields.isEmpty()) {
            return;
        }

        var toInject = fields.stream()
                .filter(field -> shouldInject(field.getType()))
                .toList();
        if (!toInject.isEmpty()) {
            initStore(extensionContext);
            toInject.forEach(field -> inject(field, testInstance, extensionContext));
        }

        var keysToInject = fields.stream()
                .filter(field -> field.getType() == AdvancementKey.class)
                .toList();
        if (!keysToInject.isEmpty()) {
            keysToInject.forEach(field -> injectField(field, testInstance, createAdvKey()));
        }
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
        return parameterContext.getParameter().getType() == AdvancementKey.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return createAdvKey();
    }

    private boolean shouldInject(Class<?> type) {
        return type == ServerMock.class || type == AdvancementMain.class || type == DatabaseManagerMock.class || type == DatabaseManager.class;
    }

    private void inject(Field field, Object testInstance, ExtensionContext extensionContext) {
        var store = extensionContext.getStore(UAAPI_NAMESPACE);
        Object val = Objects.requireNonNull(store.get(field.getType()), "Cannot find stored object for " + field.getType().getName());
        injectField(field, testInstance, val);
    }

    private AdvancementKey createAdvKey() {
        return new AdvancementKey(ADV_KEY_NAMESPACE, ADV_KEY_PREFIX + ADV_KEYS_UNIQUE_KEY.incrementAndGet());
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
}
