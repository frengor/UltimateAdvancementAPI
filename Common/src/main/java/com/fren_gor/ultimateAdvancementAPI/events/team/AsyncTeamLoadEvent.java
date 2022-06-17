package com.fren_gor.ultimateAdvancementAPI.events.team;

import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * Called when a new {@link TeamProgression} instance is created and stored in the caching system.
 * <p>The {@link TeamProgression} instance provided by this event is always valid.
 * <p>May be called asynchronously.
 *
 * @see DatabaseManager
 * @since 2.2.0
 */
public class AsyncTeamLoadEvent extends Event {

    private final TeamProgression team;

    /**
     * Creates a new {@code AsyncTeamLoadEvent}.
     *
     * @param team The loaded {@link TeamProgression}. It must be valid (see {@link TeamProgression#isValid()}).
     */
    public AsyncTeamLoadEvent(@NotNull TeamProgression team) {
        super(!Bukkit.isPrimaryThread());
        this.team = validateTeamProgression(team);
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
        return "AsyncTeamLoadEvent{" +
                "team=" + team +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AsyncTeamLoadEvent that = (AsyncTeamLoadEvent) o;

        return team.equals(that.team);
    }

    @Override
    public int hashCode() {
        return team.hashCode();
    }
}