package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.AdvancementUpdater;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.announcementMessage.IAnnouncementMessage;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.ProgressionUpdateResult;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.AsyncExecutionException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DisposedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalOperationException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.visibilities.IVisibility;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromUUID;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateProgressionValueStrict;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * The {@code Advancement} class is the parent class of every advancement.
 * It provides the basic methods and fields to work with advancements.
 * <p>It is extended only by {@link RootAdvancement} and {@link BaseAdvancement}. It cannot be extended by any other class, which should extend one of those instead.
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
    protected final AbstractAdvancementDisplay display;

    /**
     * The maximum progression of the advancement.
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    protected final int maxProgression;

    @Nullable
    private final MethodHandle iVisibilityMethod, iAnnouncementMessageMethod;

    private Advancement() {
        throw new UnsupportedOperationException("Private constructor.");
    }

    /**
     * Creates a new {@code Advancement} with a maximum progression of {@code 1}.
     *
     * @param advancementTab The advancement tab.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param display The display information of this advancement.
     */
    Advancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AbstractAdvancementDisplay display) {
        this(advancementTab, key, display, 1);
    }

    /**
     * Creates a new {@code Advancement}.
     *
     * @param advancementTab The advancement tab.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param display The display information of this advancement.
     * @param maxProgression The maximum advancement progression.
     */
    Advancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AbstractAdvancementDisplay display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        // Validate class inheritance: Advancement can be extended only by RootAdvancement and BaseAdvancement
        // This makes sure no reflection is being used to make an invalid Advancement
        // The instanceOfs order provides max speed in most cases (there is usually one root for many base advancements)
        if (!(this instanceof BaseAdvancement || this instanceof RootAdvancement)) {
            throw new IllegalOperationException(getClass().getName() + " is neither an instance of RootAdvancement nor BaseAdvancement.");
        }
        Preconditions.checkArgument(maxProgression > 0, "Maximum progression cannot be <= 0");
        this.advancementTab = Objects.requireNonNull(advancementTab, "AdvancementTab is null.");
        Preconditions.checkArgument(!advancementTab.isInitialised(), "AdvancementTab is already initialised.");
        Preconditions.checkArgument(!advancementTab.isDisposed(), "AdvancementTab is disposed.");
        this.key = new AdvancementKey(advancementTab.getNamespace(), key);
        this.display = Objects.requireNonNull(display, "Display is null.");
        this.maxProgression = maxProgression;
        this.iVisibilityMethod = this instanceof IVisibility ? getIVisibilityMethod() : null;
        this.iAnnouncementMessageMethod = this instanceof IAnnouncementMessage ? getIAnnouncementMessageMethod() : null;
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
     * Gets the maximum progression of the advancement.
     *
     * @return The maximum progression of the advancement.
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    public final int getMaxProgression() {
        return maxProgression;
    }

    /**
     * Gets the current progression of the provided player's team.
     *
     * @param player The player.
     * @return The current progression of the provided player's team.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getProgression(@NotNull Player player) {
        return getProgression(uuidFromPlayer(player));
    }

    /**
     * Gets the current progression of the provided player's team.
     *
     * @param uuid The {@link UUID} of the player.
     * @return The current progression of the provided player's team.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getProgression(@NotNull UUID uuid) {
        return getProgression(progressionFromUUID(uuid, this));
    }

    /**
     * Gets the current progression of the provided team.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The current progression of the team.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getProgression(@NotNull TeamProgression progression) {
        validateTeamProgression(progression);
        return progression.getProgression(this);
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
        validateTeamProgression(progression);
        return getProgression(progression) >= maxProgression;
    }

    /**
     * Increases the progression of the provided player's team by one.
     * <p>If the advancement gets completed, advancement rewards will be given.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param player The player who is responsible for the increment.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player) {
        return incrementProgression(player, true);
    }

    /**
     * Increases the progression of the provided player's team by one.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param player The player who is responsible for the increment.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player, boolean giveReward) {
        return incrementProgression(player, 1, giveReward);
    }

    /**
     * Increases the progression of the provided player's team.
     * <p>If the advancement gets completed, advancement rewards will be given.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param player The player who is responsible for the increment.
     * @param increment The progression increment. May be less than {@code 0}. If the final progression would be lower
     *         than {@code 0}, the progression will be set to {@code 0}.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player, int increment) {
        return incrementProgression(player, increment, true);
    }

    /**
     * Increases the progression of the provided player's team.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param player The player who is responsible for the increment.
     * @param increment The progression increment. May be less than {@code 0}. If the final progression would be lower
     *         than {@code 0}, the progression will be set to {@code 0}.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player, int increment, boolean giveReward) {
        return incrementProgression(progressionFromPlayer(player, this), player, increment, giveReward);
    }

    /**
     * Increases the progression of the provided player's team by one.
     * <p>If the advancement gets completed, advancement rewards will be given.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param uuid The {@link UUID} of the player responsible for the increment. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid) {
        return incrementProgression(uuid, true);
    }

    /**
     * Increases the progression of the provided player's team by one.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param uuid The {@link UUID} of the player responsible for the increment. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid, boolean giveReward) {
        return incrementProgression(uuid, 1, giveReward);
    }

    /**
     * Increases the progression of the provided player's team.
     * <p>If the advancement gets completed, advancement rewards will be given.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param uuid The {@link UUID} of the player responsible for the increment. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @param increment The progression increment. May be less than {@code 0}. If the final progression would be lower
     *         than {@code 0}, the progression will be set to {@code 0}.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid, int increment) {
        return incrementProgression(uuid, increment, true);
    }

    /**
     * Increases the progression of the provided player's team.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param uuid The {@link UUID} of the player responsible for the increment. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @param increment The progression increment. May be less than {@code 0}. If the final progression would be lower
     *         than {@code 0}, the progression will be set to {@code 0}.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid, int increment, boolean giveReward) {
        return incrementProgression(progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), increment, giveReward);
    }

    /**
     * Increases the progression of the provided team.
     * <p>The provided player must be an online member of the team. If no members are online or no particular player is
     * to be preferred, it can be put to {@code null}. In this case, rewards will be given to a random online team member
     * (if there are any) or the advancement will be set unredeemed.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param pro The {@link TeamProgression} of the team.
     * @param player The team member responsible for the increment. May be {@code null}.
     * @param increment The progression increment. May be less than {@code 0}. If the final progression would be lower
     *         than {@code 0}, the progression will be set to {@code 0}.
     * @param giveRewards Whether to give rewards if the advancement gets completed.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    protected CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull TeamProgression pro, @Nullable Player player, int increment, boolean giveRewards) {
        validateTeamProgression(pro);

        final DatabaseManager ds = advancementTab.getDatabaseManager();
        var completableFuture = ds.incrementProgression(key, pro, increment);

        runSync(completableFuture, advancementTab.getOwningPlugin(), (result, err) -> {
            if (err != null) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while incrementing the progression of " + key, err);
                return; // Don't go on in case of a db error
            }

            try {
                Bukkit.getPluginManager().callEvent(new AdvancementProgressionUpdateEvent(pro, result.oldProgression(), result.newProgression(), Advancement.this));
            } catch (Exception e) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while calling AdvancementProgressionUpdateEvent for " + key, e);
            }

            handleAdvancementGranting(pro, player, result.newProgression(), result.oldProgression(), giveRewards);
            handleAdvancementUpdatingToTeam(pro, result.newProgression(), result.oldProgression());
        });

        return completableFuture;
    }

    /**
     * Sets a progression for the provided player's team.
     * <p>If the advancement gets completed, advancement rewards will be given.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param player The player who is responsible for the update.
     * @param progression The new non-negative progression to set.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        return setProgression(player, progression, true);
    }

    /**
     * Sets a progression for the provided player's team.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param player The player who is responsible for the update.
     * @param progression The new non-negative progression to set.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean giveReward) {
        return setProgression(progressionFromPlayer(player, this), player, progression, giveReward);
    }

    /**
     * Sets a progression for the provided player's team.
     * <p>If the advancement gets completed, advancement rewards will be given.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param uuid The {@link UUID} of the player responsible for the update. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @param progression The new non-negative progression to set.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        return setProgression(uuid, progression, true);
    }

    /**
     * Sets a progression for the provided player's team.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param uuid The {@link UUID} of the player responsible for the update. If the player is not online, rewards will be given to a pseudorandom
     *         online member of the same team if there are any, or it will be set unredeemed.
     * @param progression The new non-negative progression to set.
     * @param giveReward Whether to give rewards if the advancement gets completed.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean giveReward) {
        return setProgression(progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), progression, giveReward);
    }

    /**
     * Sets a new progression for the provided team.
     * <p>The provided player must be an online member of the team. If no members are online or no particular player is
     * to be preferred, it can be put to {@code null}. In this case, rewards will be given to a random online team member
     * (if there are any) or the advancement will be set unredeemed.
     * <p>The progression is not updated immediately, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param pro The {@link TeamProgression} that belongs to the team.
     * @param player The team member responsible for the update. May be {@code null}.
     * @param progression The new non-negative progression to set.
     * @param giveRewards Whether to give rewards if the advancement gets completed.
     * @return A {@link CompletableFuture} which will complete with the result of the operation.
     * @see ProgressionUpdateResult
     * @see CompletableFuture
     */
    protected CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean giveRewards) {
        validateTeamProgression(pro);
        validateProgressionValueStrict(progression, maxProgression);

        final DatabaseManager ds = advancementTab.getDatabaseManager();
        var completableFuture = ds.setProgression(key, pro, progression);

        runSync(completableFuture, advancementTab.getOwningPlugin(), (result, err) -> {
            if (err != null) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while setting the progression of " + key, err);
                return; // Don't go on in case of a db error
            }

            try {
                Bukkit.getPluginManager().callEvent(new AdvancementProgressionUpdateEvent(pro, result.oldProgression(), result.newProgression(), Advancement.this));
            } catch (Exception e) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred while calling AdvancementProgressionUpdateEvent for " + key, e);
            }

            handleAdvancementGranting(pro, player, result.newProgression(), result.oldProgression(), giveRewards);
            handleAdvancementUpdatingToTeam(pro, result.newProgression(), result.oldProgression());
        });

        return completableFuture;
    }

    /**
     * Called after a progression update to handle the granting process of the advancement.
     * <p>When the new progression is greater or equal than {@link #maxProgression} and the old progression is less than {@link #maxProgression} then
     * the advancement is being completed by the provided player. If the provided player is non-null, they will receive the rewards.
     * Otherwise, if there are online members in the team, one of them will receive the advancement rewards.
     * If no member of the team are online, the advancement will be set unredeemed for that team.
     *
     * @param pro The {@link TeamProgression} of the team.
     * @param player The team member responsible for the update. May be {@code null}.
     * @param newProgression The new progression of the team.
     * @param oldProgression The previous progression of the team.
     * @param giveRewards Whether to give rewards if the advancement gets completed.
     * @throws AsyncExecutionException If this method is called outside the main thread.
     * @see #onGrant(Player, boolean)
     */
    protected void handleAdvancementGranting(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression, @Range(from = 0, to = Integer.MAX_VALUE) int oldProgression, boolean giveRewards) {
        AdvancementUtils.checkSync();
        validateTeamProgression(pro);
        if (newProgression >= this.maxProgression && oldProgression < this.maxProgression) {
            if (player != null) {
                onGrant(player, giveRewards);
            } else {
                DatabaseManager ds = advancementTab.getDatabaseManager();
                player = pro.getAnOnlineMember(ds);
                if (player != null) {
                    onGrant(player, giveRewards);
                } else {
                    ds.setUnredeemed(key, giveRewards, pro);
                }
            }
        }
    }

    /**
     * Called after a progression update to handle sending the updated advancement to the team members.
     *
     * @param pro The {@link TeamProgression} of the team.
     * @param newProgression The new progression of the team.
     * @param oldProgression The previous progression of the team.
     * @throws AsyncExecutionException If this method is called outside the main thread.
     * @see AdvancementTab#updateAdvancementsToTeam(TeamProgression)
     */
    protected void handleAdvancementUpdatingToTeam(@NotNull TeamProgression pro, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression, @Range(from = 0, to = Integer.MAX_VALUE) int oldProgression) {
        AdvancementUtils.checkSync();
        validateTeamProgression(pro);
        if (newProgression != oldProgression) {
            advancementTab.updateAdvancementsToTeam(pro);
        }
    }

    /**
     * Display the toast of this advancement to a player.
     *
     * @param player The player the toast will be shown to.
     */
    public void displayToastToPlayer(@NotNull Player player) {
        TeamProgression team = advancementTab.getDatabaseManager().getTeamProgression(player);
        AdvancementUtils.displayToast(player, display.dispatchGetIcon(player, team), display.dispatchGetTitle(player, team), display.dispatchGetFrame(player, team));
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
     * @see IVisibility
     */
    public boolean isVisible(@NotNull TeamProgression progression) {
        validateTeamProgression(progression);
        // Advancement visibility system
        if (iVisibilityMethod != null) {
            try {
                return (boolean) iVisibilityMethod.invokeWithArguments(this, progression);
            } catch (Throwable e) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred evaluating the visibility of " + key, e);
            }
        }
        // Visible by default
        return true;
    }

    /**
     * Returns a function to get the per-player chat message to be displayed when the advancement is completed.
     * <p>The returned function is called for each online player and should return the announcement message
     * to send them, or {@code null} if no message should be shown to that player.
     * <p>By default, the announcement message is vanilla like. This behavior can be changed by overriding this method
     * or implementing a suitable interface for the Advancement Announcement Message System.
     *
     * @param advancementCompleter The player who has completed the advancement.
     * @return A function which returns, for each player, the announcement message to be displayed to them.
     *         {@code null} can be returned instead if no message should be displayed to any player.
     * @implSpec This method is the core method of the Advancement Announcement Message System (AAMS).
     *         The default announcement message is returned if no suitable interfaces for the AAMS are implemented, or
     *         the result of {@link IAnnouncementMessage#getAnnouncementMessage(Advancement, Player)} otherwise.
     *         When overridden, this method (called via {@code super}) enables the AAMS features for that method.
     * @see IAnnouncementMessage
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public Function<@NotNull Player, @Nullable BaseComponent> getAnnouncementMessage(@NotNull Player advancementCompleter) {
        Preconditions.checkNotNull(advancementCompleter, "Player is null.");
        // Advancement announcement message system
        if (iAnnouncementMessageMethod != null) {
            try {
                return (Function<Player, BaseComponent>) iAnnouncementMessageMethod.invokeWithArguments(this, advancementCompleter);
            } catch (Throwable e) {
                advancementTab.getOwningPlugin().getLogger().log(Level.SEVERE, "An exception occurred evaluating the announcement message of " + key, e);
            }
        }
        // Default message, same for every player
        BaseComponent announcementMsg = AdvancementUtils.getAnnouncementMessage(this, advancementCompleter);
        return player -> announcementMsg;
    }

    /**
     * Called when the advancement is completed by a player. It handles the chat announcement message, the toast notification, and the advancement reward (see {@link #giveReward(Player)} for more information).
     *
     * @param advancementCompleter The player who completed the advancement.
     * @param giveRewards Whether to give rewards.
     * @see #giveReward(Player)
     * @see #sendAnnouncementMessageOnGrant(Player, TeamProgression)
     * @see #displayToastOnGrant(Player, TeamProgression)
     */
    public void onGrant(@NotNull Player advancementCompleter, boolean giveRewards) {
        Preconditions.checkNotNull(advancementCompleter, "Player is null.");
        final TeamProgression progression = advancementTab.getDatabaseManager().getTeamProgression(advancementCompleter);

        sendAnnouncementMessageOnGrant(advancementCompleter, progression);
        displayToastOnGrant(advancementCompleter, progression);

        if (giveRewards)
            giveReward(advancementCompleter);
    }

    /**
     * Called during {@link #onGrant(Player, boolean) onGrant} to send the chat announcement message.
     *
     * @param advancementCompleter The player who completed the advancement.
     * @param progression The team of the player who completed the advancement.
     * @see #onGrant(Player, boolean)
     */
    protected void sendAnnouncementMessageOnGrant(@NotNull Player advancementCompleter, @NotNull TeamProgression progression) {
        Preconditions.checkNotNull(advancementCompleter, "Player is null.");
        validateTeamProgression(progression);

        Boolean gameRule = advancementCompleter.getWorld().getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS);
        if ((gameRule == null || gameRule) && display.dispatchDoesAnnounceToChat(advancementCompleter, progression)) {
            @Nullable Function<Player, @Nullable BaseComponent> perPlayerMsgGetter = getAnnouncementMessage(advancementCompleter);
            if (perPlayerMsgGetter != null) {
                Stream<? extends Player> players;
                if (advancementTab.doesSendAnnouncementMessageOnlyToTeam()) {
                    players = progression.getMembers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull);
                } else {
                    players = Bukkit.getOnlinePlayers().stream();
                }
                players.forEach(p -> {
                    @Nullable BaseComponent msg = perPlayerMsgGetter.apply(p);
                    if (msg != null) {
                        p.spigot().sendMessage(msg);
                    }
                });
            }
        }
    }

    /**
     * Called during {@link #onGrant(Player, boolean) onGrant} to display the toast notification.
     *
     * @param advancementCompleter The player who completed the advancement.
     * @param progression The team of the player who completed the advancement.
     * @see #onGrant(Player, boolean)
     */
    protected void displayToastOnGrant(@NotNull Player advancementCompleter, @NotNull TeamProgression progression) {
        Preconditions.checkNotNull(advancementCompleter, "Player is null.");
        validateTeamProgression(progression);

        if (display.dispatchDoesShowToast(advancementCompleter, progression)) {
            Stream<Player> players;
            if (advancementTab.doesShowToastToTeam()) {
                players = progression.getMembers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull);
            } else {
                players = Stream.of(advancementCompleter);
            }
            players.forEach(p -> {
                // TODO Find a better solution
                runSync(advancementTab.getOwningPlugin(), 2, () -> AdvancementUtils.displayToastDuringUpdate(p, this));
            });
        }
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
        Preconditions.checkNotNull(player, "Player is null.");
        setProgression(player, maxProgression, giveRewards);
    }

    /**
     * Revoke the advancement to the player's team.
     *
     * @param player The player.
     */
    public void revoke(@NotNull Player player) {
        Preconditions.checkNotNull(player, "Player is null.");
        setProgression(player, 0, false);
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
     * Handles the serialisation of the advancement into the update packet.
     * <p>Advancement(s) to be sent have to be added to the provided {@link AdvancementUpdater}.
     * <p>This method will be called once per team.
     *
     * @param teamProgression The {@link TeamProgression} of the team of the player.
     * @param advancementUpdater The {@link AdvancementUpdater} in which the advancements to send should be added.
     */
    public abstract void onUpdate(@NotNull TeamProgression teamProgression, @NotNull AdvancementUpdater advancementUpdater);

    /**
     * Returns the NMS wrapper of this advancement.
     * Should craft the NMS wrapper once and returns it henceforth.
     *
     * @return The NMS wrapper of this advancement.
     */
    @NotNull
    public abstract PreparedAdvancementWrapper getNMSWrapper();

    /**
     * Registers the provided event into the tab {@link EventManager}.
     *
     * @param eventClass The class of the event to register.
     * @param consumer The code to run when the event occurs.
     * @param <E> The class of the event to register.
     * @throws DisposedException If the {@link AdvancementTab} of this advancement is disposed.
     * @throws IllegalArgumentException If any argument is null.
     */
    protected final <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull Consumer<E> consumer) {
        try {
            advancementTab.getEventManager().register(this, eventClass, consumer);
        } catch (IllegalStateException e) {
            throw new DisposedException(e);
        }
    }

    /**
     * Registers the provided event into the tab {@link EventManager}.
     *
     * @param eventClass The class of the event to register.
     * @param priority The priority of the event. See {@link EventPriority}.
     * @param consumer The code to run when the event occurs.
     * @param <E> The class of the event to register.
     * @throws DisposedException If the {@link AdvancementTab} of this advancement is disposed.
     * @throws IllegalArgumentException If any argument is null.
     */
    protected final <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull EventPriority priority, @NotNull Consumer<E> consumer) {
        try {
            advancementTab.getEventManager().register(this, eventClass, priority, consumer);
        } catch (IllegalStateException e) {
            throw new DisposedException(e);
        }
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
     * @return The right {@link IVisibility#isVisible(Advancement, TeamProgression)} {@link MethodHandle} or {@code null}.
     */
    @Nullable
    private MethodHandle getIVisibilityMethod() {
        return getMethod(getClass(), IVisibility.class, "isVisible", Advancement.class, TeamProgression.class);
    }

    /**
     * Gets the right {@link IAnnouncementMessage} sub-interface method that will be used by the advancement announcement message system.
     *
     * @return The right {@link IAnnouncementMessage#getAnnouncementMessage(Advancement, Player)} {@link MethodHandle} or {@code null}.
     */
    @Nullable
    private MethodHandle getIAnnouncementMessageMethod() {
        return getMethod(getClass(), IAnnouncementMessage.class, "getAnnouncementMessage", Advancement.class, Player.class);
    }

    /**
     * Gets the right sub-interface method that will be used by the AVS or AAMS.
     *
     * @param clazz The class to analyze.
     * @return The right {@link MethodHandle} or {@code null}.
     */
    @Nullable
    private MethodHandle getMethod(Class<? extends Advancement> clazz, Class<?> interfaceClass, String methodName, Class<?>... methodParameters) {
        for (Class<?> i : clazz.getInterfaces()) {
            if (i != interfaceClass && interfaceClass.isAssignableFrom(i)) {
                try {
                    final Method m = i.getDeclaredMethod(methodName, methodParameters);
                    if (m.isDefault()) {
                        // Make sure the interface method is called instead of the method in this class
                        return MethodHandles.lookup().unreflectSpecial(m, i).bindTo(this);
                    }
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    // No method found, continue
                }
            }
        }
        Class<?> sClazz = clazz.getSuperclass();
        if (Advancement.class.isAssignableFrom(sClazz) && sClazz != Advancement.class) {
            return getMethod(sClazz.asSubclass(Advancement.class), interfaceClass, methodName, methodParameters);
        }
        return null;
    }

    /**
     * Gets the {@link AbstractAdvancementDisplay} of this advancement.
     *
     * @return The {@link AbstractAdvancementDisplay} of this advancement.
     */
    @NotNull
    public final AbstractAdvancementDisplay getDisplay() {
        return display;
    }

    /**
     * <strong>This method should not be called at any time!</strong>
     * <p>It is present <i>only</i> to avoid compilation errors when implementing more than one interface of the Advancement Visibility System,
     * since every one of those interfaces implement the {@link IVisibility#isVisible(Advancement, TeamProgression)} method
     * and the compiler cannot automatically choose the method to call between the ones of the different interfaces.
     *
     * @throws IllegalOperationException Every time it's called.
     * @hidden
     * @deprecated Use {@link Advancement#isVisible(TeamProgression)}.
     */
    @Deprecated
    @Internal
    @Contract("_, _ -> fail")
    public final boolean isVisible(Advancement advancement, TeamProgression progression) {
        throw new IllegalOperationException("This method cannot be called. Use Advancement#isVisible(TeamProgression).");
    }

    /**
     * <strong>This method should not be called at any time!</strong>
     * <p>It is present <i>only</i> to avoid compilation errors when implementing more than one interface of the Advancement Announcement Message System,
     * since every one of those interfaces implement the {@link IAnnouncementMessage#getAnnouncementMessage(Advancement, Player)} method
     * and the compiler cannot automatically choose the method to call between the ones of the different interfaces.
     *
     * @throws IllegalOperationException Every time it's called.
     * @hidden
     * @deprecated Use {@link Advancement#getAnnouncementMessage(Player)}.
     */
    @Deprecated
    @Internal
    @Nullable
    @Contract("_, _ -> fail")
    public final Function<@NotNull Player, @Nullable BaseComponent> getAnnouncementMessage(@NotNull Advancement advancement, @NotNull Player advancementCompleter) {
        throw new IllegalOperationException("This method cannot be called. Use Advancement#getAnnouncementMessage(Player).");
    }
}
