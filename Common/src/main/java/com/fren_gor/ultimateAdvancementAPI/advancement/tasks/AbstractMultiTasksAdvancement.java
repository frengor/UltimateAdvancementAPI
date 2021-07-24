package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromUUID;

public abstract class AbstractMultiTasksAdvancement extends BaseAdvancement {

    public AbstractMultiTasksAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent) {
        super(key, display, parent);
    }

    public AbstractMultiTasksAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(key, display, parent, maxCriteria);
    }

    public void reloadTasks(@NotNull Player player) {
        reloadTasks(player, true);
    }

    public void reloadTasks(@NotNull UUID uuid) {
        reloadTasks(uuid, true);
    }

    public void reloadTasks(@NotNull Player player, boolean giveRewards) {
        reloadTasks(progressionFromPlayer(player, this), player, giveRewards);
    }

    public void reloadTasks(@NotNull UUID uuid, boolean giveRewards) {
        reloadTasks(progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), giveRewards);
    }

    protected abstract void reloadTasks(@NotNull TeamProgression progression, @Nullable Player player, boolean giveRewards);

}
