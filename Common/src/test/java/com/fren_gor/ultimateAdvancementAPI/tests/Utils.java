package com.fren_gor.ultimateAdvancementAPI.tests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.IDatabase;
import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import net.byteflux.libby.BukkitLibraryManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.mocked0_0_R1.VersionedServerMock;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.withSettings;

public final class Utils {

    private static Field versionsCOMPLETE_VERSION;

    static {
        try {
            versionsCOMPLETE_VERSION = Versions.class.getDeclaredField("COMPLETE_VERSION");
            versionsCOMPLETE_VERSION.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            fail("Cannot get Versions members.", e);
        }
    }

    @Contract("_ -> param1")
    public static <T> CompletableFuture<T> waitCompletion(CompletableFuture<T> completableFuture) {
        return waitCompletion(completableFuture, true);
    }

    @Contract("_, _ -> param1")
    public static <T> CompletableFuture<T> waitCompletion(CompletableFuture<T> completableFuture, boolean ticking) {
        MockBukkit.ensureMocking();
        if (completableFuture == null) {
            return null;
        }
        while (!completableFuture.isDone()) {
            if (ticking) {
                MockBukkit.getMock().getScheduler().performOneTick();
            }
            Thread.yield();
        }
        return completableFuture;
    }

    @NotNull
    static AbstractMockedServer mockServer() {
        return mockServerWith(new VersionedServerMock());
    }

    @NotNull
    static <T extends AbstractMockedServer> T mockServerWith(@NotNull T mock) {
        assertNotNull(mock);
        T server = MockBukkit.mock(mock);
        MockBukkit.ensureMocking();
        updateMockVersion(mock);
        return server;
    }

    private static void updateMockVersion(@NotNull AbstractMockedServer mocked) {
        var version = mocked.getMockedVersion();
        try {
            versionsCOMPLETE_VERSION.set(null, version);
        } catch (Throwable t) {
            fail("Couldn't set Versions.COMPLETE_VERSION to " + version + " during mocking.", t);
        }
        assertEquals(version, Versions.getNMSVersion(), "Setting Versions.COMPLETE_VERSION failed!");
    }

    /**
     * Must be called inside a:
     * <pre> {@code Utils.mockServer(() -> {
     *     newAdvancementMain(...)
     * }}</pre>
     *
     * @param plugin The plugin
     * @param databaseSupplier The {@link IDatabase} supplier.
     */
    @NotNull
    static AdvancementMain newAdvancementMain(@NotNull Plugin plugin, @NotNull IDatabaseSupplier databaseSupplier) {
        assertNotNull(Bukkit.getServer(), "newAdvancementMain(...) must be called inside Utils.mockServer(...)");
        assertNotNull(plugin);
        assertNotNull(databaseSupplier);
        try (var libraryManagerMock = Mockito.mockConstruction(BukkitLibraryManager.class,
                withSettings().defaultAnswer(i -> {
                    throw new UnsupportedOperationException("Mocked method.");
                }),
                (mock, context) -> {
                    doNothing().when(mock).addMavenCentral();
                })
        ) {
            AdvancementMain main = new AdvancementMain(plugin);
            main.load();
            main.enable(() -> databaseSupplier.apply(main));
            return main;
        } catch (Throwable t) {
            fail("Cannot instantiate AdvancementMain for tests.", t);
            return null; // Never actually runs, only needed to compile
        }
    }

    @FunctionalInterface
    public interface IDatabaseSupplier {
        @NotNull
        IDatabase apply(AdvancementMain main) throws Exception;
    }

    public abstract static class AbstractMockedServer extends ServerMock {
        @NotNull
        public abstract Optional<String> getMockedVersion();
    }

    private Utils() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
