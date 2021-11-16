package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.ArbitraryMultiTaskCriteriaUpdateException;
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

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateCriteriaStrict;

/**
 * An implementation of {@link AbstractMultiTasksAdvancement}. {@link TaskAdvancement}s have to be registered
 * using {@link #registerTasks(Set)} in order to initialise an instance of this class.
 */
public class MultiTasksAdvancement extends AbstractMultiTasksAdvancement {

    /**
     * Whether to enable arbitrary criteria in {@link MultiTasksAdvancement#setCriteriaTeamProgression(TeamProgression, Player, int, boolean)}.
     *
     * @see MultiTasksAdvancement#setCriteriaTeamProgression(TeamProgression, Player, int, boolean)
     */
    public boolean ENABLE_ARBITRARY_SET_TEAM_CRITERIA = false;
    /**
     * Whether to disable {@link ArbitraryMultiTaskCriteriaUpdateException} in {@link MultiTasksAdvancement#setCriteriaTeamProgression(TeamProgression, Player, int, boolean)}.
     * <p>Ignored when {@link #ENABLE_ARBITRARY_SET_TEAM_CRITERIA} is set to {@code true}.
     *
     * @see MultiTasksAdvancement#setCriteriaTeamProgression(TeamProgression, Player, int, boolean)
     */
    public boolean DISABLE_EXCEPTION_ON_ARBITRARY_SET_TEAM_CRITERIA = false;

    /**
     * The tasks of this advancement.
     */
    protected final Set<TaskAdvancement> tasks = new HashSet<>();

    /**
     * The cache for the criteria per team (the key is the team unique id).
     */
    protected final Map<Integer, Integer> criteriaCache = new HashMap<>();

    private boolean initialised = false, doReloads = true;

