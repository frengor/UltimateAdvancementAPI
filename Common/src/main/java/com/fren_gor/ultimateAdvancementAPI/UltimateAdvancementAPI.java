package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.database.CacheFreeingOption;
import com.fren_gor.ultimateAdvancementAPI.database.CacheFreeingOption.Option;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.ObjectResult;
import com.fren_gor.ultimateAdvancementAPI.database.Result;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.APINotInstantiatedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DuplicatedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.NotGrantedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;

/**
 * Class for making requests to the API.
 */
@SuppressWarnings("unused")
public final class UltimateAdvancementAPI {

    /**
     * The instance of the main class of the API. Set by {@link AdvancementMain} on enabling.
     */
    static AdvancementMain main;

    private static AdvancementMain getMain() throws IllegalStateException {
        if (main == null) {
            throw new IllegalStateException("UltimateAdvancementAPI is not enabled.");
        }
        return main;
    }

    /**
     * Returns a new instance of {@code UltimateAdvancementAPI}.
     *
     * @param plugin The plugin that is going to use the instance.
     * @return A new instance of {@code UltimateAdvancementAPI}.
     * @throws APINotInstantiatedException If the API is not enabled.
     */
    @NotNull
    public static UltimateAdvancementAPI getInstance(@NotNull Plugin plugin) throws APINotInstantiatedException {
        Preconditions.checkNotNull(plugin, "Plugin is null.");
        Preconditions.checkArgument(plugin.isEnabled(), "Plugin is not enabled.");
        if (main == null) {
            throw new APINotInstantiatedException();
        }
        return new UltimateAdvancementAPI(plugin);
    }

    private final Plugin plugin;

