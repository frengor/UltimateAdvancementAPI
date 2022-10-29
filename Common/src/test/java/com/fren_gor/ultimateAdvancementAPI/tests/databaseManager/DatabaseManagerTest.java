package com.fren_gor.ultimateAdvancementAPI.tests.databaseManager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.FallibleDBImpl;
import com.fren_gor.ultimateAdvancementAPI.database.IDatabase;
import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingFailedEvent;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    private static Constructor<DatabaseManager> dbManagerConstructor;

    @BeforeAll
    public static void beforeAll() throws Exception {
        dbManagerConstructor = DatabaseManager.class.getDeclaredConstructor(AdvancementMain.class, IDatabase.class);
        dbManagerConstructor.setAccessible(true);
    }

    private ServerMock server;
    private AdvancementMain advancementMain;
    private DatabaseManager databaseManager;
    private FallibleDBImpl fallible;

    private AdvancementKey KEY1;
    private AdvancementKey KEY2;
    private AdvancementKey KEY3;

    @BeforeEach
    public void setUp() throws Exception {
        server = Utils.mockServer();
        advancementMain = Utils.newAdvancementMain(MockBukkit.createMockPlugin("testPlugin"), main -> dbManagerConstructor.newInstance(main, fallible = new FallibleDBImpl(new InMemory(main.getLogger()))));
        databaseManager = advancementMain.getDatabaseManager();
        assertNotNull(fallible);

        KEY1 = new AdvancementKey("a-namespace_", "a_-key/1");
        KEY2 = new AdvancementKey("a-namespace_", "a_-key/2");
        KEY3 = new AdvancementKey("a-namespace_", "a_-key/3");
    }

    @AfterEach
    public void tearDown() throws Exception {
        advancementMain.disable();
        advancementMain = null;
        databaseManager = null;
        fallible = null;
        MockBukkit.unmock();
        server = null;
    }

    @Test
    public void playerLoadingTest() throws Exception {
        PlayerMock pl1 = loadPlayer();
        assertTrue(databaseManager.isLoaded(pl1.getUniqueId()));

        PlayerMock pl2 = loadPlayer();
        assertTrue(databaseManager.isLoaded(pl2.getUniqueId()));

        pl1.disconnect();
        assertFalse(databaseManager.isLoaded(pl1.getUniqueId()));
        pl2.disconnect();
        assertFalse(databaseManager.isLoaded(pl2.getUniqueId()));
    }

    @Test
    public void advancementProgressionTest() throws Exception {
        PlayerMock p = loadPlayer();

        {
            Entry<Integer, CompletableFuture<Integer>> entry = databaseManager.setProgression(KEY1, p, 10);

            assertEquals(0, entry.getKey());
            assertEquals(10, waitCompletion(entry.getValue()).get());
        }

        fallible.addToPlanning(Arrays.asList(true, false, true));
        Entry<Integer, CompletableFuture<Integer>> entry1 = databaseManager.setProgression(KEY2, p, 10);

        // This should fail
        Entry<Integer, CompletableFuture<Integer>> entry2 = databaseManager.setProgression(KEY2, p, 20);

        Entry<Integer, CompletableFuture<Integer>> entry3 = databaseManager.setProgression(KEY2, p, 30);

        assertEquals(0, entry1.getKey());
        assertEquals(10, entry2.getKey());
        assertEquals(20, entry3.getKey());

        assertEquals(10, waitCompletion(entry1.getValue()).get());
        assertTrue(waitCompletion(entry2.getValue()).isCompletedExceptionally());
        assertEquals(30, waitCompletion(entry3.getValue()).get());
    }

    public PlayerMock loadPlayer() {
        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicBoolean skip = new AtomicBoolean(false);
        AtomicBoolean hadSuccess = new AtomicBoolean(false);

        final Object listener = new Object();

        final PlayerMock p = server.addPlayer();

        try {
            // These events should run after a few ticks, so there shouldn't be any problem having them registered
            // after the player addition
            advancementMain.getEventManager().register(listener, PlayerLoadingCompletedEvent.class, e -> {
                if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                    if (skip.getAndSet(true)) {
                        fail("PlayerLoadingCompletedEvent called too many times for player " + e.getPlayer().getName() + '.');
                    }
                    hadSuccess.set(true);
                    finished.set(true);
                } else {
                    fail("Another player loading completed: " + e.getPlayer().getName() + " (" + e.getPlayer().getUniqueId() + ')');
                }
            });

            advancementMain.getEventManager().register(listener, PlayerLoadingFailedEvent.class, e -> {
                if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                    if (skip.getAndSet(true)) {
                        fail("PlayerLoadingCompletedEvent called too many times for player " + e.getPlayer().getName() + '.');
                    }
                    hadSuccess.set(false);
                    finished.set(true);
                } else {
                    fail("Another player loading failed: " + e.getPlayer().getName() + " (" + e.getPlayer().getUniqueId() + ')');
                }
            });

            while (!finished.get()) {
                server.getScheduler().performTicks(10);
                Thread.yield();
            }

            assertTrue(hadSuccess.get());
            assertTrue(skip.get());
        } finally {
            advancementMain.getEventManager().unregister(listener);
        }
        return p;
    }

    private <T> CompletableFuture<T> waitCompletion(CompletableFuture<T> completableFuture) {
        while (!completableFuture.isDone()) {
            server.getScheduler().performTicks(10);
            Thread.yield();
        }
        return completableFuture;
    }
}
