package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Describes which shape of an advancement frame in the advancement GUI.
 * <p>The frame also contains the default title and description colors and the message that should be sent when the advancement is granted.
 * <p>Note that the {@code AdvancementFrameType} class is a wrapper for NMS {@code AdvancementFrameType}.
 */
@RequiredArgsConstructor
public enum AdvancementFrameType {

    /**
     * A frame with squared shape. The default color is {@link ChatColor#GREEN}.
     */
    TASK(net.minecraft.server.v1_15_R1.AdvancementFrameType.TASK, ChatColor.GREEN, "has made the advancement"),
    /**
     * A frame with rounded top and bottom. The default color is {@link ChatColor#GREEN}.
     */
    GOAL(net.minecraft.server.v1_15_R1.AdvancementFrameType.GOAL, ChatColor.GREEN, "has reached the goal"),
    /**
     * A frame with thorns at the corners. The default color is {@link ChatColor#DARK_PURPLE}.
     */
    CHALLENGE(net.minecraft.server.v1_15_R1.AdvancementFrameType.CHALLENGE, ChatColor.DARK_PURPLE, "has completed the challenge");

    /**
     * The NMS instance of AdvancementFrameType.
     */
    private final net.minecraft.server.v1_15_R1.AdvancementFrameType minecraftFrameType;

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

    /**
     * Returns the appropriate {@link AdvancementFrameType} for the provided NMS {@code AdvancementFrameType}.
     *
     * @param nms The NMS {@code AdvancementFrameType}.
     * @return The appropriate {@link AdvancementFrameType}.
     * @throws IllegalArgumentException If a non-mapped {@code AdvancementFrameType} is passed as input. Should never happen.
     */
    @NotNull
    public static AdvancementFrameType fromNMS(@NotNull net.minecraft.server.v1_15_R1.AdvancementFrameType nms) {
        for (AdvancementFrameType a : values()) {
            if (a.minecraftFrameType == nms) {
                return a;
            }
        }
        // This should never run
        throw new IllegalArgumentException(nms.name() + " isn't a valid enum constant.");
    }

    /**
     * Gets the NMS {@code AdvancementFrameType}.
     *
     * @return The NMS {@code AdvancementFrameType}.
     */
    @NotNull
    public net.minecraft.server.v1_15_R1.AdvancementFrameType getMinecraftFrameType() {
        return minecraftFrameType;
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