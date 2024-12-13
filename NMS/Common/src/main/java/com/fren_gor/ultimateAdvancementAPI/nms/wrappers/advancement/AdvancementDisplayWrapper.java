package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.AbstractWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

/**
 * Wrapper class for NMS {@code AdvancementDisplay}.
 */
public abstract class AdvancementDisplayWrapper extends AbstractWrapper {

    private static Constructor<? extends AdvancementDisplayWrapper> constructor;
    private static Constructor<? extends AdvancementDisplayWrapper> componentConstructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(AdvancementDisplayWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            constructor = clazz.getDeclaredConstructor(ItemStack.class, String.class, String.class, AdvancementFrameTypeWrapper.class, float.class, float.class, boolean.class, boolean.class, boolean.class, String.class);
            componentConstructor = clazz.getDeclaredConstructor(ItemStack.class, BaseComponent.class, BaseComponent.class, AdvancementFrameTypeWrapper.class, float.class, float.class, boolean.class, boolean.class, boolean.class, String.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@code AdvancementDisplayWrapper}.
     * <p>The returned {@code AdvancementDisplayWrapper} is not hidden, doesn't show toast, doesn't announce to chat,
     * and doesn't have a background texture.
     *
     * @param icon The icon of the advancement.
     * @param title The title of the advancement.
     * @param description The description of the advancement.
     * @param frameType The frame type of the advancement.
     * @param x The x coordinate in the advancement GUI.
     * @param y The y coordinate in the advancement GUI.
     * @return A new {@code AdvancementDisplayWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y) throws ReflectiveOperationException {
        return craft(icon, title, description, frameType, x, y, null);
    }

    /**
     * Creates a new {@code AdvancementDisplayWrapper}.
     * <p>The returned {@code AdvancementDisplayWrapper} is not hidden, doesn't show toast, and doesn't announce to chat.
     *
     * @param icon The icon of the advancement.
     * @param title The title of the advancement.
     * @param description The description of the advancement.
     * @param frameType The frame type of the advancement.
     * @param x The x coordinate in the advancement GUI.
     * @param y The y coordinate in the advancement GUI.
     * @param backgroundTexture The path of the background texture of the advancement GUI. May be {@code null} if the advancement doesn't have a background texture.
     * @return A new {@code AdvancementDisplayWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, @Nullable String backgroundTexture) throws ReflectiveOperationException {
        return craft(icon, title, description, frameType, x, y, false, false, false, backgroundTexture);
    }

    /**
     * Creates a new {@code AdvancementDisplayWrapper}.
     * <p>The returned {@code AdvancementDisplayWrapper} doesn't have a background texture.
     *
     * @param icon The icon of the advancement.
     * @param title The title of the advancement.
     * @param description The description of the advancement.
     * @param frameType The frame type of the advancement.
     * @param x The x coordinate in the advancement GUI.
     * @param y The y coordinate in the advancement GUI.
     * @param showToast Whether to show toast on grant.
     * @param announceChat Whether to announce grants to chat.
     * @param hidden Whether the advancement should be not visible in the advancement GUI. Connections to other advancements are always displayed.
     * @return A new {@code AdvancementDisplayWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) throws ReflectiveOperationException {
        return craft(icon, title, description, frameType, x, y, showToast, announceChat, hidden, null);
    }

    /**
     * Creates a new {@code AdvancementDisplayWrapper}.
     *
     * @param icon The icon of the advancement.
     * @param title The title of the advancement.
     * @param description The description of the advancement.
     * @param frameType The frame type of the advancement.
     * @param x The x coordinate in the advancement GUI.
     * @param y The y coordinate in the advancement GUI.
     * @param showToast Whether to show toast on grant.
     * @param announceChat Whether to announce grants to chat.
     * @param hidden Whether the advancement should be not visible in the advancement GUI. Connections to other advancements are always displayed.
     * @param backgroundTexture The path of the background texture of the advancement GUI. May be {@code null} if the advancement doesn't have a background texture.
     * @return A new {@code AdvancementDisplayWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) throws ReflectiveOperationException {
        return constructor.newInstance(icon.clone(), title, description, frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }

    /**
     * Creates a new {@code AdvancementDisplayWrapper}.
     * <p>The returned {@code AdvancementDisplayWrapper} is not hidden, doesn't show toast, doesn't announce to chat,
     * and doesn't have a background texture.
     *
     * @param icon The icon of the advancement.
     * @param title The title of the advancement.
     * @param description The description of the advancement.
     * @param frameType The frame type of the advancement.
     * @param x The x coordinate in the advancement GUI.
     * @param y The y coordinate in the advancement GUI.
     * @return A new {@code AdvancementDisplayWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull BaseComponent description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y) throws ReflectiveOperationException {
        return craft(icon, title, description, frameType, x, y, null);
    }

    /**
     * Creates a new {@code AdvancementDisplayWrapper}.
     * <p>The returned {@code AdvancementDisplayWrapper} is not hidden, doesn't show toast, and doesn't announce to chat.
     *
     * @param icon The icon of the advancement.
     * @param title The title of the advancement.
     * @param description The description of the advancement.
     * @param frameType The frame type of the advancement.
     * @param x The x coordinate in the advancement GUI.
     * @param y The y coordinate in the advancement GUI.
     * @param backgroundTexture The path of the background texture of the advancement GUI. May be {@code null} if the advancement doesn't have a background texture.
     * @return A new {@code AdvancementDisplayWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull BaseComponent description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, @Nullable String backgroundTexture) throws ReflectiveOperationException {
        return craft(icon, title, description, frameType, x, y, false, false, false, backgroundTexture);
    }

    /**
     * Creates a new {@code AdvancementDisplayWrapper}.
     * <p>The returned {@code AdvancementDisplayWrapper} doesn't have a background texture.
     *
     * @param icon The icon of the advancement.
     * @param title The title of the advancement.
     * @param description The description of the advancement.
     * @param frameType The frame type of the advancement.
     * @param x The x coordinate in the advancement GUI.
     * @param y The y coordinate in the advancement GUI.
     * @param showToast Whether to show toast on grant.
     * @param announceChat Whether to announce grants to chat.
     * @param hidden Whether the advancement should be not visible in the advancement GUI. Connections to other advancements are always displayed.
     * @return A new {@code AdvancementDisplayWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull BaseComponent description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) throws ReflectiveOperationException {
        return craft(icon, title, description, frameType, x, y, showToast, announceChat, hidden, null);
    }

    /**
     * Creates a new {@code AdvancementDisplayWrapper}.
     *
     * @param icon The icon of the advancement.
     * @param title The title of the advancement.
     * @param description The description of the advancement.
     * @param frameType The frame type of the advancement.
     * @param x The x coordinate in the advancement GUI.
     * @param y The y coordinate in the advancement GUI.
     * @param showToast Whether to show toast on grant.
     * @param announceChat Whether to announce grants to chat.
     * @param hidden Whether the advancement should be not visible in the advancement GUI. Connections to other advancements are always displayed.
     * @param backgroundTexture The path of the background texture of the advancement GUI. May be {@code null} if the advancement doesn't have a background texture.
     * @return A new {@code AdvancementDisplayWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static AdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull BaseComponent description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) throws ReflectiveOperationException {
        return componentConstructor.newInstance(icon.clone(), title, description, frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }

    /**
     * Gets the icon of the advancement.
     *
     * @return The icon of the advancement.
     */
    @NotNull
    public abstract ItemStack getIcon();

    /**
     * Gets the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract String getTitle();

    /**
     * Gets the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @NotNull
    public abstract String getDescription();

    /**
     * Gets the frame type of the advancement.
     *
     * @return The frame type of the advancement.
     */
    @NotNull
    public abstract AdvancementFrameTypeWrapper getAdvancementFrameType();

    /**
     * Gets the x coordinate of the advancement in the advancement GUI.
     *
     * @return The x coordinate of the advancement in the advancement GUI.
     */
    public abstract float getX();

    /**
     * Gets the y coordinate of the advancement in the advancement GUI.
     *
     * @return The y coordinate of the advancement in the advancement GUI.
     */
    public abstract float getY();

    /**
     * Returns whether toast is shown on grant.
     *
     * @return Whether toast is shown on grant.
     */
    public abstract boolean doesShowToast();

    /**
     * Returns whether grants are announced to chat.
     *
     * @return Whether grants are announced to chat.
     */
    public abstract boolean doesAnnounceToChat();

    /**
     * Returns whether the advancement should be hidden in the advancement GUI.
     *
     * @return Whether the advancement should be hidden in the advancement GUI.
     */
    public abstract boolean isHidden();

    /**
     * Gets the path of the background texture of the advancement GUI.
     *
     * @return The path of the background texture of the advancement GUI, or {@code null} if the advancement doesn't have a background texture.
     */
    @Nullable
    public abstract String getBackgroundTexture();
}
