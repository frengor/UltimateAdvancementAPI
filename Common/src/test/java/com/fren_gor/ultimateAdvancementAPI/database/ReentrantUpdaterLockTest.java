package com.fren_gor.ultimateAdvancementAPI.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ReentrantUpdaterLockTest {

    private static final int THREAD_COUNT = 100;

    private ServerMock server;
    private ReentrantUpdaterLock lock;

    @BeforeEach
    void init() {
        server = Utils.mockServer();
        lock = new ReentrantUpdaterLock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
        server = null;
    }

    @Test
    void assertMainThreadThrowsTest() {
        assertTrue(Bukkit.isPrimaryThread());
        assertThrows(IllegalStateException.class, () -> lock.lock());
        assertThrows(IllegalStateException.class, () -> lock.lockInterruptibly());
        assertThrows(IllegalStateException.class, () -> lock.tryLock());
        assertThrows(IllegalStateException.class, () -> lock.tryLock(1, TimeUnit.SECONDS));
    }

    @Test
    void onlySharedLocksTest() throws Exception {
        List<CompletableFuture<Void>> cF = new ArrayList<>(THREAD_COUNT);
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            cF.add(completableFuture);
            new Thread(() -> {
                for (int n = 0; n < THREAD_COUNT; n++) {
                    lock.lock();
                    try {
                        barrier.await(1, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        completableFuture.completeExceptionally(e);
                        return;
                    } finally {
                        lock.unlock();
                    }
                }
                completableFuture.complete(null);
            }).start();
        }

        assertNoExceptions(CompletableFuture.allOf(cF.toArray(new CompletableFuture[0])), cF);
        assertFalse(barrier.isBroken());
    }

    @Test
    void mixedLocksTest() throws Exception {
        List<CompletableFuture<Void>> cF = new ArrayList<>(THREAD_COUNT);
        AtomicInteger holders = new AtomicInteger(0);
        AtomicInteger acquireCount = new AtomicInteger(0);
        for (int i = 0; i < THREAD_COUNT; i++) {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            cF.add(completableFuture);
            new Thread(() -> {
                for (int n = 0; n < THREAD_COUNT; n++) {
                    lock.lock();
                    holders.incrementAndGet();
                    acquireCount.incrementAndGet();
                    holders.decrementAndGet();
                    lock.unlock();
                }
                completableFuture.complete(null);
            }).start();
        }

        final AtomicBoolean shouldStop = new AtomicBoolean(false);
        final AtomicBoolean interrupted = new AtomicBoolean(false);

        final Thread t = new Thread(() -> {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                if (!interrupted.get()) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                shouldStop.set(true);
            }
        });

        t.start();

        var completableFuture = CompletableFuture.allOf(cF.toArray(new CompletableFuture[0]));

        while (!completableFuture.isDone()) {
            while (!lock.tryLockExclusiveLock()) {
                assertFalse(shouldStop.get(), "Timeout");
                Thread.yield();
            }
            assertFalse(shouldStop.get(), "Timeout");
            try {
                assertEquals(0, holders.get());
            } finally {
                lock.unlockExclusiveLock();
            }
        }

        assertFalse(shouldStop.get(), "Timeout");
        assertEquals(THREAD_COUNT*THREAD_COUNT, acquireCount.get());

        interrupted.set(true);
        t.interrupt();

        assertTrue(completableFuture.isDone());
        assertNoExceptions(completableFuture, cF);
    }

    private <T> void assertNoExceptions(CompletableFuture<Void> completableFuture, List<CompletableFuture<T>> list) throws Exception {
        try {
            completableFuture.get(6, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            int i = 0;
            for (var cf : list) {
                if (cf.isCompletedExceptionally()) {
                    final int n = i;
                    cf.whenComplete((t, err) -> {
                        System.err.println("Thread " + n + " completed exceptionally:");
                        err.printStackTrace();
                    });
                }
                i++;
            }
        }

        if (!completableFuture.isDone()) {
            fail("CompletableFuture has not completed in time.");
        }

        if (completableFuture.isCompletedExceptionally()) {
            fail("CompletableFuture has completed exceptionally.");
        }
    }
}
