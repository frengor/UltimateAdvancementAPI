package com.fren_gor.ultimateAdvancementAPI.tests;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.IDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public final class Utils {

    private static Constructor<?> advancementTab, databaseManagerTest;

    static {
        try {
            advancementTab = AdvancementTab.class.getDeclaredConstructor(Plugin.class, DatabaseManager.class, String.class);
            advancementTab.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot get AdvancementTab constructor.");
        }
        try {
            databaseManagerTest = DatabaseManager.class.getDeclaredConstructor(AdvancementMain.class, EventManager.class, IDatabase.class, boolean.class);
            databaseManagerTest.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot get DatabaseManager test constructor.");
        }
    }

    public static void mockServer(@NotNull Runnable runnable) {
        assertNotNull(runnable);
        try (MockedStatic<Bukkit> bukkitMock = Mockito.mockStatic(Bukkit.class)) {
            Server server = InterfaceImplementer.newFakeServer();
            bukkitMock.when(Bukkit::getServer).thenReturn(server);
            assertSame("Server mock failed", Bukkit.getServer(), server);
            PluginManager plManager = new SimplePluginManager(server, new SimpleCommandMap(server));
            bukkitMock.when(Bukkit::getPluginManager).thenReturn(plManager);
            bukkitMock.when(Bukkit::getLogger).thenReturn(Logger.getLogger("BukkitLogger"));
            runnable.run();
        }
    }

    @NotNull
    public static AdvancementTab newAdvancementTab(Plugin plugin, String namespace) {
        return newAdvancementTab(plugin, newDatabaseManager(), namespace);
    }

    @NotNull
    public static AdvancementTab newAdvancementTab(Plugin plugin, DatabaseManager databaseManager, String namespace) {
        try {
            return (AdvancementTab) advancementTab.newInstance(plugin, databaseManager, namespace);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot instantiate AdvancementTab for tests.");

            // Unreachable return statement, but necessary to compile
            return null;
        }
    }

    @NotNull
    public static DatabaseManager newDatabaseManager() {
        return newDatabaseManager(null, null, null, false);
    }

    @NotNull
    public static DatabaseManager newDatabaseManager(AdvancementMain main, EventManager manager, IDatabase database, boolean runInitialisation) {
        try {
            return (DatabaseManager) databaseManagerTest.newInstance(main, manager, database, runInitialisation);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot instantiate DatabaseManager for tests.");

            // Unreachable return statement, but necessary to compile
            return null;
        }
    }

    private Utils() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
