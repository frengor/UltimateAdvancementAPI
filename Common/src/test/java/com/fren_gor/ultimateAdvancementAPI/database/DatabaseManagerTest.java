package com.fren_gor.ultimateAdvancementAPI.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.tests.AutoInject;
import com.fren_gor.ultimateAdvancementAPI.tests.DatabaseManagerUtils;
import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import com.fren_gor.ultimateAdvancementAPI.tests.database.BlockingDBImpl.BlockedDB;
import com.fren_gor.ultimateAdvancementAPI.tests.database.DBOperation;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingFailedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.ProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamLoadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.PlayerRegisteredEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DatabaseException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DatabaseManagerClosedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotRegisteredException;
import com.fren_gor.ultimateAdvancementAPI.tests.DatabaseManagerUtils.Paused;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.fren_gor.ultimateAdvancementAPI.tests.Utils.waitCompletion;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UAAPIExtension.class)
public class DatabaseManagerTest {

    @AutoInject
    private ServerMock server;
    @AutoInject
    private AdvancementMain advancementMain;
    @AutoInject
    private DatabaseManager dbManager;
    @AutoInject
    private DatabaseManagerUtils dbManagerUtils;

    @Test
    void playerLoadingTest() throws Exception {
        PlayerMock pl1 = loadPlayer();
        assertTrue(dbManager.isLoaded(pl1.getUniqueId()));

        PlayerMock pl2 = loadPlayer();
        assertTrue(dbManager.isLoaded(pl2.getUniqueId()));

        disconnectPlayer(pl1);
        disconnectPlayer(pl2);
    }

    @Test
    void advancementSetProgressionTest(AdvancementKey key) throws Exception {
        PlayerMock p = loadPlayer();

        ProgressionUpdateResult result = waitCompletion(dbManager.setProgression(key, p, 10)).get();
        assertEquals(0, result.oldProgression());
        assertEquals(10, result.newProgression());
        p.disconnect();
    }

    @Test
    void advancementSetProgressionWithFailureTest(AdvancementKey key) throws Exception {
        PlayerMock p = loadPlayer();

        Paused paused = dbManagerUtils.pauseFutureTasks();

        dbManagerUtils.getFallible().setFallibleOps(DBOperation.UPDATE_ADVANCEMENT);
        dbManagerUtils.getFallible().addToPlanning(true, false, true);
        CompletableFuture<ProgressionUpdateResult> updateResult1 = dbManager.setProgression(key, p, 10);

        // This should fail
        CompletableFuture<ProgressionUpdateResult> updateResult2 = dbManager.setProgression(key, p, 20);

        CompletableFuture<ProgressionUpdateResult> updateResult3 = dbManager.setProgression(key, p, 30);

        paused.resume();

        waitCompletion(updateResult1);
        waitCompletion(updateResult2);
        waitCompletion(updateResult3);

        assertFalse(updateResult1.isCompletedExceptionally());
        assertTrue(updateResult2.isCompletedExceptionally());
        assertFalse(updateResult3.isCompletedExceptionally());

        assertEquals(0, updateResult1.get().oldProgression());
        assertEquals(10, updateResult3.get().oldProgression());

        assertEquals(10, updateResult1.get().newProgression());
        assertEquals(30, updateResult3.get().newProgression());
        p.disconnect();
    }

    @Test
    void advancementIncrementProgressionTest(AdvancementKey key) throws Exception {
        PlayerMock p = loadPlayer();

        ProgressionUpdateResult updateResult = waitCompletion(dbManager.incrementProgression(key, p, 10)).get();

        assertEquals(0, updateResult.oldProgression());
        assertEquals(10, updateResult.newProgression());
        p.disconnect();
    }

