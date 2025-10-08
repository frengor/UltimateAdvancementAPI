package com.fren_gor.ultimateAdvancementAPI.events.team;

import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called when a new {@link TeamProgression} instance is created and stored in the caching system.
 *
 * @see DatabaseManager
 * @deprecated Use {@link AsyncTeamLoadEvent} instead.
 */
@Deprecated(since = "2.2.0", forRemoval = true)
public class TeamLoadEvent extends Event {

    private final TeamProgression team;

    /**
     * Creates a new {@code TeamLoadEvent}.
     *
     * @param team The loaded {@link TeamProgression}.
     */
    public TeamLoadEvent(@NotNull TeamProgression team) {
        this.team = Objects.requireNonNull(team, "TeamProgression is null."); // Cannot validate TeamProgression! This is the reason this class is deprecated
    }

    /**
     * Gets the loaded {@link TeamProgression}.
     *
     * @return The loaded {@link TeamProgression}.
     */
    @NotNull
    public TeamProgression getTeamProgression() {
        return team;
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
        return "TeamLoadEvent{" +
                "team=" + team +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamLoadEvent that = (TeamLoadEvent) o;

        return team.equals(that.team);
    }

    @Override
    public int hashCode() {
        return team.hashCode();
    }
}