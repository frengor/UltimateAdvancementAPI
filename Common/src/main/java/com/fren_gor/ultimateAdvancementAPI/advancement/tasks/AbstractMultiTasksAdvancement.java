package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.ProgressionUpdateResult;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromUUID;

/**
 * The {@code AbstractMultiTasksAdvancement} class abstracts the implementation of any multi-tasks advancement,
 * providing a standard supported by the API.
 * <p>A multi-task advancement is an advancement that separates its progression into different progressions (one per task).
 * <p>An implementation of a task is {@link TaskAdvancement}, which works with any {@code AbstractMultiTasksAdvancement},
 * whereas an implementation of {@code AbstractMultiTasksAdvancement} is {@link MultiTasksAdvancement}.
 */
public abstract class AbstractMultiTasksAdvancement extends BaseAdvancement {

    /**
     * Creates an {@code AbstractMultiTasksAdvancement} with maximum progression of {@code 1}.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param parent The parent of this advancement.
     */
    public AbstractMultiTasksAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent) {
        super(key, display, parent);
    }

    /**
     * Creates an {@code AbstractMultiTasksAdvancement}.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param parent The parent of this advancement.
     * @param maxProgression The maximum progression of the task.
     */
    public AbstractMultiTasksAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        super(key, display, parent, maxProgression);
    }

    /**
     * Reloads and updates the tasks to the provided player's team members.
     * <p>This method should be invoked when the progression of a task changes.
     * <p>If the advancement gets completed, advancement rewards will be given.
     *
     * @param player The player responsible for the task's progression update.
     */
    public void reloadTasks(@NotNull Player player, @NotNull ProgressionUpdateResult result) {
        reloadTasks(player, result, true);
    }

    /**
     * Reloads and updates the tasks to the provided player's team members.
     * <p>This method should be invoked when the progression of a task changes.
     * <p>If the advancement gets completed, advancement rewards will be given.
     *
     * @param uuid The {@link UUID} of the player responsible for the task's progression update.
     */
    public void reloadTasks(@NotNull UUID uuid, @NotNull ProgressionUpdateResult result) {
        reloadTasks(uuid, result, true);
    }

    /**
     * Reloads and updates the tasks to the provided player's team members.
     * <p>This method should be invoked when the progression of a task changes.
     *
     * @param player The player responsible for the task's progression update.
     * @param giveRewards Whether to give the player the advancement rewards if the advancement gets completed.
     */
    public void reloadTasks(@NotNull Player player, @NotNull ProgressionUpdateResult result, boolean giveRewards) {
        reloadTasks(progressionFromPlayer(player, this), player, result, giveRewards);
    }

    /**
     * Reloads and updates the tasks to the provided player's team members.
     * <p>This method should be invoked when the progression of a task changes.
     *
     * @param uuid The {@link UUID} of the player responsible for the task's progression update.
     * @param giveRewards Whether to give the player the advancement rewards if the advancement gets completed.
     */
    public void reloadTasks(@NotNull UUID uuid, @NotNull ProgressionUpdateResult result, boolean giveRewards) {
        reloadTasks(progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), result, giveRewards);
    }

    /**
     * Reloads and updates the tasks to the provided team members.
     * <p>This method should be invoked when the progression of a task changes.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @param player The player responsible for the task's progression update, or {@code null} if there's not.
     *         In this case, the implementation can choose a random online member.
     */
    protected abstract void reloadTasks(@NotNull TeamProgression progression, @Nullable Player player, @NotNull ProgressionUpdateResult result, boolean giveRewards);
}
