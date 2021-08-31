package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.database.CacheFreeingOption;
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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.Validate;
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
 * All utility methods to use properly the API.
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class UltimateAdvancementAPI {

    /**
     * Main class of the plugin.@see AdvancementMain
     */
    static AdvancementMain main;

    /**
     * Returns the instance to use all the methods of UltimateAdvancementAPI class.
     *
     * @param plugin The plugin instance.
     * @return The instance to use UltimateAdvancementAPI class.
     * @throws APINotInstantiatedException If the API is not instantiated properly.
     */
    @NotNull
    public static UltimateAdvancementAPI getInstance(@NotNull Plugin plugin) throws APINotInstantiatedException {
        Validate.notNull(plugin, "Plugin is null.");
        Validate.isTrue(plugin.isEnabled(), "Plugin is not enabled.");
        if (main == null) {
            throw new APINotInstantiatedException();
        }
        return new UltimateAdvancementAPI(plugin);
    }

    private final Plugin plugin;

    /**
     * Create a new advancement tab.
     *
     * @param namespace The unique name of the tab.
     * @return A new advancement tab.
     * @throws DuplicatedException If there are more than 1 tab with the same unique name.
     */
    @NotNull
    @Contract("_ -> new")
    public AdvancementTab createAdvancementTab(@NotNull String namespace) throws DuplicatedException {
        return main.createAdvancementTab(plugin, namespace);
    }

    /**
     * Returns the wanted advancement tab.
     *
     * @param namespace The unique name of the wanted advancement tab.
     * @return The wanted advancement tab.
     */
    @Nullable
    public AdvancementTab getAdvancementTab(@NotNull String namespace) {
        return main.getAdvancementTab(namespace);
    }

    /**
     * Returns if the advancement tab has already been registered.
     *
     * @param namespace The unique name of the advancement tab.
     * @return If the advancement tab has already been registered.
     */
    public boolean isAdvancementTabRegistered(@NotNull String namespace) {
        return main.isAdvancementTabRegistered(namespace);
    }

    /**
     * Returns an unmodifiable list with the registered advancement tabs of your plugin.
     *
     * @return An unmodifiable list with the registered advancement tabs of your plugin.
     */
    @UnmodifiableView
    @NotNull
    public Collection<@NotNull AdvancementTab> getPluginAdvancementTabs() {
        return main.getPluginAdvancementTabs(plugin);
    }

    /**
     * Unregister an advancement tab.
     *
     * @param namespace The unique name of the advancement tab to be unregistered.
     */
    public void unregisterAdvancementTab(@NotNull String namespace) {
        main.unregisterAdvancementTab(namespace);
    }

    /**
     * Unregister all advancement tabs of this plugin.
     */
    public void unregisterPluginAdvancementTabs() {
        main.unregisterAdvancementTabs(plugin);
    }

    /**
     * Returns a list of all advancement unique names.
     *
     * @return A list of all advancement unique names.
     */
    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public Set<@NotNull String> getAdvancementTabNamespaces() {
        return main.getAdvancementTabNamespaces();
    }

    /**
     * Filter by name the unique names of the advancement tabs.
     *
     * @param input The filter.
     * @return A filtered list of the advancement tab unique names.
     */
    @NotNull
    @Contract(pure = true, value = "_ -> new")
    public List<@NotNull String> filterNamespaces(@Nullable String input) {
        return main.filterNamespaces(input);
    }

    /**
     * Returns an unmodifiable list with all registered advancement tabs.
     *
     * @return An unmodifiable list with all registered advancement tabs.
     */
    @NotNull
    @UnmodifiableView
    public Collection<@NotNull AdvancementTab> getTabs() {
        return main.getTabs();
    }

    /**
     * Updates every advancement for a player.
     *
     * @param player A player.
     */
    public void updatePlayer(@NotNull Player player) {
        main.updatePlayer(player);
    }

    /**
     * Returns the wanted advancement.
     * i.g. of the namespaced key: "namespace:key"
     *
     * @param namespacedKey The namespace of the advancement tab and the unique key of the advancement.
     * @return The wanted advancement, or {@code null}.
     */
    @Nullable
    public Advancement getAdvancement(@NotNull String namespacedKey) {
        return main.getAdvancement(namespacedKey);
    }

    /**
     * Returns the wanted advancement.
     *
     * @param namespace The namespace of the advancement tab.
     * @param key The unique key of the advancement.
     * @return The wanted advancement, or {@code null}.
     */
    @Nullable
    public Advancement getAdvancement(@NotNull String namespace, @NotNull String key) {
        return main.getAdvancement(namespace, key);
    }

    /**
     * Returns the wanted advancement.
     *
     * @param namespacedKey The {@link AdvancementKey} of the advancement.
     * @return The wanted advancement, or {@code null}.
     */
    @Nullable
    public Advancement getAdvancement(@NotNull AdvancementKey namespacedKey) {
        return main.getAdvancement(namespacedKey);
    }

    /**
     * Displays a custom toast to a player.
     *
     * @param player A player to show the toast.
     * @param display The advancement display to know the information to show.
     */
    public void displayCustomToast(@NotNull Player player, @NotNull AdvancementDisplay display) {
        displayCustomToast(player, display.getIcon(), display.getTitle(), display.getFrame());
    }

    /**
     * Displays a custom toast to a player.
     *
     * @param player A player to show the toast.
     * @param icon The displayed item of the toast.
     * @param title The displayed title of the toast.
     * @param frame The {@link AdvancementFrameType} of the toast.
     */
    public void displayCustomToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame) {
        AdvancementUtils.displayToast(player, icon, title, frame);
    }

    /**
     * Disable vanilla advancement.
     *
     * @throws RuntimeException This exception is a wrapper of the real one which may have been thrown while disabling vanilla advancements.
     */
    public void disableVanillaAdvancements() throws RuntimeException {
        try {
            AdvancementUtils.disableVanillaAdvancements();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't disable minecraft advancements.", e);
        }
    }

    /**
     * Returns the progression of a player's team.
     *
     * @param player The player of the team.
     * @return The team progression.
     * @throws UserNotLoadedException If the player was not loaded into the database.
     */
    @NotNull
    public TeamProgression getProgression(@NotNull Player player) throws UserNotLoadedException {
        return main.getDatabaseManager().getProgression(player);
    }

    /**
     * Returns the progression of a player's team.
     *
     * @param uuid The UUID player of the team.
     * @return The team progression.
     * @throws UserNotLoadedException If the player was not loaded into the database.
     */
    @NotNull
    public TeamProgression getProgression(@NotNull UUID uuid) throws UserNotLoadedException {
        return main.getDatabaseManager().getProgression(uuid);
    }

    /**
     * Moves a player from a team to another.
     *
     * @param playerToMove The player to move.
     * @param aDestTeamPlayer The player who belongs to the target team.
     */
    public void updatePlayerTeam(@NotNull Player playerToMove, @NotNull Player aDestTeamPlayer) {
        updatePlayerTeam(playerToMove, aDestTeamPlayer, null);
    }

    /**
     * Moves a player from a team to another.
     *
     * @param playerToMove The player to move.
     * @param aDestTeamPlayer The player who belongs to the target team.
     * @param action Code to run with the operation result.
     */
    public void updatePlayerTeam(@NotNull Player playerToMove, @NotNull Player aDestTeamPlayer, @Nullable Consumer<Result> action) {
        Validate.notNull(playerToMove, "Player to move is null.");
        Validate.notNull(aDestTeamPlayer, "Destination player (representing destination team) is null.");
        callAfterLoad(playerToMove, aDestTeamPlayer, ds -> ds.updatePlayerTeam(playerToMove, aDestTeamPlayer), action);
    }

    /**
     * Moves a player from a team to another.
     *
     * @param playerToMove The UUID player to move.
     * @param aDestTeamPlayer The UUID player who belongs to the target team.
     */
    public void updatePlayerTeam(@NotNull UUID playerToMove, @NotNull UUID aDestTeamPlayer) {
        updatePlayerTeam(playerToMove, aDestTeamPlayer, null);
    }

    /**
     * Moves a player from a team to another.
     *
     * @param playerToMove The UUID player to move.
     * @param aDestTeamPlayer The UUID player who belongs to the target team.
     * @param action The code to run with the operation result.
     */
    public void updatePlayerTeam(@NotNull UUID playerToMove, @NotNull UUID aDestTeamPlayer, @Nullable Consumer<Result> action) {
        Validate.notNull(playerToMove, "Player to move is null.");
        Validate.notNull(aDestTeamPlayer, "Destination player (representing destination team) is null.");
        callAfterLoad(playerToMove, aDestTeamPlayer, ds -> ds.updatePlayerTeam(playerToMove, aDestTeamPlayer), action);
    }

    /**
     * Create a new team for a player.
     *
     * @param playerToMove The player to move.
     */
    public void movePlayerInNewTeam(@NotNull Player playerToMove) {
        movePlayerInNewTeam(playerToMove, null);
    }

    /**
     * Create a new team for a player.
     *
     * @param playerToMove The player to move.
     * @param action The code to run with the operation result.
     */
    public void movePlayerInNewTeam(@NotNull Player playerToMove, @Nullable Consumer<ObjectResult<@NotNull TeamProgression>> action) {
        callAfterLoad(playerToMove, ds -> ds.movePlayerInNewTeam(playerToMove), action);
    }

    /**
     * Create a new team for a player.
     *
     * @param playerToMove The UUID player to move.
     */
    public void movePlayerInNewTeam(@NotNull UUID playerToMove) {
        movePlayerInNewTeam(playerToMove, null);
    }

    /**
     * Create a new team for a player.
     *
     * @param playerToMove The player to move.
     * @param action The code to run with the operation result.
     */
    public void movePlayerInNewTeam(@NotNull UUID playerToMove, @Nullable Consumer<ObjectResult<@NotNull TeamProgression>> action) {
        callAfterLoad(playerToMove, ds -> ds.movePlayerInNewTeam(playerToMove), action);
    }

    /**
     * Unregisters an offline player.
     *
     * @param player The offline player to unregister.
     * @throws IllegalStateException Whether the player is online or temporarily loaded into the database cache.
     */
    public void unregisterOfflinePlayer(@NotNull OfflinePlayer player) throws IllegalStateException {
        unregisterOfflinePlayer(player, null);
    }

    /**
     * Unregisters an offline player.
     *
     * @param player The offline player to unregister.
     * @param action The code to run with the operation result.
     * @throws IllegalStateException Whether the player is online or temporarily loaded into the database cache.
     */
    public void unregisterOfflinePlayer(@NotNull OfflinePlayer player, @Nullable Consumer<Result> action) throws IllegalStateException {
        callSyncIfNotNull(main.getDatabaseManager().unregisterOfflinePlayer(player), action);
    }

    /**
     * Unregisters an offline player.
     *
     * @param uuid The UUID player to unregister.
     * @throws IllegalStateException Whether the player is online or temporarily loaded into the database cache.
     */
    public void unregisterOfflinePlayer(@NotNull UUID uuid) throws IllegalStateException {
        unregisterOfflinePlayer(uuid, null);
    }

    /**
     * Unregisters an offline player.
     *
     * @param uuid The UUID player to unregister.
     * @param action The code to run with the operation result.
     * @throws IllegalStateException Whether the player is online or temporarily loaded into the database cache.
     */
    public void unregisterOfflinePlayer(@NotNull UUID uuid, @Nullable Consumer<Result> action) throws IllegalStateException {
        callSyncIfNotNull(main.getDatabaseManager().unregisterOfflinePlayer(uuid), action);
    }

    /**
     * Update the name of a player saved in the database.
     *
     * @param player The player to update.
     */
    public void updatePlayerName(@NotNull Player player) {
        updatePlayerName(player, null);
    }

    /**
     * Update the name of a player saved in the database.
     *
     * @param player The player to update.
     * @param action The code to run with the operation result.
     */
    public void updatePlayerName(@NotNull Player player, @Nullable Consumer<Result> action) {
        callSyncIfNotNull(main.getDatabaseManager().updatePlayerName(player), action);
    }



    /**
     * If the advancement has been completed but no rewards have been given yet.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @param action The code to run with the operation result.
     */
    public void isUnredeemed(@NotNull Advancement advancement, @NotNull Player player, @NotNull Consumer<ObjectResult<@NotNull Boolean>> action) {
        isUnredeemed(advancement, uuidFromPlayer(player), action);
    }

    /**
     * If the advancement has been completed but no rewards have been given yet.
     *
     * @param advancement The advancement.
     * @param uuid The UUID player.
     * @param action The code to run with the operation result.
     */
    public void isUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, @NotNull Consumer<ObjectResult<@NotNull Boolean>> action) {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(uuid, "UUID is null.");
        Validate.notNull(action, "Consumer is null.");
        // Don't query if advancement isn't granted, it should always return false
        if (!advancement.isGranted(uuid)) {
            action.accept(new ObjectResult<>(false));
            return;
        }
        callAfterLoad(uuid, ds -> ds.isUnredeemed(advancement.getKey(), uuid), action);
    }

    /**
     * Sets an advancement as unredeemed.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @throws NotGrantedException This requires that the advancement be completed.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player) throws NotGrantedException {
        setUnredeemed(advancement, player, true);
    }

    /**
     * Sets an advancement as unredeemed.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @param action The code to run with the operation result.
     * @throws NotGrantedException This requires that the advancement be completed.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player, @Nullable Consumer<Result> action) throws NotGrantedException {
        setUnredeemed(advancement, player, true, action);
    }

    /**
     * Sets if an advancement is unredeemed.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @param giveRewards {@code true} to set the advancement as unredeemed, {@code false} otherwise.
     * @throws NotGrantedException This requires that the advancement be completed.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player, boolean giveRewards) throws NotGrantedException {
        setUnredeemed(advancement, uuidFromPlayer(player), giveRewards);
    }

    /**
     * Sets if an advancement is unredeemed.
     *
     * @param advancement The advancement.
     * @param player The player.
     * @param giveRewards {@code true} to set the advancement as unredeemed, {@code false} otherwise.
     * @param action The code to run with the operation result.
     * @throws NotGrantedException This requires that the advancement be completed.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player, boolean giveRewards, @Nullable Consumer<Result> action) throws NotGrantedException {
        setUnredeemed(advancement, uuidFromPlayer(player), giveRewards, action);
    }

    /**
     * Sets an advancement as unredeemed.
     *
     * @param advancement The advancement.
     * @param uuid The UUID player.
     * @throws NotGrantedException This requires that the advancement be completed.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid) throws NotGrantedException {
        setUnredeemed(advancement, uuid, true);
    }

    /**
     * Sets an advancement as unredeemed.
     *
     * @param advancement The advancement.
     * @param uuid The UUID player.
     * @param action The code to run with the operation result.
     * @throws NotGrantedException This requires that the advancement be completed.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, @Nullable Consumer<Result> action) throws NotGrantedException {
        setUnredeemed(advancement, uuid, true, action);
    }

    /**
     * Sets if an advancement is unredeemed.
     *
     * @param advancement The advancement.
     * @param uuid The UUID player.
     * @param giveRewards {@code true} to set the advancement as unredeemed, {@code false} otherwise.
     * @throws NotGrantedException This requires that the advancement be completed.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, boolean giveRewards) throws NotGrantedException {
        setUnredeemed(advancement, uuid, giveRewards, null);
    }

    /**
     * Sets if an advancement is unredeemed.
     *
     * @param advancement The advancement.
     * @param uuid The UUID player.
     * @param giveRewards {@code true} to set the advancement as unredeemed, {@code false} otherwise.
     * @param action The code to run with the operation result.
     * @throws NotGrantedException This requires that the advancement be completed.
     */
    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, boolean giveRewards, @Nullable Consumer<Result> action) throws NotGrantedException {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(uuid, "UUID is null.");
        // Don't call update if advancement isn't granted, it may throw a foreign key exception
        if (!advancement.isGranted(uuid)) {
            throw new NotGrantedException();
        }

        callAfterLoad(uuid, ds -> ds.setUnredeemed(advancement.getKey(), giveRewards, uuid), action);
    }

    /**
     * Unsets an advancement as unredeemed.
     *
     * @param advancement The advancement.
     * @param player The player of a team.
     */
    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull Player player) {
        unsetUnredeemed(advancement, player, null);
    }

    /**
     * Unsets an advancement as unredeemed.
     *
     * @param advancement The advancement.
     * @param player The player of a team.
     * @param action The code to run with the operation result.
     */
    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull Player player, @Nullable Consumer<Result> action) {
        unsetUnredeemed(advancement, uuidFromPlayer(player), action);
    }

    /**
     * Unsets an advancement as unredeemed.
     *
     * @param advancement The advancement.
     * @param uuid The UUID player of a team.
     */
    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid) {
        unsetUnredeemed(advancement, uuid, null);
    }

    /**
     * Unsets an advancement as unredeemed.
     *
     * @param advancement The advancement.
     * @param uuid The UUID player of a team.
     * @param action The code to run with the operation result.
     */
    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, @Nullable Consumer<Result> action) {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(uuid, "UUID is null.");
        callAfterLoad(uuid, ds -> ds.unsetUnredeemed(advancement.getKey(), uuid), action);
    }

    /**
     * Returns if a player is loaded and cached.
     *
     * @param player The player of a team.
     * @return If a player is loaded.
     */
    public boolean isLoaded(@NotNull Player player) {
        return main.getDatabaseManager().isLoaded(player);
    }

    /**
     * Returns if a player is loaded and cached.
     *
     * @param uuid The UUID player of a team.
     * @return If a player is loaded.
     */
    public boolean isLoaded(@NotNull UUID uuid) {
        return main.getDatabaseManager().isLoaded(uuid);
    }

    /**
     * Loads from database the information about a team.
     * <p>The player will be loaded as {@link CacheFreeingOption#MANUAL(Plugin)} so then use {@link #unloadOfflinePlayer(OfflinePlayer)}
     *
     * @param player The player of a team.
     */
    public void loadOfflinePlayer(@NotNull OfflinePlayer player) {
        loadOfflinePlayer(uuidFromPlayer(player));
    }

    /**
     * Loads from database the information about a team.
     * <p>The player will be loaded as {@link CacheFreeingOption#MANUAL(Plugin)} so then use {@link #unloadOfflinePlayer(OfflinePlayer)}
     *
     * @param uuid The UUID player of a team.
     */
    public void loadOfflinePlayer(@NotNull UUID uuid) {
        loadOfflinePlayer(uuid, CacheFreeingOption.MANUAL(plugin), null);
    }

    /**
     * Loads from database the information about a team.
     * <p>The player will be loaded as {@link CacheFreeingOption#DONT_CACHE()}.
     *
     * @param player The player of a team.
     * @param action The code to run with the operation result.
     */
    public void loadOfflinePlayer(@NotNull OfflinePlayer player, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        loadOfflinePlayer(uuidFromPlayer(player), action);
    }

    /**
     * Loads from database the information about a team.
     * <p>The player will be loaded as "CacheFreeingOption.MANUAL" so then use {@link #unloadOfflinePlayer(OfflinePlayer)}
     *
     * @param uuid The UUID player of a team.
     * @param action The code to run with the operation result.
     */
    public void loadOfflinePlayer(@NotNull UUID uuid, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        loadOfflinePlayer(uuid, CacheFreeingOption.DONT_CACHE(), action);
    }

    /**
     * Loads from database the information about a team.
     *
     * @param player The player of a team.
     * @param option Select how to manage the occupied memory.
     * @param action The code to run with the operation result.
     */
    public void loadOfflinePlayer(@NotNull OfflinePlayer player, @NotNull CacheFreeingOption option, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        loadOfflinePlayer(uuidFromPlayer(player), option, action);
    }

    /**
     * Loads from database the information about a team.
     *
     * @param uuid The UUID player of a team.
     * @param option Select how to manage the occupied memory.
     * @param action The code to run with the operation result.
     */
    public void loadOfflinePlayer(@NotNull UUID uuid, @NotNull CacheFreeingOption option, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        callSyncIfNotNull(main.getDatabaseManager().loadOfflinePlayer(uuid, option), action);
    }

    /**
     * Returns if the player in cached in the plugin memory.
     *
     * @param player The player of a team.
     * @return If the player in cached in the plugin memory.
     */
    public boolean isOfflinePlayerLoaded(@NotNull OfflinePlayer player) {
        return isOfflinePlayerLoaded(uuidFromPlayer(player));
    }

    /**
     * Returns if the player in cached in the plugin memory.
     *
     * @param uuid The UUID player of a team.
     * @return If the player in cached in the plugin memory.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean isOfflinePlayerLoaded(UUID uuid) {
        return main.getDatabaseManager().isOfflinePlayerLoaded(uuid, plugin);
    }

    /**
     * Returns the number of active caches on a single player.
     *
     * @param plugin The plugin.
     * @param player The player of a team.
     * @param type The type of the loading request.
     * @return The number of the loading requests done on a single player.
     */
    public int getLoadingRequestsAmount(@NotNull Plugin plugin, @NotNull Player player, @NotNull CacheFreeingOption.Option type) {
        return getLoadingRequestsAmount(plugin, uuidFromPlayer(player), type);
    }

    /**
     * Returns the number of active caches on a single player.
     *
     * @param plugin The plugin.
     * @param offlinePlayer The offline player of a team.
     * @param type The type of the loading request.
     * @return The number of the loading requests done on a single player.
     */
    public int getLoadingRequestsAmount(@NotNull Plugin plugin, @NotNull OfflinePlayer offlinePlayer, @NotNull CacheFreeingOption.Option type) {
        return getLoadingRequestsAmount(plugin, uuidFromPlayer(offlinePlayer), type);
    }

    /**
     * Returns the number of active caches on a single player.
     *
     * @param plugin The plugin.
     * @param uuid The UUID player of a team.
     * @param type The type of the loading request.
     * @return The number of the loading requests done on a single player.
     */
    public int getLoadingRequestsAmount(@NotNull Plugin plugin, @NotNull UUID uuid, @NotNull CacheFreeingOption.Option type) {
        return main.getDatabaseManager().getLoadingRequestsAmount(plugin, uuid, type);
    }

    /**
     * Unload team information that was previously cached
     *
     * @param player The player of a team.
     */
    public void unloadOfflinePlayer(@NotNull OfflinePlayer player) {
        unloadOfflinePlayer(uuidFromPlayer(player));
    }

    /**
     * Unload team information that was previously cached
     *
     * @param uuid The UUID player of a team.
     */
    public void unloadOfflinePlayer(@NotNull UUID uuid) {
        main.getDatabaseManager().unloadOfflinePlayer(uuid, plugin);
    }

    /**
     * Gets the stored name of a player in the database.
     *
     * @param player The player.
     * @param action The code to run with the operation result.
     */
    public void getStoredPlayerName(@NotNull OfflinePlayer player, @NotNull Consumer<ObjectResult<@Nullable String>> action) {
        getStoredPlayerName(uuidFromPlayer(player), action);
    }

    /**
     * Gets the stored name of a player in the database.
     *
     * @param uuid The UUID player.
     * @param action The code to run with the operation result.
     */
    public void getStoredPlayerName(@NotNull UUID uuid, @NotNull Consumer<ObjectResult<@Nullable String>> action) {
        Validate.notNull(action, "Consumer is null.");
        main.getDatabaseManager().getStoredPlayerName(uuid).thenAccept(s -> runSync(plugin, () -> action.accept(s)));
    }

    private <T extends Result> void callAfterLoad(@NotNull Player player, @NotNull Function<DatabaseManager, CompletableFuture<T>> internalAction, @Nullable Consumer<T> action) {
        callAfterLoad(uuidFromPlayer(player), internalAction, action);
    }

    private <T extends Result> void callAfterLoad(@NotNull UUID uuid, @NotNull Function<DatabaseManager, CompletableFuture<T>> internalAction, @Nullable Consumer<T> action) {
        Validate.notNull(uuid, "UUID is null.");
        final DatabaseManager ds = main.getDatabaseManager();
        ds.loadOfflinePlayer(uuid, CacheFreeingOption.MANUAL(plugin)).thenAccept(t1 -> {
            if (t1.isExceptionOccurred()) {
                new RuntimeException("An exception occurred while loading user " + uuid + ':', t1.getOccurredException()).printStackTrace();
                return;
            }
            CompletableFuture<T> c;
            try {
                c = internalAction.apply(ds);
            } catch (Throwable t) {
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
                        } catch (Throwable t) {
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
        final DatabaseManager ds = main.getDatabaseManager();
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
                    } catch (Throwable t) {
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
                                } catch (Throwable t) {
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
