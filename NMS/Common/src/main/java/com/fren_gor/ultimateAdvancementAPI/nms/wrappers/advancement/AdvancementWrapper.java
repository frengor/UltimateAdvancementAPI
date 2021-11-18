package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.AbstractWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Constructor;

/**
 * Wrapper class for NMS {@code Advancement}.
 */
public abstract class AdvancementWrapper extends AbstractWrapper {

    private static Constructor<? extends AdvancementWrapper> rootAdvancementWrapperConstructor, baseAdvancementWrapperConstructor;
    private MinecraftKeyWrapper key;

    static {
        var clazz = ReflectionUtil.getWrapperClass(AdvancementWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            rootAdvancementWrapperConstructor = clazz.getDeclaredConstructor(MinecraftKeyWrapper.class, AdvancementDisplayWrapper.class, int.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            baseAdvancementWrapperConstructor = clazz.getDeclaredConstructor(MinecraftKeyWrapper.class, AdvancementWrapper.class, AdvancementDisplayWrapper.class, int.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@code AdvancementWrapper} for a root advancement.
     *
     * @param key The namespaced key wrapper of the advancement.
     * @param display The display wrapper of the advancement.
     * @param maxProgression The maximum progression of the advancement.
     * @return A new {@code AdvancementWrapper} for a RootAdvancement.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static AdvancementWrapper craftRootAdvancement(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) throws ReflectiveOperationException {
        return rootAdvancementWrapperConstructor.newInstance(key, display, maxProgression);
    }

    /**
     * Creates a new {@code AdvancementWrapper} for a base advancement.
     *
     * @param key The namespaced key wrapper of the advancement.
     * @param parent The wrapper of the parent advancement.
     * @param display The display wrapper of the advancement.
     * @param maxProgression The maximum progression of the advancement.
     * @return A new {@code AdvancementWrapper} for a BaseAdvancement.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static AdvancementWrapper craftBaseAdvancement(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) throws ReflectiveOperationException {
        return baseAdvancementWrapperConstructor.newInstance(key, parent, display, maxProgression);
    }

    /**
     * Gets the namespaced key wrapper of this advancement.
     *
     * @return The namespaced key wrapper of this advancement.
     */
    @NotNull
    public abstract MinecraftKeyWrapper getKey();

    /**
     * Gets the parent advancement wrapper of this advancement. Returns {@code null} if this advancement is a root advancement.
     *
     * @return The parent advancement wrapper of this advancement, or {@code null} if this advancement is a root advancement.
     */
    @Nullable
    public abstract AdvancementWrapper getParent();

    /**
     * Gets the display wrapper of this advancement.
     *
     * @return The display wrapper of this advancement.
     */
    @NotNull
    public abstract AdvancementDisplayWrapper getDisplay();

    /**
     * Gets the maximum progression of this advancement.
     *
     * @return The maximum progression of this advancement.
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    public abstract int getMaxProgression();

    @Override
    public String toString() {
        return "AdvancementWrapper{key=" + getKey() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdvancementWrapper that = (AdvancementWrapper) o;
        return getKey().equals(that.getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}
