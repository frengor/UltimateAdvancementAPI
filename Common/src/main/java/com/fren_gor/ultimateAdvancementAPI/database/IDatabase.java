package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotRegisteredException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;

/**
 * Interface for database implementations.
 * <p>The database stores the following information:
 * <ul>
 *     <li>The players name and {@link UUID};</li>
 *     <li>The team ids;</li>
 *     <li>The current progression of every advancement for every team;</li>
 *     <li>The unredeemed advancements.</li>
 * </ul>
 * <p>The connection to the database should be opened in the constructor of the implementing class.
 */
public interface IDatabase {

    /**
     * Sets up the database, like creating the tables.
     *
     * @throws SQLException If an SQL exception occurs.
     */
    void setUp() throws SQLException;

    /**
     * Opens or gets a connection to the database.
     *
     * @return The opened connection.
     * @throws SQLException If an SQL exception occurs.
     */
    Connection openConnection() throws SQLException;

    /**
     * Closes the connection to the database.
     *
     * @throws SQLException If an SQL exception occurs.
     */
    void close() throws SQLException;

    /**
     * Gets the team id of the provided player.
     *
     * @param player The player.
     * @return The team id.
     * @throws SQLException If an SQL exception occurs.
     * @throws UserNotRegisteredException If the provided player could not be found in the database.
     */
    default int getTeamId(@NotNull Player player) throws SQLException, UserNotRegisteredException {
        return getTeamId(uuidFromPlayer(player));
    }

