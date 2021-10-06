package com.fren_gor.ultimateAdvancementAPI.events.team;

import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a new {@link TeamProgression} instance is created and stored in the caching system.
 * <p>The {@link TeamProgression} instance provided by this event is always valid.
 *
 * @see DatabaseManager
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TeamLoadEvent extends Event {

    @Getter
    private final TeamProgression team;

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