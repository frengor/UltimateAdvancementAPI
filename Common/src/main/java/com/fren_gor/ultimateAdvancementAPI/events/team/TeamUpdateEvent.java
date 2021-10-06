package com.fren_gor.ultimateAdvancementAPI.events.team;

import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called when a team member joins or leaves a team.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TeamUpdateEvent extends Event {

    /**
     * The action that occurred.
     * <p>When a player moves in another team, an {@link Action#LEAVE} is always called before an {@link Action#JOIN}.
     */
    public enum Action {
        JOIN, LEAVE;
    }

    @NotNull
    private final TeamProgression team;
    @NotNull
    private final UUID playerUUID;
    @NotNull
    private final Action action;

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