    private UltimateAdvancementAPI(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new {@link AdvancementTab} with the provided namespace. The namespace must be unique.
     *
     * @param namespace The unique namespace of the tab.
     * @return The new {@link AdvancementTab}.
     * @throws DuplicatedException If another tab with the name already exists.
     * @throws IllegalStateException If the API is not enabled.
     */
    @NotNull
    @Contract("_ -> new")
    public AdvancementTab createAdvancementTab(@NotNull String namespace) throws DuplicatedException {
        return getMain().createAdvancementTab(plugin, namespace);
    }

    /**
     * Gets an advancement tab by its namespace.
     *
     * @param namespace The namespace of the advancement tab.
     * @return The advancement tab with the provided namespace, or {@code null} if there isn't any tab with such namespace.
     * @throws IllegalStateException If the API is not enabled.
     */
    @Nullable
    public AdvancementTab getAdvancementTab(@NotNull String namespace) {
        return getMain().getAdvancementTab(namespace);
    }

    /**
     * Returns whether an advancement tab with the provided namespace has already been registered.
     *
     * @param namespace The namespace of the advancement tab.
     * @return Whether an advancement tab with the provided namespace has already been registered.
     * @throws IllegalStateException If the API is not enabled.
     */
    public boolean isAdvancementTabRegistered(@NotNull String namespace) {
        return getMain().isAdvancementTabRegistered(namespace);
    }

    /**
     * Returns an unmodifiable {@link Collection} of the advancement tabs registered by the provided plugin.
     *
     * @return An unmodifiable {@link Collection} of the advancement tabs registered by the provided plugin.
     * @throws IllegalStateException If the API is not enabled.
     */
    @UnmodifiableView
    @NotNull
    public Collection<@NotNull AdvancementTab> getPluginAdvancementTabs() {
        return getMain().getPluginAdvancementTabs(plugin);
    }

    /**
     * Unregisters an advancement tab.
     *
     * @param namespace The namespace of the advancement tab to be unregistered.
     * @throws IllegalStateException If the API is not enabled.
     */
    public void unregisterAdvancementTab(@NotNull String namespace) {
        getMain().unregisterAdvancementTab(namespace);
    }

    /**
     * Unregisters all the advancement tabs owned by the provided plugin.
     *
     * @throws IllegalStateException If the API is not enabled.
     */
    public void unregisterPluginAdvancementTabs() {
        getMain().unregisterAdvancementTabs(plugin);
    }

    /**
     * Returns an unmodifiable {@link Set} containing the namespaces of every registered advancement tab.
     *
     * @return An unmodifiable {@link Set} containing the namespaces of every registered advancement tab.
     * @throws IllegalStateException If the API is not enabled.
     */
    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public Set<@NotNull String> getAdvancementTabNamespaces() {
        return getMain().getAdvancementTabNamespaces();
    }

    /**
     * Returns the namespaced keys of every registered advancement which namespaced key starts with the provided one.
     *
     * @param input The partial namespaced key that acts as a filter.
     * @return A filtered {@link List} that contains only the matching namespaced keys of the registered advancements.
     * @throws IllegalStateException If the API is not enabled.
     */
    @NotNull
    @Contract(pure = true, value = "_ -> new")
    public List<@NotNull String> filterNamespaces(@Nullable String input) {
        return getMain().filterNamespaces(input);
    }

    /**
     * Returns an unmodifiable {@link Collection} of every registered advancement tab.
     *
     * @return An unmodifiable {@link Collection} of every registered advancement tab.
     * @throws IllegalStateException If the API is not enabled.
     */
    @NotNull
    @UnmodifiableView
    public Collection<@NotNull AdvancementTab> getTabs() {
        return getMain().getTabs();
    }

    /**
     * Updates every advancement to a player.
     * <p>An advancement is updated only if its tab is shown to the player (see {@link AdvancementTab#isShownTo(Player)}).
     *
     * @param player The player to be updated.
     * @throws IllegalStateException If the API is not enabled.
     */
    public void updatePlayer(@NotNull Player player) {
        getMain().updatePlayer(player);
    }

    /**
     * Returns the advancement with the provided namespaced key.
     *
     * @param namespacedKey The namespaced key of the advancement. It must be in the format {@code "namespace:key"}.
     * @return The advancement with the provided namespaced key., or {@code null} if it doesn't exist.
     * @throws IllegalStateException If the API is not enabled.
     */
    @Nullable
    public Advancement getAdvancement(@NotNull String namespacedKey) {
        return getMain().getAdvancement(namespacedKey);
    }

    /**
     * Returns the advancement with the provided namespaced key.
     *
     * @param namespace The namespace of the advancement's tab.
     * @param key The key of the advancement.
     * @return The advancement with the provided namespaced key, or {@code null} if it doesn't exist.
     * @throws IllegalStateException If the API is not enabled.
     */
    @Nullable
    public Advancement getAdvancement(@NotNull String namespace, @NotNull String key) {
        return getMain().getAdvancement(namespace, key);
    }

    /**
     * Returns the advancement with the provided namespaced key.
     *
     * @param namespacedKey The {@link AdvancementKey} of the advancement.
     * @return The advancement with the provided namespaced key, or {@code null} if it doesn't exist.
     * @throws IllegalStateException If the API is not enabled.
     */
    @Nullable
    public Advancement getAdvancement(@NotNull AdvancementKey namespacedKey) {
        return getMain().getAdvancement(namespacedKey);
    }

    /**
     * Displays a custom toast notification to a player.
     *
     * @param player The player the toast notification will be shown to.
     * @param display The {@link AdvancementDisplay} that contains the graphic information to show.
     */
    public void displayCustomToast(@NotNull Player player, @NotNull AdvancementDisplay display) {
        displayCustomToast(player, display.getIcon(), display.getTitle(), display.getFrame());
    }

    /**
     * Displays a custom toast notification to a player.
     *
     * @param player The player the toast notification will be shown to.
     * @param icon The item of the toast notification.
     * @param title The title of the toast notification.
     * @param frame The shape of the toast notification frame.
     */
    public void displayCustomToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame) {
        AdvancementUtils.displayToast(player, icon, title, frame);
    }

    /**
     * Disables vanilla advancements until the next server restart or reload.
     *
     * @throws RuntimeException If the operation fails. It is a wrapper for the real exception.
     */
    public void disableVanillaAdvancements() throws RuntimeException {
        try {
            AdvancementUtils.disableVanillaAdvancements();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't disable minecraft advancements.", e);
        }
    }

    /**
     * Disables vanilla recipe advancements (i.e. the advancements which unlock recipes) until the next server restart or reload.
     *
     * @throws RuntimeException If the operation fails. It is a wrapper for the real exception.
     */
    public void disableVanillaRecipeAdvancements() throws RuntimeException {
        try {
            AdvancementUtils.disableVanillaRecipeAdvancements();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't disable minecraft recipe advancements.", e);
        }
    }

    /**
     * Returns the {@link TeamProgression} of the team of the provided player.
     *
     * @param player The player.
     * @return The {@link TeamProgression} of the player's team.
     * @throws UserNotLoadedException If the player was not loaded into the caching system.
     *         For more information about the caching system see {@link DatabaseManager}.
     */
    @NotNull
    public TeamProgression getTeamProgression(@NotNull Player player) throws UserNotLoadedException {
        return getMain().getDatabaseManager().getTeamProgression(player);
    }

