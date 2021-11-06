package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.AbstractWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Constructor;

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

    @NotNull
    public static AdvancementWrapper craftRootAdvancement(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) throws ReflectiveOperationException {
        return rootAdvancementWrapperConstructor.newInstance(key, display, maxCriteria);
    }

    @NotNull
    public static AdvancementWrapper craftBaseAdvancement(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) throws ReflectiveOperationException {
        return baseAdvancementWrapperConstructor.newInstance(key, parent, display, maxCriteria);
    }

    @NotNull
    public abstract MinecraftKeyWrapper getKey();

    @Nullable
    public abstract AdvancementWrapper getParent();

    @NotNull
    public abstract AdvancementDisplayWrapper getDisplay();

    @Range(from = 1, to = Integer.MAX_VALUE)
    public abstract int getMaxCriteria();

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
