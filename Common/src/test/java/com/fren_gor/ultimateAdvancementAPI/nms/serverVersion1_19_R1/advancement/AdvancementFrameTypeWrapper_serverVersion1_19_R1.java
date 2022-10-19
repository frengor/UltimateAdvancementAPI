package com.fren_gor.ultimateAdvancementAPI.nms.serverVersion1_19_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameTypeWrapper_serverVersion1_19_R1 extends AdvancementFrameTypeWrapper {

    private final FrameType frameType;

    public AdvancementFrameTypeWrapper_serverVersion1_19_R1(@NotNull FrameType frameType) {
        this.frameType = frameType;
    }

    @Override
    @NotNull
    public FrameType getFrameType() {
        return frameType;
    }

    @Override
    @NotNull
    public AdvancementFrameType toNMS() {
        throw new UnsupportedOperationException();
    }

}
