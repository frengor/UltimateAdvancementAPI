package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateCriteria;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * Called when the criteria progression of an advancement changes.
 * <p>This event differs from {@link CriteriaUpdateEvent} because it is called by {@link Advancement#setCriteriaProgression(TeamProgression, Player, int, boolean)}.
 */
public class AdvancementCriteriaUpdateEvent extends Event {

    private final TeamProgression team;

    @Range(from = 0, to = Integer.MAX_VALUE)
    private final int oldCriteria, newCriteria;

    private final Advancement advancement;

    /**
     * Creates a new {@code AdvancementCriteriaUpdateEvent}.
     *
     * @param team The {@link TeamProgression} of the updated team.
     * @param oldCriteria The old criteria prior to the update.
     * @param newCriteria The new criteria after the update.
     * @param advancement The updated {@link Advancement}.
     */
    public AdvancementCriteriaUpdateEvent(@NotNull TeamProgression team, @Range(from = 0, to = Integer.MAX_VALUE) int oldCriteria, @Range(from = 0, to = Integer.MAX_VALUE) int newCriteria, @NotNull Advancement advancement) {
        this.team = validateTeamProgression(team);
        this.oldCriteria = validateCriteria(oldCriteria);
        this.newCriteria = validateCriteria(newCriteria);
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
     * Gets the old criteria prior to the update.
     *
     * @return The old criteria prior to the update.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getOldCriteria() {
        return oldCriteria;
    }

    /**
     * Gets the new criteria after the update.
     *
     * @return The new criteria after the update.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getNewCriteria() {
        return newCriteria;
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
        return "AdvancementCriteriaUpdateEvent{" +
                "team=" + team +
                ", oldCriteria=" + oldCriteria +
                ", newCriteria=" + newCriteria +
                ", advancement=" + advancement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdvancementCriteriaUpdateEvent that = (AdvancementCriteriaUpdateEvent) o;

        if (oldCriteria != that.oldCriteria) return false;
        if (newCriteria != that.newCriteria) return false;
        if (!team.equals(that.team)) return false;
        return advancement.equals(that.advancement);
    }

    @Override
    public int hashCode() {
        int result = team.hashCode();
        result = 31 * result + oldCriteria;
        result = 31 * result + newCriteria;
        result = 31 * result + advancement.hashCode();
        return result;
    }
}