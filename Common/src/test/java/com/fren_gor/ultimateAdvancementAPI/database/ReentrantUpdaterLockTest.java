package com.fren_gor.ultimateAdvancementAPI.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.exceptions.SyncExecutionException;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

public class ReentrantUpdaterLockTest {

    private static final int THREAD_COUNT = 10;

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
        assertThrows(SyncExecutionException.class, () -> lock.lock());
        assertThrows(SyncExecutionException.class, () -> lock.lockInterruptibly());
        assertThrows(SyncExecutionException.class, () -> lock.tryLock());
        assertThrows(SyncExecutionException.class, () -> lock.tryLock(1, TimeUnit.SECONDS));
    }

    @Test
    void assertMainThreadSpecialValuesTest() {
        assertTrue(Bukkit.isPrimaryThread());
        assertFalse(lock.isHeldByCurrentThread());
        assertEquals(0, lock.getHoldCount());
    }

    @Test
    void onlySharedLocksTest() throws Exception {
        sharedLockTestsCommon(1, LockKind.LOCK);
    }

    @Test
    void onlyTwoSharedLocksTest() throws Exception {
        sharedLockTestsCommon(2, LockKind.LOCK);
    }

    @Test
    void onlyMultipleSharedLocksTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            sharedLockTestsCommon(i, LockKind.LOCK);
        }
    }

    @Test
    void onlySharedLocksInterruptiblyTest() throws Exception {
        sharedLockTestsCommon(1, LockKind.LOCK_INTERRUPTIBLY);
    }

    @Test
    void onlyTwoSharedLocksInterruptiblyTest() throws Exception {
        sharedLockTestsCommon(2, LockKind.LOCK_INTERRUPTIBLY);
    }

    @Test
    void onlyMultipleSharedLocksInterruptiblyTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            sharedLockTestsCommon(i, LockKind.LOCK_INTERRUPTIBLY);
        }
    }

    @Test
    void onlySharedTryLocksTest() throws Exception {
        sharedLockTestsCommon(1, LockKind.TRY_LOCK);
    }

    @Test
    void onlyTwoSharedTryLocksTest() throws Exception {
        sharedLockTestsCommon(2, LockKind.TRY_LOCK);
    }

    @Test
    void onlyMultipleSharedTryLocksTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            sharedLockTestsCommon(i, LockKind.TRY_LOCK);
        }
    }

    @Test
    void onlySharedTryLocksTimedTest() throws Exception {
        sharedLockTestsCommon(1, LockKind.TRY_LOCK_TIMED);
    }

    @Test
    void onlyTwoSharedTryLocksTimedTest() throws Exception {
        sharedLockTestsCommon(2, LockKind.TRY_LOCK_TIMED);
    }

    @Test
    void onlyMultipleSharedTryLocksTimedTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            sharedLockTestsCommon(i, LockKind.TRY_LOCK_TIMED);
        }
    }

    private enum LockKind {
        LOCK, LOCK_INTERRUPTIBLY, TRY_LOCK, TRY_LOCK_TIMED
    }

    private void sharedLockTestsCommon(int locksToTake, LockKind lockKind) throws Exception {
        assertTrue(locksToTake > 0);

        List<CompletableFuture<Void>> cF = new ArrayList<>(THREAD_COUNT);
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            cF.add(completableFuture);
            new Thread(() -> {
                for (int n = 0; n < THREAD_COUNT; n++) {
                    int j = 0;
                    for (; j < locksToTake; j++) {
                        switch (lockKind) {
                            case LOCK -> {
                                lock.lock();
                            }
                            case LOCK_INTERRUPTIBLY -> {
                                try {
                                    lock.lockInterruptibly();
                                } catch (InterruptedException e) {
                                    completableFuture.completeExceptionally(e);
                                    return;
                                }
                            }
                            case TRY_LOCK -> {
                                if (!lock.tryLock()) {
                                    completableFuture.completeExceptionally(new RuntimeException("tryLock() returned true"));
                                }
                            }
                            case TRY_LOCK_TIMED -> {
                                try {
                                    if (!lock.tryLock(1, TimeUnit.SECONDS)) {
                                        completableFuture.completeExceptionally(new RuntimeException("tryLock(long, TimeUnit) returned true"));
                                    }
                                } catch (InterruptedException e) {
                                    completableFuture.completeExceptionally(e);
                                    return;
                                }
                            }
                        }
                    }
                    try {
                        barrier.await(1, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        completableFuture.completeExceptionally(e);
                        return;
                    } finally {
                        for (int t = 0; t < j; t++) {
                            lock.unlock();
                        }
                    }
                }
                completableFuture.complete(null);
            }).start();
        }

        assertNoExceptions(CompletableFuture.allOf(cF.toArray(new CompletableFuture[0])), cF);
        assertFalse(barrier.isBroken());
    }

    @Test
    void mixedLocksOneSharedTest() throws Exception {
        mixedLocksTestCommon(1, LockKind.LOCK);
    }

    @Test
    void mixedLocksTwoSharedTest() throws Exception {
        mixedLocksTestCommon(2, LockKind.LOCK);
    }

    @Test
    void mixedLocksMultipleSharedTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            mixedLocksTestCommon(i, LockKind.LOCK);
        }
    }

    @Test
    void mixedLocksOneSharedInterruptiblyTest() throws Exception {
        mixedLocksTestCommon(1, LockKind.LOCK_INTERRUPTIBLY);
    }

    @Test
    void mixedLocksTwoSharedInterruptiblyTest() throws Exception {
        mixedLocksTestCommon(2, LockKind.LOCK_INTERRUPTIBLY);
    }

    @Test
    void mixedLocksMultipleSharedInterruptiblyTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            mixedLocksTestCommon(i, LockKind.LOCK_INTERRUPTIBLY);
        }
    }

    @Test
    void mixedLocksOneSharedTryLockTest() throws Exception {
        mixedLocksTestCommon(1, LockKind.TRY_LOCK);
    }

    @Test
    void mixedLocksTwoSharedTryLockTest() throws Exception {
        mixedLocksTestCommon(2, LockKind.TRY_LOCK);
    }

    @Test
    void mixedLocksMultipleSharedTryLockTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            mixedLocksTestCommon(i, LockKind.TRY_LOCK);
        }
    }

    @Test
    void mixedLocksOneSharedTryLockTimedTest() throws Exception {
        mixedLocksTestCommon(1, LockKind.TRY_LOCK_TIMED);
    }

    @Test
    void mixedLocksTwoSharedTryLockTimedTest() throws Exception {
        mixedLocksTestCommon(2, LockKind.TRY_LOCK_TIMED);
    }

    @Test
    void mixedLocksMultipleSharedTryLockTimedTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            mixedLocksTestCommon(i, LockKind.TRY_LOCK_TIMED);
        }
    }

    private void mixedLocksTestCommon(int sharedLocksToTake, LockKind lockKind) throws Exception {
        assertTrue(sharedLocksToTake > 0);

        List<CompletableFuture<Void>> cF = new ArrayList<>(THREAD_COUNT);
        AtomicInteger holders = new AtomicInteger(0);
        AtomicInteger acquireCount = new AtomicInteger(0);
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        for (int i = 0; i < THREAD_COUNT; i++) {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            cF.add(completableFuture);
            new Thread(() -> {
                for (int n = 0; n < THREAD_COUNT; n++) {
                    int j = 0;
                    for (; j < sharedLocksToTake; j++) {
                        switch (lockKind) {
                            case LOCK -> {
                                lock.lock();
                            }
                            case LOCK_INTERRUPTIBLY -> {
                                try {
                                    lock.lockInterruptibly();
                                } catch (InterruptedException e) {
                                    completableFuture.completeExceptionally(e);
                                    return;
                                }
                            }
                            case TRY_LOCK -> {
                                while (!shouldStop.get() && !lock.tryLock()) {
                                    Thread.yield();
                                }
                                if (shouldStop.get()) {
                                    completableFuture.completeExceptionally(new RuntimeException("Timeout"));
                                    return;
                                }
                            }
                            case TRY_LOCK_TIMED -> {
                                try {
                                    if (!lock.tryLock(3, TimeUnit.SECONDS)) {
                                        completableFuture.completeExceptionally(new RuntimeException("Timeout"));
                                        return;
                                    }
                                } catch (InterruptedException e) {
                                    completableFuture.completeExceptionally(e);
                                    return;
                                }
                            }
                        }
                    }
                    holders.incrementAndGet();
                    acquireCount.incrementAndGet();
                    holders.decrementAndGet();
                    for (int t = 0; t < j; t++) {
                        lock.unlock();
                    }
                }
                completableFuture.complete(null);
            }).start();
        }

        final AtomicBoolean interrupted = new AtomicBoolean(false);

        final Thread t = setTimeout(interrupted, shouldStop);
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
        assertEquals(THREAD_COUNT * THREAD_COUNT, acquireCount.get());

        interrupted.set(true);
        t.interrupt();

        assertTrue(completableFuture.isDone());
        assertNoExceptions(completableFuture, cF);
    }

    @Test
    void tryLockTest() throws Exception {
        assertTrue(lock.tryLockExclusiveLock());
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        new Thread(() -> {
            cf.complete(lock.tryLock());
        }).start();
        assertFalse(cf.get(6, TimeUnit.SECONDS));
    }

    @Test
    void tryLockTimedTest() throws Exception {
        assertTrue(lock.tryLockExclusiveLock());
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                cf.complete(lock.tryLock(500, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                cf.completeExceptionally(e);
                return;
            }
            cf.complete(null);
        });
        t.start();
        try {
            cf.get(6, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            t.interrupt();
            fail(e);
        }
    }

    @Test
    void tryLockTimedZeroTimeTest() throws Exception {
        assertTrue(lock.tryLockExclusiveLock());
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                cf.complete(lock.tryLock(0, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                cf.completeExceptionally(e);
                return;
            }
            cf.complete(null);
        });
        t.start();
        try {
            assertFalse(cf.get(6, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            t.interrupt();
            fail(e);
        }
    }

    @Test
    void lockInterruptiblyInterruptTest() throws Exception {
        assertTrue(lock.tryLockExclusiveLock());
        CompletableFuture<Void> cf = new CompletableFuture<>();
        CompletableFuture<Void> cf1 = new CompletableFuture<>();
        AtomicBoolean canContinue = new AtomicBoolean(false);
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        Thread t = new Thread(() -> {
            for (int i = 0; i < 25; i++) {
                if (shouldStop.get()) {
                    cf.completeExceptionally(new RuntimeException("Timeout!"));
                    return;
                }
                try {
                    lock.lockInterruptibly();
                } catch (InterruptedException e) {
                    if (shouldStop.get()) {
                        cf.completeExceptionally(new RuntimeException("Timeout!"));
                        return;
                    }
                    canContinue.set(true);
                    continue;
                }
                cf.completeExceptionally(new RuntimeException("Not interrupted"));
            }
            try {
                lock.lockInterruptibly();
            } catch (InterruptedException e) {
                cf.completeExceptionally(e);
                return;
            }
            lock.unlock();
            cf.complete(null);
        });
        t.start();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 25; i++) {
                if (shouldStop.get()) {
                    cf1.completeExceptionally(new RuntimeException("Timeout!"));
                    return;
                }
                t.interrupt();
                while (!shouldStop.get() && !canContinue.getAndSet(false)) {
                    Thread.yield();
                }
            }
            cf1.complete(null);
        });
        t1.start();

        AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread timeout = setTimeout(interrupted, shouldStop);
        timeout.start();

        try {
            cf1.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            t1.interrupt();
            t.interrupt();
            timeout.interrupt();
            fail(e);
        }
        assertFalse(shouldStop.get(), "Timeout");
        lock.unlockExclusiveLock();
        try {
            cf.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            t.interrupt();
            timeout.interrupt();
            fail(e);
        }

        interrupted.set(true);
        timeout.interrupt();

        assertFalse(shouldStop.get(), "Timeout");
    }

    @Test
    void tryLockTimedInterruptTest() throws Exception {
        assertTrue(lock.tryLockExclusiveLock());
        CompletableFuture<Void> cf = new CompletableFuture<>();
        CompletableFuture<Void> cf1 = new CompletableFuture<>();
        AtomicBoolean canContinue = new AtomicBoolean(false);
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        Thread t = new Thread(() -> {
            for (int i = 0; i < 25; i++) {
                if (shouldStop.get()) {
                    cf.completeExceptionally(new RuntimeException("Timeout"));
                    return;
                }
                try {
                    if (lock.tryLock(1000, TimeUnit.DAYS)) {
                        lock.unlock(); // Unlock before completing cf with error
                    }
                } catch (InterruptedException e) {
                    if (shouldStop.get()) {
                        cf.completeExceptionally(new RuntimeException("Timeout"));
                        return;
                    }
                    canContinue.set(true);
                    continue;
                }
                cf.completeExceptionally(new RuntimeException("Not interrupted"));
            }
            try {
                if (lock.tryLock(1000, TimeUnit.DAYS)) {
                    lock.unlock();
                } else {
                    cf.completeExceptionally(new RuntimeException("Time elapsed!"));
                    return;
                }
            } catch (InterruptedException e) {
                cf.completeExceptionally(e);
                return;
            }
            cf.complete(null);
        });
        t.start();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 25; i++) {
                if (shouldStop.get()) {
                    cf.completeExceptionally(new RuntimeException("Timeout"));
                    return;
                }
                t.interrupt();
                while (!shouldStop.get() && !canContinue.getAndSet(false)) {
                    Thread.yield();
                }
            }
            cf1.complete(null);
        });
        t1.start();

        AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread timeout = setTimeout(interrupted, shouldStop);
        timeout.start();

        try {
            cf1.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            t1.interrupt();
            t.interrupt();
            timeout.interrupt();
            throw e;
        }
        assertFalse(shouldStop.get(), "Timeout");
        lock.unlockExclusiveLock();
        try {
            cf.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            t.interrupt();
            timeout.interrupt();
            throw e;
        }

        interrupted.set(true);
        timeout.interrupt();

        assertFalse(shouldStop.get(), "Timeout");
    }

    @Test
    void tryLockTimedTimeoutTest() throws Exception {
        assertTrue(lock.tryLockExclusiveLock());
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                cf.complete(lock.tryLock(20, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                cf.completeExceptionally(e);
            }
        });
        t.start();
        try {
            assertFalse(cf.get(1, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            t.interrupt();
            throw e;
        }
    }

    @Test
    void tryLockTimedTimeoutWithInterruptTest() throws Exception {
        assertTrue(lock.tryLockExclusiveLock());
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                cf.complete(lock.tryLock(500, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                cf.completeExceptionally(e);
            }
        });

        AtomicBoolean interrupted = new AtomicBoolean(false);
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        Thread timeout = setTimeout(interrupted, shouldStop);
        timeout.start();

        long currentTime = System.currentTimeMillis();
        t.start();

        while (!shouldStop.get() && LockSupport.getBlocker(t) != lock) {
            Thread.yield();
        }

        if (shouldStop.get()) {
            t.interrupt();
            fail("Timeout");
        }

        // Assert that less than 500 milliseconds have passed
        assertTrue(System.currentTimeMillis() - currentTime < 500);

        LockSupport.unpark(t); // Spurious wakeup

        // The timeout is no longer needed
        interrupted.set(true);
        timeout.interrupt();

        try {
            assertFalse(cf.get(1, TimeUnit.SECONDS));
        } catch (Exception e) {
            t.interrupt();
            throw e;
        }

        // Assert at least 500 milliseconds have passed
        assertTrue(System.currentTimeMillis() - currentTime >= 500);
    }

    @Test
    void isHeldByCurrentThreadTest() throws Exception {
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        CompletableFuture<Boolean> cf1 = new CompletableFuture<>();
        new Thread(() -> {
            cf.complete(lock.isHeldByCurrentThread());
            lock.lock();
            cf1.complete(lock.isHeldByCurrentThread());
        }).start();
        assertFalse(cf.get(3, TimeUnit.SECONDS));
        assertTrue(cf1.get(3, TimeUnit.SECONDS));
    }

    @Test
    void getHoldCountTest() throws Exception {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        new Thread(() -> {
            if (lock.getHoldCount() != 0) {
                cf.completeExceptionally(new RuntimeException("count != 0"));
                return;
            }
            lock.lock();
            if (lock.getHoldCount() != 1) {
                cf.completeExceptionally(new RuntimeException("count != 1"));
                return;
            }
            lock.lock();
            if (lock.getHoldCount() != 2) {
                cf.completeExceptionally(new RuntimeException("count != 2"));
                return;
            }
            lock.unlock();
            if (lock.getHoldCount() != 1) {
                cf.completeExceptionally(new RuntimeException("count != 1 after unlock"));
                return;
            }
            lock.unlock();
            if (lock.getHoldCount() != 0) {
                cf.completeExceptionally(new RuntimeException("count != 1 after unlock"));
                return;
            }
            cf.complete(null);
        }).start();
        cf.get(3, TimeUnit.SECONDS);
    }

    @Test
    void illegalStateExceptionsTest() throws Exception {
        Field threadLockCounter = ReentrantUpdaterLock.class.getDeclaredField("threadLockCounter");
        threadLockCounter.setAccessible(true);
        ThreadLocal<Integer> counter = (ThreadLocal<Integer>) threadLockCounter.get(lock);

        CompletableFuture<Void> cf = new CompletableFuture<>();
        new Thread(() -> {
            try {
                assertThrows(IllegalStateException.class, () -> lock.unlock());
                lock.lock();
                counter.set(ReentrantUpdaterLock.MAX_LOCKS_PER_THREAD); // Don't call lock.lock() in a loop, it'd take too long
                assertEquals(ReentrantUpdaterLock.MAX_LOCKS_PER_THREAD, lock.getHoldCount());
                assertThrows(IllegalStateException.class, () -> lock.lock());
                assertThrows(IllegalStateException.class, () -> lock.lockInterruptibly());
                assertFalse(lock.tryLock());
                assertFalse(lock.tryLock(0, TimeUnit.SECONDS));
                assertEquals(ReentrantUpdaterLock.MAX_LOCKS_PER_THREAD, lock.getHoldCount());
            } catch (Exception e) {
                cf.completeExceptionally(e);
                return;
            }
            cf.complete(null);
        }).start();
        cf.get(5, TimeUnit.SECONDS);
    }

    private Thread setTimeout(AtomicBoolean interrupted, AtomicBoolean shouldStop) {
        return new Thread(() -> {
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
