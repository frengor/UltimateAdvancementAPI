package com.fren_gor.ultimateAdvancementAPI.commands;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Helper class to handle Mojang mapped servers.
 */
@Internal
public final class MojangMappingsHandler {

    /**
     * Whether to load Mojang mapped CommandAPI.
     */
    public static boolean isMojangMapped() {
        return true;
    }

    private MojangMappingsHandler() {
        throw new UnsupportedOperationException();
    }
}
