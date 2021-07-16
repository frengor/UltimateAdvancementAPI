package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.ArbitraryMultiTaskCriteriaUpdateException;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_15_R1.AdvancementProgress;
import net.minecraft.server.v1_15_R1.MinecraftKey;
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
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementProgress;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateCriteriaStrict;

public class MultiTasksAdvancement extends AbstractMultiTaskAdvancement {

    /**
     * Whether to enable arbitrary criteria in {@link MultiTasksAdvancement#setCriteriaTeamProgression(UUID, Player, int, boolean)}.
     *
     * @see MultiTasksAdvancement#setCriteriaTeamProgression(UUID, Player, int, boolean)
     */
    public boolean ENABLE_ARBITRARY_SET_TEAM_CRITERIA = false;
    /**
     * Whether to disable {@link ArbitraryMultiTaskCriteriaUpdateException} in {@link MultiTasksAdvancement#setCriteriaTeamProgression(UUID, Player, int, boolean)}.
     *
     * @see MultiTasksAdvancement#setCriteriaTeamProgression(UUID, Player, int, boolean)
     */
    public boolean DISABLE_ERROR_ON_ARBITRARY_SET_TEAM_CRITERIA = false;

    protected final Set<TaskAdvancement> tasks = new HashSet<>();
    protected final Map<Integer, Integer> criteriaCache = new HashMap<>();
    protected boolean initialised = false, doReloads = true;

