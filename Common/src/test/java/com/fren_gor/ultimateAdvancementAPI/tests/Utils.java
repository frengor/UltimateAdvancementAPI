package com.fren_gor.ultimateAdvancementAPI.tests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import net.byteflux.libby.BukkitLibraryManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.mocked0_0_R1.VersionedServerMock;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
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
        ServerMock server = mockServer();
        try {
            runnable.run();
        } finally {
            MockBukkit.unmock();
        }
    }

    public static ServerMock mockServer() {
        ServerMock server = MockBukkit.mock(new VersionedServerMock());
        MockBukkit.ensureMocking();
        return server;
    }

    /**
     * Must be called inside a:
     * <blockquote><pre>
     * Utils.mockServer(() -> {
     *     newAdvancementMain(...)
     * }</pre></blockquote>
     *
     * @param plugin The plugin
     * @param databaseManagerSupplier The database manager supplier.
     */
    public static AdvancementMain newAdvancementMain(@NotNull Plugin plugin, @NotNull DatabaseManagerSupplier databaseManagerSupplier) {
        assertNotNull(Bukkit.getServer(), "newAdvancementMain(...) must be called inside Utils.mockServer(...)");
        assertNotNull(plugin);
        assertNotNull(databaseManagerSupplier);
        AdvancementMain main = new AdvancementMain(plugin);
        try {
            ((AtomicBoolean) mainLOADED.get(main)).set(true);
            ((AtomicBoolean) mainENABLED.get(main)).set(true);
            ((AtomicBoolean) mainINVALID_VERSION.get(main)).set(false);

            mainEventManager.set(main, new EventManager(plugin));
            mainLibbyManager.set(main, testLibraryManager);
            mainDatabaseManager.set(main, Objects.requireNonNull(databaseManagerSupplier.apply(main)));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot instantiate AdvancementMain for tests.");
        }
        return main;
    }

    public interface DatabaseManagerSupplier {
        @NotNull
        DatabaseManager apply(AdvancementMain main) throws Exception;
    }

    private Utils() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
