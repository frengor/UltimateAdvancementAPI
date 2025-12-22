package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.ProgressionUpdateResult;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromUUID;

/**
 * The {@code AbstractMultiTasksAdvancement} class abstracts the implementation of any multi-tasks advancements,
 * providing a standard supported by the API.
 * <p>A multi-task advancement is an advancement that separates its progression into different progressions (one per task).
 * <p>An implementation of a task is {@link TaskAdvancement}, which works with any {@code AbstractMultiTasksAdvancement},
 * whereas an implementation of {@code AbstractMultiTasksAdvancement} is {@link MultiTasksAdvancement}.
 */
public abstract class AbstractMultiTasksAdvancement extends BaseAdvancement {

    /**
     * Creates an {@code AbstractMultiTasksAdvancement} with maximum progression of {@code 1}.
     *
     * @param parent The parent of this advancement.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param display The display information of this advancement.
     */
    public AbstractMultiTasksAdvancement(@NotNull Advancement parent, @NotNull String key, @NotNull AbstractAdvancementDisplay display) {
        this(parent, key, 1, display);
    }

    /**
     * Creates an {@code AbstractMultiTasksAdvancement}.
     *
     * @param parent The parent of this advancement.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param maxProgression The maximum progression of the task.
     * @param display The display information of this advancement.
     */
    public AbstractMultiTasksAdvancement(@NotNull Advancement parent, @NotNull String key, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression, @NotNull AbstractAdvancementDisplay display) {
        super(parent, key, maxProgression, display);
    }

    /**
     * Updates the provided team members after the progression of a task changes.
     * <p>This method should be invoked by tasks when their progression changes.
     * <p>If the advancement gets completed, advancement rewards will be given.
     *
     * @param task The task which progression changed.
     * @param player The player responsible for the task's progression change.
     * @param result The result of the progression update.
     * @param multiTaskProgression The progression of this multi-task advancement (i.e. this advancement) at the time of
     *         the update (calculated using the new progression of the task).
     */
    public void onTaskProgressionChange(@NotNull Advancement task, @NotNull Player player, @NotNull ProgressionUpdateResult result, int multiTaskProgression) {
        onTaskProgressionChange(task, player, result, multiTaskProgression, true);
    }

    /**
     * Updates the provided team members after the progression of a task changes.
     * <p>This method should be invoked by tasks when their progression changes.
     * <p>If the advancement gets completed, advancement rewards will be given.
     *
     * @param task The task which progression changed.
     * @param uuid The {@link UUID} of the player responsible for the task's progression change.
     * @param result The result of the progression update.
     * @param multiTaskProgression The progression of this multi-task advancement (i.e. this advancement) at the time of
     *         the update (calculated using the new progression of the task).
     */
    public void onTaskProgressionChange(@NotNull Advancement task, @NotNull UUID uuid, @NotNull ProgressionUpdateResult result, int multiTaskProgression) {
        onTaskProgressionChange(task, uuid, result, multiTaskProgression, true);
    }

    /**
     * Updates the provided team members after the progression of a task changes.
     * <p>This method should be invoked by tasks when their progression changes.
     *
     * @param task The task which progression changed.
     * @param player The player responsible for the task's progression change.
     * @param result The result of the progression update.
     * @param multiTaskProgression The progression of this multi-task advancement (i.e. this advancement) at the time of
     *         the update (calculated using the new progression of the task).
     * @param giveRewards Whether to give the player the advancement rewards if the advancement gets completed.
     */
    public void onTaskProgressionChange(@NotNull Advancement task, @NotNull Player player, @NotNull ProgressionUpdateResult result, int multiTaskProgression, boolean giveRewards) {
        onTaskProgressionChange(task, progressionFromPlayer(player, this), player, result, multiTaskProgression, giveRewards);
    }

    /**
     * Updates the provided team members after the progression of a task changes.
     * <p>This method should be invoked by tasks when their progression changes.
     *
     * @param task The task which progression changed.
     * @param uuid The {@link UUID} of the player responsible for the task's progression change.
     * @param result The result of the progression update.
     * @param multiTaskProgression The progression of this multi-task advancement at the time of
     *         the update (calculated using the new progression of the task).
     * @param giveRewards Whether to give the player the advancement rewards if the advancement gets completed.
     */
    public void onTaskProgressionChange(@NotNull Advancement task, @NotNull UUID uuid, @NotNull ProgressionUpdateResult result, int multiTaskProgression, boolean giveRewards) {
        onTaskProgressionChange(task, progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), result, multiTaskProgression, giveRewards);
    }

    /**
     * Updates the provided team members after the progression of a task changes.
     * <p>This method should be invoked by tasks when their progression changes.
     *
     * @param task The task which progression changed.
     * @param progression The {@link TeamProgression} of the team.
     * @param player The player responsible for the task's progression change, or {@code null} if there's not.
     *         In this case, the implementation can choose a random online team member.
     * @param result The result of the progression update.
     * @param multiTaskProgression The progression of this multi-task advancement (i.e. this advancement) at the time of
     *         the update (calculated using the new progression of the task).
     * @param giveRewards Whether to give the player the advancement rewards if the advancement gets completed.
     */
    protected abstract void onTaskProgressionChange(@NotNull Advancement task, @NotNull TeamProgression progression, @Nullable Player player, @NotNull ProgressionUpdateResult result, int multiTaskProgression, boolean giveRewards);
}
