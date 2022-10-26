package com.fren_gor.ultimateAdvancementAPI.tests.databaseManager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.plugin.PluginManagerMock;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingFailedEvent;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    private ServerMock server;
    private AdvancementMain advancementMain;
    private DatabaseManager databaseManager;

    @BeforeEach
    public void setUp() throws Exception {
        server = Utils.mockServer();
        advancementMain = Utils.newAdvancementMain(MockBukkit.createMockPlugin("testPlugin"), DatabaseManager::new);
        databaseManager = advancementMain.getDatabaseManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        advancementMain.disable();
        advancementMain = null;
        databaseManager = null;
        MockBukkit.unmock();
        server = null;
    }

    // FIXME
    //@Test
    public void test() throws Exception {
        MockPlugin pl = MockBukkit.createMockPlugin();
        PlayerMock p = server.addPlayer();

        UUID uuid = p.getUniqueId();
        //CompletableFuture<TeamProgression> i = databaseManager.loadOfflinePlayer(uuid, CacheFreeingOption.AUTOMATIC(pl, 5000));

        /*while (!i.isDone()) {
            server.getScheduler().performOneTick();
            System.out.println("Ciao");
        }

        server.getScheduler().performTicks(20);
        TeamProgression pro = i.get();
        server.getScheduler().performTicks(20);*/

        while (!databaseManager.isLoaded(uuid)) {
            server.getScheduler().performTicks(5);
            Thread.yield();
        }

        server.getScheduler().performTicks(100);

        PluginManagerMock plm = server.getPluginManager();

        plm.assertEventFired(PlayerLoadingCompletedEvent.class);
        //CompletableFuture<TeamProgression> i = databaseManager.loadOfflinePlayer(uuid, CacheFreeingOption.AUTOMATIC(pl, 5000));
        //assertTrue(i.isDone());
        System.out.println("Fine");
    }

    // FIXME
    //@Test
    public void playerLoadingTest() {
        PlayerMock pl1 = loadPlayer();
        assertTrue(databaseManager.isLoaded(pl1.getUniqueId()));

        PlayerMock pl2 = loadPlayer("APlayer");
        assertTrue(databaseManager.isLoaded(pl2.getUniqueId()));

        pl1.disconnect();
        assertFalse(databaseManager.isLoaded(pl1.getUniqueId()));
        pl2.disconnect();
        assertFalse(databaseManager.isLoaded(pl2.getUniqueId()));
    }

    public PlayerMock loadPlayer() {
        return loadPlayer(null);
    }

    public PlayerMock loadPlayer(String name) {
        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicBoolean skip = new AtomicBoolean(false);
        AtomicBoolean hadSuccess = new AtomicBoolean(false);

        Object listener = new Object();

        PlayerMock p = name == null ? server.addPlayer() : server.addPlayer(name);

        try {
            // These events should run after a few ticks, so there shouldn't be any problem having them registered
            // after the player addition
            advancementMain.getEventManager().register(listener, PlayerLoadingCompletedEvent.class, e -> {
                if (e.getPlayer().equals(p)) {
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
                if (e.getPlayer().equals(p)) {
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
