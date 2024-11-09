package com.fren_gor.ultimateAdvancementAPI.nms.wrappers;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Wrapper class for NMS {@code MinecraftKey}.
 */
public abstract class MinecraftKeyWrapper extends AbstractWrapper implements Comparable<MinecraftKeyWrapper> {

    private static final Constructor<? extends MinecraftKeyWrapper> minecraftKeyConstructor, namespacedKeyConstructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(MinecraftKeyWrapper.class);
        Preconditions.checkNotNull(clazz, "MinecraftKeyWrapper implementation not found.");
        try {
            minecraftKeyConstructor = clazz.getDeclaredConstructor(Object.class);
            namespacedKeyConstructor = clazz.getDeclaredConstructor(String.class, String.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Couldn't initialize MinecraftKeyWrapper.", e);
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

    @Override
    public String toString() {
        return getNamespace() + ':' + getKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MinecraftKeyWrapper that = (MinecraftKeyWrapper) o;

        if (!getNamespace().equals(that.getNamespace())) return false;
        return getKey().equals(that.getKey());
    }

    @Override
    public int hashCode() {
        int result = getNamespace().hashCode();
        result = 31 * result + getKey().hashCode();
        return result;
    }
}
