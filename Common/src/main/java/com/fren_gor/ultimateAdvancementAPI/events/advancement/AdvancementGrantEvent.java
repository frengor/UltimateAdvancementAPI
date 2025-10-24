package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * Called synchronously when a team completes an advancement.
 * <p>Specifically, it is called during {@link Advancement#onGrant(Player, boolean)}.
 */
public class AdvancementGrantEvent extends Event {

    private final TeamProgression team;
    private final Player advancementCompleter;
    private final Advancement advancement;
    private final boolean giveRewards;

    /**
     * Creates a new {@code AdvancementGrantEvent}.
     *
     * @param team The {@link TeamProgression} of the team which completed the advancement.
     * @param advancementCompleter The member of the team who completed the advancement.
     * @param advancement The {@link Advancement}.
     * @param giveRewards Whether rewards will be given.
     */
    public AdvancementGrantEvent(@NotNull TeamProgression team, @NotNull Player advancementCompleter, @NotNull Advancement advancement, boolean giveRewards) {
        this.team = validateTeamProgression(team);
        this.advancementCompleter = Objects.requireNonNull(advancementCompleter, "Player is null.");
        Preconditions.checkArgument(team.contains(advancementCompleter), "Team does not contain the advancement completer.");
        this.advancement = Objects.requireNonNull(advancement, "Advancement is null.");
        this.giveRewards = giveRewards;
    }

    /**
     * Gets the {@link TeamProgression} of the team which completed the advancement.
     *
     * @return The {@link TeamProgression} of the team which completed the advancement.
     */
    @NotNull
    public TeamProgression getTeamProgression() {
        return team;
    }

    /**
     * Gets the member of the team who completed the advancement.
     *
     * @return The member of the team who completed the advancement.
     */
    @NotNull
    public Player getAdvancementCompleter() {
        return advancementCompleter;
    }

    /**
     * Gets the {@link Advancement}.
     *
     * @return The {@link Advancement}.
     */
    @NotNull
    public Advancement getAdvancement() {
        return advancement;
    }

    /**
     * Gets whether rewards will be given.
     *
     * @return Whether rewards will be given.
     */
    public boolean doesGiveRewards() {
        return giveRewards;
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
        return "AdvancementGrantEvent{" +
                "team=" + team +
                ", advancementCompleter=" + advancementCompleter +
                ", advancement=" + advancement +
                ", giveRewards=" + giveRewards +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AdvancementGrantEvent that = (AdvancementGrantEvent) o;
        return giveRewards == that.giveRewards &&
                team.equals(that.team) &&
                advancementCompleter.equals(that.advancementCompleter) &&
                advancement.equals(that.advancement);
    }

    @Override
    public int hashCode() {
        int result = team.hashCode();
        result = 31 * result + advancementCompleter.hashCode();
        result = 31 * result + advancement.hashCode();
        result = 31 * result + Boolean.hashCode(giveRewards);
        return result;
    }
}