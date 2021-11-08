package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public abstract class PacketPlayOutSelectAdvancementTabWrapper implements ISendable {

    private static Constructor<? extends PacketPlayOutSelectAdvancementTabWrapper> selectNoneConstructor, selectConstructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(PacketPlayOutSelectAdvancementTabWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            selectNoneConstructor = clazz.getDeclaredConstructor();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            selectConstructor = clazz.getDeclaredConstructor(MinecraftKeyWrapper.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static PacketPlayOutSelectAdvancementTabWrapper craftSelectNone() throws ReflectiveOperationException {
        return selectNoneConstructor.newInstance();
    }

    @NotNull
    public static PacketPlayOutSelectAdvancementTabWrapper craftSelect(@NotNull MinecraftKeyWrapper key) throws ReflectiveOperationException {
        return selectConstructor.newInstance(key);
    }
}
