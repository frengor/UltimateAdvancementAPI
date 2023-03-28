package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;

import java.util.concurrent.atomic.AtomicBoolean;

public class VanillaAdvancementDisablerWrapper_mocked0_0_R1 extends VanillaAdvancementDisablerWrapper {

    private static final AtomicBoolean DISABLED = new AtomicBoolean(false);

    public static void disableVanillaAdvancements() throws Exception {
        DISABLED.set(true);
    }

    public static boolean areAdvancementsDisabled() {
        return DISABLED.get();
    }

    private VanillaAdvancementDisablerWrapper_mocked0_0_R1() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