    /**
     * Returns the {@link TeamProgression} of the team of the provided player.
     *
     * @param uuid The {@link UUID} of the player.
     * @return The {@link TeamProgression} of the player's team.
     * @throws UserNotLoadedException If the player was not loaded into the caching system.
     *         For more information about the caching system see {@link DatabaseManager}.
     */
    @NotNull
    public TeamProgression getTeamProgression(@NotNull UUID uuid) throws UserNotLoadedException {
        return getMain().getDatabaseManager().getTeamProgression(uuid);
    }

    /**
     * Moves the provided player from their team to the second player's one.
     *
     * @param playerToMove The player to move.
     * @param aDestTeamPlayer A player of the destination team.
     */
    public void updatePlayerTeam(@NotNull Player playerToMove, @NotNull Player aDestTeamPlayer) {
        updatePlayerTeam(playerToMove, aDestTeamPlayer, null);
    }

    /**
     * Moves the provided player from their team to the second player's one.
     *
     * @param playerToMove The player to move.
     * @param aDestTeamPlayer A player of the destination team.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     */
    public void updatePlayerTeam(@NotNull Player playerToMove, @NotNull Player aDestTeamPlayer, @Nullable Consumer<Result> action) {
        Preconditions.checkNotNull(playerToMove, "Player to move is null.");
        Preconditions.checkNotNull(aDestTeamPlayer, "Destination player (representing destination team) is null.");
        callAfterLoad(playerToMove, aDestTeamPlayer, ds -> ds.updatePlayerTeam(playerToMove, aDestTeamPlayer), action);
    }

    /**
     * Moves the provided player from their team to the second player's one.
     *
     * @param playerToMove The {@link UUID} of the player to move.
     * @param aDestTeamPlayer The {@link UUID} of a player of the destination team.
     */
    public void updatePlayerTeam(@NotNull UUID playerToMove, @NotNull UUID aDestTeamPlayer) {
        updatePlayerTeam(playerToMove, aDestTeamPlayer, null);
    }

    /**
     * Moves the provided player from their team to the second player's one.
     *
     * @param playerToMove The {@link UUID} of the player to move.
     * @param aDestTeamPlayer The {@link UUID} of a player of the destination team.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     */
    public void updatePlayerTeam(@NotNull UUID playerToMove, @NotNull UUID aDestTeamPlayer, @Nullable Consumer<Result> action) {
        Preconditions.checkNotNull(playerToMove, "Player to move is null.");
        Preconditions.checkNotNull(aDestTeamPlayer, "Destination player (representing destination team) is null.");
        callAfterLoad(playerToMove, aDestTeamPlayer, ds -> ds.updatePlayerTeam(playerToMove, aDestTeamPlayer), action);
    }

    /**
     * Moves the provided player into a new team.
     *
     * @param playerToMove The player to be moved.
     */
    public void movePlayerInNewTeam(@NotNull Player playerToMove) {
        movePlayerInNewTeam(playerToMove, null);
    }

    /**
     * Moves the provided player into a new team.
     *
     * @param playerToMove The player to be moved.
     * @param action A {@link Consumer} that is called synchronously after the operation with an {@link ObjectResult},
     *         which provides the {@link TeamProgression} of the player's new team (when the operation is successful).
     *         If no code should be called, it can be put to {@code null}.
     */
    public void movePlayerInNewTeam(@NotNull Player playerToMove, @Nullable Consumer<ObjectResult<@NotNull TeamProgression>> action) {
        callAfterLoad(playerToMove, ds -> ds.movePlayerInNewTeam(playerToMove), action);
    }

    /**
     * Moves the provided player into a new team.
     *
     * @param playerToMove The {@link UUID} of the player to be moved.
     */
    public void movePlayerInNewTeam(@NotNull UUID playerToMove) {
        movePlayerInNewTeam(playerToMove, null);
    }

    /**
     * Moves the provided player into a new team.
     *
     * @param playerToMove The {@link UUID} of the player to be moved.
     * @param action A {@link Consumer} that is called synchronously after the operation with an {@link ObjectResult},
     *         which provides the {@link TeamProgression} of the player's new team (when the operation is successful).
     *         If no code should be called, it can be put to {@code null}.
     */
    public void movePlayerInNewTeam(@NotNull UUID playerToMove, @Nullable Consumer<ObjectResult<@NotNull TeamProgression>> action) {
        callAfterLoad(playerToMove, ds -> ds.movePlayerInNewTeam(playerToMove), action);
    }

