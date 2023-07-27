package com.fren_gor.ultimateAdvancementAPI.tests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.FallibleDBImpl;
import com.fren_gor.ultimateAdvancementAPI.database.FallibleDBImpl.DBOperation;
import com.fren_gor.ultimateAdvancementAPI.database.IDatabase;
import com.fren_gor.ultimateAdvancementAPI.database.ProgressionUpdateResult;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingFailedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamLoadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.PlayerRegisteredEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DatabaseException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DatabaseManagerClosedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotRegisteredException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import org.bukkit.craftbukkit.mocked0_0_R1.VersionedServerMock.CustomScheduler;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    private static Constructor<DatabaseManager> dbManagerConstructor;
    private static Field executorField;

    @BeforeAll
    static void initAll() throws Exception {
        dbManagerConstructor = DatabaseManager.class.getDeclaredConstructor(AdvancementMain.class, IDatabase.class);
        dbManagerConstructor.setAccessible(true);

        executorField = DatabaseManager.class.getDeclaredField("executor");
        executorField.setAccessible(true);
    }

    private ServerMock server;
    private AdvancementMain advancementMain;
    private DatabaseManager databaseManager;
    private FallibleDBImpl fallible;
    private ExecutorService executor;

    private AdvancementKey KEY1;
    private AdvancementKey KEY2;
    private AdvancementKey KEY3;

    @BeforeEach
    void init() throws Exception {
        server = Utils.mockServer();
        advancementMain = Utils.newAdvancementMain(MockBukkit.createMockPlugin("testPlugin"), main -> dbManagerConstructor.newInstance(main, fallible = new FallibleDBImpl(new InMemory(main.getLogger()))));
        databaseManager = advancementMain.getDatabaseManager();
        assertNotNull(fallible);
        executor = (ExecutorService) executorField.get(databaseManager);
        assertNotNull(executor);

        KEY1 = new AdvancementKey("a-namespace_", "a_-key/1");
        KEY2 = new AdvancementKey("a-namespace_", "a_-key/2");
        KEY3 = new AdvancementKey("a-namespace_", "a_-key/3");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (!executor.isShutdown()) {
            // Wait for pending tasks before closing the server
            // This cycle should ideally let tasks waiting for the server main thread to finish
            waitForPendingTasks();
            for (int i = 0; i < 20; i++) {
                server.getScheduler().performOneTick();
                waitForPendingTasks();
            }
        }

        advancementMain.disable();
        advancementMain = null;
        databaseManager = null;
        fallible = null;
        MockBukkit.unmock();
        server = null;
    }

    @Test
    void playerLoadingTest() throws Exception {
        PlayerMock pl1 = loadPlayer();
        assertTrue(databaseManager.isLoaded(pl1.getUniqueId()));

        PlayerMock pl2 = loadPlayer();
        assertTrue(databaseManager.isLoaded(pl2.getUniqueId()));

        disconnectPlayer(pl1);
        disconnectPlayer(pl2);
    }

    @Test
    void advancementSetProgressionTest() throws Exception {
        PlayerMock p = loadPlayer();

        ProgressionUpdateResult result = waitCompletion(databaseManager.setProgression(KEY1, p, 10)).get();
        assertEquals(0, result.oldProgression());
        assertEquals(10, result.newProgression());
        p.disconnect();
    }

    @Test
    void advancementSetProgressionWithFailureTest() throws Exception {
        PlayerMock p = loadPlayer();

        Paused paused = pauseFutureTasks();

        fallible.setFallibleOps(DBOperation.UPDATE_ADVANCEMENT);
        fallible.addToPlanning(true, false, true);
        CompletableFuture<ProgressionUpdateResult> updateResult1 = databaseManager.setProgression(KEY2, p, 10);

        // This should fail
        CompletableFuture<ProgressionUpdateResult> updateResult2 = databaseManager.setProgression(KEY2, p, 20);

        CompletableFuture<ProgressionUpdateResult> updateResult3 = databaseManager.setProgression(KEY2, p, 30);

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
    void advancementIncrementProgressionTest() throws Exception {
        PlayerMock p = loadPlayer();

        ProgressionUpdateResult updateResult = waitCompletion(databaseManager.incrementProgression(KEY1, p, 10)).get();

        assertEquals(0, updateResult.oldProgression());
        assertEquals(10, updateResult.newProgression());
        p.disconnect();
    }

    @Test
    void advancementIncrementProgressionWithFailureTest() throws Exception {
        PlayerMock p = loadPlayer();

        Paused paused = pauseFutureTasks();

        fallible.setFallibleOps(DBOperation.UPDATE_ADVANCEMENT);
        fallible.addToPlanning(true, false, true);
        CompletableFuture<ProgressionUpdateResult> updateResult1 = databaseManager.incrementProgression(KEY2, p, 10);

        // This should fail
        CompletableFuture<ProgressionUpdateResult> updateResult2 = databaseManager.incrementProgression(KEY2, p, 20);

        CompletableFuture<ProgressionUpdateResult> updateResult3 = databaseManager.incrementProgression(KEY2, p, 30);

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

        TeamProgression tpl1 = databaseManager.getTeamProgression(pl1);
        TeamProgression tpl2 = databaseManager.getTeamProgression(pl2);
        TeamProgression tpl3 = databaseManager.getTeamProgression(pl3);

        assertTrue(tpl1.isValid());
        assertTrue(tpl2.isValid());
        assertTrue(tpl3.isValid());

        assertNotEquals(tpl1, tpl2);
        assertNotEquals(tpl2, tpl3);
        assertNotEquals(tpl1, tpl3);

        CompletableFuture<Void> cf = waitCompletion(databaseManager.updatePlayerTeam(pl1, tpl2));
        assertFalse(cf.isCompletedExceptionally());

        TeamProgression newTpl1 = databaseManager.getTeamProgression(pl1);
        TeamProgression newTpl2 = databaseManager.getTeamProgression(pl2);
        TeamProgression newTpl3 = databaseManager.getTeamProgression(pl3);

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

        Paused paused = pauseFutureTasks();

        fallible.setFallibleOps(DBOperation.MOVE_PLAYER);
        fallible.addToPlanning(true, false);
        CompletableFuture<Void> cf1 = databaseManager.updatePlayerTeam(pl1, pl2);

        // This should fail
        CompletableFuture<Void> cf2 = databaseManager.updatePlayerTeam(pl2, pl3);

        paused.resume();

        waitCompletion(cf1);
        waitCompletion(cf2);

        TeamProgression tpl1 = databaseManager.getTeamProgression(pl1);
        TeamProgression tpl2 = databaseManager.getTeamProgression(pl2);
        TeamProgression tpl3 = databaseManager.getTeamProgression(pl3);

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
        TeamProgression pro = databaseManager.getTeamProgression(p);

        CompletableFuture<TeamProgression> cf = databaseManager.movePlayerInNewTeam(p);

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
        TeamProgression pro = databaseManager.getTeamProgression(p);
        assertTrue(pro.isValid());

        Paused paused = pauseFutureTasks();

        fallible.setFallibleOps(DBOperation.MOVE_PLAYER_IN_NEW_TEAM);
        fallible.addToPlanning(false, false);
        CompletableFuture<TeamProgression> cf = databaseManager.movePlayerInNewTeam(p);

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
        TeamProgression trueTeam = databaseManager.getTeamProgression(p);
        disconnectPlayer(p);
        MockPlugin plugin = MockBukkit.createMockPlugin();
        TeamProgression team = waitCompletion(databaseManager.loadAndAddLoadingRequestToPlayer(p.getUniqueId(), plugin)).get();
        assertEquals(1, databaseManager.getLoadingRequestsAmount(p.getUniqueId(), plugin));
        assertEquals(trueTeam.getTeamId(), team.getTeamId());
        assertEquals(1, trueTeam.getSize());
        assertEquals(1, team.getSize());
        assertTrue(trueTeam.everyMemberMatch(uuid -> p.getUniqueId().equals(uuid)));
        assertTrue(team.everyMemberMatch(uuid -> p.getUniqueId().equals(uuid)));
        databaseManager.removeLoadingRequestToPlayer(p.getUniqueId(), plugin);
        assertEquals(0,databaseManager.getLoadingRequestsAmount(p.getUniqueId(), plugin));
    }

    @Test
    void loadAndAddLoadingRequestToPlayerWithFailureTest() throws Exception {
        PlayerMock p = loadPlayer();
        disconnectPlayer(p);
        MockPlugin plugin = MockBukkit.createMockPlugin();
        fallible.setFallibleOps(DBOperation.LOAD_UUID);
        fallible.addToPlanning(false);
        assertTrue(waitCompletion(databaseManager.loadAndAddLoadingRequestToPlayer(p.getUniqueId(), plugin)).isCompletedExceptionally());
        assertEquals(0,databaseManager.getLoadingRequestsAmount(p.getUniqueId(), plugin));
        assertThrows(UserNotLoadedException.class, () -> databaseManager.getTeamProgression(p.getUniqueId()));
    }

    @Test
    void loadAndAddLoadingRequestToPlayerWithNonRegisteredPlayerTest() throws Exception {
        MockPlugin plugin = MockBukkit.createMockPlugin();
        var cf = waitCompletion(databaseManager.loadAndAddLoadingRequestToPlayer(UUID.randomUUID(), plugin));
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
                if (e.getPlayerUUID().equals(p.getUniqueId())) {
                    var cf = loaded.get();
                    assertTrue(cf == null || !cf.isDone());
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

            waitForPendingTasksNoTicking();

            loaded.set(databaseManager.loadAndAddLoadingRequestToPlayer(p.getUniqueId(), plugin));
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
                ((CustomScheduler) server.getScheduler()).registerPendingTasksNow();
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
    void closingTest() throws Exception {
        PlayerMock p1 = loadPlayer();
        PlayerMock p2 = loadPlayer();
        waitCompletion(databaseManager.updatePlayerTeam(p1, p2)).get();

        TeamProgression team = databaseManager.getTeamProgression(p1);

        var cf1 = databaseManager.movePlayerInNewTeam(p1);
        waitForPendingTasksNoTicking();
        Paused paused = pauseFutureTasksNoTicking();
        var cf2 = databaseManager.movePlayerInNewTeam(p2);

        new Thread(() -> {
            while (!executor.isShutdown()) {
                Thread.yield();
            }
            paused.resumeAsync();
        }).start();

        databaseManager.close();

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
        assertEquals(1, databaseManager.getTeamProgression(player).getSize(), "Incorrect usage of disconnectPlayer inside tests");

        CompletableFuture<Void> finished = new CompletableFuture<>();
        AtomicBoolean skip = new AtomicBoolean(false);

        final Object listener = new Object();
        final EventManager manager = advancementMain.getEventManager();

        try {
            // These events should run after a few ticks, so there shouldn't be any problem having them registered
            // after the player addition
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

            player.disconnect();

            waitCompletion(finished);

            assertTrue(skip.get());
        } finally {
            manager.unregister(listener);
        }
    }

    @Contract("_ -> param1")
    private <T> CompletableFuture<T> waitCompletion(CompletableFuture<T> completableFuture) {
        if (completableFuture == null) {
            return null;
        }
        while (!completableFuture.isDone()) {
            server.getScheduler().performTicks(10);
            Thread.yield();
        }
        return completableFuture;
    }

    @Contract("_ -> param1")
    private <T> CompletableFuture<T> waitCompletionNoTicking(CompletableFuture<T> completableFuture) {
        if (completableFuture == null) {
            return null;
        }
        while (!completableFuture.isDone()) {
            ((CustomScheduler) server.getScheduler()).registerPendingTasksNow();
            Thread.yield();
        }
        return completableFuture;
    }

    private Paused pauseFutureTasks() {
        CompletableFuture<Void> waiter = new CompletableFuture<>();
        CompletableFuture<Void> blocker = new CompletableFuture<>();

        CompletableFuture<Void> paused = CompletableFuture.runAsync(() -> {
            try {
                waiter.complete(null);
                blocker.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);

        waitCompletion(waiter);
        return new Paused(blocker, paused);
    }

    private Paused pauseFutureTasksNoTicking() {
        CompletableFuture<Void> waiter = new CompletableFuture<>();
        CompletableFuture<Void> blocker = new CompletableFuture<>();

        CompletableFuture<Void> paused = CompletableFuture.runAsync(() -> {
            try {
                waiter.complete(null);
                blocker.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);

        waitCompletionNoTicking(waiter);
        return new Paused(blocker, paused);
    }

    private void waitForPendingTasks() {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> cf.complete(null), executor);

        waitCompletion(cf);
    }

    private void waitForPendingTasksNoTicking() throws Exception {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> cf.complete(null), executor);

        waitCompletionNoTicking(cf);
    }

    private final class Paused {
        private final CompletableFuture<Void> blocker;
        private final CompletableFuture<Void> paused;

        private Paused(CompletableFuture<Void> blocker, CompletableFuture<Void> paused) {
            this.blocker = blocker;
            this.paused = paused;
        }

        public void resume() {
            this.blocker.complete(null);
            waitCompletion(this.paused);
        }

        public void resumeAsync() {
            this.blocker.complete(null);
        }
    }
}
