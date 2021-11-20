package com.fren_gor.ultimateAdvancementAPI.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Called when a player loading fails.
 */
public final class PlayerLoadingFailedEvent extends Event {

    private final Player player;

    @Nullable
    private final Throwable cause;

    /**
     * Creates a new {@code PlayerLoadingFailedEvent}.
     *
     * @param player The player who couldn't be loaded.
     * @param cause The {@link Throwable} which caused of the failure of the loading, or {@code null} if no exception occurred.
     */
    public PlayerLoadingFailedEvent(@NotNull Player player, @Nullable Throwable cause) {
        this.player = Objects.requireNonNull(player, "Player is null.");
        this.cause = cause;
    }

    /**
     * Gets the player who couldn't be loaded.
     *
     * @return The player who couldn't be loaded.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the {@link Throwable} which caused the failure of the loading.
     *
     * @return The {@link Throwable} which caused of the failure of the loading, or {@code null} if no exception occurred.
     */
    @Nullable
    public Throwable getCause() {
        return cause;
    }

    /**
     * Returns whether an exception is occurred.
     *
     * @return Whether an exception is occurred.
     */
    public boolean isExceptionOccurred() {
        return cause != null;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerLoadingFailedEvent that = (PlayerLoadingFailedEvent) o;

        if (!player.equals(that.player)) return false;
        return Objects.equals(cause, that.cause);
    }

    @Override
    public int hashCode() {
        int result = player.hashCode();
        result = 31 * result + (cause != null ? cause.hashCode() : 0);
        return result;
    }
}