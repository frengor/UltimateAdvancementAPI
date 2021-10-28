package com.fren_gor.ultimateAdvancementAPI.nms.v1_17_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameTypeWrapper_v1_17_R1 extends AdvancementFrameTypeWrapper {

    private final net.minecraft.advancements.FrameType frameType;

    public AdvancementFrameTypeWrapper_v1_17_R1(@NotNull FrameType frameType) {
        this.frameType = switch (frameType) {
            case TASK -> net.minecraft.advancements.FrameType.TASK;
            case GOAL -> net.minecraft.advancements.FrameType.GOAL;
            case CHALLENGE -> net.minecraft.advancements.FrameType.CHALLENGE;
        };
    }

    @Override
    @NotNull
    public net.minecraft.advancements.FrameType getNMSFrameType() {
        return frameType;
    }
}
