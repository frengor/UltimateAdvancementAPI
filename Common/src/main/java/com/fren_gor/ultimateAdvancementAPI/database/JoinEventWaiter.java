package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingFailedEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.checkSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;

/**
 * Internal class used to ensure {@link PlayerLoadingCompletedEvent} and {@link PlayerLoadingFailedEvent} aren't fired before {@link PlayerJoinEvent}.
 */
final class JoinEventWaiter {

    private final Map<UUID, WaiterObject> waiting = new HashMap<>();
    private final Plugin plugin;

    public JoinEventWaiter(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void onLogin(@NotNull UUID uuid) {
        waiting.put(uuid, new WaiterObject());
    }

    public void onFinishLoading(@NotNull UUID uuid, long delay, @NotNull Runnable toRun, @NotNull Runnable onCancel) {
        Runnable cancelled = null, oldCancelled = null;
        synchronized (JoinEventWaiter.this) {
            WaiterObject waiter = waiting.get(uuid);
            if (waiter == null) {
                // Cannot schedule, just call onCancel (outside the synchronized block)
                cancelled = onCancel;
            } else if (waiter.scheduled) {
                // Already scheduled, just call onCancel (outside the synchronized block)
                cancelled = onCancel;
            } else {
                oldCancelled = waiter.onCancel; // Save old onCancel to be called outside the synchronized block
                if (waiter.joinEventFired) {
                    scheduleRunnable(waiter, toRun, delay, uuid);
                } else {
                    // Save values for join event
                    waiter.toRun = toRun;
                    waiter.onCancel = onCancel;
                    waiter.delay = delay;
                }
            }
        }
        if (oldCancelled != null) {
            oldCancelled.run();
        }
        if (cancelled != null) {
            cancelled.run();
        }
    }

    public synchronized void onJoin(@NotNull UUID uuid) {
        WaiterObject waiter = waiting.get(uuid);
        if (waiter == null) {
            return;
        }
        if (waiter.scheduled) {
            return;
        }
        if (waiter.toRun == null) {
            waiter.joinEventFired = true;
            return;
        }
        scheduleRunnable(waiter, waiter.toRun, waiter.delay, uuid);
    }

    public void onQuit(@NotNull UUID uuid) {
        Runnable onCancel = null;
        synchronized (JoinEventWaiter.this) {
            checkSync();
            WaiterObject w = waiting.remove(uuid);
            if (w != null) {
                w.cancel();
                onCancel = w.onCancel;
            }
        }
        if (onCancel != null) {
            onCancel.run();
        }
    }

    public void onClose() {
        List<Runnable> toCancel;
        synchronized (JoinEventWaiter.this) {
            toCancel = new ArrayList<>(waiting.size());
            for (WaiterObject w : waiting.values()) {
                w.cancel();
                toCancel.add(w.onCancel);
            }
            waiting.clear();
        }
        for (Runnable cancel : toCancel) {
            if (cancel != null) {
                cancel.run();
            }
        }
    }

    private synchronized void scheduleRunnable(@NotNull WaiterObject waiter, @NotNull Runnable toRun, long delay, @NotNull UUID uuid) {
        waiter.scheduled = true;
        waiter.task = runSync(plugin, delay, () -> {
            synchronized (JoinEventWaiter.this) {
                waiting.remove(uuid);
            }
            toRun.run();
        });
    }

    private static class WaiterObject {
        @Nullable
        private Runnable toRun, onCancel;
        private boolean joinEventFired, scheduled;
        @Nullable
        private BukkitTask task;
        private long delay;

        protected void cancel() {
            if (task != null) {
                task.cancel();
            }
        }
    }
}
