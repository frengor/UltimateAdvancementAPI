package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingFailedEvent;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
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
import java.util.function.Consumer;

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

    public void onFinishLoading(@NotNull UUID uuid, long delay, @NotNull Consumer<Player> toRun, @NotNull Runnable onCancel) {
        final Runnable cancelled;
        synchronized (JoinEventWaiter.this) {
            WaiterObject waiter = waiting.get(uuid);
            if (waiter == null) {
                // Cannot schedule, just call onCancel (outside the synchronized block)
                cancelled = onCancel;
            } else if (waiter.scheduled) {
                // Already scheduled, just call onCancel (outside the synchronized block)
                cancelled = onCancel;
            } else {
                cancelled = waiter.onCancel; // Save old onCancel to be called outside the synchronized block
                if (waiter.joinEventFired) {
                    Preconditions.checkNotNull(waiter.joinEventPlayer, "waiter.joinEventFired is null.");
                    scheduleRunnable(waiter, toRun, delay, waiter.joinEventPlayer);
                } else {
                    // Save values for join event
                    waiter.toRun = toRun;
                    waiter.onCancel = onCancel;
                    waiter.delay = delay;
                }
            }
        }
        if (cancelled != null) {
            cancelled.run();
        }
    }

    public synchronized void onJoin(@NotNull Player player) {
        WaiterObject waiter = waiting.get(player.getUniqueId());
        if (waiter == null) {
            return;
        }
        if (waiter.scheduled) {
            return;
        }
        if (waiter.toRun == null) {
            waiter.joinEventFired = true;
            waiter.joinEventPlayer = player;
            return;
        }
        scheduleRunnable(waiter, waiter.toRun, waiter.delay, player);
    }

    public void onQuit(@NotNull UUID uuid) {
        Runnable onCancel = null;
        synchronized (JoinEventWaiter.this) {
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
                if (w.onCancel != null) {
                    toCancel.add(w.onCancel);
                }
            }
            waiting.clear();
        }
        for (Runnable cancel : toCancel) {
            cancel.run();
        }
    }

    private synchronized void scheduleRunnable(@NotNull WaiterObject waiter, @NotNull Consumer<Player> toRun, long delay, @NotNull Player player) {
        waiter.scheduled = true;
        waiter.task = runSync(plugin, delay, () -> {
            synchronized (JoinEventWaiter.this) {
                waiting.remove(player.getUniqueId());
            }
            toRun.accept(player);
        });
    }

    private static class WaiterObject {
        @Nullable
        private Consumer<Player> toRun;
        @Nullable
        private Runnable onCancel;
        private boolean joinEventFired, scheduled;
        @Nullable
        private Player joinEventPlayer;
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