    /**
     * Unregisters the provided offline player, removing they from the database.
     * <p>They must be offline and not loaded into the caching system.
     * For more information about the caching system see {@link DatabaseManager}.
     *
     * @param player The offline player to unregister.
     * @throws IllegalStateException If the player is online or loaded into the caching system.
     */
    public void unregisterOfflinePlayer(@NotNull OfflinePlayer player) throws IllegalStateException {
        unregisterOfflinePlayer(player, null);
    }

    /**
     * Unregisters the provided offline player, removing they from the database.
     * <p>They must be offline and not loaded into the caching system.
     * For more information about the caching system see {@link DatabaseManager}.
     *
     * @param player The offline player to unregister.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     * @throws IllegalStateException If the player is online or loaded into the caching system.
     */
    public void unregisterOfflinePlayer(@NotNull OfflinePlayer player, @Nullable Consumer<Result> action) throws IllegalStateException {
        callSyncIfNotNull(getMain().getDatabaseManager().unregisterOfflinePlayer(player), action);
    }

    /**
     * Unregisters the provided player, removing they from the database.
     * <p>They must be offline and not loaded into the caching system.
     * For more information about the caching system see {@link DatabaseManager}.
     *
     * @param uuid The {@link UUID} of the player to unregister.
     * @throws IllegalStateException If the player is online or loaded into the caching system.
     */
    public void unregisterOfflinePlayer(@NotNull UUID uuid) throws IllegalStateException {
        unregisterOfflinePlayer(uuid, null);
    }

    /**
     * Unregisters the provided player, removing they from the database.
     * <p>They must be offline and not loaded into the caching system.
     * For more information about the caching system see {@link DatabaseManager}.
     *
     * @param uuid The {@link UUID} of the player to unregister.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     * @throws IllegalStateException If the player is online or loaded into the caching system.
     */
    public void unregisterOfflinePlayer(@NotNull UUID uuid, @Nullable Consumer<Result> action) throws IllegalStateException {
        callSyncIfNotNull(getMain().getDatabaseManager().unregisterOfflinePlayer(uuid), action);
    }

    /**
     * Updates the name of the specified player in the database.
     *
     * @param player The player to update.
     */
    public void updatePlayerName(@NotNull Player player) {
        updatePlayerName(player, null);
    }

    /**
     * Updates the name of the specified player in the database.
     *
     * @param player The player to update.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     */
    public void updatePlayerName(@NotNull Player player, @Nullable Consumer<Result> action) {
        callSyncIfNotNull(getMain().getDatabaseManager().updatePlayerName(player), action);
    }

    /**
     * Gets whether the provided advancement is unredeemed for the specified player.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @param action A {@link Consumer} that is called synchronously after the operation with an {@link ObjectResult},
     *         which provides the {@link Boolean} result.
     */
    public void isUnredeemed(@NotNull Advancement advancement, @NotNull Player player, @NotNull Consumer<ObjectResult<@NotNull Boolean>> action) {
        isUnredeemed(advancement, uuidFromPlayer(player), action);
    }

