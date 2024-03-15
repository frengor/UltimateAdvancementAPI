package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;

import java.util.concurrent.atomic.AtomicBoolean;

public class VanillaAdvancementDisablerWrapper_mocked0_0_R1 extends VanillaAdvancementDisablerWrapper {

    public static final AtomicBoolean DISABLED = new AtomicBoolean(false);

    public static void disableVanillaAdvancements() throws Exception {
        DISABLED.set(true);
    }

    private VanillaAdvancementDisablerWrapper_mocked0_0_R1() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
