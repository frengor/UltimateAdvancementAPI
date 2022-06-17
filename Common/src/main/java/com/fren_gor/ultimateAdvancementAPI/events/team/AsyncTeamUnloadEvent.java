package com.fren_gor.ultimateAdvancementAPI.events.team;

import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called after a {@link TeamProgression} instance is removed from the caching system.
 * <p>The {@link TeamProgression} instance provided by this event is always invalid.
 * <p>May be called asynchronously.
 *
 * @see DatabaseManager
 * @since 2.2.0
 */
public class AsyncTeamUnloadEvent extends Event {

    private final TeamProgression team;

    /**
     * Creates a new {@code AsyncTeamUnloadEvent}.
     *
     * @param team The {@link TeamProgression} of the unloaded team. It must be invalid (see {@link TeamProgression#isValid()}).
     */
    public AsyncTeamUnloadEvent(@NotNull TeamProgression team) {
        super(!Bukkit.isPrimaryThread());
        Preconditions.checkArgument(!Objects.requireNonNull(team, "TeamProgression is null.").isValid(), "TeamProgression is valid.");
        this.team = team;
    }

    /**
     * Gets the {@link TeamProgression} of the unloaded team.
     * <p>Note that the returned {@link TeamProgression} is always invalid (see {@link TeamProgression#isValid()}).
     *
     * @return The {@link TeamProgression} of the unloaded team.
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
        return "AsyncTeamUnloadEvent{" +
                "team=" + team +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AsyncTeamUnloadEvent that = (AsyncTeamUnloadEvent) o;

        return team.equals(that.team);
    }

    @Override
    public int hashCode() {
        return team.hashCode();
    }
}