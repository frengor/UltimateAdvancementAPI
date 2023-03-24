package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.ProgressionUpdateResult;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.ArbitraryMultiTaskProgressionUpdateException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DatabaseException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.util.AfterHandle;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.bukkit.entity.Player;
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
import java.util.concurrent.CompletableFuture;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateIncrement;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateProgressionValueStrict;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * An implementation of {@link AbstractMultiTasksAdvancement}. {@link TaskAdvancement}s have to be registered
 * using {@link #registerTasks(Set)} in order to initialise an instance of this class.
 */
public class MultiTasksAdvancement extends AbstractMultiTasksAdvancement {

    /**
     * The tasks of this advancement.
     */
    protected final Set<TaskAdvancement> tasks = new HashSet<>();

    /**
     * The cache for the team's progressions (the key is the team unique id).
     */
    protected final Map<Integer, Integer> progressionsCache = Collections.synchronizedMap(new HashMap<>());

    private boolean initialised = false, doReloads = true;

    /**
     * Creates a new {@code MultiTasksAdvancement}.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param parent The parent of this advancement.
     * @param maxProgression The sum of the maximum progressions of all the tasks that will be registered.
     */
    public MultiTasksAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        super(key, display, parent, maxProgression);
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
        registerEvent(AsyncTeamUnloadEvent.class, e -> progressionsCache.remove(e.getTeamProgression().getTeamId()));
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
        validateTeamProgression(pro);
        return getProgression(pro) >= maxProgression;
    }

    /**
     * Sets a progression for the provided player's team. Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s,
     * it is not possible (by default) to set any progression, but {@code 0} or {@link MultiTasksAdvancement#maxProgression}. Setting any progression between those will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @param progression The {@link TeamProgression} of the player.
     * @param player The player, {@code null} if it's not online. (Note: it must have been loaded into cache)
     * @param increment The new progression to set. Must be between 0 and {@link MultiTasksAdvancement#maxProgression}.
     * @param giveRewards Whether to give rewards to player if the team's progression reaches {@link MultiTasksAdvancement#maxProgression}.
     * @return
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided new progression is not {@code 0} or {@link MultiTasksAdvancement#maxProgression}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    protected CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull TeamProgression progression, @Nullable Player player, @Range(from = 0, to = 0) int increment, boolean giveRewards) throws ArbitraryMultiTaskProgressionUpdateException {
        checkInitialisation();
        validateTeamProgression(progression);
        validateIncrement(increment);

        if (increment != 0) {
            throw new ArbitraryMultiTaskProgressionUpdateException();
        }

        CompletableFuture<ProgressionUpdateResult> completableFuture = new CompletableFuture<>();

        var res = advancementTab.getDatabaseManager().incrementProgression(tasks.iterator().next().getKey(), progression, 0);
        runSync(res, getAdvancementTab().getOwningPlugin(), (result, err) -> {
            if (err != null) {
                completableFuture.completeExceptionally(new DatabaseException(new RuntimeException("An exception occurred while setting the progression of 1 or more tasks.")));
                return;
            }

            int progr = getProgression(progression);
            completableFuture.complete(new ProgressionUpdateResult(progr, progr));
        });

        return completableFuture;
    }

    /**
     * Sets a progression for the provided player's team. Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s,
     * it is not possible (by default) to set any progression, but {@code 0} or {@link MultiTasksAdvancement#maxProgression}. Setting any progression between those will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     *
     * @param progression The {@link TeamProgression} of the player.
     * @param player The player, {@code null} if it's not online. (Note: it must have been loaded into cache)
     * @param newProgression The new progression to set. Must be between 0 and {@link MultiTasksAdvancement#maxProgression}.
     * @param giveRewards Whether to give rewards to player if the team's progression reaches {@link MultiTasksAdvancement#maxProgression}.
     * @return
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided new progression is not {@code 0} or {@link MultiTasksAdvancement#maxProgression}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    protected CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull TeamProgression progression, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression, boolean giveRewards) throws ArbitraryMultiTaskProgressionUpdateException {
        checkInitialisation();
        validateTeamProgression(progression);
        validateProgressionValueStrict(newProgression, maxProgression);

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
                completableFuture.completeExceptionally(new DatabaseException(new RuntimeException("An exception occurred while setting the progression of 1 or more tasks.")));
                return;
            }

            int oldProgr = 0, newProgr = 0;
            try {
                for (CompletableFuture<ProgressionUpdateResult> res : results) {
                    final ProgressionUpdateResult progr = res.get();
                    oldProgr += progr.oldProgression();
                    newProgr += progr.newProgression();
                }
            } catch (Exception e) {
                e.printStackTrace();
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
    protected void reloadTasks(@NotNull TeamProgression progression, @Nullable Player player, @NotNull ProgressionUpdateResult result, boolean giveRewards) {
        checkInitialisation();
        Preconditions.checkNotNull(result, "ProgressionUpdateResult is null");
        validateTeamProgression(progression);

        resetProgressionCache(progression);
        int newProgression = getProgression(progression);
        int oldProgression = newProgression - result.oldProgression();

        // Update MultiTasksAdvancement to players since a task has been updated
        // Note that the return of getProgression should be changed from the previous call
        handlePlayer(progression, player, newProgression, oldProgression, giveRewards, AfterHandle.UPDATE_ADVANCEMENTS_TO_TEAM);
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

    /**
     * Updates the progression cache for the provided team.
     *
     * @param pro The team to update.
     * @param progression The new progression to store in cache.
     */
    protected void updateProgressionCache(@NotNull TeamProgression pro, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        validateTeamProgression(pro);
        validateProgressionValueStrict(progression, maxProgression);
        progressionsCache.put(pro.getTeamId(), progression);
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