    @Test
    void advancementIncrementProgressionWithFailureTest(AdvancementKey key) throws Exception {
        PlayerMock p = loadPlayer();

        Paused paused = dbManagerUtils.pauseFutureTasks();

        dbManagerUtils.getFallible().setFallibleOps(DBOperation.UPDATE_ADVANCEMENT);
        dbManagerUtils.getFallible().addToPlanning(true, false, true);
        CompletableFuture<ProgressionUpdateResult> updateResult1 = dbManager.incrementProgression(key, p, 10);

        // This should fail
        CompletableFuture<ProgressionUpdateResult> updateResult2 = dbManager.incrementProgression(key, p, 20);

        CompletableFuture<ProgressionUpdateResult> updateResult3 = dbManager.incrementProgression(key, p, 30);

        paused.resume();

        waitCompletion(updateResult1);
        waitCompletion(updateResult2);
        waitCompletion(updateResult3);

        assertFalse(updateResult1.isCompletedExceptionally());
        assertTrue(updateResult2.isCompletedExceptionally());
        assertFalse(updateResult3.isCompletedExceptionally());

        assertEquals(0, updateResult1.get().oldProgression());
        assertEquals(10, updateResult3.get().oldProgression());

        assertEquals(10, updateResult1.get().newProgression());
        assertEquals(40, updateResult3.get().newProgression());
        p.disconnect();
    }

    @Test
    void updatePlayerTeamTest() {
        PlayerMock pl1 = loadPlayer();
        PlayerMock pl2 = loadPlayer();
        PlayerMock pl3 = loadPlayer();

        TeamProgression tpl1 = dbManager.getTeamProgression(pl1);
        TeamProgression tpl2 = dbManager.getTeamProgression(pl2);
        TeamProgression tpl3 = dbManager.getTeamProgression(pl3);

        assertTrue(tpl1.isValid());
        assertTrue(tpl2.isValid());
        assertTrue(tpl3.isValid());

        assertNotEquals(tpl1, tpl2);
        assertNotEquals(tpl2, tpl3);
        assertNotEquals(tpl1, tpl3);

        CompletableFuture<Void> cf = waitCompletion(dbManager.updatePlayerTeam(pl1, tpl2));
        assertFalse(cf.isCompletedExceptionally());

        TeamProgression newTpl1 = dbManager.getTeamProgression(pl1);
        TeamProgression newTpl2 = dbManager.getTeamProgression(pl2);
        TeamProgression newTpl3 = dbManager.getTeamProgression(pl3);

        assertFalse(tpl1.isValid());
        assertTrue(tpl2.isValid());
        assertTrue(tpl3.isValid());

        assertTrue(newTpl1.isValid());
        assertTrue(newTpl2.isValid());
        assertTrue(newTpl3.isValid());

        assertNotEquals(tpl1, newTpl1);
        assertEquals(tpl2, newTpl2);
        assertEquals(tpl3, newTpl3);

        assertEquals(newTpl1, newTpl2);
        assertNotEquals(newTpl2, newTpl3);
        assertNotEquals(newTpl1, newTpl3);

        assertTrue(newTpl1.contains(pl1.getUniqueId()));
        assertTrue(newTpl1.contains(pl2.getUniqueId()));
        assertFalse(newTpl1.contains(pl3.getUniqueId()));
        assertFalse(newTpl3.contains(pl1.getUniqueId()));
        assertFalse(newTpl3.contains(pl2.getUniqueId()));
        assertTrue(newTpl3.contains(pl3.getUniqueId()));

        pl1.disconnect();
        pl2.disconnect();
        pl3.disconnect();
    }

    @Test
    void updatePlayerTeamWithFailureTest() {
        PlayerMock pl1 = loadPlayer();
        PlayerMock pl2 = loadPlayer();
        PlayerMock pl3 = loadPlayer();

        Paused paused = dbManagerUtils.pauseFutureTasks();

        dbManagerUtils.getFallible().setFallibleOps(DBOperation.MOVE_PLAYER);
        dbManagerUtils.getFallible().addToPlanning(true, false);
        CompletableFuture<Void> cf1 = dbManager.updatePlayerTeam(pl1, pl2);

        // This should fail
        CompletableFuture<Void> cf2 = dbManager.updatePlayerTeam(pl2, pl3);

        paused.resume();

        waitCompletion(cf1);
        waitCompletion(cf2);

        TeamProgression tpl1 = dbManager.getTeamProgression(pl1);
        TeamProgression tpl2 = dbManager.getTeamProgression(pl2);
        TeamProgression tpl3 = dbManager.getTeamProgression(pl3);

        assertTrue(tpl1.isValid());
        assertTrue(tpl2.isValid());
        assertTrue(tpl3.isValid());

        assertEquals(tpl1, tpl2);
        assertNotEquals(tpl1, tpl3);
        assertNotEquals(tpl2, tpl3);

        assertTrue(tpl1.contains(pl1.getUniqueId()));
        assertTrue(tpl1.contains(pl2.getUniqueId()));
        assertFalse(tpl1.contains(pl3.getUniqueId()));
        assertFalse(tpl3.contains(pl1.getUniqueId()));
        assertFalse(tpl3.contains(pl2.getUniqueId()));
        assertTrue(tpl3.contains(pl3.getUniqueId()));

        pl1.disconnect();
        pl2.disconnect();
        pl3.disconnect();
    }

