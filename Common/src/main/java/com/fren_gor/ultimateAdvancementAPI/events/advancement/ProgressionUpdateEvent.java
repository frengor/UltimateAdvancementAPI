package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateProgressionValue;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * Called synchronously when a team's progression of an advancement changes.
 * <p>This event differs from {@link AdvancementProgressionUpdateEvent} because it is called by {@link DatabaseManager}.
 *
 * @since 3.0.0
 */
public class ProgressionUpdateEvent extends Event {

    private final TeamProgression team;

    @Range(from = 0, to = Integer.MAX_VALUE)
    private final int oldProgression, newProgression;

    private final AdvancementKey advancementKey;

    /**
     * Creates a new {@code ProgressionUpdateEvent}.
     *
     * @param team The {@link TeamProgression} of the updated team.
     * @param oldProgression The old progression prior to the update.
     * @param newProgression The new progression after the update.
     * @param advancementKey The {@link AdvancementKey} of the updated {@link Advancement}.
     */
    public ProgressionUpdateEvent(@NotNull TeamProgression team, @Range(from = 0, to = Integer.MAX_VALUE) int oldProgression, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression, @NotNull AdvancementKey advancementKey) {
        this.team = validateTeamProgression(team);
        this.oldProgression = validateProgressionValue(oldProgression);
        this.newProgression = validateProgressionValue(newProgression);
        this.advancementKey = Objects.requireNonNull(advancementKey, "AdvancementKey is null.");
    }

    /**
     * Gets the {@link TeamProgression} of the updated team.
     *
     * @return The {@link TeamProgression} of the updated team.
     */
    public TeamProgression getTeamProgression() {
        return team;
    }

    /**
     * Gets the old progression prior to the update.
     *
     * @return The old progression prior to the update.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getOldProgression() {
        return oldProgression;
    }

    /**
     * Gets the new progression after the update.
     *
     * @return The new progression after the update.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getNewProgression() {
        return newProgression;
    }

    /**
     * Gets the {@link AdvancementKey} of the updated {@link Advancement}.
     *
     * @return The {@link AdvancementKey} of the updated {@link Advancement}.
     */
    public AdvancementKey getAdvancementKey() {
        return advancementKey;
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
        return "ProgressionUpdateEvent{" +
                "team=" + team +
                ", oldProgression=" + oldProgression +
                ", newProgression=" + newProgression +
                ", advancementKey=" + advancementKey +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgressionUpdateEvent that = (ProgressionUpdateEvent) o;

        if (oldProgression != that.oldProgression) return false;
        if (newProgression != that.newProgression) return false;
        if (!team.equals(that.team)) return false;
        return advancementKey.equals(that.advancementKey);
    }

    @Override
    public int hashCode() {
        int result = team.hashCode();
        result = 31 * result + oldProgression;
        result = 31 * result + newProgression;
        result = 31 * result + advancementKey.hashCode();
        return result;
    }
}