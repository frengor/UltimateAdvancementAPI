package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public final class ReentrantUpdaterLock implements Lock {
    private final ThreadLocal<Integer> threadLockCounter = ThreadLocal.withInitial(() -> 0);
    private final Object lock = new Object();

    private boolean updateLockRequested = false;
    private int activeLocksCounter = 0;

    ReentrantUpdaterLock() {
    }

    boolean tryLockExclusiveLock() {
        AdvancementUtils.checkSync();

        synchronized (lock) {
            updateLockRequested = true;
            return activeLocksCounter == 0;
        }
    }

    void unlockExclusiveLock() {
        AdvancementUtils.checkSync();

        synchronized (lock) {
            updateLockRequested = false;
            lock.notifyAll();
        }
    }

    @Override
    public void lock() {
        checkMainThread();

        int currentThreadCounter = threadLockCounter.get();
        if (currentThreadCounter > 0) {
            // Already holds a lock
            threadLockCounter.set(currentThreadCounter + 1);
            return;
        }

        boolean interrupted = false;
        synchronized (lock) {
            while (updateLockRequested) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }

            activeLocksCounter++;
        }

        // At this point threadLockCounter is surely 0, so we set it to 1
        threadLockCounter.set(1);

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        checkMainThread();

        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        int currentThreadCounter = threadLockCounter.get();
        if (currentThreadCounter > 0) {
            // Already holds a lock
            threadLockCounter.set(currentThreadCounter + 1);
            return;
        }

        synchronized (lock) {
            while (updateLockRequested) {
                lock.wait();
            }

            activeLocksCounter++;
        }

        // At this point threadLockCounter is surely 0, so we set it to 1
        threadLockCounter.set(1);
    }

    @Override
    public boolean tryLock() {
        checkMainThread();

        int currentThreadCounter = threadLockCounter.get();
        if (currentThreadCounter > 0) {
            // Already holds a lock
            threadLockCounter.set(currentThreadCounter + 1);
            return true;
        }

        synchronized (lock) {
            if (updateLockRequested) {
                return false;
            }

            activeLocksCounter++;
        }

        // At this point threadLockCounter is surely 0, so we set it to 1
        threadLockCounter.set(1);
        return true;
    }

    @Override
    public boolean tryLock(long l, @NotNull TimeUnit timeUnit) throws InterruptedException {
        checkMainThread();

        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        int currentThreadCounter = threadLockCounter.get();
        if (currentThreadCounter > 0) {
            // Already holds a lock
            threadLockCounter.set(currentThreadCounter + 1);
            return true;
        }

        synchronized (lock) {
            long nanos = timeUnit.toNanos(l);
            if (nanos <= 0) {
                if (updateLockRequested) {
                    return false;
                }
            } else {
                nanos += System.nanoTime();
                while (updateLockRequested) {
                    long timeout = nanos - System.nanoTime();
                    if (timeout <= 0) {
                        return false;
                    }
                    TimeUnit.NANOSECONDS.timedWait(lock, timeout);
                }
            }

            activeLocksCounter++;
        }

        // At this point threadLockCounter is surely 0, so we set it to 1
        threadLockCounter.set(1);
        return true;
    }

    @Override
    public void unlock() {
        checkMainThread();

        int currentThreadCounter = threadLockCounter.get();
        if (currentThreadCounter == 0) {
            throw new IllegalStateException("Thread " + Thread.currentThread().getName() + " has called ReentrantUpdaterLock#unlock() too many times.");
        }
        threadLockCounter.set(--currentThreadCounter);
        if (currentThreadCounter == 0) {
            synchronized (lock) {
                activeLocksCounter--;
            }
        }
    }

    @NotNull
    @Override
    @Contract("-> fail")
    public Condition newCondition() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    private void checkMainThread() {
        if (Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("The main thread cannot hold any ReentrantUpdaterLock lock.");
        }
    }
}