    @Test
    void moveInNewTeamTest() throws Exception {
        PlayerMock p = loadPlayer();
        TeamProgression pro = dbManager.getTeamProgression(p);

        CompletableFuture<TeamProgression> cf = dbManager.movePlayerInNewTeam(p);

        waitCompletion(cf);

        assertFalse(cf.isCompletedExceptionally());
        TeamProgression newPro = cf.get();

        assertNotEquals(pro, newPro);
        assertFalse(pro.contains(p));
        assertTrue(newPro.contains(p));
        assertTrue(newPro.isValid());

        p.disconnect();
    }

    @Test
    void moveInNewTeamWithFailureTest() throws Exception {
        PlayerMock p = loadPlayer();
        TeamProgression pro = dbManager.getTeamProgression(p);
        assertTrue(pro.isValid());

        Paused paused = dbManagerUtils.pauseFutureTasks();

        dbManagerUtils.getFallible().setFallibleOps(DBOperation.MOVE_PLAYER_IN_NEW_TEAM);
        dbManagerUtils.getFallible().addToPlanning(false, false);
        CompletableFuture<TeamProgression> cf = dbManager.movePlayerInNewTeam(p);

        paused.resume();

        waitCompletion(cf);

        assertTrue(p.isOnline());

        assertTrue(cf.isCompletedExceptionally());
        assertTrue(pro.isValid());
        assertTrue(pro.contains(p));

        p.disconnect();
    }

    @Test
    void loadAndAddLoadingRequestToPlayerTest() throws Exception {
        PlayerMock p = loadPlayer();
        TeamProgression trueTeam = dbManager.getTeamProgression(p);
        disconnectPlayer(p);
        MockPlugin plugin = MockBukkit.createMockPlugin();
        TeamProgression team = waitCompletion(dbManager.loadAndAddLoadingRequestToPlayer(p.getUniqueId(), plugin)).get();
        assertEquals(1, dbManager.getLoadingRequestsAmount(p.getUniqueId(), plugin));
        assertEquals(trueTeam.getTeamId(), team.getTeamId());
        assertEquals(1, trueTeam.getSize());
        assertEquals(1, team.getSize());
        assertTrue(trueTeam.everyMemberMatch(uuid -> p.getUniqueId().equals(uuid)));
        assertTrue(team.everyMemberMatch(uuid -> p.getUniqueId().equals(uuid)));
        dbManager.removeLoadingRequestToPlayer(p.getUniqueId(), plugin);
        assertEquals(0, dbManager.getLoadingRequestsAmount(p.getUniqueId(), plugin));
    }

    @Test
    void loadAndAddLoadingRequestToPlayerWithFailureTest() throws Exception {
        PlayerMock p = loadPlayer();
        disconnectPlayer(p);
        MockPlugin plugin = MockBukkit.createMockPlugin();
        dbManagerUtils.getFallible().setFallibleOps(DBOperation.LOAD_UUID);
        dbManagerUtils.getFallible().addToPlanning(false);
        assertTrue(waitCompletion(dbManager.loadAndAddLoadingRequestToPlayer(p.getUniqueId(), plugin)).isCompletedExceptionally());
        assertEquals(0, dbManager.getLoadingRequestsAmount(p.getUniqueId(), plugin));
        assertThrows(UserNotLoadedException.class, () -> dbManager.getTeamProgression(p.getUniqueId()));
    }

    @Test
    void loadAndAddLoadingRequestToPlayerWithNonRegisteredPlayerTest() throws Exception {
        MockPlugin plugin = MockBukkit.createMockPlugin();
        UUID uuid = UUID.randomUUID();
        plugin.getLogger().warning("Expecting a " + UserNotRegisteredException.class.getSimpleName() + " for user " + uuid + '.');
        var cf = waitCompletion(dbManager.loadAndAddLoadingRequestToPlayer(uuid, plugin));
        assertTrue(cf.isCompletedExceptionally());
        try {
            cf.get(0, TimeUnit.SECONDS);
            fail();
        } catch (ExecutionException e) {
            assertInstanceOf(DatabaseException.class, e.getCause());
            assertInstanceOf(UserNotRegisteredException.class, e.getCause().getCause());
        }
    }

