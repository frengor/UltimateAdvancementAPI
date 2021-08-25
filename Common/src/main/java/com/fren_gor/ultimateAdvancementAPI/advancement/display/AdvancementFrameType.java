package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * AdvancementFrameType describe which shape has the advancement in the GUI and which description color is related to.
 * There are 3 types:
 * - task (squared shape with green as default color).
 * - goal (square with rounded top and bottom with green as default color).
 * - challenge (thorns at the corners with dark purple as default color).
 */
@RequiredArgsConstructor
public enum AdvancementFrameType {

    TASK(net.minecraft.server.v1_15_R1.AdvancementFrameType.TASK, ChatColor.GREEN, "has made the advancement"),
    GOAL(net.minecraft.server.v1_15_R1.AdvancementFrameType.GOAL, ChatColor.GREEN, "has reached the goal"),
    CHALLENGE(net.minecraft.server.v1_15_R1.AdvancementFrameType.CHALLENGE, ChatColor.DARK_PURPLE, "has completed the challenge");

    /**
     * Real minecraft class for the AdvancementFrameType, it is used in packets.
     */
    @Getter
    private final net.minecraft.server.v1_15_R1.AdvancementFrameType minecraftFrameType;

    /**
     * Which color is related to the frame, it is used to color the description.
     */
    @Getter
    private final ChatColor color;

    /**
     * How the advancement completion message should end.
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Getter
    private final String chatText;

    /**
     * Given a nms AdvancementFrameType, returns a new AdvancementFrameType.
     * @param nms Nms advancement frame type.
     * @return An AdvancementFrameType.
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

}