    /**
     * Creates a new {@code MultiTasksAdvancement}.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param parent The parent of this advancement.
     * @param maxCriteria The sum of the maximum criteria of all the tasks that will be registered.
     */
    public MultiTasksAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(key, display, parent, maxCriteria);
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
        int criteria = 0;
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
            criteria += t.getMaxCriteria();
        }
        if (criteria != maxCriteria) {
            throw new IllegalArgumentException("Expected max criteria (" + maxCriteria + ") doesn't match the tasks' total one (" + criteria + ").");
        }
        this.tasks.addAll(tasks);
        registerEvent(TeamUnloadEvent.class, e -> criteriaCache.remove(e.getTeamProgression().getTeamId()));
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
    public int getTeamCriteria(@NotNull TeamProgression progression) {
        checkInitialisation();
        Validate.notNull(progression, "TeamProgression is null.");
        Integer criteria = criteriaCache.get(progression.getTeamId());
        if (criteria == null) {
            int c = 0;
            for (TaskAdvancement t : tasks) {
                c += progression.getCriteria(t);
            }
            criteriaCache.put(progression.getTeamId(), c);
            return c;
        } else {
            return criteria;
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
        return getTeamCriteria(pro) >= maxCriteria;
    }

    /**
     * Sets a criteria for this advancement. Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s,
     * it is not possible (by default) to set any criteria, but {@code 0} or {@link MultiTasksAdvancement#maxCriteria}. Setting any criteria between these will result in an {@link ArbitraryMultiTaskCriteriaUpdateException}.
     * <p>
     * To enable arbitrary criteria, set {@link MultiTasksAdvancement#ENABLE_ARBITRARY_SET_TEAM_CRITERIA} to {@code true} ({@code false} by default).
     * To disable the {@link ArbitraryMultiTaskCriteriaUpdateException} set {@link MultiTasksAdvancement#DISABLE_EXCEPTION_ON_ARBITRARY_SET_TEAM_CRITERIA} to {@code true}.
     * </p>
     *
     * @param progression The {@link TeamProgression} of the player.
     * @param player The player, null if it's not online. (Note: it must have been loaded into cache)
     * @param criteria The criteria to set. Must be between 0 and {@link MultiTasksAdvancement#maxCriteria}.
     * @param giveRewards Whether to give rewards to player if criteria reaches {@link MultiTasksAdvancement#maxCriteria}.
     * @throws ArbitraryMultiTaskCriteriaUpdateException When criteria is not {@code 0} or {@link MultiTasksAdvancement#maxCriteria} and either {@link MultiTasksAdvancement#ENABLE_ARBITRARY_SET_TEAM_CRITERIA} or {@link MultiTasksAdvancement#DISABLE_EXCEPTION_ON_ARBITRARY_SET_TEAM_CRITERIA} are not set to {@code true}.
     * @throws IllegalStateException If the multi-task advancement is not initialised.
     */
    @Override
    protected void setCriteriaTeamProgression(@NotNull TeamProgression progression, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveRewards) throws ArbitraryMultiTaskCriteriaUpdateException {
        checkInitialisation();
        Validate.notNull(progression, "TeamProgression is null.");
        validateCriteriaStrict(criteria, maxCriteria);

        int current = getTeamCriteria(progression);
        if (current == criteria) {
            return; // Unnecessary update
        }

        doReloads = false;
        try {
            if (criteria == maxCriteria) {
                for (TaskAdvancement t : tasks) {
                    t.setCriteriaTeamProgression(progression, player, t.getMaxCriteria(), giveRewards);
                }
            } else if (criteria == 0) {
                for (TaskAdvancement t : tasks) {
                    t.setCriteriaTeamProgression(progression, player, 0, giveRewards);
                }
            } else if (ENABLE_ARBITRARY_SET_TEAM_CRITERIA) {
                if (criteria < current) {
                    for (TaskAdvancement t : tasks) {
                        int tc = t.getTeamCriteria(progression);
                        if (current - tc > criteria) {
                            t.setCriteriaTeamProgression(progression, player, 0, false);
                        } else if (current - tc <= criteria) {
                            t.setCriteriaTeamProgression(progression, player, tc + criteria - current, false);
                            break;
                        }
                    }
                } else /*if (criteria > current)*/ {
                    for (TaskAdvancement t : tasks) {
                        int ta = t.getTeamCriteria(progression);
                        int tc = t.getMaxCriteria() - ta;
                        if (current + tc < criteria) {
                            t.setCriteriaTeamProgression(progression, player, t.getMaxCriteria(), giveRewards);
                        } else if (current + tc >= criteria) {
                            t.setCriteriaTeamProgression(progression, player, ta + criteria - current, giveRewards);
                            break;
                        }
                    }
                }
            } else {
                if (DISABLE_EXCEPTION_ON_ARBITRARY_SET_TEAM_CRITERIA)
                    return;
                throw new ArbitraryMultiTaskCriteriaUpdateException();
            }
        } finally {
            doReloads = true;
        }
        updateTeamCriteriaCache(progression, criteria);

        handlePlayer(progression, player, criteria, current, giveRewards, AfterHandle.UPDATE_ADVANCEMENTS_TO_TEAM);
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

            int current = getTeamCriteria(progression);
            resetTeamCriteriaCache(progression);

            // Update MultiTasksAdvancement to players since a task has been updated
            // Note that the return of getTeamCriteria should be changed from the previous call
            handlePlayer(progression, player, getTeamCriteria(progression), current, giveRewards, AfterHandle.UPDATE_ADVANCEMENTS_TO_TEAM);
        }
    }

    /**
     * Resets the team criteria cache for every team.
     */
    public void resetCriteriaCache() {
        criteriaCache.clear();
    }

    /**
     * Resets the team criteria cache for the provided team.
     *
     * @param pro The team to remove from the cache.
     */
    public void resetTeamCriteriaCache(@NotNull TeamProgression pro) {
        Validate.notNull(pro, "TeamProgression is null.");
        criteriaCache.remove(pro.getTeamId());
    }

    /**
     * Updates the team criteria cache for the provided team.
     *
     * @param pro The team to update.
     * @param criteria The new criteria to store in cache.
     */
    protected void updateTeamCriteriaCache(@NotNull TeamProgression pro, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        Validate.notNull(pro, "TeamProgression is null.");
        validateCriteriaStrict(criteria, maxCriteria);
        criteriaCache.put(pro.getTeamId(), criteria);
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
