package com.fren_gor.ultimateAdvancementAPI.commands;

/**
 * Helper class to handle Mojang mapped servers.
 */
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
