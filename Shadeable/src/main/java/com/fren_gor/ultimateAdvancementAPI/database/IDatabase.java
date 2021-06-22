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

public interface IDatabase {

    void setUp() throws SQLException;

    Connection openConnection() throws SQLException;

    void close() throws SQLException;

    default int getTeamId(@NotNull Player player) throws SQLException, UserNotRegisteredException {
        return getTeamId(uuidFromPlayer(player));
    }

    int getTeamId(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException;

    List<UUID> getTeamMembers(int teamId) throws SQLException;

    Map<AdvancementKey, Integer> getTeamAdvancements(int teamId) throws SQLException;

    // boolean is true iff player is registered (not loaded)
    Entry<TeamProgression, Boolean> loadOrRegisterPlayer(@NotNull UUID uuid, @NotNull String name) throws SQLException;

    TeamProgression loadUUID(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException;

    void updateAdvancement(@NotNull AdvancementKey key, int teamId, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) throws SQLException;

    List<Entry<AdvancementKey, Boolean>> getUnredeemed(int teamId) throws SQLException;

    void setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, int teamId) throws SQLException;

    boolean isUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException;

    void unsetUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException;

    void unsetUnredeemed(@NotNull List<Entry<AdvancementKey, Boolean>> keyList, int teamId) throws SQLException;

    default void unregisterPlayer(@NotNull Player player) throws SQLException {
        unregisterPlayer(uuidFromPlayer(player));
    }

    void unregisterPlayer(@NotNull UUID uuid) throws SQLException;

    default void movePlayer(@NotNull Player player, int newTeamId) throws SQLException {
        movePlayer(uuidFromPlayer(player), newTeamId);
    }

    void movePlayer(@NotNull UUID uuid, int newTeamId) throws SQLException;

    default TeamProgression movePlayerInNewTeam(@NotNull Player player) throws SQLException {
        return movePlayerInNewTeam(uuidFromPlayer(player));
    }

    TeamProgression movePlayerInNewTeam(@NotNull UUID uuid) throws SQLException;

    default UUID getPlayerByName(@NotNull String name) throws SQLException, UserNotRegisteredException {
        List<UUID> l = getPlayersByName(name);
        if (l.size() == 0) {
            throw new UserNotRegisteredException("Couldn't find any player with name '" + name + '\'');
        }
        return l.get(0);
    }

    List<UUID> getPlayersByName(@NotNull String name) throws SQLException;

    String getPlayerName(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException;

    void updatePlayerName(@NotNull UUID uuid, @NotNull String name) throws SQLException;

    void clearUpTeams() throws SQLException;

}
