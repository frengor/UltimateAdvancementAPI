package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
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

@SuppressWarnings("unused")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class UltimateAdvancementAPI {

    static AdvancementMain main;

    @NotNull
    public static UltimateAdvancementAPI getInstance(@NotNull Plugin plugin) throws APINotInstantiatedException {
        Validate.notNull(plugin, "Plugin is null.");
        if (main == null) {
            throw new APINotInstantiatedException();
        }
        return new UltimateAdvancementAPI(plugin);
    }

    private final Plugin plugin;

    @NotNull
    @Contract("_ -> new")
    public AdvancementTab createAdvancementTab(@NotNull String namespace) throws DuplicatedException {
        return main.createAdvancementTab(plugin, namespace);
    }

    @Nullable
    public AdvancementTab getAdvancementTab(@NotNull String namespace) {
        return main.getAdvancementTab(namespace);
    }

    public boolean isAdvancementTabRegistered(@NotNull String namespace) {
        return main.isAdvancementTabRegistered(namespace);
    }

    @UnmodifiableView
    @NotNull
    public Collection<@NotNull AdvancementTab> getPluginAdvancementTabs() {
        return main.getPluginAdvancementTabs(plugin);
    }

    public void unregisterAdvancementTab(@NotNull String namespace) {
        main.unregisterAdvancementTab(namespace);
    }

    public void unregisterPluginAdvancementTabs() {
        main.unregisterAdvancementTabs(plugin);
    }

    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public Set<@NotNull String> getAdvancementTabNamespaces() {
        return main.getAdvancementTabNamespaces();
    }

    @NotNull
    @Contract(pure = true, value = "_ -> new")
    public List<@NotNull String> filterNamespaces(@Nullable String input) {
        return main.filterNamespaces(input);
    }

    @NotNull
    @UnmodifiableView
    public Collection<@NotNull AdvancementTab> getTabs() {
        return main.getTabs();
    }

    public void updatePlayer(@NotNull Player player) {
        main.updatePlayer(player);
    }

    @Nullable
    public Advancement getAdvancement(@NotNull String namespacedKey) {
        return main.getAdvancement(namespacedKey);
    }

    @Nullable
    public Advancement getAdvancement(@NotNull String namespace, @NotNull String key) {
        return main.getAdvancement(namespace, key);
    }

    @Nullable
    public Advancement getAdvancement(@NotNull AdvancementKey namespacedKey) {
        return main.getAdvancement(namespacedKey);
    }

    public void displayCustomToast(@NotNull Player player, @NotNull AdvancementDisplay display) {
        displayCustomToast(player, display.icon, display.title, display.frame);
    }

    public void displayCustomToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame) {
        AdvancementUtils.displayToast(player, icon, title, frame);
    }

    public void disableVanillaAdvancements() throws RuntimeException {
        try {
            AdvancementUtils.disableVanillaAdvancements();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't disable minecraft advancements.", e);
        }
    }

    @NotNull
    public TeamProgression getProgression(@NotNull Player player) throws UserNotLoadedException {
        return main.getDatabaseManager().getProgression(player);
    }

    @NotNull
    public TeamProgression getProgression(@NotNull UUID uuid) throws UserNotLoadedException {
        return main.getDatabaseManager().getProgression(uuid);
    }

    public void updatePlayerTeam(@NotNull Player playerToMove, @NotNull Player aDestTeamPlayer) {
        updatePlayerTeam(playerToMove, aDestTeamPlayer, null);
    }

    public void updatePlayerTeam(@NotNull Player playerToMove, @NotNull Player aDestTeamPlayer, @Nullable Consumer<Result> action) {
        Validate.notNull(playerToMove, "Player to move is null.");
        Validate.notNull(aDestTeamPlayer, "Destination player (representing destination team) is null.");
        callAfterLoad(playerToMove, aDestTeamPlayer, ds -> ds.updatePlayerTeam(playerToMove, aDestTeamPlayer), action);
    }

    public void updatePlayerTeam(@NotNull UUID playerToMove, @NotNull UUID aDestTeamPlayer) {
        updatePlayerTeam(playerToMove, aDestTeamPlayer, null);
    }

    public void updatePlayerTeam(@NotNull UUID playerToMove, @NotNull UUID aDestTeamPlayer, @Nullable Consumer<Result> action) {
        Validate.notNull(playerToMove, "Player to move is null.");
        Validate.notNull(aDestTeamPlayer, "Destination player (representing destination team) is null.");
        callAfterLoad(playerToMove, aDestTeamPlayer, ds -> ds.updatePlayerTeam(playerToMove, aDestTeamPlayer), action);
    }

    public void movePlayerInNewTeam(@NotNull Player playerToMove) {
        movePlayerInNewTeam(playerToMove, null);
    }

    public void movePlayerInNewTeam(@NotNull Player playerToMove, @Nullable Consumer<ObjectResult<@NotNull TeamProgression>> action) {
        callAfterLoad(playerToMove, ds -> ds.movePlayerInNewTeam(playerToMove), action);
    }

    public void movePlayerInNewTeam(@NotNull UUID playerToMove) {
        movePlayerInNewTeam(playerToMove, null);
    }

    public void movePlayerInNewTeam(@NotNull UUID playerToMove, @Nullable Consumer<ObjectResult<@NotNull TeamProgression>> action) {
        callAfterLoad(playerToMove, ds -> ds.movePlayerInNewTeam(playerToMove), action);
    }

    public void unregisterOfflinePlayer(@NotNull OfflinePlayer player) throws IllegalStateException {
        unregisterOfflinePlayer(player, null);
    }

    public void unregisterOfflinePlayer(@NotNull OfflinePlayer player, @Nullable Consumer<Result> action) throws IllegalStateException {
        callSyncIfNotNull(main.getDatabaseManager().unregisterOfflinePlayer(player), action);
    }

    public void unregisterOfflinePlayer(@NotNull UUID uuid) throws IllegalStateException {
        unregisterOfflinePlayer(uuid, null);
    }

    public void unregisterOfflinePlayer(@NotNull UUID uuid, @Nullable Consumer<Result> action) throws IllegalStateException {
        callSyncIfNotNull(main.getDatabaseManager().unregisterOfflinePlayer(uuid), action);
    }

    public void updatePlayerName(@NotNull Player player) {
        updatePlayerName(player, null);
    }

    public void updatePlayerName(@NotNull Player player, @Nullable Consumer<Result> action) {
        callSyncIfNotNull(main.getDatabaseManager().updatePlayerName(player), action);
    }

    public void isUnredeemed(@NotNull Advancement advancement, @NotNull Player player, @NotNull Consumer<ObjectResult<@NotNull Boolean>> action) {
        isUnredeemed(advancement, uuidFromPlayer(player), action);
    }

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

    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player) throws NotGrantedException {
        setUnredeemed(advancement, player, true);
    }

    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player, @Nullable Consumer<Result> action) throws NotGrantedException {
        setUnredeemed(advancement, player, true, action);
    }

    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player, boolean giveRewards) throws NotGrantedException {
        setUnredeemed(advancement, uuidFromPlayer(player), giveRewards);
    }

    public void setUnredeemed(@NotNull Advancement advancement, @NotNull Player player, boolean giveRewards, @Nullable Consumer<Result> action) throws NotGrantedException {
        setUnredeemed(advancement, uuidFromPlayer(player), giveRewards, action);
    }

    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid) throws NotGrantedException {
        setUnredeemed(advancement, uuid, true);
    }

    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, @Nullable Consumer<Result> action) throws NotGrantedException {
        setUnredeemed(advancement, uuid, true, action);
    }

    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, boolean giveRewards) throws NotGrantedException {
        setUnredeemed(advancement, uuid, giveRewards, null);
    }

    public void setUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, boolean giveRewards, @Nullable Consumer<Result> action) throws NotGrantedException {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(uuid, "UUID is null.");
        // Don't call update if advancement isn't granted, it may throw a foreign key exception
        if (!advancement.isGranted(uuid)) {
            throw new NotGrantedException();
        }

        callAfterLoad(uuid, ds -> ds.setUnredeemed(advancement.getKey(), giveRewards, uuid), action);
    }

    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull Player player) {
        unsetUnredeemed(advancement, player, null);
    }

    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull Player player, @Nullable Consumer<Result> action) {
        unsetUnredeemed(advancement, uuidFromPlayer(player), action);
    }

    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid) {
        unsetUnredeemed(advancement, uuid, null);
    }

    public void unsetUnredeemed(@NotNull Advancement advancement, @NotNull UUID uuid, @Nullable Consumer<Result> action) {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(uuid, "UUID is null.");
        callAfterLoad(uuid, ds -> ds.unsetUnredeemed(advancement.getKey(), uuid), action);
    }

    public boolean isLoaded(@NotNull Player player) {
        return main.getDatabaseManager().isLoaded(player);
    }

    public boolean isLoaded(@NotNull UUID uuid) {
        return main.getDatabaseManager().isLoaded(uuid);
    }

    public void loadOfflinePlayer(@NotNull OfflinePlayer player) {
        loadOfflinePlayer(uuidFromPlayer(player));
    }

    public void loadOfflinePlayer(@NotNull UUID uuid) {
        loadOfflinePlayer(uuid, CacheFreeingOption.MANUAL(plugin), null);
    }

    public void loadOfflinePlayer(@NotNull OfflinePlayer player, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        loadOfflinePlayer(uuidFromPlayer(player), action);
    }

    public void loadOfflinePlayer(@NotNull UUID uuid, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        loadOfflinePlayer(uuid, CacheFreeingOption.DONT_CACHE(), action);
    }

    public void loadOfflinePlayer(@NotNull OfflinePlayer player, @NotNull CacheFreeingOption option, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        loadOfflinePlayer(uuidFromPlayer(player), option, action);
    }

    public void loadOfflinePlayer(@NotNull UUID uuid, @NotNull CacheFreeingOption option, @Nullable Consumer<ObjectResult<@Nullable TeamProgression>> action) {
        callSyncIfNotNull(main.getDatabaseManager().loadOfflinePlayer(uuid, option), action);
    }

    public boolean isOfflinePlayerLoaded(@NotNull OfflinePlayer player) {
        return isOfflinePlayerLoaded(uuidFromPlayer(player));
    }

    @Contract(pure = true, value = "null -> false")
    public boolean isOfflinePlayerLoaded(UUID uuid) {
        return main.getDatabaseManager().isOfflinePlayerLoaded(uuid, plugin);
    }

    public void unloadOfflinePlayer(@NotNull OfflinePlayer player) {
        unloadOfflinePlayer(uuidFromPlayer(player));
    }

    public void unloadOfflinePlayer(@NotNull UUID uuid) {
        main.getDatabaseManager().unloadOfflinePlayer(uuid, plugin);
    }

    public void getStoredPlayerName(@NotNull OfflinePlayer player, @NotNull Consumer<ObjectResult<@Nullable String>> action) {
        getStoredPlayerName(uuidFromPlayer(player), action);
    }

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
