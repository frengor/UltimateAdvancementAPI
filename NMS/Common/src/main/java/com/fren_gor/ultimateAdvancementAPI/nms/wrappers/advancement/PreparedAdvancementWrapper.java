package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Constructor;

/**
 * {@code PreparedAdvancementWrapper} instances can be converted into an {@link AdvancementWrapper}
 * using {@link #toAdvancementWrapper(AdvancementDisplayWrapper)}.
 */
public abstract class PreparedAdvancementWrapper {

    private static final Constructor<? extends PreparedAdvancementWrapper> constructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(PreparedAdvancementWrapper.class);
        Preconditions.checkNotNull(clazz, "PreparedAdvancementWrapper implementation not found.");
        try {
            constructor = clazz.getDeclaredConstructor(MinecraftKeyWrapper.class, PreparedAdvancementWrapper.class, int.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Couldn't initialize PreparedAdvancementWrapper.", e);
        }
    }

    /**
     * Creates a new {@code PreparedAdvancementWrapper}.
     *
     * @param key The namespaced key wrapper of the advancement.
     * @param maxProgression The maximum progression of the advancement.
     * @return A new {@code PreparedAdvancementWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static PreparedAdvancementWrapper craft(@NotNull MinecraftKeyWrapper key, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) throws ReflectiveOperationException {
        return craft(key, null, maxProgression);
    }

    /**
     * Creates a new {@code PreparedAdvancementWrapper}.
     *
     * @param key The namespaced key wrapper of the advancement.
     * @param parent The parent of this {@code PreparedAdvancementWrapper}.
     * @param maxProgression The maximum progression of the advancement.
     * @return A new {@code PreparedAdvancementWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static PreparedAdvancementWrapper craft(@NotNull MinecraftKeyWrapper key, @Nullable PreparedAdvancementWrapper parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) throws ReflectiveOperationException {
        Preconditions.checkNotNull(key, "MinecraftKeyWrapper is null.");
        Preconditions.checkArgument(maxProgression > 0, "Maximum progression cannot be <= 0");
        return constructor.newInstance(key, parent, maxProgression);
    }

    /**
     * Gets the namespaced key wrapper of the advancement.
     *
     * @return The namespaced key wrapper of the advancement.
     */
    @NotNull
    public abstract MinecraftKeyWrapper getKey();

    /**
     * Gets the parent of this advancement. Returns {@code null} if this advancement is a root advancement.
     *
     * @return The parent of this advancement, or {@code null} if this advancement is a root advancement.
     */
    @Nullable
    public abstract PreparedAdvancementWrapper getParent();

    /**
     * Gets the maximum progression of the advancement.
     *
     * @return The maximum progression of the advancement.
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    public abstract int getMaxProgression();

    /**
     * Creates a copy of this {@code PreparedAdvancementWrapper} with the provided parent advancement.
     *
     * @param parent The parent of the returned {@code PreparedAdvancementWrapper}, may be {@code null}.
     * @return A copy of this {@code PreparedAdvancementWrapper} with the provided parent advancement.
     */
    @NotNull
    @Contract("_ -> new")
    public abstract PreparedAdvancementWrapper withParent(@Nullable PreparedAdvancementWrapper parent);

    /**
     * Converts this {@code PreparedAdvancementWrapper} into an {@link AdvancementWrapper}.
     *
     * @param display The display wrapper of this advancement.
     * @return A new {@link AdvancementWrapper} derived from this {@code PreparedAdvancementWrapper}.
     */
    @NotNull
    public abstract AdvancementWrapper toAdvancementWrapper(@NotNull AdvancementDisplayWrapper display);

    @Override
    public String toString() {
        return "PreparedAdvancementWrapper{key=" + getKey() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreparedAdvancementWrapper that = (PreparedAdvancementWrapper) o;

        return getKey().equals(that.getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}
