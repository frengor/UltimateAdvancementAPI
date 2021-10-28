package com.fren_gor.ultimateAdvancementAPI.nms.wrappers;

import com.fren_gor.ultimateAdvancementAPI.nms.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Wrapper class for NMS {@code MinecraftKey}.
 */
public abstract class MinecraftKeyWrapper implements Comparable<MinecraftKeyWrapper> {

    private static Constructor<? extends MinecraftKeyWrapper> minecraftKeyConstructor, namespacedKeyConstructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(MinecraftKeyWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            minecraftKeyConstructor = clazz.getConstructor(Object.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            namespacedKeyConstructor = clazz.getConstructor(String.class, String.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@code MinecraftKeyWrapper} with the same namespaced key of the provided NMS {@code MinecraftKey}.
     *
     * @param minecraftKey The NMS {@code MinecraftKey}.
     * @return A new {@code MinecraftKeyWrapper} with the same namespaced key of the provided NMS {@code MinecraftKey}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     * @throws ClassCastException If the provided object is not a NMS {@code MinecraftKey}.
     */
    @NotNull
    public static MinecraftKeyWrapper craft(@NotNull Object minecraftKey) throws ReflectiveOperationException, ClassCastException {
        return minecraftKeyConstructor.newInstance(minecraftKey);
    }

    /**
     * Creates a new {@code MinecraftKeyWrapper} with the provided namespace and key.
     * <p>The namespace must match the following pattern: {@code [a-z0-9_.-]+}.
     * <p>The key must match the following pattern: {@code [a-z0-9_.-/]+}.
     *
     * @param namespace The namespace.
     * @param key The key.
     * @return A new {@code MinecraftKeyWrapper} with the provided namespace and key.
     * @throws ReflectiveOperationException If reflections goes wrong.
     * @throws IllegalArgumentException If the provided namespace or key does not match their pattern.
     */
    @NotNull
    public static MinecraftKeyWrapper craft(@NotNull String namespace, @NotNull String key) throws ReflectiveOperationException, IllegalArgumentException {
        return namespacedKeyConstructor.newInstance(namespace, key);
    }

    /**
     * Gets the namespace.
     *
     * @return The namespace.
     */
    @NotNull
    public abstract String getNamespace();

    /**
     * Gets the key.
     *
     * @return The key.
     */
    @NotNull
    public abstract String getKey();

    @NotNull
    public abstract Object getNMSKey();
}
