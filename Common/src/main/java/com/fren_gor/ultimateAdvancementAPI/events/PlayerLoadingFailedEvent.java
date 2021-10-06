package com.fren_gor.ultimateAdvancementAPI.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player loading fails.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class PlayerLoadingFailedEvent extends Event {

    @NotNull
    private final Player player;
    @Nullable
    private final Throwable cause;

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
}