    @Test
    void registeredEventTest() throws Exception {
        final Object listener = new Object();
        final EventManager manager = advancementMain.getEventManager();
        final List<Entry<UUID, TeamProgression>> registeredPlayers = Collections.synchronizedList(new ArrayList<>());

        try {
            manager.register(listener, AsyncTeamLoadEvent.class, event -> {
                registeredPlayers.add(new SimpleEntry<>(null, event.getTeamProgression())); // Using SimpleEntry since UUID is null
            });
            manager.register(listener, PlayerRegisteredEvent.class, event -> {
                AdvancementUtils.checkSync();
                registeredPlayers.add(Map.entry(event.getPlayerUUID(), event.getTeamProgression()));
            });

            PlayerMock p = loadPlayer();
            synchronized (registeredPlayers) {
                assertEquals(2, registeredPlayers.size());
                var entry1 = registeredPlayers.get(0);
                var entry2 = registeredPlayers.get(1);
                assertNull(entry1.getKey());
                assertNotNull(entry2.getKey());
                assertEquals(entry1.getValue(), entry2.getValue());
                assertEquals(p.getUniqueId(), entry2.getKey());
            }
        } finally {
            manager.unregister(listener);
        }
    }

    @Test
    void registeredEventWithLoadAndAddLoadingRequestToPlayerTest() throws Exception {
        final CompletableFuture<Void> registered = new CompletableFuture<>();
        final AtomicReference<CompletableFuture<TeamProgression>> loaded = new AtomicReference<>();
        final Object listener = new Object();
        final EventManager manager = advancementMain.getEventManager();

        final MockPlugin plugin = MockBukkit.createMockPlugin();
        final PlayerMock p = server.addPlayer();

        try {
            manager.register(listener, PlayerRegisteredEvent.class, e -> {
                AdvancementUtils.checkSync();
                if (e.getPlayerUUID().equals(p.getUniqueId())) {
                    var cf = loaded.get();
                    assertNotNull(cf);
                    assertFalse(cf.isDone());
                    registered.complete(null);
                } else {
                    fail("Registered another player: " + e.getPlayerUUID());
                }
            });

            manager.register(listener, PlayerLoadingCompletedEvent.class, e -> {
                AdvancementUtils.checkSync();
                if (!registered.isDone() || !loaded.get().isDone()) {
                    fail("Player loading finished at wrong time: " + e.getPlayer().getName() + " (" + e.getPlayer().getUniqueId() + ')');
                }
            });

            manager.register(listener, PlayerLoadingFailedEvent.class, e -> {
                AdvancementUtils.checkSync();
                fail("Player loading failed: " + e.getPlayer().getName() + " (" + e.getPlayer().getUniqueId() + ')');
            });

            dbManagerUtils.waitForPendingTasks(false);

            loaded.set(dbManager.loadAndAddLoadingRequestToPlayer(p.getUniqueId(), plugin));
            loaded.get().handle((team, err) -> {
                if (err != null) {
                    return fail(err);
                } else {
                    assertTrue(registered.isDone());
                    assertFalse(registered.isCompletedExceptionally());
                    return team;
                }
            });

            for (int i = 0; i < 100; i++) {
                assertFalse(registered.isDone());
                assertFalse(loaded.get().isDone());
                Thread.yield();
            }

            server.getScheduler().performTicks(10);

            assertTrue(registered.isDone());
            assertFalse(registered.isCompletedExceptionally());
            assertTrue(loaded.get().isDone());
            assertFalse(loaded.get().isCompletedExceptionally());

        } finally {
            manager.unregister(listener);
        }
    }

