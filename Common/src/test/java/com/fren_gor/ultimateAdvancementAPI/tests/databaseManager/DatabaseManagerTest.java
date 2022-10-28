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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
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

    @BeforeEach
    public void setUp() throws Exception {
        server = Utils.mockServer();
        advancementMain = Utils.newAdvancementMain(MockBukkit.createMockPlugin("testPlugin"), main -> dbManagerConstructor.newInstance(main, fallible = new FallibleDBImpl(new InMemory(main.getLogger()))));
        databaseManager = advancementMain.getDatabaseManager();
        assertNotNull(fallible);
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
}
