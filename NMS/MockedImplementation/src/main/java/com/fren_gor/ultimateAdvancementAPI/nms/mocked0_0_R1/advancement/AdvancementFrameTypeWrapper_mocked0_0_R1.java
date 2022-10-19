package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameTypeWrapper_mocked0_0_R1 extends AdvancementFrameTypeWrapper {

    private final FrameType frameType;

    public AdvancementFrameTypeWrapper_mocked0_0_R1(@NotNull FrameType frameType) {
        this.frameType = frameType;
    }

    @Override
    @NotNull
    public FrameType getFrameType() {
        return frameType;
    }

    @Override
    @NotNull
    public Object toNMS() {
        throw new UnsupportedOperationException();
    }
}
