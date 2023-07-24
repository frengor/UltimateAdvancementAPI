package com.fren_gor.ultimateAdvancementAPI.events.team;

import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Called after a player is unregistered.
 * <p>May be called asynchronously.
 *
 * @see DatabaseManager#unregisterOfflinePlayer(UUID)
 * @since 2.2.0
 */
public class AsyncPlayerUnregisteredEvent extends Event {

    private final UUID uuid;

    /**
     * Creates a new {@code AsyncPlayerUnregisteredEvent}.
     *
     * @param uuid The {@link UUID} of the player which has been unregistered.
     */
    public AsyncPlayerUnregisteredEvent(@NotNull UUID uuid) {
        super(!Bukkit.isPrimaryThread());
        this.uuid = Objects.requireNonNull(uuid, "UUID is null.");
    }

    /**
     * Gets the {@link UUID} of the player which has been unregistered.
     *
     * @return The {@link UUID} of the player which has been unregistered.
     */
    @NotNull
    public UUID getPlayerUUID() {
        return uuid;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public String toString() {
        return "AsyncPlayerUnregisteredEvent{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AsyncPlayerUnregisteredEvent that = (AsyncPlayerUnregisteredEvent) o;

        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}