    @Test
    void updaterLockLockingTest(AdvancementKey key) throws Exception {
        PlayerMock pl = loadPlayer();
        CompletableFuture<Void> completed = new CompletableFuture<>();
        CompletableFuture<Void> releaseLock = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                dbManager.updaterLock.lock();
                completed.complete(null);
                releaseLock.get();
            } catch (ExecutionException | InterruptedException ignored) {
            } finally {
                dbManager.updaterLock.unlock();
            }
        });
        t.start();

        waitCompletion(completed);

        var cf = dbManager.setProgression(key, pl, 10);

        for (int i = 0; i < 100; i++) {
            assertFalse(cf.isDone());
            server.getPluginManager().assertEventNotFired(ProgressionUpdateEvent.class);
            server.getScheduler().performOneTick();
        }

        assertFalse(cf.isDone());
        server.getPluginManager().assertEventNotFired(ProgressionUpdateEvent.class);

        releaseLock.complete(null);
        ProgressionUpdateResult res = waitCompletion(cf).get();

        server.getPluginManager().assertEventFired(ProgressionUpdateEvent.class);
        assertEquals(0, res.oldProgression());
        assertEquals(10, res.newProgression());

        disconnectPlayer(pl);
    }

    @Test
    void closingTest() throws Exception {
        PlayerMock p1 = loadPlayer();
        PlayerMock p2 = loadPlayer();
        waitCompletion(dbManager.updatePlayerTeam(p1, p2)).get();

        TeamProgression team = dbManager.getTeamProgression(p1);

        var cf1 = dbManager.movePlayerInNewTeam(p1);
        dbManagerUtils.waitForPendingTasks(false);
        Paused paused = dbManagerUtils.pauseFutureTasks(false);
        var cf2 = dbManager.movePlayerInNewTeam(p2);

        new Thread(() -> {
            while (!dbManagerUtils.getExecutor().isShutdown()) {
                Thread.yield();
            }
            paused.resume();
        }).start();

        dbManager.close();

        assertFalse(team.isValid());

        try {
            cf1.get(0, TimeUnit.SECONDS);
            fail();
        } catch (ExecutionException e) {
            assertInstanceOf(DatabaseManagerClosedException.class, e.getCause());
        }
        try {
            cf2.get(0, TimeUnit.SECONDS);
            fail();
        } catch (ExecutionException e) {
            assertInstanceOf(DatabaseManagerClosedException.class, e.getCause());
        }
    }

    @Test
    void joinAndQuitTest() {
        dbManagerUtils.getBlocking().setBlockingOps(DBOperation.LOAD_OR_REGISTER_PLAYER);
        dbManagerUtils.getBlocking().setPlanning(false);
        PlayerMock p = server.addPlayer();
        disconnectPlayer(p, false, dbManagerUtils.getBlocking().getBlockedDB(DBOperation.LOAD_OR_REGISTER_PLAYER));
        server.getPluginManager().assertEventFired(PlayerRegisteredEvent.class, e -> e.getPlayerUUID().equals(p.getUniqueId()));
    }

    @Test
    void createNewTeamTest() throws Exception {
        MockPlugin plugin = MockBukkit.createMockPlugin();
        PlayerMock p1 = loadPlayer();

        TeamProgression team = waitCompletion(dbManager.createNewTeamWithOneLoadingRequest(plugin)).get();
        assertTrue(team.isValid());
        assertEquals(1, dbManager.getLoadingRequestsAmount(team, plugin));

        PlayerMock p2 = loadPlayer();

        TeamProgression t1 = dbManager.getTeamProgression(p1);
        TeamProgression t2 = dbManager.getTeamProgression(p2);
        assertNotEquals(t1.getTeamId(), t2.getTeamId()); // Just to be sure
        assertNotEquals(team.getTeamId(), t1.getTeamId());
        assertNotEquals(team.getTeamId(), t2.getTeamId());

        dbManager.removeLoadingRequestToTeam(team, plugin);
        assertFalse(team.isValid());
    }

    @Test
    void createNewTeamAndMovePlayerInsideTest() throws Exception {
        MockPlugin plugin = MockBukkit.createMockPlugin();
        PlayerMock p1 = loadPlayer();

        TeamProgression t1 = dbManager.getTeamProgression(p1);

        TeamProgression team = waitCompletion(dbManager.createNewTeamWithOneLoadingRequest(plugin)).get();
        assertTrue(team.isValid());
        assertEquals(1, dbManager.getLoadingRequestsAmount(team, plugin));

        waitCompletion(dbManager.updatePlayerTeam(p1, team)).get();

        assertFalse(t1.isValid());

        dbManager.removeLoadingRequestToTeam(team, plugin);
        assertTrue(team.isValid());
        assertEquals(0, dbManager.getLoadingRequestsAmount(team, plugin));

        disconnectPlayer(p1);
        assertFalse(team.isValid());
    }

    @Test
    void makeSurePlayerAreNotMovedBeforeRegisterEventTest() throws Exception {
        MockPlugin plugin = MockBukkit.createMockPlugin();
        TeamProgression team = waitCompletion(dbManager.createNewTeamWithOneLoadingRequest(plugin)).get();

        Paused paused = dbManagerUtils.pauseFutureTasks();

        PlayerMock p = server.addPlayer();

        server.getPluginManager().assertEventNotFired(PlayerRegisteredEvent.class);
        assertTrue(team.isValid());

        assertThrows(UserNotLoadedException.class, () -> dbManager.updatePlayerTeam(p, team));

        server.getPluginManager().assertEventNotFired(PlayerRegisteredEvent.class);
        assertTrue(team.isValid());

        // Let PlayerRegisteredEvent fire
        paused.resume();
        dbManagerUtils.waitForPendingTasks();
        server.getScheduler().performTicks(20);

        server.getPluginManager().assertEventFired(PlayerRegisteredEvent.class);
        assertTrue(team.isValid());

        waitCompletion(dbManager.updatePlayerTeam(p, team)).get();
        dbManager.removeLoadingRequestToTeam(team, plugin);
        disconnectPlayer(p);
    }

    private PlayerMock loadPlayer() {
        CompletableFuture<Void> finished = new CompletableFuture<>();
        AtomicBoolean skip = new AtomicBoolean(false);
        AtomicBoolean hadSuccess = new AtomicBoolean(false);

        final Object listener = new Object();
        final EventManager manager = advancementMain.getEventManager();

        final PlayerMock p = server.addPlayer();

        try {
            // These events should run after a few ticks, so there shouldn't be any problem having them registered
            // after the player addition
            manager.register(listener, PlayerLoadingCompletedEvent.class, e -> {
                AdvancementUtils.checkSync();

                if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                    if (skip.getAndSet(true)) {
                        fail("PlayerLoadingCompletedEvent called too many times for player " + e.getPlayer().getName() + '.');
                    }
                    hadSuccess.set(true);
                    finished.complete(null);
                } else {
                    fail("Another player loading completed: " + e.getPlayer().getName() + " (" + e.getPlayer().getUniqueId() + ')');
                }
            });

            manager.register(listener, PlayerLoadingFailedEvent.class, e -> {
                AdvancementUtils.checkSync();

                if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                    if (skip.getAndSet(true)) {
                        fail("PlayerLoadingCompletedEvent called too many times for player " + e.getPlayer().getName() + '.');
                    }
                    hadSuccess.set(false);
                    finished.complete(null);
                } else {
                    fail("Another player loading failed: " + e.getPlayer().getName() + " (" + e.getPlayer().getUniqueId() + ')');
                }
            });

            waitCompletion(finished);

            assertTrue(hadSuccess.get());
            assertTrue(skip.get());
        } finally {
            manager.unregister(listener);
        }
        return p;
    }

    /**
     * Correct to use only if the player's team has only the player itself as member
     */
    private void disconnectPlayer(PlayerMock player) {
        disconnectPlayer(player, true, null);
    }

    /**
     * Correct to use only if the player's team has only the player itself as member
     */
    private void disconnectPlayer(PlayerMock player, boolean checkTeamSize, BlockedDB blocking) {
        if (checkTeamSize) {
            assertEquals(1, dbManager.getTeamProgression(player).getSize(), "Incorrect usage of disconnectPlayer inside tests");
        }

        CompletableFuture<Void> finished = new CompletableFuture<>();
        AtomicBoolean skip = new AtomicBoolean(false);

        final Object listener = new Object();
        final EventManager manager = advancementMain.getEventManager();

        try {
            manager.register(listener, AsyncTeamUnloadEvent.class, e -> {
                if (e.getTeamProgression().contains(player.getUniqueId())) {
                    if (skip.getAndSet(true)) {
                        fail("AsyncTeamUnloadEvent called too many times for player " + player.getName() + '.');
                    }
                    finished.complete(null);
                } else {
                    fail("Another AsyncTeamUnloadEvent got called: (" + e.getTeamProgression().getTeamId() + ')');
                }
            });

            if (blocking != null) {
                blocking.waitBlock();
            }

            player.disconnect();

            if (blocking != null) {
                blocking.resume();
            }

            waitCompletion(finished);

            assertTrue(skip.get());
        } finally {
            manager.unregister(listener);
        }
    }
}
