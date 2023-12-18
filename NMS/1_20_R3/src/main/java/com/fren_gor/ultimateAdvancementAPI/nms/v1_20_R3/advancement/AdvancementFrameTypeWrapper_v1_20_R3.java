package com.fren_gor.ultimateAdvancementAPI.nms.v1_20_R3.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameTypeWrapper_v1_20_R3 extends AdvancementFrameTypeWrapper {

    private final net.minecraft.advancements.AdvancementType mcFrameType;

    private final FrameType frameType;

    public AdvancementFrameTypeWrapper_v1_20_R3(@NotNull FrameType frameType) {
        this.frameType = frameType;
        this.mcFrameType = switch (frameType) {
            case TASK -> net.minecraft.advancements.AdvancementType.TASK;
            case GOAL -> net.minecraft.advancements.AdvancementType.GOAL;
            case CHALLENGE -> net.minecraft.advancements.AdvancementType.CHALLENGE;
        };
    }

    @Override
    @NotNull
    public FrameType getFrameType() {
        return frameType;
    }

    @Override
    @NotNull
    public net.minecraft.advancements.AdvancementType toNMS() {
        return mcFrameType;
    }
}
