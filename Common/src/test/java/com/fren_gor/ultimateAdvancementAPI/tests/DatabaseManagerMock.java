package com.fren_gor.ultimateAdvancementAPI.tests;

import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.BlockingDBImpl;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.FallibleDBImpl;
import com.fren_gor.ultimateAdvancementAPI.database.IDatabase;
import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerMock {

    private static final Constructor<DatabaseManager> dbManagerConstructor;
    private static final Field executorField;

    static {
        try {
            dbManagerConstructor = DatabaseManager.class.getDeclaredConstructor(AdvancementMain.class, IDatabase.class);
            dbManagerConstructor.setAccessible(true);

            executorField = DatabaseManager.class.getDeclaredField("executor");
            executorField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final ServerMock server;
    private final DatabaseManager databaseManager;
    private final FallibleDBImpl fallible;
    private final BlockingDBImpl blocking;
    private final ExecutorService executor;

    DatabaseManagerMock(AdvancementMain main, ServerMock server) throws Exception {
        this.server = server;
        this.blocking = new BlockingDBImpl(new InMemory(main.getLogger()));
        this.fallible = new FallibleDBImpl(this.blocking);
        this.databaseManager = dbManagerConstructor.newInstance(main, this.fallible);
        this.executor = (ExecutorService) executorField.get(this.databaseManager);
        assertNotNull(executor);
    }

    void disable() {
        if (!executor.isShutdown()) {
            // Wait for pending tasks before closing the server
            // This cycle should ideally let tasks waiting for the server main thread to finish
            for (int i = 0; i < 20; i++) {
                waitForPendingTasks();
            }
        }
    }

    @Contract("_ -> param1")
    public <T> CompletableFuture<T> waitCompletion(CompletableFuture<T> completableFuture) {
        return waitCompletion(completableFuture, true);
    }

    @Contract("_, _ -> param1")
    public <T> CompletableFuture<T> waitCompletion(CompletableFuture<T> completableFuture, boolean ticking) {
        if (completableFuture == null) {
            return null;
        }
        while (!completableFuture.isDone()) {
            if (ticking) {
                server.getScheduler().performOneTick();
            }
            Thread.yield();
        }
        return completableFuture;
    }

    public Paused pauseFutureTasks() {
        return pauseFutureTasks(true);
    }

    public Paused pauseFutureTasks(boolean ticking) {
        CompletableFuture<Void> waiter = new CompletableFuture<>();
        CompletableFuture<Void> blocker = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                waiter.complete(null);
                blocker.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);

        waitCompletion(waiter, ticking);
        return new Paused(blocker);
    }

    public void waitForPendingTasks() {
        waitForPendingTasks(true);
    }

    public void waitForPendingTasks(boolean ticking) {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> cf.complete(null), executor);

        waitCompletion(cf, ticking);
    }

    public static final class Paused {
        private final CompletableFuture<Void> blocker;

        private Paused(CompletableFuture<Void> blocker) {
            this.blocker = blocker;
        }

        public void resume() {
            this.blocker.complete(null);
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public FallibleDBImpl getFallible() {
        return fallible;
    }

    public BlockingDBImpl getBlocking() {
        return blocking;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
