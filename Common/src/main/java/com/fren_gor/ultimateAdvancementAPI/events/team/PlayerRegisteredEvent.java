package com.fren_gor.ultimateAdvancementAPI.events.team;

import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * Called synchronously after a player is registered and added for the first time to a (brand new) team.
 * <p>The {@link AsyncTeamLoadEvent} loading the team is always called before this event.
 *
 * @since 3.0.0
 */
public class PlayerRegisteredEvent extends Event {

    private final TeamProgression team;
    private final UUID uuid;

    /**
     * Creates a new {@code PlayerRegisteredEvent}.
     *
     * @param team The {@link TeamProgression} of the player's team. It must be valid (see {@link TeamProgression#isValid()}).
     * @param uuid The {@link UUID} of the player which has been unregistered.
     */
    public PlayerRegisteredEvent(@NotNull TeamProgression team, @NotNull UUID uuid) {
        this.team = validateTeamProgression(team);
        this.uuid = Objects.requireNonNull(uuid, "UUID is null.");
    }

    /**
     * Gets the {@link TeamProgression} of the player's team.
     *
     * @return The {@link TeamProgression} of the player's team.
     */
    @NotNull
    public TeamProgression getTeamProgression() {
        return team;
    }

    /**
     * Gets the {@link UUID} of the player which has been registered.
     *
     * @return The {@link UUID} of the player which has been registered.
     */
    @NotNull
    public UUID getPlayerUUID() {
        return uuid;
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
        return "PlayerRegisteredEvent{" +
                "team=" + team +
                ", uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerRegisteredEvent that = (PlayerRegisteredEvent) o;

        if (!team.equals(that.team)) return false;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        int result = team.hashCode();
        result = 31 * result + uuid.hashCode();
        return result;
    }
}