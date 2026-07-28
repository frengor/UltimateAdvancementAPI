package com.fren_gor.ultimateAdvancementAPI.nms.v26_2_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import net.minecraft.advancements.AdvancementType;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameTypeWrapper_v26_2_R1 extends AdvancementFrameTypeWrapper {

    private final AdvancementType mcFrameType;
    private final FrameType frameType;

    public AdvancementFrameTypeWrapper_v26_2_R1(@NotNull FrameType frameType) {
        this.frameType = frameType;
        this.mcFrameType = switch (frameType) {
            case TASK -> AdvancementType.TASK;
            case GOAL -> AdvancementType.GOAL;
            case CHALLENGE -> AdvancementType.CHALLENGE;
        };
    }

    @Override
    @NotNull
    public FrameType getFrameType() {
        return frameType;
    }

    @Override
    @NotNull
    public AdvancementType toNMS() {
        return mcFrameType;
    }
}
