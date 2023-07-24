package com.fren_gor.ultimateAdvancementAPI.events.team;

import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * Called synchronously after a player switches team.
 *
 * @since 3.0.0
 */
public class TeamUpdateEvent extends Event {

    private final TeamProgression oldTeam, newTeam;
    private final UUID playerUUID;

    /**
     * Creates a new {@code AsyncTeamUpdateEvent}.
     *
     * @param oldTeam The {@link TeamProgression} of the old player's team. It must be valid (see {@link TeamProgression#isValid()}).
     * @param newTeam The {@link TeamProgression} of the new player's team. It must be valid (see {@link TeamProgression#isValid()}).
     * @param playerUUID The {@link UUID} of the player.
     */
    public TeamUpdateEvent(@NotNull TeamProgression oldTeam, @NotNull TeamProgression newTeam, @NotNull UUID playerUUID) {
        this.oldTeam = validateTeamProgression(Objects.requireNonNull(oldTeam, "OldTeam is null."));
        this.newTeam = validateTeamProgression(Objects.requireNonNull(newTeam, "NewTeam is null."));
        this.playerUUID = Objects.requireNonNull(playerUUID, "UUID is null.");
    }

    /**
     * Gets the old {@link TeamProgression} of the player's team.
     *
     * @return The old {@link TeamProgression} of the player's team.
     */
    @NotNull
    public TeamProgression getOldTeamProgression() {
        return oldTeam;
    }

    /**
     * Gets the new {@link TeamProgression} of the player's team.
     *
     * @return The new {@link TeamProgression} of the player's team.
     */
    @NotNull
    public TeamProgression getNewTeamProgression() {
        return newTeam;
    }

    /**
     * Gets the {@link UUID} of the player.
     *
     * @return The {@link UUID} of the player.
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
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
        return "TeamUpdateEvent{" +
                "oldTeam=" + oldTeam +
                ", newTeam=" + newTeam +
                ", playerUUID=" + playerUUID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamUpdateEvent that = (TeamUpdateEvent) o;

        if (!oldTeam.equals(that.oldTeam)) return false;
        if (!newTeam.equals(that.newTeam)) return false;
        return playerUUID.equals(that.playerUUID);
    }

    @Override
    public int hashCode() {
        int result = oldTeam.hashCode();
        result = 31 * result + newTeam.hashCode();
        result = 31 * result + playerUUID.hashCode();
        return result;
    }
}