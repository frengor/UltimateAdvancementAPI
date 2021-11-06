package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;

public abstract class PacketPlayOutAdvancementsWrapper implements ISendable {

    private static Constructor<? extends PacketPlayOutAdvancementsWrapper> resetConstructor, sendConstructor, removeConstructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(PacketPlayOutAdvancementsWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            resetConstructor = clazz.getDeclaredConstructor();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            sendConstructor = clazz.getDeclaredConstructor(Map.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            removeConstructor = clazz.getDeclaredConstructor(Set.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static PacketPlayOutAdvancementsWrapper craftResetPacket() throws ReflectiveOperationException {
        return resetConstructor.newInstance();
    }

    public static PacketPlayOutAdvancementsWrapper craftSendPacket(@NotNull Map<AdvancementWrapper, Integer> toSend) throws ReflectiveOperationException {
        return sendConstructor.newInstance(toSend);
    }

    public static PacketPlayOutAdvancementsWrapper craftRemovePacket(@NotNull Set<MinecraftKeyWrapper> toRemove) throws ReflectiveOperationException {
        return removeConstructor.newInstance(toRemove);
    }
}
