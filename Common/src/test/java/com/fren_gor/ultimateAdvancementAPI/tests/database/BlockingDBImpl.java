package com.fren_gor.ultimateAdvancementAPI.tests.database;

import com.fren_gor.ultimateAdvancementAPI.database.IDatabase;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotRegisteredException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BlockingDBImpl implements IDatabase {

    private final IDatabase inner;
    private final Deque<Boolean> planning = new LinkedList<>();
    private final Map<DBOperation, BlockedDB> blocking = new EnumMap<>(DBOperation.class);

    public BlockingDBImpl(@NotNull IDatabase inner) {
        this.inner = Objects.requireNonNull(inner);
    }

    @NotNull
    public synchronized BlockedDB getBlockedDB(DBOperation operation) {
        return Objects.requireNonNull(blocking.get(operation));
    }

    public synchronized void addToPlanning(boolean... planning) {
        for (boolean b : planning) {
            this.planning.addLast(b);
        }
    }

    public synchronized void setPlanning(boolean... planning) {
        clearPlanning();
        addToPlanning(planning);
    }

    public synchronized void clearPlanning() {
        this.planning.clear();
    }

    public synchronized void addBlockingOps(DBOperation... operations) {
        for (DBOperation op : operations) {
            this.blocking.put(op, new BlockedDB(op));
        }
    }

    public synchronized void setBlockingOps(DBOperation... operations) {
        clearBlockingOps();
        addBlockingOps(operations);
    }

    public synchronized void clearBlockingOps() {
        List<DBOperation> blocked = new LinkedList<>(); // LinkedList is fine since elements are added only in case of failure
        for (BlockedDB op : blocking.values()) {
            if (op.isBlocked()) {
                blocked.add(op.operation);
            }
        }
        if (!blocked.isEmpty()) {
            StringJoiner j = new StringJoiner(", ");
            blocked.forEach(op -> j.add(op.name()));
            throw new IllegalStateException("Tried to clear blocking operation while being blocked on: " + j);
        }
        this.blocking.clear();
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

    private void checkPlanning(DBOperation operation) {
        BlockedDB blocked;
        synchronized (this) {
            blocked = blocking.get(operation);
            if (blocked == null || planning.isEmpty() || planning.removeFirst()) {
                return;
            }
        }
        try {
            blocked.block();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final class BlockedDB {
        private final DBOperation operation;
        private CompletableFuture<Void> blocker;
        private CompletableFuture<Void> waiter;

        BlockedDB(DBOperation operation) {
            this.operation = operation;
        }

        void block() throws ExecutionException, InterruptedException {
            CompletableFuture<Void> block;
            synchronized (this) {
                if (blocker != null) {
                    throw new IllegalStateException("A " + operation.name() + " db operation is already blocked.");
                }
                block = blocker = new CompletableFuture<>();
                if (waiter == null) {
                    waiter = CompletableFuture.completedFuture(null);
                } else {
                    try {
                        waiter.complete(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            block.get();
        }

        public synchronized boolean isBlocked() {
            return blocker != null;
        }

        public void waitBlock() {
            CompletableFuture<Void> wait;
            synchronized (this) {
                if (isBlocked() || waiter != null) {
                    return;
                }
                wait = waiter = new CompletableFuture<>();
            }
            try {
                wait.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void waitBlockAndResume() {
            waitBlock();
            resume();
        }

        public synchronized void resume() {
            if (blocker == null) {
                throw new IllegalStateException(operation.name() + " db operation is not blocked.");
            }
            try {
                blocker.complete(null);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                blocker = null;
            }
        }
    }
}