    /**
     * Gets the team id of the provided player.
     *
     * @param uuid The player {@link UUID}.
     * @return The team id.
     * @throws SQLException If an SQL exception occurs.
     * @throws UserNotRegisteredException If the provided player could not be found in the database.
     */
    int getTeamId(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException;

    /**
     * Gets the team members.
     *
     * @param teamId The team id.
     * @return The team members.
     * @throws SQLException If an SQL exception occurs.
     */
    List<UUID> getTeamMembers(int teamId) throws SQLException;

    /**
     * Gets the team advancements mapped with their respective progressions.
     * <p>The resulting map can contain keys of not-existent advancements that might exist in the database.
     *
     * @param teamId The team id.
     * @return The team advancements mapped with their respective progressions.
     * @throws SQLException If an SQL exception occurs.
     */
    Map<AdvancementKey, Integer> getTeamAdvancements(int teamId) throws SQLException;

    /**
     * Loads the provided player or registers they if they are new.
     *
     * @param uuid The player {@link UUID}.
     * @param name The player name.
     * @return A pair containing a (always new) {@link TeamProgression} with the player team information and
     *         whether the player has been registered (so it was not found in the database).
     * @throws SQLException If an SQL exception occurs.
     */
    Entry<TeamProgression, Boolean> loadOrRegisterPlayer(@NotNull UUID uuid, @NotNull String name) throws SQLException;

    /**
     * Loads the provided player from the database.
     * If the player is not registered an {@link UserNotRegisteredException} is thrown.
     *
     * @param uuid The player {@link UUID}.
     * @return The player team's {@link TeamProgression}.
     * @throws SQLException If an SQL exception occurs.
     * @throws UserNotRegisteredException If the provided player could not be found in the database.
     * @see #loadOrRegisterPlayer(UUID, String)
     */
    TeamProgression loadUUID(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException;

    /**
     * Updates the progression for the provided advancement and the specified team.
     *
     * @param key The advancement key.
     * @param teamId The team id.
     * @param progression The new progression.
     * @throws SQLException If an SQL exception occurs.
     */
    void updateAdvancement(@NotNull AdvancementKey key, int teamId, @Range(from = 0, to = Integer.MAX_VALUE) int progression) throws SQLException;

    /**
     * Get the list of the unredeemed advancements of the provided team.
     *
     * @param teamId The team id.
     * @return A list of pairs containing the key of the unredeemed advancement and whether to give rewards for it
     *         (see {@link #setUnredeemed(AdvancementKey, boolean, int)}).
     * @throws SQLException If an SQL exception occurs.
     */
    List<Entry<AdvancementKey, Boolean>> getUnredeemed(int teamId) throws SQLException;

    /**
     * Sets the provided advancement unredeemed for the specified team.
     *
     * @param key The advancement key.
     * @param giveRewards Whether to give rewards on redeem.
     * @param teamId The team id.
     * @throws SQLException If an SQL exception occurs.
     */
    void setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, int teamId) throws SQLException;

    /**
     * Returns whether the provided advancement is unredeemed for the specified team.
     *
     * @param key The advancement key.
     * @param teamId The team id.
     * @return Whether the provided advancement is unredeemed for the specified team.
     * @throws SQLException If an SQL exception occurs.
     */
    boolean isUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException;

    /**
     * Sets the provided advancement redeemed for the specified team if it is unredeemed.
     *
     * @param key The advancement key.
     * @param teamId The team id.
     * @throws SQLException If an SQL exception occurs.
     */
    void unsetUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException;

    /**
     * Sets the provided advancements redeemed for the specified team.
     *
     * @param keyList The list of advancements to set redeemed.
     * @param teamId The team id.
     * @throws SQLException If an SQL exception occurs.
     * @implSpec The default implementation ignores the boolean value in each pair of the list.
     */
    void unsetUnredeemed(@NotNull List<Entry<AdvancementKey, Boolean>> keyList, int teamId) throws SQLException;

    /**
     * Deletes the provided player from the database.
     *
     * @param player The player to be unregistered.
     * @throws SQLException If an SQL exception occurs.
     */
    default void unregisterPlayer(@NotNull Player player) throws SQLException {
        unregisterPlayer(uuidFromPlayer(player));
    }

    /**
     * Deletes the provided player from the database.
     *
     * @param uuid The {@link UUID} of the player to be unregistered.
     * @throws SQLException If an SQL exception occurs.
     */
    void unregisterPlayer(@NotNull UUID uuid) throws SQLException;

    /**
     * Moves the provided player to the specified team.
     *
     * @param player The player to be moved.
     * @param newTeamId The team id.
     * @throws SQLException If an SQL exception occurs.
     */
    default void movePlayer(@NotNull Player player, int newTeamId) throws SQLException {
        movePlayer(uuidFromPlayer(player), newTeamId);
    }

    /**
     * Moves the provided player to the specified team.
     *
     * @param uuid The {@link UUID} of the player to be moved.
     * @param newTeamId The team id.
     * @throws SQLException If an SQL exception occurs.
     */
    void movePlayer(@NotNull UUID uuid, int newTeamId) throws SQLException;

    /**
     * Moves a player in a new team and returns the {@link TeamProgression} of the new team.
     *
     * @param player The player to be moved apart from their team.
     * @return The {@link TeamProgression} of the new team.
     * @throws SQLException If an SQL exception occurs.
     */
    default TeamProgression movePlayerInNewTeam(@NotNull Player player) throws SQLException {
        return movePlayerInNewTeam(uuidFromPlayer(player));
    }

    /**
     * Moves a player in a new team and returns the {@link TeamProgression} of the new team.
     *
     * @param uuid The {@link UUID} of the player to be moved apart from their team.
     * @return The {@link TeamProgression} of the new team.
     * @throws SQLException If an SQL exception occurs.
     */
    TeamProgression movePlayerInNewTeam(@NotNull UUID uuid) throws SQLException;

    /**
     * Gets the {@link UUID} of the player with the specified name.
     * <p>Since more than one player could have the same name in the database (for example, if the old owner changes
     * name without updating it in the database and another player with that name joins the server), this method
     * returns the {@link UUID} of the first player found with the specified name.
     *
     * @param name The player name.
     * @return The {@link UUID} of the first player found with the provided name.
     * @throws SQLException If an SQL exception occurs.
     * @throws UserNotRegisteredException If the provided player name could not be found in the database.
     */
    default UUID getPlayerByName(@NotNull String name) throws SQLException, UserNotRegisteredException {
        List<UUID> l = getPlayersByName(name);
        if (l.size() == 0) {
            throw new UserNotRegisteredException("Couldn't find any player with name '" + name + '\'');
        }
        return l.get(0);
    }

    /**
     * Gets a list of the players with the specified name (in the database).
     * <p>Since more than one player could have the same name in the database (for example, if the old owner changes
     * name without updating it in the database and another player with that name joins the server), this method
     * could return a list with more than one {@link UUID}.
     *
     * @param name The player name.
     * @return A list of the {@link UUID}s of the players with the specified name.
     * @throws SQLException If an SQL exception occurs.
     */
    List<UUID> getPlayersByName(@NotNull String name) throws SQLException;

    /**
     * Gets the stored name for the specified player.
     *
     * @param uuid The player {@link UUID}.
     * @return The stored name.
     * @throws SQLException If an SQL exception occurs.
     * @throws UserNotRegisteredException If the provided player could not be found in the database.
     */
    String getPlayerName(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException;

    /**
     * Updates the player name in the database.
     *
     * @param uuid The {@link UUID} of the player to be updated.
     * @param name The new player name.
     * @throws SQLException If an SQL exception occurs.
     */
    void updatePlayerName(@NotNull UUID uuid, @NotNull String name) throws SQLException;

    /**
     * Clears the unused team ids in the database.
     *
     * @throws SQLException If an SQL exception occurs.
     */
    void clearUpTeams() throws SQLException;
}