    public MultiTasksAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(advancementTab, key, display, parent, maxCriteria);
    }

    public void registerTasks(@NotNull TaskAdvancement... tasks) {
        registerTasks(Sets.newHashSet(Objects.requireNonNull(tasks)));
    }

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
            if (t.getMultitask() != this) {
                throw new IllegalArgumentException("TaskAdvancement parent (" + t.getMultitask().key + ") doesn't match with this MultiTaskAdvancement. (" + key + ").");
            }
            if (!advancementTab.isOwnedByThisTab(t)) {
                throw new IllegalArgumentException("TaskAdvancement " + t.key + " is not owned by this tab (" + advancementTab.getNamespace() + ").");
            }
            criteria += t.maxCriteria;
        }
        if (criteria != maxCriteria) {
            throw new IllegalArgumentException("Expected max criteria (" + maxCriteria + ") doesn't match the tasks' total one (" + criteria + ").");
        }
        this.tasks.addAll(tasks);
        registerEvent(TeamUnloadEvent.class, e -> {
            criteriaCache.remove(e.getTeam().getTeamId());
        });
        initialised = true;
    }

    @NotNull
    @UnmodifiableView
    public Set<@NotNull TaskAdvancement> getTasks() {
        checkInitialisation();
        return Collections.unmodifiableSet(tasks);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull UUID uuid) {
        checkInitialisation();
        Validate.notNull(uuid, "UUID is null.");
        TeamProgression progression = advancementTab.getDatabaseManager().getProgression(uuid);
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

    @Override
    public boolean isGranted(@NotNull UUID uuid) {
        checkInitialisation();
        Validate.notNull(uuid, "UUID is null.");
        return getTeamCriteria(uuid) >= maxCriteria;
    }

    /**
     * Sets a criteria for this advancement. Since {@link MultiTasksAdvancement} handles a set of {@link TaskAdvancement}s,
     * it is not possible (by default) to set any criteria, but {@code 0} or {@link MultiTasksAdvancement#maxCriteria}. Setting any criteria between these will result in an {@link ArbitraryMultiTaskCriteriaUpdateException}.
     * <p>
     * To enable arbitrary criteria, set {@link MultiTasksAdvancement#ENABLE_ARBITRARY_SET_TEAM_CRITERIA} to {@code true} ({@code false} by default).
     * To disable the {@link ArbitraryMultiTaskCriteriaUpdateException} set {@link MultiTasksAdvancement#DISABLE_ERROR_ON_ARBITRARY_SET_TEAM_CRITERIA} to {@code true}.
     * </p>
     *
     * @param uuid The uuid of the player.
     * @param player The player, null if it's not online. (Note: it must have been loaded into cache)
     * @param criteria The criteria to set. Must be between 0 and {@link MultiTasksAdvancement#maxCriteria}.
     * @param giveRewards Whether to give rewards to player if criteria reaches {@link MultiTasksAdvancement#maxCriteria}.
     * @throws ArbitraryMultiTaskCriteriaUpdateException When criteria is not {@code 0} or {@link MultiTasksAdvancement#maxCriteria} and either {@link MultiTasksAdvancement#ENABLE_ARBITRARY_SET_TEAM_CRITERIA} or {@link MultiTasksAdvancement#DISABLE_ERROR_ON_ARBITRARY_SET_TEAM_CRITERIA} are not set to {@code true}.
     */
    @Override
    protected void setCriteriaTeamProgression(@NotNull UUID uuid, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveRewards) throws ArbitraryMultiTaskCriteriaUpdateException {
        checkInitialisation();
        Validate.notNull(uuid, "UUID is null.");
        validateCriteriaStrict(criteria, maxCriteria);

        int current = getTeamCriteria(uuid);
        if (current == criteria) {
            return; // Unnecessary update
        }
        final TeamProgression progression = advancementTab.getDatabaseManager().getProgression(uuid);

        doReloads = false;
        try {
            if (criteria == maxCriteria) {
                for (TaskAdvancement t : tasks) {
                    t.setCriteriaTeamProgression(uuid, player, t.getMaxCriteria(), giveRewards);
                }
            } else if (criteria == 0) {
                for (TaskAdvancement t : tasks) {
                    t.setCriteriaTeamProgression(uuid, player, 0, giveRewards);
                }
            } else if (ENABLE_ARBITRARY_SET_TEAM_CRITERIA) {
                if (criteria < current) {
                    for (TaskAdvancement t : tasks) {
                        int tc = t.getTeamCriteria(uuid);
                        if (current - tc > criteria) {
                            t.setCriteriaTeamProgression(uuid, player, 0, false);
                        } else if (current - tc <= criteria) {
                            t.setCriteriaTeamProgression(uuid, player, tc + criteria - current, false);
                            break;
                        }
                    }
                } else /*if (criteria > current)*/ {
                    for (TaskAdvancement t : tasks) {
                        int ta = t.getTeamCriteria(uuid);
                        int tc = t.getMaxCriteria() - ta;
                        if (current + tc < criteria) {
                            t.setCriteriaTeamProgression(uuid, player, t.getMaxCriteria(), giveRewards);
                        } else if (current + tc >= criteria) {
                            t.setCriteriaTeamProgression(uuid, player, ta + criteria - current, giveRewards);
                            break;
                        }
                    }
                }
            } else {
                if (DISABLE_ERROR_ON_ARBITRARY_SET_TEAM_CRITERIA)
                    return;
                throw new ArbitraryMultiTaskCriteriaUpdateException();
            }
        } finally {
            doReloads = true;
        }
        updateTeamCriteriaCache(progression, criteria);

        handlePlayer(advancementTab.getDatabaseManager(), progression, uuid, player, criteria, current, giveRewards);
    }

    @Override
    public void reloadTasks(@NotNull UUID uuid, @Nullable Player player, boolean giveRewards) {
        if (doReloads) { // Skip reloads when update comes from ourselves
            Validate.notNull(uuid, "UUID is null.");

            int current = getTeamCriteria(uuid);
            TeamProgression pro = advancementTab.getDatabaseManager().getProgression(uuid);
            resetTeamCriteriaCache(pro);

            // Update MultiTasksAdvancement to players since a task has been updated
            handlePlayer(advancementTab.getDatabaseManager(), pro, uuid, player, getTeamCriteria(uuid), current, giveRewards);
        }
    }

    public void resetCriteriaCache() {
        criteriaCache.clear();
    }

    public void resetTeamCriteriaCache(@NotNull TeamProgression pro) {
        criteriaCache.remove(pro.getTeamId());
    }

    protected void updateTeamCriteriaCache(@NotNull TeamProgression pro, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        criteriaCache.put(pro.getTeamId(), criteria);
    }

    private void checkInitialisation() {
        if (!initialised) {
            throw new IllegalStateException("MultiTaskAdvancement hasn't been initialised yet.");
        }
    }

    @Override
    public void onUpdate(@NotNull UUID uuid, @NotNull Set<net.minecraft.server.v1_15_R1.Advancement> advancementList, @NotNull Map<MinecraftKey, AdvancementProgress> progresses, @NotNull TeamProgression teamProgression, @NotNull Set<MinecraftKey> added) {
        if (isVisible(uuid)) {
            net.minecraft.server.v1_15_R1.Advancement mcAdv = getMinecraftAdvancement();
            advancementList.add(mcAdv);
            MinecraftKey key = getMinecraftKey();
            added.add(key);
            progresses.put(key, getAdvancementProgress(mcAdv, getTeamCriteria(uuid)));
        }
    }

    @Override
    public void onDispose() {
        super.onDispose();
        for (TaskAdvancement t : tasks) {
            t.onDispose();
        }
    }
}
