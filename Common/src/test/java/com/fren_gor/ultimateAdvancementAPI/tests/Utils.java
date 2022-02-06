package com.fren_gor.ultimateAdvancementAPI.tests;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import net.byteflux.libby.BukkitLibraryManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class Utils {

    private static Field mainEventManager, mainDatabaseManager, mainLibbyManager, mainLOADED, mainENABLED, mainINVALID_VERSION;

    static {
        try {
            mainEventManager = AdvancementMain.class.getDeclaredField("eventManager");
            mainEventManager.setAccessible(true);
            mainDatabaseManager = AdvancementMain.class.getDeclaredField("databaseManager");
            mainDatabaseManager.setAccessible(true);
            mainLibbyManager = AdvancementMain.class.getDeclaredField("libbyManager");
            mainLibbyManager.setAccessible(true);
            mainLOADED = AdvancementMain.class.getDeclaredField("LOADED");
            mainLOADED.setAccessible(true);
            mainENABLED = AdvancementMain.class.getDeclaredField("ENABLED");
            mainENABLED.setAccessible(true);
            mainINVALID_VERSION = AdvancementMain.class.getDeclaredField("INVALID_VERSION");
            mainINVALID_VERSION.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            fail("Cannot get AdvancementMain members.");
        }
    }

    private static final BukkitLibraryManager testLibraryManager;

    static {
        testLibraryManager = Mockito.mock(BukkitLibraryManager.class);
        final var exception = new UnsupportedOperationException("Mocked method.");
        when(testLibraryManager.toString()).thenThrow(exception);
        when(testLibraryManager.downloadLibrary(any())).thenThrow(exception);
        when(testLibraryManager.resolveLibrary(any())).thenThrow(exception);
        when(testLibraryManager.getIsolatedClassLoaderOf(any())).thenThrow(exception);
        when(testLibraryManager.getLogLevel()).thenThrow(exception);
        when(testLibraryManager.getRepositories()).thenThrow(exception);

        // Void methods
        doThrow(exception).when(testLibraryManager).addMavenLocal();
        doThrow(exception).when(testLibraryManager).addJCenter();
        doThrow(exception).when(testLibraryManager).addMavenCentral();
        doThrow(exception).when(testLibraryManager).addRepository(any());
        doThrow(exception).when(testLibraryManager).addSonatype();
        doThrow(exception).when(testLibraryManager).addJitPack();
        doThrow(exception).when(testLibraryManager).loadLibrary(any());
        doThrow(exception).when(testLibraryManager).setLogLevel(any());

        // Just to be sure mocking has gone well
        assertThrows(exception.getClass(), testLibraryManager::addMavenLocal);
        verify(testLibraryManager).addMavenLocal();
    }

    public static void mockServer(@NotNull Runnable runnable) {
        assertNotNull(runnable);
        try (MockedStatic<Bukkit> bukkitMock = mockServer()) {
            runnable.run();
        }
    }

    public static MockedStatic<Bukkit> mockServer() {
        MockedStatic<Bukkit> bukkitMock = Mockito.mockStatic(Bukkit.class);
        Server server = InterfaceImplementer.newFakeServer();
        bukkitMock.when(Bukkit::getServer).thenReturn(server);
        assertSame("Server mock failed", Bukkit.getServer(), server);
        PluginManager plManager = new SimplePluginManager(server, new SimpleCommandMap(server));
        bukkitMock.when(Bukkit::getPluginManager).thenReturn(plManager);
        bukkitMock.when(Bukkit::getLogger).thenReturn(Logger.getLogger("BukkitLogger"));
        return bukkitMock;
    }

    /**
     * Must be called inside a:
     * <blockquote><pre>
     * Utils.mockServer(() -> {
     *     newAdvancementMain(...)
     * }</pre></blockquote>
     *
     * @param plugin The plugin
     */
    public static AdvancementMain newAdvancementMain(@NotNull Plugin plugin) {
        return newAdvancementMain(plugin, new EventManager(plugin));
    }

    /**
     * Must be called inside a:
     * <blockquote><pre>
     * Utils.mockServer(() -> {
     *     newAdvancementMain(...)
     * }</pre></blockquote>
     *
     * @param plugin The plugin
     * @param manager The event manager
     */
    public static AdvancementMain newAdvancementMain(@NotNull Plugin plugin, @NotNull EventManager manager) {
        assertNotNull("newAdvancementMain(...) must be called inside Utils.mockServer(...)", Bukkit.getServer());
        assertNotNull(plugin);
        assertNotNull(manager);
        AdvancementMain main = new AdvancementMain(plugin);
        try {
            ((AtomicBoolean) mainLOADED.get(main)).set(true);
            ((AtomicBoolean) mainENABLED.get(main)).set(true);
            ((AtomicBoolean) mainINVALID_VERSION.get(main)).set(false);

            mainEventManager.set(main, manager);
            mainDatabaseManager.set(main, new DatabaseManager(main));
            mainLibbyManager.set(main, testLibraryManager);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot instantiate AdvancementMain for tests.");
        }
        return main;
    }

    private Utils() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
