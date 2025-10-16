package com.fren_gor.ultimateAdvancementAPI.tests;

import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static com.fren_gor.ultimateAdvancementAPI.tests.Utils.waitCompletion;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerUtils {

    private static final Field executorField;

    static {
        try {
            executorField = DatabaseManager.class.getDeclaredField("executor");
            executorField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final DatabaseManager databaseManager;
    private final ExecutorService executor;

    DatabaseManagerUtils(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager);
        try {
            this.executor = (ExecutorService) executorField.get(this.databaseManager);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
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

    public ExecutorService getExecutor() {
        return executor;
    }
}
