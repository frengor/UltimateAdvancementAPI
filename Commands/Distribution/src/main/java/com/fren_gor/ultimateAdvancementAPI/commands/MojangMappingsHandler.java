package com.fren_gor.ultimateAdvancementAPI.commands;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Helper class to handle Mojang mapped servers.
 */
@Internal
public final class MojangMappingsHandler {

    private static final boolean IS_PAPER = ReflectionUtil.classExists("io.papermc.paper.advancement.AdvancementDisplay");

    /**
     * Whether to load Mojang mapped CommandAPI.
     */
    public static boolean isMojangMapped() {
        // Load the Mojang mapped CommandAPI on Paper 1.20.6+ as a workaround for https://github.com/PaperMC/Paper/issues/10713
        return IS_PAPER && (ReflectionUtil.VERSION > 20 || (ReflectionUtil.VERSION == 20 && ReflectionUtil.MINOR_VERSION >= 6));
    }

    private MojangMappingsHandler() {
        throw new UnsupportedOperationException();
    }
}
