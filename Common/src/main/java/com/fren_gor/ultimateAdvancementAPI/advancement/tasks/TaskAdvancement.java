package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementCriteriaUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_15_R1.AdvancementProgress;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateCriteriaStrict;

public class TaskAdvancement extends BaseAdvancement {

    public TaskAdvancement(@NotNull String key, @NotNull AbstractMultiTasksAdvancement multitask) {
        this(key, multitask, 1);
    }

    public TaskAdvancement(@NotNull String key, @NotNull AbstractMultiTasksAdvancement multitask, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        this(key, new AdvancementDisplay(Material.GRASS_BLOCK, Objects.requireNonNull(key, "Key is null."), AdvancementFrameType.TASK, false, false, 0, 0), multitask, maxCriteria);
    }

    public TaskAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull AbstractMultiTasksAdvancement multitask) {
        this(key, display, multitask, 1);
    }

    public TaskAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull AbstractMultiTasksAdvancement multitask, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(key, display, Objects.requireNonNull(multitask, "AbstractMultiTasksAdvancement is null."), maxCriteria);
    }

    @Override
    @Nullable
    @Contract(pure = true, value = "_ -> null")
    public final BaseComponent[] getAnnounceMessage(@NotNull Player player) {
        return null;
    }

    @Override
    protected void setCriteriaTeamProgression(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveRewards) {
        Validate.notNull(pro, "TeamProgression is null.");
        validateCriteriaStrict(criteria, maxCriteria);

        final DatabaseManager ds = advancementTab.getDatabaseManager();
        int old = ds.updateCriteria(key, pro, criteria);

        try {
            Bukkit.getPluginManager().callEvent(new AdvancementCriteriaUpdateEvent(old, criteria, this));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        handlePlayer(pro, player, criteria, old, giveRewards, null);
        getMultiTasksAdvancement().reloadTasks(pro, player, giveRewards);
    }

    @Override
    @Contract("_ -> false")
    public final boolean isVisible(@NotNull Player player) {
        return false;
    }

    @Override
    @Contract("_ -> false")
    public final boolean isVisible(@NotNull UUID uuid) {
        return false;
    }

    @Override
    @Contract("_ -> false")
    public final boolean isVisible(@NotNull TeamProgression progression) {
        return false;
    }

    @Override
    public void onGrant(@NotNull Player player, boolean giveRewards) {
        Validate.notNull(player, "Player is null.");

        if (giveRewards)
            giveReward(player);
    }

    @Override
    public boolean isValid() {
        return getMultiTasksAdvancement().isValid();
    }

    @NotNull
    public AbstractMultiTasksAdvancement getMultiTasksAdvancement() {
        return (AbstractMultiTasksAdvancement) parent;
    }

    @Override
    public void validateRegister() throws InvalidAdvancementException {
        // Always throw since Tasks cannot be registered in Tabs
        throw new InvalidAdvancementException("TaskAdvancements cannot be registered in any AdvancementTab.");
    }

    // ============ Overridden methods which throw an UnsupportedOperationException ============

    @Override
    @NotNull
    public final net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void displayToastToPlayer(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean isShownTo(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onUpdate(@NotNull TeamProgression pro, @NotNull Set<net.minecraft.server.v1_15_R1.Advancement> advancementList, @NotNull Map<MinecraftKey, AdvancementProgress> progresses, @NotNull Set<MinecraftKey> added) {
        throw new UnsupportedOperationException();
    }
}
