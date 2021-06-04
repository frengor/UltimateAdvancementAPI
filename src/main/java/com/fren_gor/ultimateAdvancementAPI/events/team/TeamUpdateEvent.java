package com.fren_gor.ultimateAdvancementAPI.events.team;

import lombok.Data;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper=false)
public class TeamUpdateEvent extends Event {

    // Leave before update
    public enum Action {
        JOIN, LEAVE;
    }

    @NotNull
    private final TeamProgression team;
    @NotNull
    private final UUID player;
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