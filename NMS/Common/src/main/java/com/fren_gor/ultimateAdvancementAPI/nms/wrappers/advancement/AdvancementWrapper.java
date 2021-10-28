package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Constructor;

public abstract class AdvancementWrapper {

    private static Constructor<? extends AdvancementWrapper> rootAdvancementWrapperConstructor, baseAdvancementWrapperConstructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(AdvancementWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            baseAdvancementWrapperConstructor = clazz.getConstructor(MinecraftKeyWrapper.class, AdvancementWrapper.class, AdvancementDisplayWrapper.class, int.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            rootAdvancementWrapperConstructor = clazz.getConstructor(MinecraftKeyWrapper.class, String.class, AdvancementDisplayWrapper.class, int.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static AdvancementWrapper craftRootAdvancement(@NotNull MinecraftKeyWrapper key, @NotNull String backgroundPath, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) throws ReflectiveOperationException {
        return baseAdvancementWrapperConstructor.newInstance(key, backgroundPath, display, maxCriteria);
    }

    @NotNull
    public static AdvancementWrapper craftBaseAdvancement(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) throws ReflectiveOperationException {
        return rootAdvancementWrapperConstructor.newInstance(key, parent, display, maxCriteria);
    }

    @NotNull
    public abstract MinecraftKeyWrapper getKey();

    @Nullable
    public abstract AdvancementWrapper getParent();

    @NotNull
    public abstract AdvancementDisplayWrapper getDisplay();

    @Range(from = 1, to = Integer.MAX_VALUE)
    public abstract int getMaxCriteria();

    @NotNull
    public abstract Object getNMSAdvancement();
}
