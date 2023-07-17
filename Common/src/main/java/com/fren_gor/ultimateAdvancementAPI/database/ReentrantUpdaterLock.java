package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.exceptions.SyncExecutionException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A (fair) lock used to avoid team and progression updates in <i>async</i> code.
 * <p>When one or more locks are held by any thread, updates to teams and progressions will be delayed until all
 * granted locks are released.
 * <p>Multiple locks can be acquired (and hold) at the same time, even by different threads, in a reentrant manner
 * (i.e. a thread which already holds a lock will always succeed to obtain a new lock).
 * When the updater needs to update a team or progression, new <i>non-reentrant</i> locks will not be granted and
 * will need to wait for the end of the update. New <i>reentrant</i> locks are always permitted.
 * <br>Also, taking a lock and not releasing it will <i>never</i> block the main thread, but will
 * starve updates (i.e. team and progression updates will not be applied until all the active locks are released).
 * <p>As a last note, <strong>using this lock on the main thread is useless</strong> and trying to use it will
 * throw a {@link SyncExecutionException}.
 * <br>
 * <p>
 * <strong>Implementation note:</strong><br>
 * Each thread can hold up to a maximum of {@link #MAX_LOCKS_PER_THREAD} locks at the same time.
 *
 * @see DatabaseManager
 * @see DatabaseManager#updaterLock
 * @since 3.0.0
 */
public final class ReentrantUpdaterLock implements Lock {
    /**
     * Maximum number of locks that each thread can hold at the same time.
     */
    public static final int MAX_LOCKS_PER_THREAD = Integer.MAX_VALUE;

    private final Semaphore mutex = new Semaphore(1, true);

    private boolean updateLockRequested = false;
    private long activeLocksCounter = 0; // Number of threads which are holding at least one lock
    private final ThreadLocal<Integer> threadLockCounter = ThreadLocal.withInitial(() -> 0); // Number of locks held by each thread
    private Set<Thread> blocked = new HashSet<>();

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

        final Set<Thread> oldBlocked;
        mutex.acquireUninterruptibly();
        try {
            updateLockRequested = false;
            oldBlocked = blocked;
            blocked = new HashSet<>();
            activeLocksCounter = oldBlocked.size();
        } finally {
            mutex.release();
        }
        for (Thread t : oldBlocked) {
            LockSupport.unpark(t);
        }
    }

    /**
     * Acquires a lock for the current thread, blocking if necessary.
     *
     * @throws IllegalStateException If the current thread already holds {@link #MAX_LOCKS_PER_THREAD} locks.
     * @throws SyncExecutionException If this method is called on the main thread.
     */
    @Override
    public void lock() {
        checkMainThread();

        int currentThreadCounter = threadLockCounter.get();
        if (currentThreadCounter > 0) {
            // Already holds a lock
            incrementThreadLockCounter(currentThreadCounter);
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
            boolean blockedContains;
            do {
                LockSupport.park(this);

                // Take a lock and check if blocked contains the current thread
                mutex.acquireUninterruptibly();
                try {
                    blockedContains = blocked.contains(currentThread);
                } finally {
                    mutex.release();
                }
            } while (blockedContains);

            // activeLocksCounter has already been incremented for us by unlockExclusiveLock()
        } else {
            activeLocksCounter++;
            mutex.release();
        }

        // At this point threadLockCounter is surely 0, so we set it to 1
        threadLockCounter.set(1);
    }

    /**
     * Acquires a lock for the current thread, blocking if necessary, unless it gets interrupted.
     *
     * @throws InterruptedException If the current thread is interrupted when this method is called or if it gets
     *         interrupted while blocked.
     * @throws IllegalStateException If the current thread already holds {@link #MAX_LOCKS_PER_THREAD} locks.
     * @throws SyncExecutionException If this method is called on the main thread.
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        checkMainThread();

        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        int currentThreadCounter = threadLockCounter.get();
        if (currentThreadCounter > 0) {
            // Already holds a lock
            incrementThreadLockCounter(currentThreadCounter);
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

                // Take a lock and check if blocked contains the current thread
                mutex.acquireUninterruptibly();
                try {
                    blockedContains = blocked.contains(currentThread);
                    interrupted = Thread.interrupted(); // Must be done after acquireUninterruptibly()

                    if (interrupted && blockedContains) {
                        // In this case the lock hasn't been acquired but the thread was interrupted
                        // Remove from blocked so that the main thread doesn't call LockSupport#unpark on this thread
                        blocked.remove(currentThread);
                        throw new InterruptedException();
                    }
                } finally {
                    mutex.release();
                }
            } while (blockedContains);

            if (interrupted) {
                // Restore the interrupt status if the lock was acquired
                currentThread.interrupt();
            }

            // activeLocksCounter has already been incremented for us by unlockExclusiveLock()
        } else {
            activeLocksCounter++;
            mutex.release();
        }

        // At this point threadLockCounter is surely 0, so we set it to 1
        threadLockCounter.set(1);
    }

    /**
     * Acquires a lock for the current thread only if it can be immediately granted at the time of invocation of this method.
     * <p>If the current thread already holds {@link #MAX_LOCKS_PER_THREAD} locks, the lock is not acquired and {@code false} is returned.
     *
     * @return {@code true} if the lock has been acquired for this thread, {@code false} otherwise.
     * @throws SyncExecutionException If this method is called on the main thread.
     */
    @Override
    public boolean tryLock() {
        checkMainThread();

        int currentThreadCounter = threadLockCounter.get();
        if (currentThreadCounter == MAX_LOCKS_PER_THREAD) {
            return false;
        }
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

    /**
     * Acquires a lock for the current thread, blocking if necessary, unless it gets interrupted or the time runs out.
     * <p>If the current thread already holds {@link #MAX_LOCKS_PER_THREAD} locks, the lock is not acquired and {@code false} is returned.
     *
     * @param time The maximum time to wait for the lock.
     * @param timeUnit The time unit of the time argument.
     * @return {@code true} if the lock has been acquired for this thread, {@code false} if the time ran out.
     * @throws InterruptedException If the current thread is interrupted when this method is called or if it gets
     *         interrupted while blocked.
     * @throws SyncExecutionException If this method is called on the main thread.
     */
    @Override
    public boolean tryLock(long time, @NotNull TimeUnit timeUnit) throws InterruptedException {
        checkMainThread();

        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        int currentThreadCounter = threadLockCounter.get();
        if (currentThreadCounter == MAX_LOCKS_PER_THREAD) {
            return false;
        }
        if (currentThreadCounter > 0) {
            // Already holds a lock
            threadLockCounter.set(currentThreadCounter + 1);
            return true;
        }

        Thread currentThread = Thread.currentThread();
        long nanos = timeUnit.toNanos(time);
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

                    // Take a lock and check if blocked contains the current thread
                    mutex.acquireUninterruptibly();
                    try {
                        blockedContains = blocked.contains(currentThread);
                        interrupted = Thread.interrupted(); // Must be done after acquireUninterruptibly()

                        if (interrupted && blockedContains) {
                            // In this case the lock hasn't been acquired but the thread was interrupted
                            // Remove from blocked so that the main thread doesn't call LockSupport#unpark on this thread
                            blocked.remove(currentThread);
                            throw new InterruptedException();
                        }
                    } finally {
                        mutex.release();
                    }
                } while (blockedContains);

                if (interrupted) {
                    // Restore the interrupt status if the lock was acquired
                    currentThread.interrupt();
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

    /**
     * Releases a lock held by the current thread.
     *
     * @throws IllegalStateException If the current thread doesn't hold any locks.
     * @throws SyncExecutionException If this method is called on the main thread.
     */
    @Override
    public void unlock() {
        checkMainThread();

        int currentThreadCounter = threadLockCounter.get();
        if (currentThreadCounter == 0) {
            throw new IllegalStateException("Thread " + Thread.currentThread().getName() + " called ReentrantUpdaterLock#unlock() while not holding any lock.");
        }
        threadLockCounter.set(--currentThreadCounter); // This cannot overflow since currentThreadCounter is always > 0
        if (currentThreadCounter == 0) {
            mutex.acquireUninterruptibly();
            activeLocksCounter--;
            mutex.release();
        }
    }

    /**
     * Returns whether the current thread holds at least one lock.
     * <p>This method always returns {@code false} when called on the main thread.
     *
     * @return {@code true} if the current thread holds at least one lock, {@code false} otherwise.
     * @see ReentrantLock#isHeldByCurrentThread() ReentrantLock.isHeldByCurrentThread() for possible usages of this method.
     */
    public boolean isHeldByCurrentThread() {
        return threadLockCounter.get() > 0;
    }

    /**
     * Returns the number of holds on this lock by the current thread.
     * <p>This method always returns {@code 0} when called on the main thread.
     *
     * @return The number of holds on this lock by the current thread.
     * @see ReentrantLock#getHoldCount() ReentrantLock.getHoldCount() for possible usages of this method.
     */
    @Range(from = 0, to = MAX_LOCKS_PER_THREAD)
    public int getHoldCount() {
        return threadLockCounter.get();
    }

    /**
     * {@link Condition}s are not supported.
     *
     * @throws UnsupportedOperationException This always throws {@link UnsupportedOperationException}.
     */
    @Override
    @Contract("-> fail")
    public Condition newCondition() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    private void incrementThreadLockCounter(int counterToIncrement) {
        if (counterToIncrement == MAX_LOCKS_PER_THREAD) {
            throw new IllegalStateException("Thread " + Thread.currentThread().getName() + " already holds the maximum amount of locks permitted per-thread.");
        }
        threadLockCounter.set(counterToIncrement + 1);
    }

    private void checkMainThread() {
        if (Bukkit.isPrimaryThread()) {
            throw new SyncExecutionException("The main thread cannot hold any ReentrantUpdaterLock lock.");
        }
    }
}
