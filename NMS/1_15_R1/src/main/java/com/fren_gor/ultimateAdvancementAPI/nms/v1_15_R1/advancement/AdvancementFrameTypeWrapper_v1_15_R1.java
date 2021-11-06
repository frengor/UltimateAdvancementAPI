package com.fren_gor.ultimateAdvancementAPI.nms.v1_15_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import net.minecraft.server.v1_15_R1.AdvancementFrameType;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameTypeWrapper_v1_15_R1 extends AdvancementFrameTypeWrapper {

    private final AdvancementFrameType mcFrameType;
    private final FrameType frameType;

    public AdvancementFrameTypeWrapper_v1_15_R1(@NotNull FrameType frameType) {
        this.frameType = frameType;
        this.mcFrameType = switch (frameType) {
            case TASK -> AdvancementFrameType.TASK;
            case GOAL -> AdvancementFrameType.GOAL;
            case CHALLENGE -> AdvancementFrameType.CHALLENGE;
        };
    }

    @Override
    @NotNull
    public FrameType getFrameType() {
        return frameType;
    }

    @Override
    @NotNull
    public AdvancementFrameType toNMS() {
        return mcFrameType;
    }
}
