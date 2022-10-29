package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotRegisteredException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

public class FallibleDBImpl implements IDatabase {

    private final IDatabase inner;
    private final Deque<Boolean> planning = new LinkedList<>();

    public FallibleDBImpl(@NotNull IDatabase inner) {
        this.inner = Objects.requireNonNull(inner);
    }

    public void addToPlanning(List<@NotNull Boolean> planning) {
        for (boolean b : planning) {
            this.planning.addLast(b);
        }
    }

    public void setPlanning(List<@NotNull Boolean> planning) {
        clearPlanning();
        for (boolean b : planning) {
            this.planning.addLast(b);
        }
    }

    public void clearPlanning() {
        this.planning.clear();
    }

    @Override
    public void setUp() throws SQLException {
        inner.setUp();
    }

    @Override
    public Connection openConnection() throws SQLException {
        return inner.openConnection();
    }

    @Override
    public void close() throws SQLException {
        inner.close();
    }

    @Override
    public int getTeamId(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException {
        checkPlanning();
        return inner.getTeamId(uuid);
    }

    @Override
    public List<UUID> getTeamMembers(int teamId) throws SQLException {
        checkPlanning();
        return inner.getTeamMembers(teamId);
    }

    @Override
    public Map<AdvancementKey, Integer> getTeamAdvancements(int teamId) throws SQLException {
        checkPlanning();
        return inner.getTeamAdvancements(teamId);
    }

    @Override
    public Entry<TeamProgression, Boolean> loadOrRegisterPlayer(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        checkPlanning();
        return inner.loadOrRegisterPlayer(uuid, name);
    }

    @Override
    public TeamProgression loadUUID(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException {
        checkPlanning();
        return inner.loadUUID(uuid);
    }

    @Override
    public void updateAdvancement(@NotNull AdvancementKey key, int teamId, @Range(from = 0, to = Integer.MAX_VALUE) int progression) throws SQLException {
        checkPlanning();
        inner.updateAdvancement(key, teamId, progression);
    }

    @Override
    public List<Entry<AdvancementKey, Boolean>> getUnredeemed(int teamId) throws SQLException {
        checkPlanning();
        return inner.getUnredeemed(teamId);
    }

    @Override
    public void setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, int teamId) throws SQLException {
        checkPlanning();
        inner.setUnredeemed(key, giveRewards, teamId);
    }

    @Override
    public boolean isUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException {
        checkPlanning();
        return inner.isUnredeemed(key, teamId);
    }

    @Override
    public void unsetUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException {
        checkPlanning();
        inner.unsetUnredeemed(key, teamId);
    }

    @Override
    public void unsetUnredeemed(@NotNull List<Entry<AdvancementKey, Boolean>> keyList, int teamId) throws SQLException {
        checkPlanning();
        inner.unsetUnredeemed(keyList, teamId);
    }

    @Override
    public void unregisterPlayer(@NotNull UUID uuid) throws SQLException {
        checkPlanning();
        inner.unregisterPlayer(uuid);
    }

    @Override
    public void movePlayer(@NotNull UUID uuid, int newTeamId) throws SQLException {
        checkPlanning();
        inner.movePlayer(uuid, newTeamId);
    }

    @Override
    public TeamProgression movePlayerInNewTeam(@NotNull UUID uuid) throws SQLException {
        checkPlanning();
        return inner.movePlayerInNewTeam(uuid);
    }

    @Override
    public List<UUID> getPlayersByName(@NotNull String name) throws SQLException {
        checkPlanning();
        return inner.getPlayersByName(name);
    }

    @Override
    public String getPlayerName(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException {
        checkPlanning();
        return inner.getPlayerName(uuid);
    }

    @Override
    public void updatePlayerName(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        checkPlanning();
        inner.updatePlayerName(uuid, name);
    }

    @Override
    public void clearUpTeams() throws SQLException {
        // The call to clearUpTeams() is mostly an implementation detail, and calling checkPlanning() here means making
        // writing tests harder. So calling checkPlanning() is avoided here.
        inner.clearUpTeams();
    }

    private void checkPlanning() throws SQLException {
        if (!planning.isEmpty() && !planning.removeFirst()) {
            throw new PlannedFailureException();
        }
    }

    public static final class PlannedFailureException extends SQLException {
        public PlannedFailureException() {
            super("Planned failure!");
        }
    }
}
