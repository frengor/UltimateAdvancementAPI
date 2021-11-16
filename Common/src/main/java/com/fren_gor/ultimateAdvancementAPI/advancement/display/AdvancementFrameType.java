package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Describes which shape of an advancement frame in the advancement GUI.
 * <p>The frame also contains the default title and description colors and the message that should be sent when the advancement is granted.
 */
public enum AdvancementFrameType {

    /**
     * A frame with squared shape. The default color is {@link ChatColor#GREEN}.
     */
    TASK(AdvancementFrameTypeWrapper.TASK, ChatColor.GREEN, "has made the advancement"),
    /**
     * A frame with rounded top and bottom. The default color is {@link ChatColor#GREEN}.
     */
    GOAL(AdvancementFrameTypeWrapper.GOAL, ChatColor.GREEN, "has reached the goal"),
    /**
     * A frame with thorns at the corners. The default color is {@link ChatColor#DARK_PURPLE}.
     */
    CHALLENGE(AdvancementFrameTypeWrapper.CHALLENGE, ChatColor.DARK_PURPLE, "has completed the challenge");

    /**
     * The {@code AdvancementFrameType} NMS wrapper.
     */
    private final AdvancementFrameTypeWrapper wrapper;

    /**
     * The default title and description colors.
     */
    private final ChatColor color;

    /**
     * The message that should be sent when the advancement is granted.
     * <p><strong>Note:</strong> this is not the complete message.
     *
     * @see Advancement#getAnnounceMessage(Player)
     */
    private final String chatText;

    AdvancementFrameType(@NotNull AdvancementFrameTypeWrapper wrapper, @NotNull ChatColor color, @NotNull String chatText) {
        this.wrapper = wrapper;
        this.color = color;
        this.chatText = chatText;
    }

    /**
     * Returns the appropriate {@link AdvancementFrameType} for the provided NMS wrapper {@link AdvancementFrameTypeWrapper}.
     *
     * @param nms The NMS wrapper for {@code AdvancementFrameType}.
     * @return The appropriate {@link AdvancementFrameType}.
     * @throws IllegalArgumentException If a non-mapped {@code AdvancementFrameType} is passed as input. Should never happen.
     */
    @NotNull
    public static AdvancementFrameType fromNMS(@NotNull AdvancementFrameTypeWrapper nms) {
        for (AdvancementFrameType a : values()) {
            if (a.wrapper.equals(nms)) {
                return a;
            }
        }
        // This should never run
        throw new IllegalArgumentException(nms + " isn't a valid enum constant.");
    }

    /**
     * Gets the {@code AdvancementFrameType} NMS wrapper.
     *
     * @return The {@code AdvancementFrameType} NMS wrapper.
     */
    @NotNull
    public AdvancementFrameTypeWrapper getNMSWrapper() {
        return wrapper;
    }

    /**
     * Gets the default title and description colors.
     *
     * @return The default title and description colors.
     */
    @NotNull
    public ChatColor getColor() {
        return color;
    }

    /**
     * Gets the message that should be sent when the advancement is granted.
     * <p><strong>Note:</strong> this is not the complete message.
     *
     * @return The message that should be sent when the advancement is granted.
     * @see Advancement#getAnnounceMessage(Player)
     */
    @NotNull
    public String getChatText() {
        return chatText;
    }
}