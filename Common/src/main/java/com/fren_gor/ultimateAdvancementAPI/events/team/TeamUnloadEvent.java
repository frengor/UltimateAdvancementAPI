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
 * Called after a {@link TeamProgression} instance is removed from the caching system.
 * <p>The {@link TeamProgression} instance provided by this event is always invalid.
 *
 * @see DatabaseManager
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TeamUnloadEvent extends Event {

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