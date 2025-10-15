package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.display.DefaultStyle;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Describes which shape of an advancement frame in the advancement GUI.
 * <p>The frame also contains the default title and description styles and the message that should be sent when the advancement is granted.
 */
public enum AdvancementFrameType {

    /**
     * A frame with squared shape. The default style has color {@link ChatColor#GREEN}.
     */
    TASK(AdvancementFrameTypeWrapper.TASK, new DefaultStyle(ChatColor.GREEN), "has made the advancement"),
    /**
     * A frame with rounded top and bottom. The default style has color {@link ChatColor#GREEN}.
     */
    GOAL(AdvancementFrameTypeWrapper.GOAL, new DefaultStyle(ChatColor.GREEN), "has reached the goal"),
    /**
     * A frame with thorns at the corners. The default style has color {@link ChatColor#DARK_PURPLE}.
     */
    CHALLENGE(AdvancementFrameTypeWrapper.CHALLENGE, new DefaultStyle(ChatColor.DARK_PURPLE), "has completed the challenge");

    /**
     * The {@code AdvancementFrameType} NMS wrapper.
     */
    private final AdvancementFrameTypeWrapper wrapper;

    /**
     * The default title and description styles for announcement messages.
     */
    private final DefaultStyle style;

    /**
     * The message that should be sent when the advancement is granted.
     * <p><strong>Note:</strong> this is not the complete message.
     */
    private final String chatText;

    AdvancementFrameType(@NotNull AdvancementFrameTypeWrapper wrapper, @NotNull DefaultStyle style, @NotNull String chatText) {
        this.wrapper = Objects.requireNonNull(wrapper);
        this.style = Objects.requireNonNull(style);
        this.chatText = Objects.requireNonNull(chatText);
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
     * Gets the default title and description styles for announcement messages.
     *
     * @return The default title and description styles for announcement messages.
     */
    @NotNull
    public DefaultStyle getStyle() {
        return style;
    }

    /**
     * Gets the message that should be sent when the advancement is granted.
     * <p><strong>Note:</strong> this is not the complete message.
     *
     * @return The message that should be sent when the advancement is granted.
     */
    @NotNull
    public String getChatText() {
        return chatText;
    }
}