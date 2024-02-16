package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Constructor;

/**
 * {@code PreparedAdvancementWrapper} instances can be converted into an {@link AdvancementWrapper}
 * using {@link #toAdvancementWrapper(AdvancementDisplayWrapper)} or {@link #toAdvancementWrapperWithParent(AdvancementDisplayWrapper, PreparedAdvancementWrapper)}.
 */
public abstract class PreparedAdvancementWrapper {

    private static Constructor<? extends PreparedAdvancementWrapper> constructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(PreparedAdvancementWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            constructor = clazz.getDeclaredConstructor(MinecraftKeyWrapper.class, PreparedAdvancementWrapper.class, int.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
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
     * Converts this {@code PreparedAdvancementWrapper} into an {@link AdvancementWrapper}.
     *
     * @param display The display wrapper of this advancement.
     * @return A new {@link AdvancementWrapper} derived from this {@code PreparedAdvancementWrapper}.
     */
    @NotNull
    public abstract AdvancementWrapper toAdvancementWrapper(@NotNull AdvancementDisplayWrapper display);

    /**
     * Converts this {@code PreparedAdvancementWrapper} into an {@link AdvancementWrapper} with a specific parent advancement.
     *
     * @param parent The parent of the returned advancement.
     * @param display The display wrapper of this advancement.
     * @return A new {@link AdvancementWrapper} derived from this {@code PreparedAdvancementWrapper}.
     */
    @NotNull
    public abstract AdvancementWrapper toAdvancementWrapperWithParent(@NotNull AdvancementDisplayWrapper display, @NotNull PreparedAdvancementWrapper parent);

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
