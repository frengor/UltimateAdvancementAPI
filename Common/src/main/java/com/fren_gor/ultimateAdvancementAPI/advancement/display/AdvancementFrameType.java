package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * AdvancementFrameType describe which shape has the advancement
 */
@RequiredArgsConstructor
public enum AdvancementFrameType {

    TASK(net.minecraft.server.v1_15_R1.AdvancementFrameType.TASK, ChatColor.GREEN, "advancement"),
    GOAL(net.minecraft.server.v1_15_R1.AdvancementFrameType.GOAL, ChatColor.GREEN, "goal"),
    CHALLENGE(net.minecraft.server.v1_15_R1.AdvancementFrameType.CHALLENGE, ChatColor.DARK_PURPLE, "challenge");

    /**
     * Real minecraft class for the AdvancementFrameType, it is used in packets
     */
    @Getter
    private final net.minecraft.server.v1_15_R1.AdvancementFrameType minecraftFrameType;

    /**
     * Which color is related to the frame, it is used to color the description in hover message in {@link Advancement#getAnnounceMessage(Player)}
     */
    @Getter
    private final ChatColor color;

    /**
     * Which world needs to be sent in the description in hover message in {@link Advancement#getAnnounceMessage(Player)}
     */
    @Getter
    private final String chatText;

    /**
     * Given a minecraft nms AdvancementFrameType, returns a new AdvancementFrameType
     * @param nms - nms advancement frame type
     * @return - AdvancementFrameType
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