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
        throw new UnsupportedOperationException("MojangMappingsHandler hasn't been overridden, please don't depend on ultimateadvancementapi-commands-common directly!");
    }

    private MojangMappingsHandler() {
        throw new UnsupportedOperationException();
    }
}
