package com.fren_gor.ultimateAdvancementAPI.events;

import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * Called when a player is loaded successfully.
 * <p>Will always be called after the {@link PlayerJoinEvent} for such player.
 */
public final class PlayerLoadingCompletedEvent extends Event {

    private final Player player;
    private final TeamProgression progression;

    /**
     * Creates a new {@code PlayerLoadingCompletedEvent}.
     *
     * @param player The loaded player.
     * @param progression The {@link TeamProgression} of the loaded player's team. It must be valid (see {@link TeamProgression#isValid()}).
     */
    public PlayerLoadingCompletedEvent(@NotNull Player player, @NotNull TeamProgression progression) {
        this.player = Objects.requireNonNull(player, "Player is null.");
        this.progression = validateTeamProgression(progression);
    }

    /**
     * Gets the loaded player.
     *
     * @return The loaded player.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the {@link TeamProgression} of the loaded player's team.
     *
     * @return The {@link TeamProgression} of the loaded player's team.
     */
    @NotNull
    public TeamProgression getTeamProgression() {
        return progression;
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
        return "PlayerLoadingCompletedEvent{" +
                "player=" + player +
                ", progression=" + progression +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerLoadingCompletedEvent that = (PlayerLoadingCompletedEvent) o;

        if (!player.equals(that.player)) return false;
        return progression.equals(that.progression);
    }

    @Override
    public int hashCode() {
        int result = player.hashCode();
        result = 31 * result + progression.hashCode();
        return result;
    }
}