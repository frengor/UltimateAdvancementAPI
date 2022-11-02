package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager.ProgressionUpdateResult;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateProgressionValueStrict;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * The {@code TaskAdvancement} class represents a task. It can be used by any {@link AbstractMultiTasksAdvancement} subclass
 * to separate an advancement progression into different progression (one per task).
 * <p>For example, the advancement "mine 5 blocks of every plank" can be made using a {@code TaskAdvancement}
 * for every plank (with a progression of 5) and registering them into an {@link AbstractMultiTasksAdvancement}.
 * <p>{@code TaskAdvancement}s are saved into the database, but they are never sent to players.
 * For this reason they cannot be registered in tabs either.
 */
public class TaskAdvancement extends BaseAdvancement {

    /**
     * Creates a new {@code TaskAdvancement} with a maximum progression of {@code 1}.
     *
     * @param key The unique key of the task. It must be unique among the other advancements of the tab.
     * @param multitask The {@link AbstractMultiTasksAdvancement} that owns this task.
     */
    public TaskAdvancement(@NotNull String key, @NotNull AbstractMultiTasksAdvancement multitask) {
        this(key, multitask, 1);
    }

    /**
     * Creates a new {@code TaskAdvancement}.
     *
     * @param key The unique key of the task. It must be unique among the other advancements of the tab.
     * @param multitask The {@link AbstractMultiTasksAdvancement} that owns this task.
     * @param maxProgression The maximum progression of the task.
     */
    public TaskAdvancement(@NotNull String key, @NotNull AbstractMultiTasksAdvancement multitask, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        this(key, new AdvancementDisplay.Builder(Material.GRASS_BLOCK, Objects.requireNonNull(key, "Key is null.")).build(), multitask, maxProgression);
    }

    /**
     * Creates a new {@code TaskAdvancement} with a maximum progression of {@code 1}.
     *
     * @param key The unique key of the task. It must be unique among the other advancements of the tab.
     * @param display The display information of this task.
     * @param multitask The {@link AbstractMultiTasksAdvancement} that owns this task.
     */
    public TaskAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull AbstractMultiTasksAdvancement multitask) {
        this(key, display, multitask, 1);
    }

    /**
     * Creates a new {@code TaskAdvancement}.
     *
     * @param key The unique key of the task. It must be unique among the other advancements of the tab.
     * @param display The display information of this task.
     * @param multitask The {@link AbstractMultiTasksAdvancement} that owns this task.
     * @param maxProgression The maximum progression of the task.
     */
    public TaskAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull AbstractMultiTasksAdvancement multitask, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        super(key, display, Objects.requireNonNull(multitask, "AbstractMultiTasksAdvancement is null."), maxProgression);
    }

    /**
     * {@inheritDoc}
     * This method returns {@code null} by default.
     *
     * @return Always {@code null}.
     */
    @Override
    @Nullable
    @Contract(pure = true, value = "_ -> null")
    public final BaseComponent[] getAnnounceMessage(@NotNull Player player) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setProgression(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean giveRewards) {
        validateTeamProgression(pro);
        validateProgressionValueStrict(progression, maxProgression);

        final DatabaseManager ds = advancementTab.getDatabaseManager();
        ProgressionUpdateResult result = ds.setProgression(key, pro, progression);

        try {
            Bukkit.getPluginManager().callEvent(new AdvancementProgressionUpdateEvent(pro, result.oldProgression(), progression, this));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        handlePlayer(pro, player, progression, result.oldProgression(), giveRewards, null);
        getMultiTasksAdvancement().reloadTasks(pro, player, giveRewards);
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
     * <p>Since {@code TaskAdvancement}s are not sent to players, this method doesn't send toast notifications and chat messages.
     */
    @Override
    public void onGrant(@NotNull Player player, boolean giveRewards) {
        Preconditions.checkNotNull(player, "Player is null.");

        if (giveRewards)
            giveReward(player);
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
    public final AdvancementWrapper getNMSWrapper() {
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
    public void onUpdate(@NotNull TeamProgression teamProgression, @NotNull Map<AdvancementWrapper, Integer> addedAdvancements) {
        throw new UnsupportedOperationException();
    }
}
