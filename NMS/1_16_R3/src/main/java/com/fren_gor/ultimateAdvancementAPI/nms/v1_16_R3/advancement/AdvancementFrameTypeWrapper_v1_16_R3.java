package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R3.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import net.minecraft.server.v1_16_R3.AdvancementFrameType;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameTypeWrapper_v1_16_R3 extends AdvancementFrameTypeWrapper {

    private final AdvancementFrameType frameType;

    public AdvancementFrameTypeWrapper_v1_16_R3(@NotNull FrameType frameType) {
        this.frameType = switch (frameType) {
            case TASK -> AdvancementFrameType.TASK;
            case GOAL -> AdvancementFrameType.GOAL;
            case CHALLENGE -> AdvancementFrameType.CHALLENGE;
        };
    }

    @Override
    @NotNull
    public AdvancementFrameType getNMSFrameType() {
        return frameType;
    }
}
