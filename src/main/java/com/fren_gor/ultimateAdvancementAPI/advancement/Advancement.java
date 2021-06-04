package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementCriteriaUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.visibilities.IVisibility;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_15_R1.AdvancementProgress;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementProgress;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateCriteriaStrict;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateIncrement;

public abstract class Advancement {

    @NotNull
    protected final AdvancementKey key;
    @NotNull
    protected final AdvancementTab advancementTab;
    @Getter
    @NotNull
    protected final AdvancementDisplay display;

    @Range(from = 0, to = Integer.MAX_VALUE)
    protected final int maxCriteria;

    private Advancement() {
        throw new UnsupportedOperationException();
    }

    Advancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display) {
        this(advancementTab, key, display, 1);
    }

    Advancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        Validate.isTrue(maxCriteria > 0, "Max criteria cannot be <= 0");
        this.key = new AdvancementKey(advancementTab.getNamespace(), key);
        this.advancementTab = Objects.requireNonNull(advancementTab);
        this.display = Objects.requireNonNull(display);
        this.maxCriteria = maxCriteria;
    }

    @NotNull
    public final AdvancementKey getKey() {
        return key;
    }

    @NotNull
    public final AdvancementTab getAdvancementTab() {
        return advancementTab;
    }

    @NotNull
    public final MinecraftKey getMinecraftKey() {
        return key.toMinecraftKey();
    }

    @Range(from = 1, to = Integer.MAX_VALUE)
    public int getMaxCriteria() {
        return maxCriteria;
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull Player player) {
        return getTeamCriteria(uuidFromPlayer(player));
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull UUID uuid) {
        return advancementTab.getDatabaseManager().getProgression(uuid).getCriteria(this);
    }

    public boolean isGranted(@NotNull Player player) {
        return isGranted(uuidFromPlayer(player));
    }

    public boolean isGranted(@NotNull UUID uuid) {
        Validate.notNull(uuid, "UUID is null.");
        return getTeamCriteria(uuid) >= maxCriteria;
    }

    /**
     * Get the message to be displayed in chat when an advancement is made.<br>
     * The message is sent to everybody.
     *
     * @param player The player who's made the advancement.
     * @return The message to be displayed.
     */
    public abstract @Nullable BaseComponent[] getAnnounceMessage(@NotNull Player player);

    /**
     * Should craft the NMS advancement once and returns it henceforth.
     *
     * @return The NMS advancement representing this advancement.
     */
    public abstract @NotNull net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement();

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid) {
        return incrementTeamCriteria(uuid, true);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, boolean giveReward) {
        return incrementTeamCriteria(uuid, Bukkit.getPlayer(Objects.requireNonNull(uuid)), 1, giveReward);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int increment) {
        return incrementTeamCriteria(uuid, increment, true);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        return incrementTeamCriteria(uuid, Bukkit.getPlayer(uuid), increment, giveReward);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player) {
        return incrementTeamCriteria(player, true);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, boolean giveReward) {
        return incrementTeamCriteria(uuidFromPlayer(player), player, 1, giveReward);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment) {
        return incrementTeamCriteria(player, increment, true);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        return incrementTeamCriteria(uuidFromPlayer(player), player, increment, giveReward);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int incrementTeamCriteria(@NotNull UUID uuid, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveRewards) {
        validateIncrement(increment);
        Validate.notNull(uuid, "UUID is null.");

        int crit = getTeamCriteria(uuid);
        if (crit >= maxCriteria) {
            return crit;
        }
        int newCriteria = crit + increment;
        if (newCriteria > maxCriteria) {
            newCriteria = maxCriteria;
        }
        setCriteriaTeamProgression(uuid, player, newCriteria, giveRewards);
        return newCriteria;
    }

    public void setCriteriaTeamProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        setCriteriaTeamProgression(uuid, criteria, true);
    }

    public void setCriteriaTeamProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveReward) {
        setCriteriaTeamProgression(uuid, Bukkit.getPlayer(Objects.requireNonNull(uuid)), criteria, giveReward);
    }

    public void setCriteriaTeamProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        setCriteriaTeamProgression(player, criteria, true);
    }

    public void setCriteriaTeamProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveReward) {
        setCriteriaTeamProgression(uuidFromPlayer(player), player, criteria, giveReward);
    }

    protected void setCriteriaTeamProgression(@NotNull UUID uuid, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveRewards) {
        validateCriteriaStrict(criteria, maxCriteria);
        Validate.notNull(uuid, "UUID is null.");

        final DatabaseManager ds = advancementTab.getDatabaseManager();
        TeamProgression pro = ds.getProgression(uuid);
        int old = ds.updateCriteria(key, pro, criteria);

        try {
            Bukkit.getPluginManager().callEvent(new AdvancementCriteriaUpdateEvent(old, criteria, this));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        handlePlayer(ds, pro, uuid, player, criteria, old, giveRewards);
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
                    return; // Skip advancement update, no player is online
                }
            }
        advancementTab.updateAdvancementsToTeam(uuid);
    }

    public void displayToastToPlayer(@NotNull Player player) {
        AdvancementUtils.displayToast(player, display.getIcon(), display.getTitle(), display.getFrame());
    }

    public boolean isVisible(@NotNull Player player) {
        return isVisible(uuidFromPlayer(player));
    }

    public boolean isVisible(@NotNull UUID uuid) {
        if (this instanceof IVisibility) {
            return ((IVisibility) this).isAdvancementVisible(uuid);
        }
        return true;
    }

    @Contract(pure = true, value = "null -> false")
    public boolean isShownTo(Player player) {
        return advancementTab.isShownTo(player);
    }

    public void onGrant(@NotNull Player player, boolean giveRewards) {
        Validate.notNull(player, "Player is null.");

        // Send complete messages
        if (display.doesAnnounceToChat()) {
            BaseComponent[] msg = getAnnounceMessage(player);
            if (msg != null)
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(msg);
                }
        }

        // Show Toast
        if (display.doesShowToast()) {
            AdvancementUtils.displayToastDuringUpdate(player, this);
        }

        // Display handled by client (see AdvancementDisplay#showToast())
        if (giveRewards)
            giveReward(player);
    }

    public abstract void giveReward(@NotNull Player player);

    public void grant(@NotNull Player player) {
        grant(player, true);
    }

    public void grant(@NotNull Player player, boolean giveRewards) {
        Validate.notNull(player, "Player is null.");
        setCriteriaTeamProgression(player, maxCriteria, giveRewards);
    }

    public void revoke(@NotNull Player player) {
        Validate.notNull(player, "Player is null.");
        setCriteriaTeamProgression(player, 0, false);
    }

    public void onUpdate(@NotNull UUID uuid, @NotNull Set<net.minecraft.server.v1_15_R1.Advancement> advancementList, @NotNull Map<MinecraftKey, AdvancementProgress> progresses, @NotNull TeamProgression teamProgression, @NotNull Set<MinecraftKey> added) {
        if (isVisible(uuid)) {
            net.minecraft.server.v1_15_R1.Advancement mcAdv = getMinecraftAdvancement();
            advancementList.add(mcAdv);
            MinecraftKey key = getMinecraftKey();
            added.add(key);
            progresses.put(key, getAdvancementProgress(mcAdv, teamProgression.getCriteria(this)));
        }
    }

    public void onDispose() {
    }

    public boolean isValid() {
        return advancementTab.isActive() && advancementTab.getAdvancement(key) == this;
    }

    protected final <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull Consumer<E> consumer) {
        advancementTab.getEventManager().register(this, eventClass, consumer);
    }

    protected final <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull EventPriority priority, @NotNull Consumer<E> consumer) {
        advancementTab.getEventManager().register(this, eventClass, priority, consumer);
    }

    /**
     * Used by {@link IVisibility}.
     *
     * @return This advancement instance.
     */
    public final Advancement getAdvancement() {
        return this;
    }

    @Override
    public String toString() {
        return key.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Advancement that = (Advancement) o;

        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}



