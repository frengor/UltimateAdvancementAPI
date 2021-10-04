package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementCriteriaUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalOperationException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.util.AfterHandle;
import com.fren_gor.ultimateAdvancementAPI.visibilities.IVisibility;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementProgress;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromUUID;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateCriteriaStrict;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateIncrement;

/**
 * The {@code Advancement} class is the parent class of every advancement.
 * It provides the basic methods and fields to work with advancements.
 * It is extended only by RootAdvancement and BaseAdvancement. It cannot be extended by any other class.
 */
public abstract class Advancement {

    /**
     * The namespaced key of the advancement, which identifies it univocally.
     * <p>Note that the namespace of the key is the tab's one.
     */
    @NotNull
    protected final AdvancementKey key;

    /**
     * The advancement tab of the advancement.
     */
    @NotNull
    protected final AdvancementTab advancementTab;

    /**
     * The advancement display of the advancement.
     */
    @NotNull
    protected final AdvancementDisplay display;

    @Nullable
    private final Method iVisibilityMethod;

    /**
     * The maximum criteria of the advancement.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected final int maxCriteria;

    private Advancement() {
        throw new UnsupportedOperationException("Private constructor.");
    }

    /**
     * Creates a new {@code Advancement} with a maximum criteria of {@code 1}.
     *
     * @param advancementTab The advancement tab.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     */
    Advancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display) {
        this(advancementTab, key, display, 1);
    }

    /**
     * Creates a new {@code Advancement}.
     *
     * @param advancementTab The advancement tab.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param maxCriteria The maximum advancement criteria.
     */
    Advancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        // Validate class inheritance: Advancement can be extended only by RootAdvancement and BaseAdvancement
        // This makes sure no reflection is being used to make an invalid Advancement
        // The instanceOfs order provides max speed in most cases (there is usually one root for many base advancements)
        if (!(this instanceof BaseAdvancement || this instanceof RootAdvancement)) {
            throw new IllegalOperationException(getClass().getName() + " is neither an instance of RootAdvancement nor BaseAdvancement.");
        }
        Validate.isTrue(maxCriteria > 0, "Max criteria cannot be <= 0");
        this.advancementTab = Objects.requireNonNull(advancementTab, "AdvancementTab is null.");
        Validate.isTrue(!advancementTab.isInitialised(), "AdvancementTab is already initialised.");
        Validate.isTrue(!advancementTab.isDisposed(), "AdvancementTab is disposed.");
        this.key = new AdvancementKey(advancementTab.getNamespace(), key);
        this.display = Objects.requireNonNull(display, "Display is null.");
        this.maxCriteria = maxCriteria;
        if (this instanceof IVisibility) {
            this.iVisibilityMethod = getIVisibilityMethod(getClass());
        } else {
            this.iVisibilityMethod = null;
        }
    }

    /**
     * Gets the advancement namespaced key.
     *
     * @return The advancement namespaced key.
     */
    @NotNull
    public final AdvancementKey getKey() {
        return key;
    }

    /**
     * Gets the advancement tab of the advancement.
     *
     * @return The advancement tab of the advancement.
     */
    @NotNull
    public final AdvancementTab getAdvancementTab() {
        return advancementTab;
    }

    /**
     * Gets the MinecraftKey of the advancement.
     *
     * @return The advancement MinecraftKey.
     * @deprecated Use {@code getKey().toMinecraftKey()} instead.
     */
    @NotNull
    @Deprecated
    public final MinecraftKey getMinecraftKey() {
        return key.toMinecraftKey();
    }

    /**
     * Gets the maximum criteria of the advancement.
     *
     * @return The maximum criteria of the advancement.
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    public final int getMaxCriteria() {
        return maxCriteria;
    }

    /**
     * Gets the current criteria of the provided player's team.
     *
     * @param player The player.
     * @return The current criteria of the provided player's team.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull Player player) {
        return getTeamCriteria(uuidFromPlayer(player));
    }

    /**
     * Gets the current criteria of the provided player's team.
     *
     * @param uuid The {@link UUID} of the player.
     * @return The current criteria of the provided player's team.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull UUID uuid) {
        return getTeamCriteria(progressionFromUUID(uuid, this));
    }

    /**
     * Gets the current criteria of the provided team.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The current criteria of the team.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull TeamProgression progression) {
        Validate.notNull(progression, "TeamProgression is null.");
        return progression.getCriteria(this);
    }

    /**
     * Returns whether the advancement has been completed by the provided player's team.
     *
     * @param player The player.
     * @return Whether the advancement has been completed by the provided player's team.
     */
    public boolean isGranted(@NotNull Player player) {
        return isGranted(uuidFromPlayer(player));
    }

    /**
     * Returns whether the advancement has been completed by the provided player's team.
     *
     * @param uuid The {@link UUID} of the player.
     * @return Whether the advancement has been completed by the provided player's team.
     */
    public boolean isGranted(@NotNull UUID uuid) {
        return isGranted(progressionFromUUID(uuid, this));
    }

    /**
     * Returns whether the advancement has been completed by the provided team.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return Whether the advancement has been completed by the provided team.
     */
    public boolean isGranted(@NotNull TeamProgression progression) {
        Validate.notNull(progression, "TeamProgression is null.");
        return getTeamCriteria(progression) >= maxCriteria;
    }

    /**
     * Gets the chat message to be sent when an advancement is completed.
     * <p>The message is sent to everybody online on the server.
     *
     * @param player The player who has completed the advancement.
     * @return The message to be displayed, or {@code null} if no message should be displayed.
     */
    @Nullable
    public BaseComponent[] getAnnounceMessage(@NotNull Player player) {
        Validate.notNull(player, "Player is null.");
        ChatColor color = display.getFrame().getColor();
        return new ComponentBuilder(player.getName() + ' ' + display.getFrame().getChatText() + ' ')
                .color(ChatColor.WHITE)
                .append(new ComponentBuilder("[")
                                .color(color)
                                .event(new HoverEvent(Action.SHOW_TEXT, display.getChatDescription()))
                                .create()
                        , FormatRetention.NONE)
                .append(display.getChatTitle(), FormatRetention.EVENTS)
                .append(new ComponentBuilder("]")
                                .color(color)
                                .create()
                        , FormatRetention.EVENTS)
                .create();
    }

    /**
     * Increases the progression of the provided player's team by one.
     * <p>If the advancement gets completed, advancement rewards will be given.
     *
     * @param player The player who is responsible for the criteria increment.
     * @return The new criteria progression. It is always less or equal to {@link #maxCriteria}.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player) {
        return incrementTeamCriteria(player, true);
    }

    /**
     * Increases the progression of the provided player's team by one.
     *
     * @param player The player who is responsible for the criteria increment.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     * @return The new criteria progression. It is always less or equal to {@link #maxCriteria}.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, boolean giveReward) {
        return incrementTeamCriteria(player, 1, giveReward);
    }

    /**
     * Increases the progression of the provided player's team.
     * <p>If the advancement gets completed, advancement rewards will be given.
     *
     * @param player The player who is responsible for the criteria increment.
     * @param increment The progression increment. Must be greater than {@code 0}.
     * @return The new criteria progression. It is always less or equal to {@link #maxCriteria}.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, @Range(from = 1, to = Integer.MAX_VALUE) int increment) {
        return incrementTeamCriteria(player, increment, true);
    }

    /**
     * Increases the progression of the provided player's team.
     *
     * @param player The player who is responsible for the criteria increment.
     * @param increment The progression increment. Must be greater than {@code 0}.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     * @return The new criteria progression. It is always less or equal to {@link #maxCriteria}.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, @Range(from = 1, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        return incrementTeamCriteria(progressionFromPlayer(player, this), player, increment, giveReward);
    }

    /**
     * Increases the progression of the provided player's team by one.
     * <p>If the advancement gets completed, advancement rewards will be given.
     *
     * @param uuid The {@link UUID} of the player responsible for the criteria increment. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @return The new criteria progression. It is always less or equal to {@link #maxCriteria}.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid) {
        return incrementTeamCriteria(uuid, true);
    }

    /**
     * Increases the progression of the provided player's team by one.
     *
     * @param uuid The {@link UUID} of the player responsible for the criteria increment. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     * @return The new criteria progression. It is always less or equal to {@link #maxCriteria}.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, boolean giveReward) {
        return incrementTeamCriteria(uuid, 1, giveReward);
    }

    /**
     * Increases the progression of the provided player's team.
     * <p>If the advancement gets completed, advancement rewards will be given.
     *
     * @param uuid The {@link UUID} of the player responsible for the criteria increment. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @param increment The progression increment. Must be greater than {@code 0}.
     * @return The new criteria progression. It is always less or equal to {@link #maxCriteria}.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, @Range(from = 1, to = Integer.MAX_VALUE) int increment) {
        return incrementTeamCriteria(uuid, increment, true);
    }

    /**
     * Increases the progression of the provided player's team.
     *
     * @param uuid The {@link UUID} of the player responsible for the criteria increment. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @param increment The progression increment. Must be greater than {@code 0}.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     * @return The new criteria progression. It is always less or equal to {@link #maxCriteria}.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, @Range(from = 1, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        return incrementTeamCriteria(progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), increment, giveReward);
    }

    /**
     * Increases the progression of the provided team.
     * <p>The provided player must be an online member of the team. If no members are online or no particular player is
     * to be preferred, it can be put to {@code null}. In this case, rewards will be given to a pseudorandom online member
     * if there are any or the advancement will be set unredeemed.
     *
     * @param pro The {@link TeamProgression} of the team.
     * @param player The team member responsible for the criteria increment. May be {@code null}.
     * @param increment The progression increment. Must be greater than {@code 0}.
     * @param giveRewards Whether to give rewards if the advancement gets completed.
     * @return The new criteria progression. It is always less or equal to {@link #maxCriteria}.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int incrementTeamCriteria(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 1, to = Integer.MAX_VALUE) int increment, boolean giveRewards) {
        Validate.notNull(pro, "TeamProgression is null.");
        validateIncrement(increment);

        int crit = getTeamCriteria(pro);
        if (crit >= maxCriteria) {
            return crit;
        }
        int newCriteria = crit + increment;
        if (newCriteria > maxCriteria) {
            newCriteria = maxCriteria;
        }
        setCriteriaTeamProgression(pro, player, newCriteria, giveRewards);
        return newCriteria;
    }

    /**
     * Sets a criteria progression for the provided player's team.
     * <p>If the advancement gets completed, advancement rewards will be given.
     *
     * @param player The player who is responsible for the criteria update.
     * @param criteria The new non-negative criteria progression to set.
     */
    public void setCriteriaTeamProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        setCriteriaTeamProgression(player, criteria, true);
    }

    /**
     * Sets a criteria progression for the provided player's team.
     *
     * @param player The player who is responsible for the criteria update.
     * @param criteria The new non-negative criteria progression to set.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     */
    public void setCriteriaTeamProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveReward) {
        setCriteriaTeamProgression(progressionFromPlayer(player, this), player, criteria, giveReward);
    }

    /**
     * Sets a criteria progression for the provided player's team.
     * <p>If the advancement gets completed, advancement rewards will be given.
     *
     * @param uuid The {@link UUID} of the player responsible for the criteria update. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @param criteria The new non-negative criteria progression to set.
     */
    public void setCriteriaTeamProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        setCriteriaTeamProgression(uuid, criteria, true);
    }

    /**
     * Sets a criteria progression for the provided player's team.
     *
     * @param uuid The {@link UUID} of the player responsible for the criteria update. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @param criteria The new non-negative criteria progression to set.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     */
    public void setCriteriaTeamProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveReward) {
        setCriteriaTeamProgression(progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), criteria, giveReward);
    }

    /**
     * Sets a criteria progression for the provided team.
     * <p>The provided player must be an online member of the team. If no members are online or no particular player is
     * to be preferred, it can be put to {@code null}. In this case, rewards will be given to a pseudorandom online member
     * if there are any or the advancement will be set unredeemed.
     *
     * @param pro The {@link TeamProgression} that belongs to the team.
     * @param player The team member responsible for the criteria update. May be {@code null}.
     * @param criteria The new non-negative criteria progression to set.
     * @param giveRewards Whether to give rewards if the advancement gets completed.
     */
    protected void setCriteriaTeamProgression(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveRewards) {
        Validate.notNull(pro, "TeamProgression is null.");
        validateCriteriaStrict(criteria, maxCriteria);

        final DatabaseManager ds = advancementTab.getDatabaseManager();
        int old = ds.updateCriteria(key, pro, criteria);

        try {
            Bukkit.getPluginManager().callEvent(new AdvancementCriteriaUpdateEvent(pro, old, criteria, this));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        handlePlayer(pro, player, criteria, old, giveRewards, AfterHandle.UPDATE_ADVANCEMENTS_TO_TEAM);
    }

    /**
     * Handles the reward process of the advancement.
     * <p>When the new criteria is greater or equal than {@link #maxCriteria} and the old criteria is less than {@link #maxCriteria} then
     * the advancement is being completed by the provided player. If the provided player is non-null, they will receive the rewards.
     * Otherwise, if there are online members in the team one of them will receive the advancement rewards.
     * If no member of the team are online, the advancement will be set unredeemed for that team.
     *
     * @param pro The {@link TeamProgression} of the team.
     * @param player The team member responsible for the criteria update. May be {@code null}.
     * @param criteria The new non-negative criteria progression that has been set.
     * @param old The previous criteria progression of the team.
     * @param giveRewards Whether to give rewards if the advancement gets completed.
     * @param afterHandle The action to perform after the reward process, or {@code null} to don't do any action.
     *         The default action updates the tab's advancement to the team (see {@link AfterHandle#UPDATE_ADVANCEMENTS_TO_TEAM}).
     */
    protected void handlePlayer(@NotNull TeamProgression pro, @Nullable Player player, int criteria, int old, boolean giveRewards, @Nullable AfterHandle afterHandle) {
        Validate.notNull(pro, "TeamProgression is null.");
        if (criteria >= this.maxCriteria && old < this.maxCriteria) {
            if (player != null) {
                onGrant(player, giveRewards);
            } else {
                DatabaseManager ds = advancementTab.getDatabaseManager();
                player = pro.getAnOnlineMember(ds);
                if (player != null) {
                    onGrant(player, giveRewards);
                } else {
                    ds.setUnredeemed(key, giveRewards, pro);
                    return; // Skip advancement update, no player is online
                }
            }
        }
        if (afterHandle != null)
            afterHandle.apply(pro, player, this);
    }

    /**
     * Display the toast of this advancement to a player.
     *
     * @param player The player the toast will be shown to.
     */
    public void displayToastToPlayer(@NotNull Player player) {
        AdvancementUtils.displayToast(player, display.getIcon(), display.getTitle(), display.getFrame());
    }

    /**
     * Returns whether the advancement is visible to the provided player.
     * <p>An advancement is visible to a player if and only if it is visible to all the players in the player's team.
     * By default, every advancement is visible to every player, but this behavior can be changed
     * overriding {@link #isVisible(TeamProgression)} or implementing a suitable interface for the
     * Advancement Visibility System (see {@link IVisibility} for more information).
     *
     * @param player The player.
     * @return Whether the advancement is visible to the player.
     */
    public boolean isVisible(@NotNull Player player) {
        return isVisible(uuidFromPlayer(player));
    }

    /**
     * Returns whether the advancement is visible to the provided player.
     * <p>An advancement is visible to a player if and only if it is visible to all the players in the player's team.
     * By default, every advancement is visible to every player, but this behavior can be changed
     * overriding {@link #isVisible(TeamProgression)} or implementing a suitable interface for the
     * Advancement Visibility System (see {@link IVisibility} for more information).
     *
     * @param uuid The {@link UUID} of the player.
     * @return Whether the advancement is visible to the player.
     */
    public boolean isVisible(@NotNull UUID uuid) {
        return isVisible(progressionFromUUID(uuid, this));
    }

    /**
     * Returns whether the advancement is visible to the provided team.
     * <p>An advancement is visible to a team if and only if it is visible to all the players in the team.
     * By default, every advancement is visible to every player, but this behavior can be changed
     * overriding this method or implementing a suitable interface for the Advancement Visibility System.
     *
     * @param progression The{@link TeamProgression} of the team.
     * @return Whether the advancement is visible to the provided team.
     * @implSpec This method is the core method of the Advancement Visibility System (AVS).
     *         The return value is {@code true} if no suitable interfaces for the AVS are implemented, or
     *         the result of {@link IVisibility#isVisible(Advancement, TeamProgression)} otherwise.
     *         When overridden, this method (called via {@code super}) enables the AVS features for that method.
     */
    public boolean isVisible(@NotNull TeamProgression progression) {
        // Advancement visibility system
        if (iVisibilityMethod != null) {
            try {
                return (boolean) iVisibilityMethod.invoke(this, this, progression);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Visible by default
        return true;
    }

    /**
     * Returns whether the advancement tab of this advancement is shown to the provided player.
     *
     * @param player The player.
     * @return Whether the advancement tab of this advancement is shown to the player.
     * @deprecated Use {@code getAdvancementTab().isShownTo(player)} instead.
     */
    @Deprecated
    @Contract(pure = true, value = "null -> false")
    public boolean isShownTo(Player player) {
        return getAdvancementTab().isShownTo(player);
    }

    /**
     * Called when the advancement is completed by a player. It handles the chat message, the toast notification, and the advancement rewards (see {@link #giveReward(Player)} for more information).
     *
     * @param player The player who completed the advancement.
     * @param giveRewards Whether to give rewards.
     */
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
            // TODO Find a better solution
            runSync(advancementTab.getOwningPlugin(), () -> AdvancementUtils.displayToastDuringUpdate(player, this));
        }

        if (giveRewards)
            giveReward(player);
    }

    /**
     * Grants the advancement to the player's team giving rewards.
     *
     * @param player The player.
     */
    public void grant(@NotNull Player player) {
        grant(player, true);
    }

    /**
     * Grant the advancement to the player's team.
     *
     * @param player The player
     * @param giveRewards Whether to give rewards.
     */
    public void grant(@NotNull Player player, boolean giveRewards) {
        Validate.notNull(player, "Player is null.");
        setCriteriaTeamProgression(player, maxCriteria, giveRewards);
    }

    /**
     * Revoke the advancement to the player's team.
     *
     * @param player The player.
     */
    public void revoke(@NotNull Player player) {
        Validate.notNull(player, "Player is null.");
        setCriteriaTeamProgression(player, 0, false);
    }

    /**
     * Handles the serialisation of the advancement into the update packet.
     *
     * @param teamProgression The {@link TeamProgression} of the team.
     * @param advancementList THe advancement list.
     * @param progresses The progressions.
     * @param added The advancement added to advancementList.
     */
    public void onUpdate(@NotNull TeamProgression teamProgression, @NotNull Set<net.minecraft.server.v1_15_R1.Advancement> advancementList, @NotNull Map<MinecraftKey, AdvancementProgress> progresses, @NotNull Set<MinecraftKey> added) {
        if (isVisible(teamProgression)) {
            net.minecraft.server.v1_15_R1.Advancement mcAdv = getMinecraftAdvancement();
            advancementList.add(mcAdv);
            MinecraftKey key = getMinecraftKey();
            added.add(key);
            progresses.put(key, getAdvancementProgress(mcAdv, getTeamCriteria(teamProgression)));
        }
    }

    /**
     * Gives the rewards to the provided player when the advancement is completed.
     *
     * @param player The player who has completed the advancement.
     * @implSpec This method does not do anything by default. Override it to change its behavior.
     */
    public void giveReward(@NotNull Player player) {
    }

    /**
     * Called when the advancement is registered by its {@link AdvancementTab} through {@link AdvancementTab#registerAdvancements(RootAdvancement, Set)}.
     * <p>No exception should be thrown, to validate the advancement registration see {@link #validateRegister()}.
     * <p>Note that not every method of {@link AdvancementTab} is available, since this method is called before the tab is set initialised.
     *
     * @implSpec This method does not do anything by default. Override it to change its behavior.
     */
    public void onRegister() {
    }

    /**
     * Validate the advancement after it has been registered by the advancement tab.
     * If the validation fails, a {@link InvalidAdvancementException} should be thrown.
     * <p>For example, {@link BaseAdvancement} overrides this method to make sure it and its parent belongs to the same tab.
     * <p>Note that every method of {@link AdvancementTab} is available, since this method is called after every advancement has been registered
     * and the tab has already been initialised.
     *
     * @throws InvalidAdvancementException If the validation fails.
     * @implSpec This method does not do anything by default. Override it to change its behavior.
     */
    public void validateRegister() throws InvalidAdvancementException {
    }

    /**
     * Called when the advancement is disposed.
     *
     * @implSpec This method does not do anything by default. Override it to change its behavior.
     */
    public void onDispose() {
    }

    /**
     * Returns whether the advancement is valid.
     * <p>An advancement is valid if and only if it belongs to its advancement tab and that tab is active.
     *
     * @return Whether the advancement is valid.
     */
    public boolean isValid() {
        return advancementTab.isActive() && advancementTab.hasAdvancement(this);
    }

    /**
     * Returns the NMS advancement of this advancement.
     * Should craft the NMS advancement once and returns it henceforth.
     *
     * @return The NMS advancement representing this advancement.
     */
    public abstract @NotNull net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement();

    /**
     * Registers the provided event into the tab {@link EventManager}.
     *
     * @param eventClass The class of the event to register.
     * @param consumer The code to run when the event occurs.
     * @param <E> The class of the event to register.
     */
    protected final <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull Consumer<E> consumer) {
        advancementTab.getEventManager().register(this, eventClass, consumer);
    }

    /**
     * Registers the provided event into the tab {@link EventManager}.
     *
     * @param eventClass The class of the event to register.
     * @param priority The priority of the event. See {@link EventPriority}.
     * @param consumer The code to run when the event occurs.
     * @param <E> The class of the event to register.
     */
    protected final <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull EventPriority priority, @NotNull Consumer<E> consumer) {
        advancementTab.getEventManager().register(this, eventClass, priority, consumer);
    }

    /**
     * Returns the advancement namespaced key as {@link String}.
     * <p>Calling this method is equivalent to call {@code getKey().toString()}.
     *
     * @return The advancement namespaced key as {@link String}.
     * @see AdvancementKey#toString()
     */
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

    /**
     * Gets the right {@link IVisibility} sub-interface method that will be used by the advancement visibility system.
     *
     * @param clazz The class to analyze.
     * @return The right {@link IVisibility#isVisible(Advancement, TeamProgression)} method or {@code null}.
     */
    @Nullable
    private Method getIVisibilityMethod(Class<? extends Advancement> clazz) {

        for (Class<?> i : clazz.getInterfaces()) {
            if (i != IVisibility.class && IVisibility.class.isAssignableFrom(i)) {
                try {
                    final Method m = i.getDeclaredMethod("isVisible", Advancement.class, TeamProgression.class);
                    if (m.isDefault()) {
                        return m;
                    }
                } catch (NoSuchMethodException e) {
                    // No method found, continue
                }
            }
        }
        Class<?> sClazz = clazz.getSuperclass();
        if (Advancement.class.isAssignableFrom(sClazz) && sClazz != Advancement.class) {
            return getIVisibilityMethod(sClazz.asSubclass(Advancement.class));
        }
        return null;
    }

    /**
     * Gets the {@link AdvancementDisplay} of this advancement.
     *
     * @return The {@link AdvancementDisplay} of this advancement.
     */
    public AdvancementDisplay getDisplay() {
        return display;
    }
}