    /**
     * Returns whether the provided advancement is unredeemed for the specified team.
     *
     * @param advancement The advancement.
     * @param uuid The {@link UUID} of the player.
     * @param action A {@link Consumer} that is called synchronously after the operation with an {@link ObjectResult},
     *         which provides the {@link Boolean} result.
     */
    public void isUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, @NotNull Consumer<ObjectResult<@NotNull Boolean>> action) {
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        Preconditions.checkNotNull(uuid, "UUID is null.");
        Preconditions.checkNotNull(action, "Consumer is null.");
        // Don't query if advancement isn't granted, it should always return false
        if (!advancement.isGranted(uuid)) {
            action.accept(new ObjectResult<>(false));
            return;
        }
        callAfterLoad(uuid, ds -> ds.isUnredeemed(advancement.getKey(), uuid), action);
    }

    /**
     * Sets the provided advancement unredeemed for the specified player.
     * <p>Rewards will be given on redeem.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @throws NotGrantedException If the advancement is not granted for the specified player.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player) throws NotGrantedException {
        setUnredeemed(advancement, player, true);
    }

    /**
     * Sets the provided advancement unredeemed for the specified player.
     * <p>Rewards will be given on redeem.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     * @throws NotGrantedException If the advancement is not granted for the specified player.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player, @Nullable Consumer<Result> action) throws NotGrantedException {
        setUnredeemed(advancement, player, true, action);
    }

    /**
     * Sets the provided advancement unredeemed for the specified player.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @param giveRewards Whether to give rewards on redeem.
     * @throws NotGrantedException If the advancement is not granted for the specified player.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player, boolean giveRewards) throws NotGrantedException {
        setUnredeemed(advancement, uuidFromPlayer(player), giveRewards);
    }

    /**
     * Sets the provided advancement unredeemed for the specified player.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @param giveRewards Whether to give rewards on redeem.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     * @throws NotGrantedException If the advancement is not granted for the specified player.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player, boolean giveRewards, @Nullable Consumer<Result> action) throws NotGrantedException {
        setUnredeemed(advancement, uuidFromPlayer(player), giveRewards, action);
    }

    /**
     * Sets the provided advancement unredeemed for the specified player.
     * <p>Rewards will be given on redeem.
     *
     * @param advancement The advancement.
     * @param uuid The {@link UUID} of the player.
     * @throws NotGrantedException If the advancement is not granted for the specified player.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid) throws NotGrantedException {
        setUnredeemed(advancement, uuid, true);
    }

    /**
     * Sets the provided advancement unredeemed for the specified player.
     * <p>Rewards will be given on redeem.
     *
     * @param advancement The advancement.
     * @param uuid The {@link UUID} of the player.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     * @throws NotGrantedException If the advancement is not granted for the specified player.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, @Nullable Consumer<Result> action) throws NotGrantedException {
        setUnredeemed(advancement, uuid, true, action);
    }

    /**
     * Sets the provided advancement unredeemed for the specified player.
     *
     * @param advancement The advancement.
     * @param uuid The {@link UUID} of the player.
     * @param giveRewards Whether to give rewards on redeem.
     * @throws NotGrantedException If the advancement is not granted for the specified player.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, boolean giveRewards) throws NotGrantedException {
        setUnredeemed(advancement, uuid, giveRewards, null);
    }

    /**
     * Sets the provided advancement unredeemed for the specified player.
     *
     * @param advancement The advancement.
     * @param uuid The {@link UUID} of the player.
     * @param giveRewards Whether to give rewards on redeem.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     * @throws NotGrantedException If the advancement is not granted for the specified player.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, boolean giveRewards, @Nullable Consumer<Result> action) throws NotGrantedException {
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        Preconditions.checkNotNull(uuid, "UUID is null.");
        // Don't call update if advancement isn't granted, it may throw a foreign key exception
        if (!advancement.isGranted(uuid)) {
            throw new NotGrantedException();
        }

        callAfterLoad(uuid, ds -> ds.setUnredeemed(advancement.getKey(), giveRewards, uuid), action);
    }

    /**
     * Redeems the specified advancement for the provided player.
     *
     * @param advancement The advancement.
     * @param player The player.
     */
    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull Player player) {
        unsetUnredeemed(advancement, player, null);
    }

    /**
     * Redeems the specified advancement for the provided player.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     */
    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull Player player, @Nullable Consumer<Result> action) {
        unsetUnredeemed(advancement, uuidFromPlayer(player), action);
    }

    /**
     * Redeems the specified advancement for the provided player.
     *
     * @param advancement The advancement.
     * @param uuid The {@link UUID} of the player.
     */
    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid) {
        unsetUnredeemed(advancement, uuid, null);
    }

    /**
     * Redeems the specified advancement for the provided player.
     *
     * @param advancement The advancement.
     * @param uuid The {@link UUID} of the player.
     * @param action A {@link Consumer} that is called synchronously after the operation with a {@link Result}.
     *         If no code should be called, it can be put to {@code null}.
     */
    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, @Nullable Consumer<Result> action) {
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        Preconditions.checkNotNull(uuid, "UUID is null.");
        callAfterLoad(uuid, ds -> ds.unsetUnredeemed(advancement.getKey(), uuid), action);
    }

    /**
     * Returns whether the provided player is loaded into the caching system.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param player The player.
     * @return Whether the provided player is loaded into the caching system.
     */
    public boolean isLoaded(@NotNull Player player) {
        return getMain().getDatabaseManager().isLoaded(player);
    }

    /**
     * Returns whether the provided offline player is loaded into the caching system.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param player The offline player.
     * @return Whether the provided player is loaded into the caching system.
     */
    public boolean isLoaded(@NotNull OfflinePlayer player) {
        return getMain().getDatabaseManager().isLoaded(player);
    }

    /**
     * Returns whether the provided offline player is loaded into the caching system.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param uuid The {@link UUID} of the player.
     * @return Whether the provided player is loaded into the caching system.
     */
    public boolean isLoaded(@NotNull UUID uuid) {
        return getMain().getDatabaseManager().isLoaded(uuid);
    }

    /**
     * Loads the provided offline player from the database into the caching system.
     * <p>The offline player will be loaded manually (see {@link CacheFreeingOption#MANUAL(Plugin)}),
     * so {@link #unloadOfflinePlayer(OfflinePlayer)} must be called to unload they.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param player The offline player to load.
     */
    public void loadOfflinePlayer(@NotNull OfflinePlayer player) {
        loadOfflinePlayer(uuidFromPlayer(player));
    }

    /**
     * Loads the provided player from the database into the caching system.
     * <p>The player will be loaded manually (see {@link CacheFreeingOption#MANUAL(Plugin)}),
     * so {@link #unloadOfflinePlayer(UUID)} must be called to unload they.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param uuid The {@link UUID} of the player to load.
     */
    public void loadOfflinePlayer(@NotNull UUID uuid) {
        loadOfflinePlayer(uuid, CacheFreeingOption.MANUAL(plugin), null);
    }

    /**
     * Loads the provided offline player from the database without putting they into the caching system (see {@link CacheFreeingOption#DONT_CACHE()}).
     * <p>The loaded {@link TeamProgression} can be retrieved through the {@link Consumer}&lt;{@link ObjectResult}&gt;.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param player The offline player to load.
     * @param action A {@link Consumer} that is called synchronously after the operation with an {@link ObjectResult},
     *         which provides the loaded {@link TeamProgression}.
     */
    public void loadOfflinePlayer(@NotNull OfflinePlayer player, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        loadOfflinePlayer(uuidFromPlayer(player), action);
    }

    /**
     * Loads the provided player from the database without putting they into the caching system (see {@link CacheFreeingOption#DONT_CACHE()}).
     * <p>The loaded {@link TeamProgression} can be retrieved through the {@link Consumer}&lt;{@link ObjectResult}&gt;.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param uuid The {@link UUID} of the player to load.
     * @param action A {@link Consumer} that is called synchronously after the operation with an {@link ObjectResult},
     *         which provides the loaded {@link TeamProgression}.
     */
    public void loadOfflinePlayer(@NotNull UUID uuid, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        loadOfflinePlayer(uuid, CacheFreeingOption.DONT_CACHE(), action);
    }

    /**
     * Loads the provided offline player from the database with the specified {@link CacheFreeingOption}.
     * <p>For more information about the {@link CacheFreeingOption} see {@link DatabaseManager#loadOfflinePlayer(UUID, CacheFreeingOption)}.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param player The offline player to load.
     * @param option The chosen {@link CacheFreeingOption}.
     * @param action A {@link Consumer} that is called synchronously after the operation with an {@link ObjectResult},
     *         which provides the loaded {@link TeamProgression}.
     */
    public void loadOfflinePlayer(@NotNull OfflinePlayer player, @NotNull CacheFreeingOption option, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        loadOfflinePlayer(uuidFromPlayer(player), option, action);
    }

    /**
     * Loads the provided player from the database with the specified {@link CacheFreeingOption}.
     * <p>For more information about the {@link CacheFreeingOption} see {@link DatabaseManager#loadOfflinePlayer(UUID, CacheFreeingOption)}.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param uuid The {@link UUID} of the player to load.
     * @param option The chosen {@link CacheFreeingOption}.
     * @param action A {@link Consumer} that is called synchronously after the operation with an {@link ObjectResult},
     *         which provides the loaded {@link TeamProgression}.
     */
    public void loadOfflinePlayer(@NotNull UUID uuid, @NotNull CacheFreeingOption option, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        callSyncIfNotNull(getMain().getDatabaseManager().loadOfflinePlayer(uuid, option), action);
    }

    /**
     * Returns whether at least one loading request is currently active for the specified offline player.
     *
     * @param player The offline player.
     * @return Whether at least one loading request is currently active for the specified offline player.
     */
    public boolean isOfflinePlayerLoaded(@NotNull OfflinePlayer player) {
        return isOfflinePlayerLoaded(uuidFromPlayer(player));
    }

    /**
     * Returns whether at least one loading request is currently active for the specified player.
     *
     * @param uuid The {@link UUID} of the player.
     * @return Whether at least one loading request is currently active for the specified player.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean isOfflinePlayerLoaded(UUID uuid) {
        return getMain().getDatabaseManager().isOfflinePlayerLoaded(uuid, plugin);
    }

    /**
     * Returns the number of currently active loading requests for the specified player with the provided {@link CacheFreeingOption.Option}.
     *
     * @param player The player.
     * @param type The {@link CacheFreeingOption.Option}.
     * @return The number of the currently active player loading requests.
     * @see DatabaseManager#getLoadingRequestsAmount(Plugin, UUID, Option)
     */
    public int getLoadingRequestsAmount(@NotNull Player player, @NotNull CacheFreeingOption.Option type) {
        return getLoadingRequestsAmount(uuidFromPlayer(player), type);
    }

    /**
     * Returns the number of currently active loading requests for the specified offline player with the provided {@link CacheFreeingOption.Option}.
     *
     * @param offlinePlayer The offline player.
     * @param type The {@link CacheFreeingOption.Option}.
     * @return The number of the currently active player loading requests.
     * @see DatabaseManager#getLoadingRequestsAmount(Plugin, UUID, Option)
     */
    public int getLoadingRequestsAmount(@NotNull OfflinePlayer offlinePlayer, @NotNull CacheFreeingOption.Option type) {
        return getLoadingRequestsAmount(uuidFromPlayer(offlinePlayer), type);
    }

    /**
     * Returns the number of currently active loading requests for the specified player with the provided {@link CacheFreeingOption.Option}.
     *
     * @param uuid The {@link UUID} of the player.
     * @param type The {@link CacheFreeingOption.Option}.
     * @return The number of the currently active player loading requests.
     * @see DatabaseManager#getLoadingRequestsAmount(Plugin, UUID, Option)
     */
    public int getLoadingRequestsAmount(@NotNull UUID uuid, @NotNull CacheFreeingOption.Option type) {
        return getMain().getDatabaseManager().getLoadingRequestsAmount(plugin, uuid, type);
    }

    /**
     * Removes one manual caching request for the provided offline player. If the counter reaches zero, they will be unloaded from the caching system.
     * <p>Note that this method will only unload players loaded with {@link CacheFreeingOption#MANUAL(Plugin)}.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param player The player to unload.
     */
    public void unloadOfflinePlayer(@NotNull OfflinePlayer player) {
        unloadOfflinePlayer(uuidFromPlayer(player));
    }

    /**
     * Removes one manual caching request for the provided player. If the counter reaches zero, they will be unloaded from the caching system.
     * <p>Note that this method will only unload players loaded with {@link CacheFreeingOption#MANUAL(Plugin)}.
     * <p>For more information about the caching system see {@link DatabaseManager}.
     *
     * @param uuid The {@link UUID} of the player to unload.
     */
    public void unloadOfflinePlayer(@NotNull UUID uuid) {
        getMain().getDatabaseManager().unloadOfflinePlayer(uuid, plugin);
    }

    /**
     * Gets the in-database stored name of the provided player.
     *
     * @param player The player.
     * @param action A {@link Consumer} that is called synchronously after the operation with an {@link ObjectResult},
     *         which provides the in-database stored name of the player.
     */
    public void getStoredPlayerName(@NotNull OfflinePlayer player, @NotNull Consumer<ObjectResult<@Nullable String>> action) {
        getStoredPlayerName(uuidFromPlayer(player), action);
    }

    /**
     * Gets the in-database stored name of the provided player.
     *
     * @param uuid The {@link UUID} of the player.
     * @param action A {@link Consumer} that is called synchronously after the operation with an {@link ObjectResult},
     *         which provides the in-database stored name of the player.
     */
    public void getStoredPlayerName(@NotNull UUID uuid, @NotNull Consumer<ObjectResult<@Nullable String>> action) {
        Preconditions.checkNotNull(action, "Consumer is null.");
        getMain().getDatabaseManager().getStoredPlayerName(uuid).thenAccept(s -> runSync(plugin, () -> action.accept(s)));
    }

    private <T extends Result> void callAfterLoad(@NotNull Player player, @NotNull Function<DatabaseManager, CompletableFuture<T>> internalAction, @Nullable Consumer<T> action) {
        callAfterLoad(uuidFromPlayer(player), internalAction, action);
    }

    private <T extends Result> void callAfterLoad(@NotNull UUID uuid, @NotNull Function<DatabaseManager, CompletableFuture<T>> internalAction, @Nullable Consumer<T> action) {
        Preconditions.checkNotNull(uuid, "UUID is null.");
        final DatabaseManager ds = getMain().getDatabaseManager();
        ds.loadOfflinePlayer(uuid, CacheFreeingOption.MANUAL(plugin)).thenAccept(t1 -> {
            if (t1.isExceptionOccurred()) {
                new RuntimeException("An exception occurred while loading user " + uuid + ':', t1.getOccurredException()).printStackTrace();
                return;
            }
            CompletableFuture<T> c;
            try {
                c = internalAction.apply(ds);
            } catch (Exception t) {
                new RuntimeException("An exception occurred while calling API method:", t).printStackTrace();
                ds.unloadOfflinePlayer(uuid, plugin);
                return;
            }
            c.thenAccept(b -> {
                if (action != null) {
                    runSync(plugin, () -> {
                        try {
                            if (plugin.isEnabled())
                                action.accept(b);
                        } catch (Exception t) {
                            new RuntimeException("An exception occurred while calling " + plugin.getName() + "'s Consumer:", t).printStackTrace();
                        } finally {
                            ds.unloadOfflinePlayer(uuid, plugin);
                        }
                    });
                } else
                    ds.unloadOfflinePlayer(uuid, plugin);
            });
        });
    }

    private <T extends Result> void callAfterLoad(@NotNull Player player1, @NotNull Player player2, @NotNull Function<DatabaseManager, CompletableFuture<T>> internalAction, @Nullable Consumer<T> action) {
        callAfterLoad(Objects.requireNonNull(player1, "Player1 is null.").getUniqueId(), Objects.requireNonNull(player2, "Player2 is null.").getUniqueId(), internalAction, action);
    }

    private <T extends Result> void callAfterLoad(@NotNull UUID uuid1, @NotNull UUID uuid2, @NotNull Function<DatabaseManager, CompletableFuture<T>> internalAction, @Nullable Consumer<T> action) {
        final DatabaseManager ds = getMain().getDatabaseManager();
        final CacheFreeingOption cacheFreeingOption = CacheFreeingOption.MANUAL(plugin);
        ds.loadOfflinePlayer(uuid1, cacheFreeingOption).thenAccept(t1 -> {
            if (t1.isExceptionOccurred()) {
                new RuntimeException("An exception occurred while loading 1st user " + uuid1 + ':', t1.getOccurredException()).printStackTrace();
            } else {
                ds.loadOfflinePlayer(uuid2, cacheFreeingOption).thenAccept(t2 -> {
                    if (t2.isExceptionOccurred()) {
                        new RuntimeException("An exception occurred while loading 2nd user " + uuid2 + ':', t2.getOccurredException()).printStackTrace();
                        ds.unloadOfflinePlayer(uuid1, plugin);
                        return;
                    }
                    CompletableFuture<T> c;
                    try {
                        c = internalAction.apply(ds);
                    } catch (Exception t) {
                        new RuntimeException("An exception occurred while calling API method:", t).printStackTrace();
                        ds.unloadOfflinePlayer(uuid1, plugin);
                        ds.unloadOfflinePlayer(uuid2, plugin);
                        return;
                    }
                    c.thenAccept(b -> {
                        if (action != null) {
                            runSync(plugin, () -> {
                                try {
                                    if (plugin.isEnabled())
                                        action.accept(b);
                                } catch (Exception t) {
                                    new RuntimeException("An exception occurred while calling " + plugin.getName() + "'s Consumer:", t).printStackTrace();
                                } finally {
                                    ds.unloadOfflinePlayer(uuid1, plugin);
                                    ds.unloadOfflinePlayer(uuid2, plugin);
                                }
                            });
                        } else {
                            ds.unloadOfflinePlayer(uuid1, plugin);
                            ds.unloadOfflinePlayer(uuid2, plugin);
                        }
                    });
                });
            }
        });
    }

    private <T extends Result> void callSyncIfNotNull(@NotNull CompletableFuture<T> completableFuture, @Nullable Consumer<T> action) {
        if (action != null) {
            completableFuture.thenAccept(t -> runSync(plugin, () -> action.accept(t)));
        }
    }

    private UltimateAdvancementAPI() {
        throw new UnsupportedOperationException();
    }
}
