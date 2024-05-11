package com.fren_gor.ultimateAdvancementAPI.commands;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;

/**
 * Helper class to handle Mojang mapped servers.
 */
@Internal
public final class MojangMappingsHandler {

    /**
     * Whether to load Mojang mapped CommandAPI.
     */
    @Contract(value = "-> _", pure = true) // Don't make IntelliJ think this code always throws, since in reality it doesn't
    public static boolean isMojangMapped() {
        throw new UnsupportedOperationException("MojangMappingsHandler hasn't been overridden, please don't depend on ultimateadvancementapi-commands-common directly!");
    }

    private MojangMappingsHandler() {
        throw new UnsupportedOperationException();
    }
}
