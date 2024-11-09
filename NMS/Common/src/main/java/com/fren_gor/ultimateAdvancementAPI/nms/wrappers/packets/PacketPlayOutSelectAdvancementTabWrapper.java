package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Wrapper class for NMS {@code PacketPlayOutSelectAdvancementTab}.
 */
public abstract class PacketPlayOutSelectAdvancementTabWrapper implements ISendable {

    private static final Constructor<? extends PacketPlayOutSelectAdvancementTabWrapper> selectNoneConstructor, selectConstructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(PacketPlayOutSelectAdvancementTabWrapper.class);
        Preconditions.checkNotNull(clazz, "PacketPlayOutSelectAdvancementTabWrapper implementation not found.");
        try {
            selectNoneConstructor = clazz.getDeclaredConstructor();
            selectConstructor = clazz.getDeclaredConstructor(MinecraftKeyWrapper.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Couldn't initialize PacketPlayOutSelectAdvancementTabWrapper.", e);
        }
    }

    /**
     * Creates a new {@code PacketPlayOutSelectAdvancementTabWrapper} which deselect any selected tab in the advancement GUI.
     *
     * @return A new {@code PacketPlayOutSelectAdvancementTabWrapper} which deselect any selected tab in the advancement GUI.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static PacketPlayOutSelectAdvancementTabWrapper craftSelectNone() throws ReflectiveOperationException {
        return selectNoneConstructor.newInstance();
    }

    /**
     * Creates a new {@code PacketPlayOutSelectAdvancementTabWrapper} which selects the specified tab.
     *
     * @param key The key of the root advancement of the tab to select.
     * @return A new {@code PacketPlayOutSelectAdvancementTabWrapper} which selects the specified tab.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static PacketPlayOutSelectAdvancementTabWrapper craftSelect(@NotNull MinecraftKeyWrapper key) throws ReflectiveOperationException {
        Preconditions.checkNotNull(key, "MinecraftKeyWrapper is null.");
        return selectConstructor.newInstance(key);
    }
}
