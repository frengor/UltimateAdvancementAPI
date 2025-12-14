package com.fren_gor.ultimateAdvancementAPI.database;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.fren_gor.eventManagerAPI.EventManager;
import io.papermc.paper.event.connection.configuration.PlayerConnectionInitialConfigureEvent;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Internal class used to support Paper-only events in {@code DatabaseManager}.
 */
final class PaperEvents {

    private final Logger logger;
    private final EventManager eventManager;

    public PaperEvents(@NotNull Logger logger, @NotNull EventManager eventManager) {
        this.logger = logger;
        this.eventManager = eventManager;
    }

    public void registerPlayerConnectionInitialConfigureEvent(
        @NotNull Object listener,
        @NotNull BiConsumer<UUID, String> onConnectionInitialConfigure
    ) {
        eventManager.register(listener, PlayerConnectionInitialConfigureEvent.class, EventPriority.LOWEST, e -> {
            PlayerProfile profile = e.getConnection().getProfile();
            UUID uuid;
            String name;
            try {
                uuid = Objects.requireNonNull(profile.getId(), "UUID is null.");
                name = Objects.requireNonNull(profile.getName(), "Name is null.");
            } catch (NullPointerException ex) {
                // Should never happen, but better be sure
                logger.log(Level.SEVERE, "Couldn't retrieve player information, they will not be loaded from the database", ex);
                return;
            }
            onConnectionInitialConfigure.accept(uuid, name);
        });
    }

    public void registerPlayerConnectionCloseEvent(
        @NotNull Object listener,
        @NotNull Consumer<UUID> onConnectionClose
    ) {
        eventManager.register(listener, PlayerConnectionCloseEvent.class, e -> {
            onConnectionClose.accept(e.getPlayerUniqueId());
        });
    }
}
