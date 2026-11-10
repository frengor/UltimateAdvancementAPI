package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.chat.IComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.adventure.AdventureComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Internal class used to support Paper-only APIs in {@code AdvancementUtils}.
 */
final class PaperUtils {

    @NotNull
    static IComponent getDisplayName(@NotNull Player player) {
        return AdventureComponent.of(player.displayName());
    }

    private PaperUtils() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
