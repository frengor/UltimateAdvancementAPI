package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.AdvancementUpdater;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.ProgressionUpdateResult;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.ProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.google.common.collect.MapMaker;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateProgressionValueStrict;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * The {@code TaskAdvancement} class represents a task. It can be used by any {@link AbstractMultiTasksAdvancement} subclass
 * to separate an advancement progression into different progressions (one per task).
 * <p>For example, the advancement "mine 5 blocks of every plank" can be made using a {@code TaskAdvancement}
 * for every plank (with a progression of 5) and registering them into an {@link AbstractMultiTasksAdvancement}.
 * <p>{@code TaskAdvancement}s are saved into the database, but they are never sent to players.
 * For this reason they cannot be registered in an {@link AdvancementTab}.
 */
public class TaskAdvancement extends BaseAdvancement {

    // CompletableFuture -> MultiTask's progression at time of ProgressionUpdateEvent
    private final Map<CompletableFuture<ProgressionUpdateResult>, Integer> pendingUpdates = new MapMaker()
            .concurrencyLevel(1)
            .weakKeys() // This makes the map use identity comparison (==) for keys
            .makeMap();

    /**
     * Creates a new {@code TaskAdvancement} with a maximum progression of {@code 1}.
     *
     * @param multitask The {@link AbstractMultiTasksAdvancement} that owns this task.
     * @param key The unique key of the task. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     */
    public TaskAdvancement(@NotNull AbstractMultiTasksAdvancement multitask, @NotNull String key) {
        this(multitask, key, 1);
    }

    /**
     * Creates a new {@code TaskAdvancement}.
     *
     * @param multitask The {@link AbstractMultiTasksAdvancement} that owns this task.
     * @param key The unique key of the task. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param maxProgression The maximum progression of the task.
     */
    public TaskAdvancement(@NotNull AbstractMultiTasksAdvancement multitask, @NotNull String key, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        this(multitask, key, maxProgression, new AdvancementDisplayBuilder(Material.GRASS_BLOCK, Objects.requireNonNull(key, "Key is null.")).build());
    }

    /**
     * Creates a new {@code TaskAdvancement} with a maximum progression of {@code 1}.
     *
     * @param multitask The {@link AbstractMultiTasksAdvancement} that owns this task.
     * @param key The unique key of the task. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param display The display information of this task.
     */
    public TaskAdvancement(@NotNull AbstractMultiTasksAdvancement multitask, @NotNull String key, @NotNull AbstractAdvancementDisplay display) {
        this(multitask, key, 1, display);
    }

