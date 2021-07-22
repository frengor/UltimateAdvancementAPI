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
    @NotNull
    @Contract(pure = true, value = "-> fail")
    public final net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nullable
    @Contract(pure = true, value = "_ -> null")
    public final BaseComponent[] getAnnounceMessage(@NotNull Player player) {
        return null;
    }

    @Override
    protected void setCriteriaTeamProgression(@NotNull UUID uuid, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveRewards) {
        validateCriteriaStrict(criteria, maxCriteria);
        Validate.notNull(uuid, "UUID is null.");

        final DatabaseManager ds = advancementTab.getDatabaseManager();
        final TeamProgression pro = ds.getProgression(uuid);
        int old = ds.updateCriteria(key, pro, criteria);

        try {
            Bukkit.getPluginManager().callEvent(new AdvancementCriteriaUpdateEvent(old, criteria, this));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        handlePlayer(ds, pro, uuid, player, criteria, old, giveRewards);
        getMultiTasksAdvancement().reloadTasks(uuid, player, giveRewards);
    }

    protected void handlePlayer(@NotNull DatabaseManager ds, @Nullable TeamProgression pro, @NotNull UUID uuid, @Nullable Player player, int criteria, int old, boolean giveRewards) {
        if (criteria == this.maxCriteria && old < this.maxCriteria)
            if (player != null) {
                onGrant(player, giveRewards);
            } else {
                if (pro == null) {
                    pro = ds.getProgression(uuid);
                }
                Player p = pro.getAnOnlineMember(ds);
                if (p != null) {
                    onGrant(p, giveRewards);
                } else {
                    ds.setUnredeemed(key, giveRewards, pro);
                }
            }
    }

    @Override
    @Contract(pure = true, value = "_ -> fail")
    public final void displayToastToPlayer(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Contract(pure = true, value = "_ -> fail")
    public final boolean isShownTo(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void grant(@NotNull Player player, boolean giveRewards) {
        super.grant(player, giveRewards);
    }

    @Override
    public final void revoke(@NotNull Player player) {
        super.revoke(player);
    }

    @Override
    @Contract(pure = true, value = "_, _, _, _, _ -> fail")
    public void onUpdate(@NotNull UUID uuid, @NotNull Set<net.minecraft.server.v1_15_R1.Advancement> advancementList, @NotNull Map<MinecraftKey, AdvancementProgress> progresses, @NotNull TeamProgression teamProgression, @NotNull Set<MinecraftKey> added) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onGrant(@NotNull Player player, boolean giveRewards) {
        Validate.notNull(player, "Player is null.");

        if (giveRewards)
            giveReward(player);
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
}