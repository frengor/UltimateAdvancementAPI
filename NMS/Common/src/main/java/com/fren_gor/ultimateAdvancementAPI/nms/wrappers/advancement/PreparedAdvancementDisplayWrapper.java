package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * {@code PreparedAdvancementDisplayWrapper} instances can be converted into an {@link AdvancementDisplayWrapper}
 * using {@link #toBaseAdvancementDisplay()} or {@link #toRootAdvancementDisplay(String)}.
 */
public abstract class PreparedAdvancementDisplayWrapper {

    private static Constructor<? extends PreparedAdvancementDisplayWrapper> constructor;

    static {
        var clazz = ReflectionUtil.getWrapperClass(PreparedAdvancementDisplayWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            constructor = clazz.getDeclaredConstructor(ItemStack.class, String.class, String.class, AdvancementFrameTypeWrapper.class, float.class, float.class, boolean.class, boolean.class, boolean.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@code PreparedAdvancementDisplayWrapper}.
     * <p>The returned {@code PreparedAdvancementDisplayWrapper} is not hidden, doesn't show toast, doesn't announce to chat.
     *
     * @param icon The icon of the advancement.
     * @param title The title of the advancement.
     * @param description The description of the advancement.
     * @param frameType The frame type of the advancement.
     * @param x The x coordinate in the advancement GUI.
     * @param y The y coordinate in the advancement GUI.
     * @return A new {@code PreparedAdvancementDisplayWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static PreparedAdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y) throws ReflectiveOperationException {
        return craft(icon, title, description, frameType, x, y, false, false, false);
    }

    /**
     * Creates a new {@code PreparedAdvancementDisplayWrapper}.
     *
     * @param icon The icon of the advancement.
     * @param title The title of the advancement.
     * @param description The description of the advancement.
     * @param frameType The frame type of the advancement.
     * @param x The x coordinate in the advancement GUI.
     * @param y The y coordinate in the advancement GUI.
     * @param showToast Whether to show toast on grant.
     * @param announceChat Whether to announce grants to chat.
     * @param hidden Whether the advancement shouldn't be visible in the advancement GUI. Connections to other advancements are always displayed.
     * @return A new {@code PreparedAdvancementDisplayWrapper}.
     * @throws ReflectiveOperationException If reflections goes wrong.
     */
    @NotNull
    public static PreparedAdvancementDisplayWrapper craft(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) throws ReflectiveOperationException {
        return constructor.newInstance(icon.clone(), title, description, frameType, x, y, showToast, announceChat, hidden);
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
     * Converts this {@code PreparedAdvancementDisplayWrapper} into an {@link AdvancementDisplayWrapper}.
     *
     * @return A new {@link AdvancementDisplayWrapper} derived from this {@code PreparedAdvancementDisplayWrapper}.
     */
    @NotNull
    public abstract AdvancementDisplayWrapper toBaseAdvancementDisplay();

    /**
     * Converts this {@code PreparedAdvancementDisplayWrapper} into an {@link AdvancementDisplayWrapper} with the provided background texture.
     *
     * @param backgroundTexture The path of the background texture of the advancement GUI.
     * @return A new {@link AdvancementDisplayWrapper} derived from this {@code PreparedAdvancementDisplayWrapper}.
     */
    @NotNull
    public abstract AdvancementDisplayWrapper toRootAdvancementDisplay(@NotNull String backgroundTexture);
}
