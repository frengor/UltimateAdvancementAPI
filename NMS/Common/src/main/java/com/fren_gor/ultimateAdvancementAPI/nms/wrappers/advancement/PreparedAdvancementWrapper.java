package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Constructor;

public abstract class PreparedAdvancementWrapper {

    private static Constructor<? extends PreparedAdvancementWrapper> constructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(PreparedAdvancementWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            constructor = clazz.getDeclaredConstructor(MinecraftKeyWrapper.class, AdvancementDisplayWrapper.class, int.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static PreparedAdvancementWrapper craft(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) throws ReflectiveOperationException {
        return constructor.newInstance(key, display, maxCriteria);
    }

    @NotNull
    public abstract MinecraftKeyWrapper getKey();

    @NotNull
    public abstract AdvancementDisplayWrapper getDisplay();

    @Range(from = 1, to = Integer.MAX_VALUE)
    public abstract int getMaxCriteria();

    @NotNull
    public abstract AdvancementWrapper toRootAdvancementWrapper();

    @NotNull
    public abstract AdvancementWrapper toBaseAdvancementWrapper(@NotNull AdvancementWrapper parent);

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
