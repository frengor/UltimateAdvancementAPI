package com.fren_gor.ultimateAdvancementAPI.events;

import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is loaded successfully.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class PlayerLoadingCompletedEvent extends Event {

    @NotNull
    private final Player player;
    @NotNull
    private final TeamProgression progression;

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