package org.bukkit.craftbukkit.mocked0_0_R1;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.scheduler.BukkitSchedulerMock;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.google.common.base.Preconditions;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class used to mock the server (see {@link MockBukkit#mock(ServerMock)}).
 * <p>Using this class allows {@link ReflectionUtil} to work correctly. A mocked implementation of nms wrappers
 * can be placed in the package {@code com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1}.
 */
public class VersionedServerMock extends ServerMock {

    // MOCK BUKKIT WORKAROUND:
    // Scheduling using runTaskLater or runTaskTimer from another thread doesn't currently work in MockBukkit (due to a
    // race conditions I suppose, I'm not sure though). This workaround basically avoids scheduling the tasks directly.
    // A task is immediately scheduled the first time getScheduler() is called. Let's call it the "main task".
    // It is used to have some code which can execute on the main thread. runTaskLater adds the task to execute to a
    // (synchronized) list, which is then used by the main task to get the tasks and schedule them from the main thread.
    // runTaskTimer works similarly. This way, tasks scheduled using runTaskLater or runTaskTimer from another thread
    // are, in reality, scheduled from the main thread.

    private final Scheduler scheduler = new Scheduler(this);
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    public VersionedServerMock() {
    }

    @NotNull
    @Override
    public BukkitSchedulerMock getScheduler() {
        if (!scheduled.getAndSet(true)) {
            scheduler.startTask();
        }
        return scheduler;
    }

    private static class Scheduler extends BukkitSchedulerMock {
        private final ServerMock server;
        private final LinkedList<DelayedTask> delayedTasks = new LinkedList<>();
        private final LinkedList<TimerTask> timerTasks = new LinkedList<>();

        public Scheduler(ServerMock server) {
            this.server = server;
        }

        void startTask() {
            assertTrue(server.isOnMainThread());

            super.runTaskTimer(null /* Yeah, this only works with null, even though it is marked as @NonNull */, () -> {
                synchronized (delayedTasks) {
                    if (!delayedTasks.isEmpty()) {
                        for (DelayedTask t : delayedTasks) {
                            t.c.complete(super.runTaskLater(t.plugin, t.runnable, t.delay));
                        }
                        delayedTasks.clear();
                    }
                }
                synchronized (timerTasks) {
                    if (!timerTasks.isEmpty()) {
                        for (TimerTask t : timerTasks) {
                            t.c.complete(super.runTaskTimer(t.plugin, t.runnable, t.delay, t.period));
                        }
                        timerTasks.clear();
                    }
                }
            }, 0, 1);
        }

        @Override
        public void shutdown() {
            synchronized (delayedTasks) {
                delayedTasks.clear();
            }
            synchronized (timerTasks) {
                timerTasks.clear();
            }
            super.shutdown();
        }

        @Override
        @NotNull
        public BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull Runnable task, long delay) {
            Preconditions.checkNotNull(plugin);
            Preconditions.checkNotNull(task);
            if (server.isOnMainThread()) {
                return super.runTaskLater(plugin, task, delay);
            } else {
                CompletableFuture<BukkitTask> c = new CompletableFuture<>();
                synchronized (delayedTasks) {
                    delayedTasks.addLast(new DelayedTask(c, plugin, task, delay));
                }
                try {
                    return c.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        @NotNull
        public BukkitTask runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) {
            Preconditions.checkNotNull(plugin);
            Preconditions.checkNotNull(task);
            if (server.isOnMainThread()) {
                return super.runTaskTimer(plugin, task, delay, period);
            } else {
                CompletableFuture<BukkitTask> c = new CompletableFuture<>();
                synchronized (timerTasks) {
                    timerTasks.addLast(new TimerTask(c, plugin, task, delay, period));
                }
                try {
                    return c.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        record DelayedTask(CompletableFuture<BukkitTask> c, Plugin plugin, Runnable runnable, long delay) {
        }

        record TimerTask(CompletableFuture<BukkitTask> c, Plugin plugin, Runnable runnable, long delay, long period) {
        }
    }
}
