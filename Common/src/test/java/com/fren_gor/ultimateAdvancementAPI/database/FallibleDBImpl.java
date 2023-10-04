package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotRegisteredException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class FallibleDBImpl implements IDatabase {

    private final IDatabase inner;
    private final Deque<Boolean> planning = new LinkedList<>();
    private final Set<DBOperation> filter = EnumSet.allOf(DBOperation.class);

    public FallibleDBImpl(@NotNull IDatabase inner) {
        this.inner = Objects.requireNonNull(inner);
    }

    public synchronized void addToPlanning(boolean... planning) {
        for (boolean b : planning) {
            this.planning.addLast(b);
        }
    }

    public synchronized void setPlanning(boolean... planning) {
        clearPlanning();
        for (boolean b : planning) {
            this.planning.addLast(b);
        }
    }

    public synchronized void clearPlanning() {
        this.planning.clear();
    }

    public synchronized void addFallibleOps(DBOperation... operations) {
        this.filter.addAll(Arrays.asList(operations));
    }

    public synchronized void setFallibleOps(DBOperation... operations) {
        clearFallibleOps();
        this.filter.addAll(Arrays.asList(operations));
    }

    public synchronized void clearFallibleOps() {
        this.filter.clear();
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
        checkPlanning(DBOperation.GET_TEAM_ID);
        return inner.getTeamId(uuid);
    }

    @Override
    public List<UUID> getTeamMembers(int teamId) throws SQLException {
        checkPlanning(DBOperation.GET_TEAM_MEMBERS);
        return inner.getTeamMembers(teamId);
    }

    @Override
    public Map<AdvancementKey, Integer> getTeamAdvancements(int teamId) throws SQLException {
        checkPlanning(DBOperation.GET_TEAM_ADVANCEMENTS);
        return inner.getTeamAdvancements(teamId);
    }

    @Override
    public Entry<TeamProgression, Boolean> loadOrRegisterPlayer(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        checkPlanning(DBOperation.LOAD_OR_REGISTER_PLAYER);
        return inner.loadOrRegisterPlayer(uuid, name);
    }

    @Override
    public TeamProgression loadUUID(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException {
        checkPlanning(DBOperation.LOAD_UUID);
        return inner.loadUUID(uuid);
    }

    @Override
    public TeamProgression createNewTeam() throws SQLException {
        checkPlanning(DBOperation.CREATE_NEW_TEAM);
        return inner.createNewTeam();
    }

    @Override
    public void updateAdvancement(@NotNull AdvancementKey key, int teamId, @Range(from = 0, to = Integer.MAX_VALUE) int progression) throws SQLException {
        checkPlanning(DBOperation.UPDATE_ADVANCEMENT);
        inner.updateAdvancement(key, teamId, progression);
    }

    @Override
    public LinkedList<Entry<AdvancementKey, Boolean>> getUnredeemed(int teamId) throws SQLException {
        checkPlanning(DBOperation.GET_UNREDEEMED);
        return inner.getUnredeemed(teamId);
    }

    @Override
    public void setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, int teamId) throws SQLException {
        checkPlanning(DBOperation.SET_UNREDEEMED);
        inner.setUnredeemed(key, giveRewards, teamId);
    }

    @Override
    public boolean isUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException {
        checkPlanning(DBOperation.IS_UNREDEEMED);
        return inner.isUnredeemed(key, teamId);
    }

    @Override
    public void unsetUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException {
        checkPlanning(DBOperation.UNSET_UNREDEEMED);
        inner.unsetUnredeemed(key, teamId);
    }

    @Override
    public void unsetUnredeemed(@NotNull List<Entry<AdvancementKey, Boolean>> keyList, int teamId) throws SQLException {
        checkPlanning(DBOperation.UNSET_UNREDEEMED);
        inner.unsetUnredeemed(keyList, teamId);
    }

    @Override
    public void unregisterPlayer(@NotNull UUID uuid) throws SQLException {
        checkPlanning(DBOperation.UNREGISTER_PLAYER);
        inner.unregisterPlayer(uuid);
    }

    @Override
    public void movePlayer(@NotNull UUID uuid, int newTeamId) throws SQLException {
        checkPlanning(DBOperation.MOVE_PLAYER);
        inner.movePlayer(uuid, newTeamId);
    }

    @Override
    public TeamProgression movePlayerInNewTeam(@NotNull UUID uuid) throws SQLException {
        checkPlanning(DBOperation.MOVE_PLAYER_IN_NEW_TEAM);
        return inner.movePlayerInNewTeam(uuid);
    }

    @Override
    public List<UUID> getPlayersByName(@NotNull String name) throws SQLException {
        checkPlanning(DBOperation.GET_PLAYERS_BY_NAME);
        return inner.getPlayersByName(name);
    }

    @Override
    public String getPlayerName(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException {
        checkPlanning(DBOperation.GET_PLAYER_NAME);
        return inner.getPlayerName(uuid);
    }

    @Override
    public void updatePlayerName(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        checkPlanning(DBOperation.UPDATE_PLAYER_NAME);
        inner.updatePlayerName(uuid, name);
    }

    @Override
    public void clearUpTeams() throws SQLException {
        checkPlanning(DBOperation.CLEAR_UP_TEAMS);
        inner.clearUpTeams();
    }

    private synchronized void checkPlanning(DBOperation operation) throws SQLException {
        if (filter.contains(operation) && !planning.isEmpty() && !planning.removeFirst()) {
            throw new PlannedFailureException();
        }
    }

    public static final class PlannedFailureException extends SQLException {
        public PlannedFailureException() {
            super("Planned failure!");
        }

        @Override
        public void printStackTrace() {
            // Don't print the entire stack trace, the failure is fine. However, at the same time,
            // print at least that the failure is planned to avoid possible confusion with other printed messages
            System.err.println(PlannedFailureException.class.getName() + ": " + this.getMessage());
        }
    }

    public static final class RuntimePlannedFailureException extends RuntimeException {
        public RuntimePlannedFailureException() {
            super("Planned runtime failure!");
        }

        @Override
        public void printStackTrace() {
            // Don't print the entire stack trace, the failure is fine. However, at the same time,
            // print at least that the failure is planned to avoid possible confusion with other printed messages
            System.err.println(RuntimePlannedFailureException.class.getName() + ": " + this.getMessage());
        }
    }

    public enum DBOperation {
        GET_TEAM_ID,
        GET_TEAM_MEMBERS,
        GET_TEAM_ADVANCEMENTS,
        LOAD_OR_REGISTER_PLAYER,
        LOAD_UUID,
        CREATE_NEW_TEAM,
        UPDATE_ADVANCEMENT,
        GET_UNREDEEMED,
        SET_UNREDEEMED,
        IS_UNREDEEMED,
        UNSET_UNREDEEMED,
        UNREGISTER_PLAYER,
        MOVE_PLAYER,
        MOVE_PLAYER_IN_NEW_TEAM,
        GET_PLAYERS_BY_NAME,
        GET_PLAYER_NAME,
        UPDATE_PLAYER_NAME,
        CLEAR_UP_TEAMS;
    }
}
