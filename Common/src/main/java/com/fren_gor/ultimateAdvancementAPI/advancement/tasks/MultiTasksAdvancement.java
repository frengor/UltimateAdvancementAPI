package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.ArbitraryMultiTaskProgressionUpdateException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.util.AfterHandle;
import com.google.common.collect.Sets;
import org.apache.commons.lang.Validate;
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

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateProgressionValueStrict;

/**
 * An implementation of {@link AbstractMultiTasksAdvancement}. {@link TaskAdvancement}s have to be registered
 * using {@link #registerTasks(Set)} in order to initialise an instance of this class.
 */
public class MultiTasksAdvancement extends AbstractMultiTasksAdvancement {

    /**
     * Whether to enable arbitrary progression updates in {@link MultiTasksAdvancement#setProgression(TeamProgression, Player, int, boolean)}.
     *
     * @see MultiTasksAdvancement#setProgression(TeamProgression, Player, int, boolean)
     */
    protected boolean ENABLE_ARBITRARY_SET_PROGRESSION = false;

    /**
     * Whether to disable {@link ArbitraryMultiTaskProgressionUpdateException} in {@link MultiTasksAdvancement#setProgression(TeamProgression, Player, int, boolean)}.
     * <p>Ignored when {@link #ENABLE_ARBITRARY_SET_PROGRESSION} is set to {@code true}.
     *
     * @see MultiTasksAdvancement#setProgression(TeamProgression, Player, int, boolean)
     */
    protected boolean DISABLE_EXCEPTION_ON_ARBITRARY_SET_PROGRESSION = false;

    /**
     * The tasks of this advancement.
     */
    protected final Set<TaskAdvancement> tasks = new HashSet<>();

    /**
     * The cache for the team's progressions (the key is the team unique id).
     */
    protected final Map<Integer, Integer> progressionsCache = new HashMap<>();

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
        Validate.notNull(tasks, "Set<TaskAdvancement> is null.");
        int progression = 0;
        for (TaskAdvancement t : tasks) {
            if (t == null) {
                throw new IllegalArgumentException("A TaskAdvancement is null.");
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
        registerEvent(TeamUnloadEvent.class, e -> progressionsCache.remove(e.getTeamProgression().getTeamId()));
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
        Validate.notNull(progression, "TeamProgression is null.");
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

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    public boolean isGranted(@NotNull TeamProgression pro) {
        checkInitialisation();
        Validate.notNull(pro, "TeamProgression is null.");
        return getProgression(pro) >= maxProgression;
    }

    /**
     * Sets a progression for the provided player's team. Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s,
     * it is not possible (by default) to set any progression, but {@code 0} or {@link MultiTasksAdvancement#maxProgression}. Setting any progression between those will result in an {@link ArbitraryMultiTaskProgressionUpdateException}.
     * <p>
     * To enable arbitrary progression updates, set {@link MultiTasksAdvancement#ENABLE_ARBITRARY_SET_PROGRESSION} to {@code true} ({@code false} by default).
     * To prevent the throwing of {@link ArbitraryMultiTaskProgressionUpdateException}s set {@link MultiTasksAdvancement#DISABLE_EXCEPTION_ON_ARBITRARY_SET_PROGRESSION} to {@code true}.
     * </p>
     *
     * @param progression The {@link TeamProgression} of the player.
     * @param player The player, {@code null} if it's not online. (Note: it must have been loaded into cache)
     * @param newProgression The new progression to set. Must be between 0 and {@link MultiTasksAdvancement#maxProgression}.
     * @param giveRewards Whether to give rewards to player if the team's progression reaches {@link MultiTasksAdvancement#maxProgression}.
     * @throws ArbitraryMultiTaskProgressionUpdateException When the provided new progression is not {@code 0} or {@link MultiTasksAdvancement#maxProgression} and either {@link MultiTasksAdvancement#ENABLE_ARBITRARY_SET_PROGRESSION} or {@link MultiTasksAdvancement#DISABLE_EXCEPTION_ON_ARBITRARY_SET_PROGRESSION} are not set to {@code true}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    protected void setProgression(@NotNull TeamProgression progression, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression, boolean giveRewards) throws ArbitraryMultiTaskProgressionUpdateException {
        checkInitialisation();
        Validate.notNull(progression, "TeamProgression is null.");
        validateProgressionValueStrict(newProgression, maxProgression);

        int current = getProgression(progression);
        if (current == newProgression) {
            return; // Unnecessary update
        }

        doReloads = false;
        try {
            if (newProgression == maxProgression) {
                for (TaskAdvancement t : tasks) {
                    t.setProgression(progression, player, t.getMaxProgression(), giveRewards);
                }
            } else if (newProgression == 0) {
                for (TaskAdvancement t : tasks) {
                    t.setProgression(progression, player, 0, giveRewards);
                }
            } else if (ENABLE_ARBITRARY_SET_PROGRESSION) {
                if (newProgression < current) {
                    for (TaskAdvancement t : tasks) {
                        int tc = t.getProgression(progression);
                        if (current - tc > newProgression) {
                            t.setProgression(progression, player, 0, false);
                        } else if (current - tc <= newProgression) {
                            t.setProgression(progression, player, tc + newProgression - current, false);
                            break;
                        }
                    }
                } else /*if (newProgression > current)*/ {
                    for (TaskAdvancement t : tasks) {
                        int ta = t.getProgression(progression);
                        int tc = t.getMaxProgression() - ta;
                        if (current + tc < newProgression) {
                            t.setProgression(progression, player, t.getMaxProgression(), giveRewards);
                        } else if (current + tc >= newProgression) {
                            t.setProgression(progression, player, ta + newProgression - current, giveRewards);
                            break;
                        }
                    }
                }
            } else {
                if (DISABLE_EXCEPTION_ON_ARBITRARY_SET_PROGRESSION)
                    return;
                throw new ArbitraryMultiTaskProgressionUpdateException();
            }
        } finally {
            doReloads = true;
        }
        updateProgressionCache(progression, newProgression);

        handlePlayer(progression, player, newProgression, current, giveRewards, AfterHandle.UPDATE_ADVANCEMENTS_TO_TEAM);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    protected void reloadTasks(@NotNull TeamProgression progression, @Nullable Player player, boolean giveRewards) {
        checkInitialisation();
        if (doReloads) { // Skip reloads when update comes from ourselves
            Validate.notNull(progression, "TeamProgression is null.");

            int current = getProgression(progression);
            resetProgressionCache(progression);

            // Update MultiTasksAdvancement to players since a task has been updated
            // Note that the return of getProgression should be changed from the previous call
            handlePlayer(progression, player, getProgression(progression), current, giveRewards, AfterHandle.UPDATE_ADVANCEMENTS_TO_TEAM);
        }
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
        Validate.notNull(pro, "TeamProgression is null.");
        progressionsCache.remove(pro.getTeamId());
    }

    /**
     * Updates the progression cache for the provided team.
     *
     * @param pro The team to update.
     * @param progression The new progression to store in cache.
     */
    protected void updateProgressionCache(@NotNull TeamProgression pro, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        Validate.notNull(pro, "TeamProgression is null.");
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
