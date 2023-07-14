package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

public final class ReentrantUpdaterLock implements Lock {
    private final ThreadLocal<Integer> threadLockCounter = ThreadLocal.withInitial(() -> 0);
    private final Semaphore mutex = new Semaphore(1, true);

    private boolean updateLockRequested = false;
    private int activeLocksCounter = 0;
    private final Set<Thread> blocked = Collections.synchronizedSet(new HashSet<>());

    ReentrantUpdaterLock() {
    }

    boolean tryLockExclusiveLock() {
        AdvancementUtils.checkSync();

        mutex.acquireUninterruptibly();
        try {
            updateLockRequested = true;
            return activeLocksCounter == 0;
        } finally {
            mutex.release();
        }
    }

    void unlockExclusiveLock() {
        AdvancementUtils.checkSync();

        mutex.acquireUninterruptibly();
        try {
            updateLockRequested = false;
            synchronized (blocked) { // FIXME Substitute with something fair
                activeLocksCounter = blocked.size();
                for (Thread t : blocked) {
                    LockSupport.unpark(t);
                }
                blocked.clear();
            }
        } finally {
            mutex.release();
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

        Thread currentThread = Thread.currentThread();
        mutex.acquireUninterruptibly();
        if (updateLockRequested) {
            // Park
            try {
                blocked.add(currentThread);
            } finally {
                mutex.release();
            }
            do {
                LockSupport.park(this);
            } while (blocked.contains(currentThread));

            // activeLocksCounter has already been incremented for us by unlockExclusiveLock()
        } else {
            activeLocksCounter++;
            mutex.release();
        }

        // At this point threadLockCounter is surely 0, so we set it to 1
        threadLockCounter.set(1);
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

        Thread currentThread = Thread.currentThread();
        mutex.acquireUninterruptibly();
        if (updateLockRequested) {
            // Park
            try {
                blocked.add(currentThread);
            } finally {
                mutex.release();
            }

            boolean blockedContains, interrupted;
            do {
                LockSupport.park(this);
                interrupted = Thread.interrupted();
                blockedContains = blocked.contains(currentThread);
            } while (blockedContains && !interrupted);

            // Don't throw an InterruptedException if the lock has been acquired
            if (interrupted) {
                if (!blockedContains) { // FIXME This should be if (blockedContains), also more synchronization is probably needed
                    throw new InterruptedException();
                } else {
                    // Restore the interrupt status
                    currentThread.interrupt();
                }
            }

            // activeLocksCounter has already been incremented for us by unlockExclusiveLock()
        } else {
            activeLocksCounter++;
            mutex.release();
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

        mutex.acquireUninterruptibly();
        try {
            if (updateLockRequested) {
                return false;
            } else {
                activeLocksCounter++;
            }
        } finally {
            mutex.release();
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

        Thread currentThread = Thread.currentThread();
        long nanos = timeUnit.toNanos(l);
        mutex.acquireUninterruptibly();
        if (nanos <= 0) {
            // Don't block
            try {
                if (updateLockRequested) {
                    return false;
                } else {
                    activeLocksCounter++;
                }
            } finally {
                mutex.release();
            }
        } else {
            nanos += System.nanoTime();
            if (updateLockRequested) {
                // Park
                try {
                    blocked.add(currentThread);
                } finally {
                    mutex.release();
                }

                boolean blockedContains, interrupted;
                do {
                    long timeout = nanos - System.nanoTime();
                    if (timeout <= 0) {
                        return false;
                    }
                    LockSupport.parkNanos(this, timeout);
                    interrupted = Thread.interrupted();
                    blockedContains = blocked.contains(currentThread);
                } while (blockedContains && !interrupted);

                // Don't throw an InterruptedException if the lock has been acquired
                if (interrupted) {
                    if (!blockedContains) { // FIXME This should be if (blockedContains), also more synchronization is probably needed
                        throw new InterruptedException();
                    } else {
                        // Restore the interrupt status
                        currentThread.interrupt();
                    }
                }

                // activeLocksCounter has already been incremented for us by unlockExclusiveLock()
            } else {
                activeLocksCounter++;
                mutex.release();
            }
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
            mutex.acquireUninterruptibly();
            activeLocksCounter--;
            mutex.release();
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
