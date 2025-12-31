package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.ProgressionUpdateResult;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.ProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.ArbitraryMultiTaskProgressionUpdateException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateProgressionValueStrict;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * An implementation of {@link AbstractMultiTasksAdvancement}. {@link TaskAdvancement}s have to be registered
 * using {@link #registerTasks(Set)} in order to initialise an instance of this class.
 */
public class MultiTasksAdvancement extends AbstractMultiTasksAdvancement {

    private final Set<TaskAdvancement> tasks = new HashSet<>();

    // Initialized with values in registerTasks(...)
    // Must always be immutable
    private Set<AdvancementKey> tasksKeys = Collections.emptySet();

    // Team id -> progression
    private final Map<Integer, Integer> progressionsCache = Collections.synchronizedMap(new HashMap<>());

    private boolean initialised = false;

    /**
     * Creates a new {@code MultiTasksAdvancement} with maximum progression of {@code 1}.
     *
     * @param parent The parent of this advancement.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param display The display information of this advancement.
     */
    public MultiTasksAdvancement(@NotNull Advancement parent, @NotNull String key, @NotNull AbstractAdvancementDisplay display) {
        this(parent, key, 1, display);
    }

    /**
     * Creates a new {@code MultiTasksAdvancement}.
     *
     * @param parent The parent of this advancement.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param maxProgression The sum of the maximum progressions of all the tasks that will be registered.
     * @param display The display information of this advancement.
     */
    public MultiTasksAdvancement(@NotNull Advancement parent, @NotNull String key, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression, @NotNull AbstractAdvancementDisplay display) {
        super(parent, key, maxProgression, display);
    }

    /**
     * Register the tasks for this multi-task advancement, initializing it. Thus, it cannot be called twice.
     *
     * @param tasks The tasks to register. Cannot include any {@code null} task.
     */
    public void registerTasks(@NotNull TaskAdvancement... tasks) {
        registerTasks(Sets.newHashSet(Objects.requireNonNull(tasks)));
    }

    /**
     * Register the tasks for this multi-task advancement, initializing it. Thus, it cannot be called twice.
     *
     * @param tasks The tasks to register. Cannot include any {@code null} task.
     */
    public void registerTasks(@NotNull Set<TaskAdvancement> tasks) {
        if (initialised) {
            throw new IllegalStateException("MultiTaskAdvancement is already initialised.");
        }
        AdvancementUtils.checkSync();
        Preconditions.checkNotNull(tasks, "Set<TaskAdvancement> is null.");
        int progression = 0;
        for (TaskAdvancement t : tasks) {
            if (t == null) {
                throw new NullPointerException("A TaskAdvancement is null.");
            }
            if (t.getMultiTasksAdvancement() != this) {
                throw new IllegalArgumentException("TaskAdvancement's AbstractMultiTasksAdvancement (" + t.getMultiTasksAdvancement().getKey() + ") doesn't match with this MultiTasksAdvancement (" + key + ").");
            }
            // Useless check (the one above should make sure this one here is always true)
            /*if (!advancementTab.isOwnedByThisTab(t)) {
                throw new IllegalArgumentException("TaskAdvancement " + t.getKey() + " is not owned by this tab (" + advancementTab.getNamespace() + ").");
            }*/
            progression += t.getMaxProgression();
        }
        if (progression != maxProgression) {
            throw new IllegalArgumentException("Expected max progression (" + maxProgression + ") doesn't match the tasks' total one (" + progression + ").");
        }
        this.tasks.addAll(tasks);
        this.tasksKeys = tasks.stream().map(Advancement::getKey).collect(Collectors.toUnmodifiableSet());

        registerEvent(AsyncTeamUnloadEvent.class, EventPriority.HIGHEST, e -> {
            progressionsCache.remove(e.getTeamProgression().getTeamId());
        });

        registerEvent(ProgressionUpdateEvent.class, EventPriority.LOW, e -> {
            if (!tasksKeys.contains(e.getAdvancementKey())) {
                return;
            }
            resetProgressionCache(e.getTeamProgression());
        });

        initialised = true;
    }

    /**
     * Gets an unmodifiable {@link Set} of the registered tasks.
     *
     * @return An unmodifiable {@link Set} of the registered tasks.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @NotNull
    @UnmodifiableView
    public Set<@NotNull TaskAdvancement> getTasks() {
        checkInitialisation();
        return Collections.unmodifiableSet(tasks);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getProgression(@NotNull TeamProgression progression) {
        checkInitialisation();
        validateTeamProgression(progression);
        synchronized (progressionsCache) {
            Integer progr = progressionsCache.get(progression.getTeamId());
            if (progr == null) {
                int c = 0;
                for (TaskAdvancement t : tasks) {
                    c += progression.getProgression(t);
                }
                progressionsCache.put(progression.getTeamId(), c);
                return c;
            } else {
                return progr;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public boolean isGranted(@NotNull TeamProgression pro) {
        checkInitialisation();
        return super.isGranted(pro);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to use any
     * increment except {@code 0}. Using any other increment will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided increment is not {@code 0}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player) {
        return super.incrementProgression(player);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to use any
     * increment except {@code 0}. Using any other increment will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided increment is not {@code 0}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player, boolean giveReward) {
        return super.incrementProgression(player, giveReward);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to use any
     * increment except {@code 0}. Using any other increment will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided increment is not {@code 0}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player, int increment) {
        return super.incrementProgression(player, increment);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to use any
     * increment except {@code 0}. Using any other increment will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided increment is not {@code 0}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player, int increment, boolean giveReward) {
        return super.incrementProgression(player, increment, giveReward);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to use any
     * increment except {@code 0}. Using any other increment will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided increment is not {@code 0}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid) {
        return super.incrementProgression(uuid);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to use any
     * increment except {@code 0}. Using any other increment will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided increment is not {@code 0}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid, boolean giveReward) {
        return super.incrementProgression(uuid, giveReward);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to use any
     * increment except {@code 0}. Using any other increment will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided increment is not {@code 0}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid, int increment) {
        return super.incrementProgression(uuid, increment);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to use any
     * increment except {@code 0}. Using any other increment will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided increment is not {@code 0}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid, int increment, boolean giveReward) {
        return super.incrementProgression(uuid, increment, giveReward);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to use any
     * increment except {@code 0}. Using any other increment will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided increment is not {@code 0}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    protected CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull TeamProgression progression, @Nullable Player player, int increment, boolean giveRewards) throws ArbitraryMultiTaskProgressionUpdateException {
        checkInitialisation();
        validateTeamProgression(progression);

        if (increment != 0) {
            throw new ArbitraryMultiTaskProgressionUpdateException();
        }

        CompletableFuture<ProgressionUpdateResult> completableFuture = new CompletableFuture<>();

        var task = tasks.iterator().next();
        var res = task.incrementProgression(progression, player, 0, giveRewards);
        runSync(res, getAdvancementTab().getOwningPlugin(), (result, err) -> {
            if (err != null) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while incrementing the progression of multi-task " + key, err);
                completableFuture.completeExceptionally(err);
                return; // Don't go on in case of a db error
            }

            int progr = 0;
            try {
                progr = getProgression(progression);
            } catch (Exception e) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while processing the result of incrementProgression of multi-task " + key, e);
                completableFuture.completeExceptionally(e);
            } finally {
                completableFuture.complete(new ProgressionUpdateResult(progr, progr));
            }
        });

        return completableFuture;
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to set any
     * progression except {@code 0} or {@link MultiTasksAdvancement#maxProgression}. Setting any progression other than
     * those will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided new progression is not {@code 0} or {@link MultiTasksAdvancement#maxProgression}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        return super.setProgression(player, progression);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to set any
     * progression except {@code 0} or {@link MultiTasksAdvancement#maxProgression}. Setting any progression other than
     * those will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided new progression is not {@code 0} or {@link MultiTasksAdvancement#maxProgression}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean giveReward) {
        return super.setProgression(player, progression, giveReward);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to set any
     * progression except {@code 0} or {@link MultiTasksAdvancement#maxProgression}. Setting any progression other than
     * those will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided new progression is not {@code 0} or {@link MultiTasksAdvancement#maxProgression}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        return super.setProgression(uuid, progression);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to set any
     * progression except {@code 0} or {@link MultiTasksAdvancement#maxProgression}. Setting any progression other than
     * those will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided new progression is not {@code 0} or {@link MultiTasksAdvancement#maxProgression}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean giveReward) {
        return super.setProgression(uuid, progression, giveReward);
    }

    /**
     * {@inheritDoc}
     * Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s, it is not possible to set any
     * progression except {@code 0} or {@link MultiTasksAdvancement#maxProgression}. Setting any progression other than
     * those will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided new progression is not {@code 0} or {@link MultiTasksAdvancement#maxProgression}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    protected CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull TeamProgression progression, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression, boolean giveRewards) throws ArbitraryMultiTaskProgressionUpdateException {
        checkInitialisation();
        validateTeamProgression(progression);
        validateProgressionValueStrict(newProgression, maxProgression);

        @SuppressWarnings("unchecked")
        final CompletableFuture<ProgressionUpdateResult>[] results = new CompletableFuture[this.tasks.size()];
        int i = 0;

        if (newProgression == maxProgression) {
            for (TaskAdvancement t : tasks) {
                results[i++] = t.setProgression(progression, player, t.getMaxProgression(), giveRewards);
            }
        } else if (newProgression == 0) {
            for (TaskAdvancement t : tasks) {
                results[i++] = t.setProgression(progression, player, 0, giveRewards);
            }
        } else {
            throw new ArbitraryMultiTaskProgressionUpdateException();
        }

        CompletableFuture<ProgressionUpdateResult> completableFuture = new CompletableFuture<>();

        CompletableFuture.allOf(results).whenComplete((result, err) -> {
            if (err != null) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while setting the progression of multi-task " + key, err);
                completableFuture.completeExceptionally(err);
                return; // Don't go on in case of a db error
            }

            int oldProgr = 0, newProgr = 0;
            try {
                for (CompletableFuture<ProgressionUpdateResult> res : results) {
                    // res is already completed
                    final ProgressionUpdateResult progr = res.get(0, TimeUnit.SECONDS);
                    oldProgr += progr.oldProgression();
                    newProgr += progr.newProgression();
                }
            } catch (Exception e) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while processing the result of setProgression of multi-task " + key, e);
                completableFuture.completeExceptionally(e);
            } finally {
                completableFuture.complete(new ProgressionUpdateResult(oldProgr, newProgr));
            }
        });

        return completableFuture;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    protected void onTaskProgressionChange(@NotNull Advancement task, @NotNull TeamProgression progression, @Nullable Player player, @NotNull ProgressionUpdateResult result, int multiTaskProgression, boolean giveRewards) {
        checkInitialisation();
        AdvancementUtils.checkSync();
        validateTeamProgression(progression);
        Preconditions.checkNotNull(task, "Task is null");
        Preconditions.checkNotNull(result, "ProgressionUpdateResult is null");
        Preconditions.checkArgument(this.tasksKeys.contains(task.getKey()), task.getKey() + " is not a TaskAdvancement of this MultiTaskAdvancement (" + this.getKey() + ')');
        validateProgressionValueStrict(multiTaskProgression, this.maxProgression);
        validateProgressionValueStrict(result.newProgression(), task.getMaxProgression());
        validateProgressionValueStrict(result.oldProgression(), task.getMaxProgression());

        int oldMultiTaskProgression = multiTaskProgression - (result.newProgression() - result.oldProgression());
        validateProgressionValueStrict(oldMultiTaskProgression, this.maxProgression);

        try {
            Bukkit.getPluginManager().callEvent(new AdvancementProgressionUpdateEvent(this, progression, oldMultiTaskProgression, multiTaskProgression));
        } catch (Exception e) {
            advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while calling AdvancementProgressionUpdateEvent for " + key, e);
        }

        handleAdvancementGranting(progression, player, multiTaskProgression, oldMultiTaskProgression, giveRewards);
        handleAdvancementUpdatingToTeam(progression, multiTaskProgression, oldMultiTaskProgression);
    }

    /**
     * Resets the progression cache for every team.
     */
    public void resetProgressionCache() {
        progressionsCache.clear();
    }

    /**
     * Resets the progression cache for the provided team.
     *
     * @param pro The team to remove from the cache.
     */
    public void resetProgressionCache(@NotNull TeamProgression pro) {
        validateTeamProgression(pro);
        progressionsCache.remove(pro.getTeamId());
    }

    private void checkInitialisation() {
        if (!initialised) {
            throw new IllegalStateException("MultiTasksAdvancement hasn't been initialised yet.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateRegister() throws InvalidAdvancementException {
        if (!initialised) {
            throw new InvalidAdvancementException("MultiTasksAdvancement hasn't been initialised yet.");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public void onDispose() {
        checkInitialisation();
        for (TaskAdvancement t : tasks) {
            t.onDispose();
        }
        progressionsCache.clear();
        super.onDispose();
    }

    /**
     * Returns whether the multi-task advancement is initialised.
     *
     * @return Whether the multi-task advancement is initialised.
     */
    public boolean isInitialised() {
        return initialised;
    }
}
