package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateProgressionValue;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * Called when a team's progression of an advancement changes.
 * <p>This event differs from {@link AsyncProgressionUpdateEvent} because it is called (synchronously) by {@link Advancement#setProgression(TeamProgression, Player, int, boolean)}
 * and {@link Advancement#incrementProgression(TeamProgression, Player, int, boolean)}.
 */
public class AdvancementProgressionUpdateEvent extends Event {

    private final TeamProgression team;

    @Range(from = 0, to = Integer.MAX_VALUE)
    private final int oldProgression, newProgression;

    private final Advancement advancement;

    /**
     * Creates a new {@code AdvancementProgressionUpdateEvent}.
     *
     * @param team The {@link TeamProgression} of the updated team.
     * @param oldProgression The old progression prior to the update.
     * @param newProgression The new progression after the update.
     * @param advancement The updated {@link Advancement}.
     */
    public AdvancementProgressionUpdateEvent(@NotNull TeamProgression team, @Range(from = 0, to = Integer.MAX_VALUE) int oldProgression, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression, @NotNull Advancement advancement) {
        this.team = validateTeamProgression(team);
        this.oldProgression = validateProgressionValue(oldProgression);
        this.newProgression = validateProgressionValue(newProgression);
        this.advancement = Objects.requireNonNull(advancement, "Advancement is null.");
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
     * Gets the updated {@link Advancement}.
     *
     * @return The updated {@link Advancement}.
     */
    public Advancement getAdvancement() {
        return advancement;
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
        return "AdvancementProgressionUpdateEvent{" +
                "team=" + team +
                ", oldProgression=" + oldProgression +
                ", newProgression=" + newProgression +
                ", advancement=" + advancement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdvancementProgressionUpdateEvent that = (AdvancementProgressionUpdateEvent) o;

        if (oldProgression != that.oldProgression) return false;
        if (newProgression != that.newProgression) return false;
        if (!team.equals(that.team)) return false;
        return advancement.equals(that.advancement);
    }

    @Override
    public int hashCode() {
        int result = team.hashCode();
        result = 31 * result + oldProgression;
        result = 31 * result + newProgression;
        result = 31 * result + advancement.hashCode();
        return result;
    }
}