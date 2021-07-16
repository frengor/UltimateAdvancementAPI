package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;

public abstract class AbstractMultiTasksAdvancement extends BaseAdvancement {

    public AbstractMultiTasksAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent) {
        super(advancementTab, key, display, parent);
    }

    public AbstractMultiTasksAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(advancementTab, key, display, parent, maxCriteria);
    }

    public void reloadTasks(@NotNull Player player) {
        reloadTasks(player, true);
    }

    public void reloadTasks(@NotNull UUID uuid) {
        reloadTasks(uuid, true);
    }

    public void reloadTasks(@NotNull Player player, boolean giveRewards) {
        reloadTasks(uuidFromPlayer(player), player, giveRewards);
    }

    public void reloadTasks(@NotNull UUID uuid, boolean giveRewards) {
        reloadTasks(uuid, Bukkit.getPlayer(uuid), giveRewards);
    }

    protected abstract void reloadTasks(@NotNull UUID uuid, @Nullable Player player, boolean giveRewards);

}
