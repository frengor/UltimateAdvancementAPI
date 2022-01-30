package com.fren_gor.ultimateAdvancementAPI.tests;

import com.google.common.base.Preconditions;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.ExceptionMethod;
import org.bukkit.Server;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;

public final class InterfaceImplementer {

    private static final char[] PLAYER_NAME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    @SuppressWarnings("unchecked")
    public static <T> T implement(@NotNull final Class<T> interfaceToImpl, ImplementedMethod... implementedMethods) {
        Preconditions.checkArgument(interfaceToImpl.isInterface(), "Passed class is not an interface.");
        Map<String, ImplementedMethod> methods = Arrays.stream(implementedMethods).collect(Collectors.toMap(i -> i.getName(), i -> i));
        return (T) Proxy.newProxyInstance(InterfaceImplementer.class.getClassLoader(), new Class[]{interfaceToImpl}, (object, method, args) -> {
            var function = methods.get(method.getName());
            if (function != null) {
                return function.invoke(object, args);
            }
            return switch (method.getName()) {
                case "equals" -> object == args[0];
                case "hashCode" -> System.identityHashCode(object);
                case "toString" -> interfaceToImpl.getName() + '@' + Integer.toHexString(System.identityHashCode(object));
                default -> throw new UnsupportedOperationException(method.getName());
            };
        });
    }

    @NotNull
    public static Player newFakePlayer(@NotNull UUID uuid) {
        StringBuilder b = new StringBuilder(16);
        Random rand = new Random();
        for (int i = 0; i < 16; i++) {
            b.append(PLAYER_NAME_CHARS[rand.nextInt(PLAYER_NAME_CHARS.length)]);
        }
        return newFakePlayer(uuid, b.toString());
    }

    @NotNull
    public static Player newFakePlayer(@NotNull UUID uuid, @NotNull String name) {
        Preconditions.checkNotNull(uuid, "UUID not null.");
        Preconditions.checkNotNull(name, "Name not null.");
        return InterfaceImplementer.implement(Player.class,
                new ImplementedMethod("getUniqueId", (o, args) -> uuid),
                new ImplementedMethod("getName", (o, args) -> name),
                new ImplementedMethod("getDisplayName", (o, args) -> name),
                new ImplementedMethod("getType", (o, args) -> EntityType.PLAYER),
                new ImplementedMethod("equals", (o, args) -> {
                    Object other = args[0];
                    if (o == other) return true;
                    if (!(other instanceof Player that)) return false;

                    return Objects.equals(uuid, that.getUniqueId());
                }),
                new ImplementedMethod("hashCode", (o, args) -> uuid.hashCode()),
                new ImplementedMethod("toString", (o, args) -> "Player{uuid=" + uuid + '}')
        );
    }

    @NotNull
    public static Plugin newFakePlugin(@NotNull String name) {
        Preconditions.checkNotNull(name, "Name is null.");
        Logger logger = Logger.getLogger(name);
        return InterfaceImplementer.implement(Plugin.class,
                new ImplementedMethod("getLogger", (o, args) -> logger),
                new ImplementedMethod("getName", (o, args) -> name),
                new ImplementedMethod("isEnabled", (o, args) -> true),
                new ImplementedMethod("equals", (o, args) -> {
                    Object other = args[0];
                    if (o == other) return true;
                    if (!(other instanceof Plugin that)) return false;

                    // Fake plugin is always enabled
                    return that.isEnabled() && name.equals(that.getName());
                }),
                new ImplementedMethod("hashCode", (o, args) -> name.hashCode()),
                new ImplementedMethod("toString", (o, args) -> "Plugin{name=" + name + '}')
        );
    }

    public static Server newFakeServer() {
        var fakeServer = new ByteBuddy()
                .subclass(Object.class)
                .implement(Server.class)
                .name("org.bukkit.craftbukkit.serverVersion1_17_R1.FakeServer")
                .method(isDeclaredBy(Server.class)).intercept(ExceptionMethod.throwing(UnsupportedOperationException.class))
                .make().load(InterfaceImplementer.class.getClassLoader());
        try {
            return (Server) fakeServer.getLoaded().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class ImplementedMethod {

        private final String name;
        private final BiFunction<Object, Object[], Object> methodFunction;

        public ImplementedMethod(@NotNull String name, BiFunction<Object, Object[], Object> methodFunction) {
            Preconditions.checkNotNull(name, "Name is null.");
            Preconditions.checkNotNull(methodFunction, "Function is null.");
            this.name = name;
            this.methodFunction = methodFunction;
        }

        @NotNull
        public String getName() {
            return name;
        }

        public Object invoke(@NotNull Object object, Object... args) {
            return methodFunction.apply(object, args);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImplementedMethod that = (ImplementedMethod) o;

            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