    /**
     * Creates a new {@code TaskAdvancement}.
     *
     * @param multitask The {@link AbstractMultiTasksAdvancement} that owns this task.
     * @param key The unique key of the task. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param maxProgression The maximum progression of the task.
     * @param display The display information of this task.
     */
    public TaskAdvancement(@NotNull AbstractMultiTasksAdvancement multitask, @NotNull String key, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression, @NotNull AbstractAdvancementDisplay display) {
        super(Objects.requireNonNull(multitask, "AbstractMultiTasksAdvancement is null."), key, maxProgression, display);

        registerEvent(ProgressionUpdateEvent.class, EventPriority.HIGHEST, e -> {
            if (!this.getKey().equals(e.getAdvancementKey())) {
                return;
            }

            pendingUpdates.put(e.getUpdateCompletableFuture(), multitask.getProgression(e.getTeamProgression()));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull TeamProgression pro, @Nullable Player player, int increment, boolean giveRewards) {
        validateTeamProgression(pro);

        final DatabaseManager ds = advancementTab.getDatabaseManager();
        var completableFuture = ds.incrementProgression(key, pro, increment, maxProgression);

        runSync(completableFuture, advancementTab.getOwningPlugin(), (result, err) -> {
            if (err != null) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while incrementing the progression of " + key, err);
                return; // Don't go on in case of a db error
            }

            try {
                Bukkit.getPluginManager().callEvent(new AdvancementProgressionUpdateEvent(this, pro, result.oldProgression(), result.newProgression()));
            } catch (Exception e) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while calling AdvancementProgressionUpdateEvent for " + key, e);
            }

            // Use try-finally to make sure to call reloadTasks, since not doing so will leak memory
            try {
                handleAdvancementGranting(pro, player, result.newProgression(), result.oldProgression(), giveRewards);
            } finally {
                Integer multiTaskProgression = pendingUpdates.remove(completableFuture);
                if (multiTaskProgression != null) {
                    getMultiTasksAdvancement().onTaskProgressionChange(this, pro, player, result, multiTaskProgression, giveRewards);
                }
            }
        });

        return completableFuture;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean giveRewards) {
        validateTeamProgression(pro);
        validateProgressionValueStrict(progression, maxProgression);

        final DatabaseManager ds = advancementTab.getDatabaseManager();
        var completableFuture = ds.setProgression(key, pro, progression);

        runSync(completableFuture, advancementTab.getOwningPlugin(), (result, err) -> {
            if (err != null) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while setting the progression of " + key, err);
                return; // Don't go on in case of a db error
            }

            try {
                Bukkit.getPluginManager().callEvent(new AdvancementProgressionUpdateEvent(this, pro, result.oldProgression(), result.newProgression()));
            } catch (Exception e) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while calling AdvancementProgressionUpdateEvent for " + key, e);
            }

            // Use try-finally to make sure to call reloadTasks, since not doing so will leak memory
            try {
                handleAdvancementGranting(pro, player, result.newProgression(), result.oldProgression(), giveRewards);
            } finally {
                Integer multiTaskProgression = pendingUpdates.remove(completableFuture);
                if (multiTaskProgression != null) {
                    getMultiTasksAdvancement().onTaskProgressionChange(this, pro, player, result, multiTaskProgression, giveRewards);
                }
            }
        });

        return completableFuture;
    }

    /**
     * {@inheritDoc}
     * Since {@code TaskAdvancement}s are not sent to players, this method always returns {@code false}.
     *
     * @return Always {@code false}.
     */
    @Override
    @Contract("_ -> false")
    public final boolean isVisible(@NotNull Player player) {
        return false;
    }

    /**
     * {@inheritDoc}
     * Since {@code TaskAdvancement}s are not sent to players, this method always returns {@code false}.
     *
     * @return Always {@code false}.
     */
    @Override
    @Contract("_ -> false")
    public final boolean isVisible(@NotNull UUID uuid) {
        return false;
    }

    /**
     * {@inheritDoc}
     * Since {@code TaskAdvancement}s are not sent to players, this method always returns {@code false}.
     *
     * @return Always {@code false}.
     */
    @Override
    @Contract("_ -> false")
    public final boolean isVisible(@NotNull TeamProgression progression) {
        return false;
    }

    /**
     * {@inheritDoc}
     * Since {@code TaskAdvancement}s are not sent to players, this method always returns {@code null}.
     *
     * @return Always {@code null}.
     */
    @Override
    @Nullable
    @Contract("_ -> null")
    public final Function<@NotNull Player, @Nullable BaseComponent> getAnnouncementMessage(@NotNull Player advancementCompleter) {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>Since {@code TaskAdvancement}s are not sent to players, this method doesn't send any announcement messages.
     */
    @Override
    protected void sendAnnouncementMessageOnGrant(@NotNull Player advancementCompleter, @NotNull TeamProgression progression) {
    }

    /**
     * {@inheritDoc}
     * <p>Since {@code TaskAdvancement}s are not sent to players, this method doesn't display any toast notifications.
     */
    @Override
    protected void displayToastOnGrant(@NotNull Player advancementCompleter, @NotNull TeamProgression progression) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return getMultiTasksAdvancement().isValid();
    }

    /**
     * Gets the task's {@link AbstractMultiTasksAdvancement}.
     *
     * @return The task's {@link AbstractMultiTasksAdvancement}.
     */
    @NotNull
    public AbstractMultiTasksAdvancement getMultiTasksAdvancement() {
        return (AbstractMultiTasksAdvancement) parent;
    }

    /**
     * Validate the advancement after it has been registered by the advancement tab.
     * <p>Since {@code TaskAdvancement}s cannot be registered in tabs, this method always fails.
     *
     * @throws InvalidAdvancementException Every time this method is called.
     */
    @Override
    public void validateRegister() throws InvalidAdvancementException {
        // Always throw since Tasks cannot be registered in Tabs
        throw new InvalidAdvancementException("TaskAdvancements cannot be registered in any AdvancementTab.");
    }

    // ============ Overridden methods which throw an UnsupportedOperationException ============

    /**
     * {@inheritDoc}
     * <p>Since {@code TaskAdvancement}s cannot be registered in tabs, this method always throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Every time this method is called.
     */
    @Override
    @NotNull
    public final PreparedAdvancementWrapper getNMSWrapper() throws ReflectiveOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>Since {@code TaskAdvancement}s cannot be registered in tabs, this method always throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Every time this method is called.
     */
    @Override
    public final void displayToastToPlayer(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>Since {@code TaskAdvancement}s cannot be registered in tabs, this method always throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Every time this method is called.
     */
    @Override
    public void onUpdate(@NotNull TeamProgression teamProgression, @NotNull AdvancementUpdater advancementUpdater) throws ReflectiveOperationException {
        throw new UnsupportedOperationException();
    }
}
