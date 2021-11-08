package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.AbstractWrapper;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

public abstract class AdvancementDisplayWrapper extends AbstractWrapper {

    private static Constructor<? extends AdvancementDisplayWrapper> constructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(AdvancementDisplayWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            constructor = clazz.getDeclaredConstructor(ItemStack.class, String.class, String.class, AdvancementFrameTypeWrapper.class, float.class, float.class, boolean.class, boolean.class, boolean.class, String.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y) throws ReflectiveOperationException {
        return craft(icon, title, description, frameType, x, y, null);
    }

    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, @Nullable String backgroundTexture) throws ReflectiveOperationException {
        return craft(icon, title, description, frameType, x, y, false, false, false, backgroundTexture);
    }

    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) throws ReflectiveOperationException {
        return craft(icon, title, description, frameType, x, y, showToast, announceChat, hidden, null);
    }

    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) throws ReflectiveOperationException {
        return constructor.newInstance(icon.clone(), title, description, frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }

    @NotNull
    public abstract ItemStack getIcon();

    @NotNull
    public abstract String getTitle();

    @NotNull
    public abstract String getDescription();

    @NotNull
    public abstract AdvancementFrameTypeWrapper getAdvancementFrameType();

    public abstract float getX();

    public abstract float getY();

    public abstract boolean doesShowToast();

    public abstract boolean doesAnnounceToChat();

    public abstract boolean isHidden();

    @Nullable
    public abstract String getBackgroundTexture();
}
