package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.CacheFreeingOption.Option;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression.ProgressionUpdaterManager.ScheduleResult;
import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import com.fren_gor.ultimateAdvancementAPI.database.impl.MySQL;
import com.fren_gor.ultimateAdvancementAPI.database.impl.SQLite;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingFailedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncPlayerUnregisteredEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamLoadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUpdateEvent.Action;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DatabaseException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.File;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * The database manager. It handles the connection to the database and caches the requested values to improve performances.
 * <p>The caching system caches teams using {@link TeamProgression}s and keeps a link between each online player and the
 * associated {@link TeamProgression}. Two players who are part of the same team will always be associated to the same {@link TeamProgression} object.
 * More formally, the object returned by {@link #getTeamProgression(Player)} is the same if and only the players are members of the same team:
 * <blockquote><pre>
 * TeamProgression teamP1 = getProgression(playerOne);
 * TeamProgression teamP2 = getProgression(playerTwo);
 * if (teamP1.contains(p2)) { // Players are members of the same team
 *    assert teamP1 == teamP2;
 * } else { // Players are in two separate teams
 *    assert teamP1 != teamP2;
 * }</pre></blockquote>
 * By default, players are kept in cache until they quit.
 * However, this behavior can be overridden through the {@link #loadOfflinePlayer(UUID, CacheFreeingOption)} method,
 * which forces a player to stay in cache even if they quit. If the player is not online, they'll be loaded.
 * <p>There is, however, a limit on the maximum amount of requests a plugin can do.
 * For more information, see {@link DatabaseManager#getLoadingRequestsAmount(Plugin, UUID, CacheFreeingOption.Option)}.
 * <p>This class is thread safe.
 */
public final class DatabaseManager {

    /**
     * Max possible loading requests a plugin can make simultaneously per offline player.
     * <p>Limit is applied to automatic and manual requests separately and doesn't apply to requests which don't cache.
     */
    public static final int MAX_SIMULTANEOUS_LOADING_REQUESTS = Character.MAX_VALUE;
    private static final int LOAD_EVENTS_DELAY = 3;

    // A single-thread executor is used to maintain executed queries sequential
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final AdvancementMain main;
    private final Map<UUID, TeamProgression> progressionCache = new HashMap<>();
    private final Map<UUID, TempUserMetadata> tempLoaded = new HashMap<>();
    private final EventManager eventManager;
    private final IDatabase database;

    /**
     * Creates a new {@code DatabaseManager} which uses an in-memory database.
     *
     * @param main The {@link AdvancementMain}.
     * @throws Exception If anything goes wrong.
     */
    public DatabaseManager(@NotNull AdvancementMain main) throws Exception {
        Preconditions.checkNotNull(main, "AdvancementMain is null.");
        this.main = main;
        this.eventManager = main.getEventManager();

        database = new InMemory(main.getLogger());
        commonSetUp();
    }

    /**
     * Creates a new {@code DatabaseManager} which uses a SQLite database.
     *
     * @param main The {@link AdvancementMain}.
     * @param dbFile The SQLite database file.
     * @throws Exception If anything goes wrong.
     */
    public DatabaseManager(@NotNull AdvancementMain main, @NotNull File dbFile) throws Exception {
        Preconditions.checkNotNull(main, "AdvancementMain is null.");
        Preconditions.checkNotNull(dbFile, "Database file is null.");
        this.main = main;
        this.eventManager = main.getEventManager();

        database = new SQLite(dbFile, main.getLogger());
        commonSetUp();
    }

    /**
     * Creates a new {@code DatabaseManager} which uses a MySQL database.
     *
     * @param main The {@link AdvancementMain}.
     * @param username The username.
     * @param password The password.
     * @param databaseName The name of the database.
     * @param host The MySQL host.
     * @param port The MySQL port. Must be greater than zero.
     * @param poolSize The pool size. Must be greater than zero.
     * @param connectionTimeout The connection timeout. Must be greater or equal to 250.
     * @throws Exception If anything goes wrong.
     */
    public DatabaseManager(@NotNull AdvancementMain main, @NotNull String username, @NotNull String password, @NotNull String databaseName, @NotNull String host, @Range(from = 1, to = Integer.MAX_VALUE) int port, @Range(from = 1, to = Integer.MAX_VALUE) int poolSize, @Range(from = 250, to = Long.MAX_VALUE) long connectionTimeout) throws Exception {
        Preconditions.checkNotNull(main, "AdvancementMain is null.");
        this.main = main;
        this.eventManager = main.getEventManager();

        database = new MySQL(username, password, databaseName, host, port, poolSize, connectionTimeout, main.getLogger(), main.getLibbyManager());
        commonSetUp();
    }

    private void commonSetUp() throws SQLException {
        // Run it sync to avoid using uninitialized database
        database.setUp();

        eventManager.register(this, PlayerLoginEvent.class, EventPriority.LOWEST, e -> CompletableFuture.runAsync(() -> {
            try {
                loadPlayerMainFunction(e.getPlayer());
            } catch (Exception ex) {
                System.err.println("Cannot load player " + e.getPlayer().getName() + ':');
                ex.printStackTrace();
                runSync(main, LOAD_EVENTS_DELAY, () -> Bukkit.getPluginManager().callEvent(new PlayerLoadingFailedEvent(e.getPlayer(), ex)));
            }
        }, executor));
        eventManager.register(this, PlayerQuitEvent.class, EventPriority.MONITOR, e -> {
            synchronized (DatabaseManager.this) {
                TempUserMetadata meta = tempLoaded.get(e.getPlayer().getUniqueId());
                if (meta != null) {
                    meta.isOnline = false;
                    // If meta isn't null then a plugin is using the player's TeamProgression
                } else {
                    TeamProgression t = progressionCache.remove(e.getPlayer().getUniqueId());
                    if (t != null && t.noMemberMatch(progressionCache::containsKey)) {
                        t.inCache.set(false); // Invalidate TeamProgression
                        callEventCatchingExceptions(new AsyncTeamUnloadEvent(t));
                    }
                }
            }
        });
        eventManager.register(this, PluginDisableEvent.class, EventPriority.HIGHEST, e -> {
            synchronized (DatabaseManager.this) {
                List<UUID> list = new LinkedList<>();
                for (Entry<UUID, TempUserMetadata> en : tempLoaded.entrySet()) {
                    // Make sure they will be unloaded
                    if (en.getValue().pluginRequests.remove(e.getPlugin()) != null) {
                        list.add(en.getKey());
                    }
                }
                for (UUID u : list) {
                    // Handle unload
                    unloadOfflinePlayer(u, e.getPlugin());
                }
            }
        });
        CompletableFuture.runAsync(() -> {
            try {
                database.clearUpTeams();
            } catch (SQLException e) {
                System.err.println("Cannot clear up unused team ids:");
                e.printStackTrace();
            }
        }, executor);
    }

    /**
     * Closes the connection to the database and frees the cache.
     * <p>This method does not call {@link Event}s.
     */
    public void unregister() {
        // Shutdown executor before database connection
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                // There are other tasks
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("It was not possible to terminate some tasks.");
                }
            }
        } catch (InterruptedException ignored) {
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // Preserve interrupt status
        } catch (Exception ignored) {
            executor.shutdownNow();
        }

        if (eventManager.isEnabled())
            eventManager.unregister(this);
        try {
            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        synchronized (this) {
            tempLoaded.clear();
            progressionCache.forEach((u, t) -> t.inCache.set(false)); // Invalidate TeamProgression
            progressionCache.clear();
        }
    }

    /**
     * Main function to load the provided player from the database.
     * <p><strong>Should be called async.</strong>
     *
     * @param player The player to load.
     * @throws SQLException If anything goes wrong.
     */
    private void loadPlayerMainFunction(final @NotNull Player player) throws SQLException {
        Entry<TeamProgression, Boolean> entry = loadOrRegisterPlayer(player);
        final TeamProgression pro = entry.getKey();
        runSync(main, LOAD_EVENTS_DELAY, () -> {
            Bukkit.getPluginManager().callEvent(new PlayerLoadingCompletedEvent(player, pro));
            if (entry.getValue()) {
                callEventCatchingExceptions(new AsyncTeamUpdateEvent(pro, player.getUniqueId(), Action.JOIN));
            }
            main.updatePlayer(player);
            CompletableFuture.runAsync(() -> processUnredeemed(player, pro), executor);
        });
    }

    /**
     * Load the provided player from the database. If they are not present, this method registers they.
     * <p><strong>Should be called async.</strong>
     *
     * @param player The player.
     * @return A pair containing the loaded {@link TeamProgression} and a {@code boolean},
     *         which is {@code true} if and only if the player was not found in the database.
     * @throws SQLException If anything goes wrong.
     */
    @NotNull
    private synchronized Entry<TeamProgression, Boolean> loadOrRegisterPlayer(@NotNull Player player) throws SQLException {
        final UUID uuid = player.getUniqueId();

        TeamProgression pro = progressionCache.get(uuid);
        if (pro != null) {
            // Don't let the player to be unloaded from cache
            TempUserMetadata meta = tempLoaded.get(uuid);
            if (meta != null) {
                meta.isOnline = true;
            }
            return new SimpleEntry<>(pro, false);
        }

        pro = searchTeamProgressionDeeply(uuid);
        if (pro != null) {
            progressionCache.put(uuid, pro); // Direct caching
            updatePlayerName(player);
            return new SimpleEntry<>(pro, false);
        }

        Entry<TeamProgression, Boolean> e = database.loadOrRegisterPlayer(uuid, player.getName());
        updatePlayerName(player);
        e.getKey().inCache.set(true); // Set TeamProgression valid
        progressionCache.put(uuid, e.getKey());
        callEventCatchingExceptions(new AsyncTeamLoadEvent(e.getKey()));
        return e;
    }

    /**
     * Search if the provided player's team is already in cache (so if any other team member is loaded)
     * and returns the {@link TeamProgression} object.
     *
     * @param uuid The player {@link UUID}.
     * @return The player team if found, {@code null} otherwise.
     */
    @Nullable
    private synchronized TeamProgression searchTeamProgressionDeeply(@NotNull UUID uuid) {
        for (TeamProgression progression : progressionCache.values()) {
            if (progression.contains(uuid)) {
                return progression;
            }
        }
        return null;
    }

    /**
     * Process unredeemed advancements for the provided player and team. The player is assumed to be in the team.
     * <p><strong>Should be called async.</strong>
     *
     * @param player The player.
     * @param pro The player's team.
     */
    private void processUnredeemed(final @NotNull Player player, final @NotNull TeamProgression pro) {
        final List<Entry<AdvancementKey, Boolean>> list;
        try {
            list = database.getUnredeemed(pro.getTeamId());
        } catch (SQLException e) {
            System.err.println("Cannot fetch unredeemed advancements:");
            e.printStackTrace();
            return;
        }

        if (list.size() != 0)
            runSync(main, () -> {
                Iterator<Entry<AdvancementKey, Boolean>> it = list.iterator();
                final List<Entry<Advancement, Boolean>> advs = new LinkedList<>();
                while (it.hasNext()) {
                    Entry<AdvancementKey, Boolean> k = it.next();
                    Advancement a = main.getAdvancement(k.getKey());
                    if (a == null || !a.getAdvancementTab().isShownTo(player)) {
                        it.remove();
                    } else {
                        advs.add(new SimpleEntry<>(a, k.getValue()));
                    }
                }
                if (advs.size() != 0)
                    CompletableFuture.runAsync(() -> {
                        try {
                            database.unsetUnredeemed(list, pro.getTeamId());
                        } catch (SQLException e) {
                            System.err.println("Cannot unset unredeemed advancements:");
                            e.printStackTrace();
                            return;
                        }
                        runSync(main, () -> {
                            for (Entry<Advancement, Boolean> e : advs) {
                                e.getKey().onGrant(player, e.getValue());
                            }
                        });
                    }, executor);
            });
    }

    /**
     * Updates the name of the specified player in the database.
     *
     * @param player The player to progressionUpdate.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @see UltimateAdvancementAPI#updatePlayerName(Player)
     */
    @NotNull
    public CompletableFuture<Void> updatePlayerName(@NotNull Player player) {
        Preconditions.checkNotNull(player, "Player cannot be null.");
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                database.updatePlayerName(player.getUniqueId(), player.getName());
            } catch (SQLException e) {
                System.err.println("Cannot progressionUpdate player " + player.getName() + " name:");
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }
            completableFuture.complete(null);
        }, executor);

        return completableFuture;
    }

    /**
     * Moves the provided player from their team to the second player's one.
     *
     * @param playerToMove The player to move.
     * @param otherTeamMember A player of the destination team.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#updatePlayerTeam(Player, Player, Consumer)
     */
    @NotNull
    public CompletableFuture<Void> updatePlayerTeam(@NotNull Player playerToMove, @NotNull Player otherTeamMember) throws UserNotLoadedException {
        return updatePlayerTeam(playerToMove, getTeamProgression(otherTeamMember));
    }

    /**
     * Moves the provided player from their team to the second player's one.
     *
     * @param playerToMove The {@link UUID} of the player to move.
     * @param otherTeamMember The {@link UUID} of a player of the destination team.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#updatePlayerTeam(UUID, UUID, Consumer)
     */
    @NotNull
    public CompletableFuture<Void> updatePlayerTeam(@NotNull UUID playerToMove, @NotNull UUID otherTeamMember) throws UserNotLoadedException {
        return updatePlayerTeam(playerToMove, Bukkit.getPlayer(playerToMove), getTeamProgression(otherTeamMember));
    }

    /**
     * Moves the provided player from their team to the specified one.
     *
     * @param playerToMove The player to move.
     * @param otherTeamProgression The {@link TeamProgression} of the target team.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    @NotNull
    public CompletableFuture<Void> updatePlayerTeam(@NotNull Player playerToMove, @NotNull TeamProgression otherTeamProgression) throws UserNotLoadedException {
        return updatePlayerTeam(uuidFromPlayer(playerToMove), playerToMove, otherTeamProgression);
    }

    @NotNull
    private CompletableFuture<Void> updatePlayerTeam(@NotNull UUID playerToMove, @Nullable Player ptm, @NotNull TeamProgression otherTeamProgression) throws UserNotLoadedException {
        Preconditions.checkNotNull(playerToMove, "Player to move is null.");
        validateTeamProgression(otherTeamProgression);

        final TeamProgression oldProgression;
        synchronized (DatabaseManager.this) {
            oldProgression = progressionCache.get(playerToMove);
        }

        if (oldProgression == null) {
            throw new UserNotLoadedException(playerToMove);
        }

        if (otherTeamProgression.contains(playerToMove)) {
            // Player is already in that team
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                database.movePlayer(playerToMove, otherTeamProgression.getTeamId());
            } catch (SQLException e) {
                System.err.println("Cannot move player " + (ptm == null ? playerToMove : ptm) + " into team " + otherTeamProgression.getTeamId());
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }

            // Get old progression again since it may be changed
            final TeamProgression oldTeam;
            synchronized (DatabaseManager.this) {
                oldTeam = progressionCache.get(playerToMove);

                if (!otherTeamProgression.isValid()) {
                    completableFuture.completeExceptionally(new IllegalArgumentException("Destination team's TeamProgression is invalid."));
                }
            }
            replacePlayerTeam(oldTeam, otherTeamProgression, playerToMove);

            if (ptm != null) {
                processUnredeemed(ptm, otherTeamProgression);

                runSync(main, () -> {
                    main.updatePlayer(ptm);
                });
            }

            completableFuture.complete(null);
        }, executor);

        return completableFuture;
    }

    /**
     * Moves the provided player into a new team.
     *
     * @param player The player.
     * @return A {@link CompletableFuture} which provides the new player team's {@link TeamProgression}.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#movePlayerInNewTeam(Player)
     */
    public CompletableFuture<TeamProgression> movePlayerInNewTeam(@NotNull Player player) throws UserNotLoadedException {
        return movePlayerInNewTeam(uuidFromPlayer(player), player);
    }

    /**
     * Moves the provided player into a new team.
     *
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} which provides the new player team's {@link TeamProgression}.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#movePlayerInNewTeam(UUID)
     */
    public CompletableFuture<TeamProgression> movePlayerInNewTeam(@NotNull UUID uuid) throws UserNotLoadedException {
        return movePlayerInNewTeam(uuid, Bukkit.getPlayer(uuid));
    }

    private CompletableFuture<TeamProgression> movePlayerInNewTeam(@NotNull UUID uuid, @Nullable Player ptr) throws UserNotLoadedException {
        Preconditions.checkNotNull(uuid, "UUID is null.");
        final TeamProgression oldProgression;
        synchronized (DatabaseManager.this) {
            oldProgression = progressionCache.get(uuid);
        }

        if (oldProgression == null) {
            throw new UserNotLoadedException(uuid);
        }

        CompletableFuture<TeamProgression> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            final TeamProgression newPro;
            try {
                newPro = database.movePlayerInNewTeam(uuid);
            } catch (SQLException e) {
                System.err.println("Cannot remove player " + (ptr == null ? uuid : ptr.getName()) + " from their team:");
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }

            // Get old progression again since it may be changed
            final TeamProgression oldTeam;
            synchronized (DatabaseManager.this) {
                oldTeam = progressionCache.get(uuid);
            }
            replacePlayerTeam(oldTeam, newPro, uuid);

            if (ptr != null) {
                runSync(main, () -> {
                    main.updatePlayer(ptr);
                });
            }

            completableFuture.complete(newPro);
        }, executor);

        return completableFuture;
    }

    /**
     * Unregisters the provided player. The player must be offline and not loaded into the cache.
     *
     * @param player The player to unregister.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @throws IllegalStateException If the player is online or loaded into the cache.
     * @see UltimateAdvancementAPI#unregisterOfflinePlayer(OfflinePlayer)
     */
    public CompletableFuture<Void> unregisterOfflinePlayer(@NotNull OfflinePlayer player) throws IllegalStateException {
        return unregisterOfflinePlayer(uuidFromPlayer(player));
    }

    /**
     * Unregisters the provided player. The player must be offline and not loaded into the cache.
     *
     * @param uuid The {@link UUID} of the player to unregister.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @throws IllegalStateException If the player is online or loaded into the cache.
     * @see UltimateAdvancementAPI#unregisterOfflinePlayer(UUID)
     */
    public CompletableFuture<Void> unregisterOfflinePlayer(@NotNull UUID uuid) throws IllegalStateException {
        Preconditions.checkNotNull(uuid, "UUID is null.");
        AdvancementUtils.checkSync();
        if (Bukkit.getPlayer(uuid) != null) {
            throw new IllegalStateException("Player is online.");
        }

        synchronized (DatabaseManager.this) {
            if (tempLoaded.containsKey(uuid))
                throw new IllegalStateException("Player is temporary loaded.");
        }

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                database.unregisterPlayer(uuid);
            } catch (SQLException e) {
                System.err.println("Cannot unregister player " + uuid + ':');
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }

            callEventCatchingExceptions(new AsyncPlayerUnregisteredEvent(uuid));
            completableFuture.complete(null);
        }, executor);

        return completableFuture;
    }

    /**
     * Updates the progression of the specified advancement.
     *
     * @param key The advancement key.
     * @param uuid The {@link UUID} of the player who made the advancement.
     * @param newProgression The new progression.
     * @return A pair containing the old progression and a {@link CompletableFuture} which provides the result of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    @NotNull
    public Entry<Integer, CompletableFuture<Integer>> setProgression(@NotNull AdvancementKey key, @NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) throws UserNotLoadedException {
        return setProgression(key, getTeamProgression(uuid), newProgression);
    }

    /**
     * Updates the progression of the specified advancement.
     *
     * @param key The advancement key.
     * @param player The player who made the advancement.
     * @param newProgression The new progression.
     * @return A pair containing the old progression and a {@link CompletableFuture} which provides the result of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    @NotNull
    public Entry<Integer, CompletableFuture<Integer>> setProgression(@NotNull AdvancementKey key, @NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) throws UserNotLoadedException {
        return setProgression(key, uuidFromPlayer(player), newProgression);
    }

    /**
     * Updates the progression of the specified advancement.
     *
     * @param key The advancement key.
     * @param progression The {@link TeamProgression} of the team which made the advancement.
     * @param newProgression The new progression.
     * @return A pair containing the old progression and a {@link CompletableFuture} which provides the result of the operation.
     */
    @NotNull
    public Entry<Integer, CompletableFuture<Integer>> setProgression(@NotNull AdvancementKey key, @NotNull TeamProgression progression, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) {
        Preconditions.checkNotNull(key, "Key is null.");
        validateTeamProgression(progression);
        Preconditions.checkArgument(progression.getSize() > 0, "TeamProgression doesn't contain any player.");
        AdvancementUtils.checkSync();

        ScheduleResult result = progression.progressionUpdaterManager.scheduleSetUpdate(key, newProgression);

        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            if (result.progressionUpdate().oldValue != newProgression) { // Don't update the db if the saved progression won't change
                try {
                    database.updateAdvancement(key, progression.getTeamId(), result.newCachedValue());
                } catch (SQLException e) {
                    System.err.println("Cannot set progression of advancement " + key + " to team " + progression.getTeamId() + ':');
                    e.printStackTrace();
                    result.progressionUpdate().updateEndedExceptionally();
                    completableFuture.completeExceptionally(new DatabaseException(e));
                    return;
                } catch (Exception e) {
                    result.progressionUpdate().updateEndedExceptionally();
                    completableFuture.completeExceptionally(new DatabaseException(e));
                    return;
                }
            }
            int resultingProgression = result.progressionUpdate().updateEndedSuccessfully();
            result.progressionUpdate().checkDisposal();
            completableFuture.complete(resultingProgression);
        }, executor);

        return new SimpleEntry<>(result.oldValue(), completableFuture);
    }

    /**
     * Updates the progression of the specified advancement.
     *
     * @param key The advancement key.
     * @param progression The {@link TeamProgression} of the team which made the advancement.
     * @param increment The increment of the progression.
     * @return A pair containing the old progression and a {@link CompletableFuture} which provides the result of the operation.
     */
    @NotNull
    public Entry<Integer, CompletableFuture<Integer>> incrementProgression(@NotNull AdvancementKey key, @NotNull TeamProgression progression, @Range(from = 0, to = Integer.MAX_VALUE) int increment) {
        Preconditions.checkNotNull(key, "Key is null.");
        validateTeamProgression(progression);
        Preconditions.checkArgument(progression.getSize() > 0, "TeamProgression doesn't contain any player.");
        AdvancementUtils.checkSync();

        ScheduleResult result = progression.progressionUpdaterManager.scheduleIncrementUpdate(key, increment);

        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            if (increment != 0) { // Don't update the db if the saved progression won't change
                try {
                    database.updateAdvancement(key, progression.getTeamId(), result.newCachedValue());
                } catch (SQLException e) {
                    System.err.println("Cannot increment progression of advancement " + key + " to team " + progression.getTeamId() + ':');
                    e.printStackTrace();
                    result.progressionUpdate().updateEndedExceptionally();
                    completableFuture.completeExceptionally(new DatabaseException(e));
                    return;
                } catch (Exception e) {
                    result.progressionUpdate().updateEndedExceptionally();
                    completableFuture.completeExceptionally(new DatabaseException(e));
                    return;
                }
            }
            int resultingProgression = result.progressionUpdate().updateEndedSuccessfully();
            result.progressionUpdate().checkDisposal();
            completableFuture.complete(resultingProgression);
        }, executor);

        return new SimpleEntry<>(result.oldValue(), completableFuture);
    }

    /**
     * Returns the {@link TeamProgression} of the team of the provided player.
     *
     * @param player The player.
     * @return The {@link TeamProgression} of the player's team.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#getTeamProgression(Player)
     */
    @NotNull
    public TeamProgression getTeamProgression(@NotNull Player player) throws UserNotLoadedException {
        return getTeamProgression(uuidFromPlayer(player));
    }

    /**
     * Returns the {@link TeamProgression} of the team of the provided player.
     *
     * @param uuid The {@link UUID} of the player.
     * @return The {@link TeamProgression} of the player's team.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#getTeamProgression(UUID)
     */
    @NotNull
    public synchronized TeamProgression getTeamProgression(@NotNull UUID uuid) throws UserNotLoadedException {
        Preconditions.checkNotNull(uuid, "UUID is null.");
        TeamProgression pro = progressionCache.get(uuid);
        AdvancementUtils.checkTeamProgressionNotNull(pro, uuid);
        return pro;
    }

    /**
     * Returns whether the provided player is loaded into the cache.
     *
     * @param player The player.
     * @return Whether the provided player is loaded into the cache.
     * @see UltimateAdvancementAPI#isLoaded(Player)
     */
    @Contract(pure = true)
    public boolean isLoaded(@NotNull Player player) {
        return isLoaded(uuidFromPlayer(player));
    }

    /**
     * Returns whether the provided offline player is loaded into the cache.
     *
     * @param player The player.
     * @return Whether the provided offline player is loaded into the cache.
     * @see UltimateAdvancementAPI#isLoaded(OfflinePlayer)
     */
    @Contract(pure = true)
    public boolean isLoaded(@NotNull OfflinePlayer player) {
        return isLoaded(uuidFromPlayer(player));
    }

    /**
     * Returns whether the provided player is loaded into the cache.
     *
     * @param uuid The {@link UUID} of the player.
     * @return Whether the provided player is loaded into the cache.
     * @see UltimateAdvancementAPI#isLoaded(UUID)
     */
    @Contract(pure = true, value = "null -> false")
    public synchronized boolean isLoaded(UUID uuid) {
        return progressionCache.containsKey(uuid);
    }

    /**
     * Returns whether the provided player is online and loaded into the cache.
     *
     * @param player The player.
     * @return Whether the provided player is online and loaded into the cache.
     */
    @Contract(pure = true)
    public boolean isLoadedAndOnline(@NotNull Player player) {
        return isLoadedAndOnline(uuidFromPlayer(player));
    }

    /**
     * Returns whether the provided player is online and loaded into the cache.
     *
     * @param uuid The {@link UUID} of the player.
     * @return Whether the provided player is online and loaded into the cache.
     */
    @Contract(pure = true, value = "null -> false")
    public synchronized boolean isLoadedAndOnline(UUID uuid) {
        if (isLoaded(uuid)) {
            TempUserMetadata t = tempLoaded.get(uuid);
            return t == null || t.isOnline;
        }
        return false;
    }

    /**
     * Returns the number of currently active loading requests done by a plugin for the specified player with the provided {@link CacheFreeingOption.Option}.
     * <p>There is a maximum per-plugin amount of requests that can be done for each player, which is {@link #MAX_SIMULTANEOUS_LOADING_REQUESTS}.
     * <p>This limit is applied to <a href="./CacheFreeingOption.Option.html#AUTOMATIC"><code>CacheFreeingOption.Option#AUTOMATIC</code></a> and <a href="./CacheFreeingOption.Option.html#MANUAL"><code>CacheFreeingOption.Option#MANUAL</code></a> separately
     * (so a plugin can do maximum {@link #MAX_SIMULTANEOUS_LOADING_REQUESTS} automatic requests and {@link #MAX_SIMULTANEOUS_LOADING_REQUESTS} manual requests simultaneously).
     * Since <a href="./CacheFreeingOption.Option.html#DONT_CACHE"><code>CacheFreeingOption.Option#DONT_CACHE</code></a> doesn't cache, no limit is applied to it.
     *
     * @param plugin The plugin.
     * @param uuid The {@link UUID} of the player.
     * @param type The {@link CacheFreeingOption.Option}.
     * @return The number of the currently active player loading requests.
     * @see UltimateAdvancementAPI#getLoadingRequestsAmount(UUID, CacheFreeingOption.Option)
     */
    @Contract(pure = true)
    @Range(from = 0, to = MAX_SIMULTANEOUS_LOADING_REQUESTS)
    public synchronized int getLoadingRequestsAmount(@NotNull Plugin plugin, @NotNull UUID uuid, @NotNull CacheFreeingOption.Option type) {
        Preconditions.checkNotNull(plugin, "Plugin is null.");
        Preconditions.checkNotNull(uuid, "UUID is null.");
        Preconditions.checkNotNull(type, "CacheFreeingOption.Option is null.");
        TempUserMetadata t = tempLoaded.get(uuid);
        if (t == null) {
            return 0;
        }
        return switch (type) {
            case AUTOMATIC -> t.getAuto(plugin);
            case MANUAL -> t.getManual(plugin);
            default -> 0;
        };
    }

    /**
     * Returns whether the provided advancement is unredeemed for the specified player.
     *
     * @param key The advancement key.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} which provides a boolean value that is {@code true} if the
     *         provided advancement is unredeemed for the specified player, false otherwise.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#isUnredeemed(Advancement, UUID, Consumer)
     */
    @NotNull
    public CompletableFuture<Boolean> isUnredeemed(@NotNull AdvancementKey key, @NotNull UUID uuid) throws UserNotLoadedException {
        return isUnredeemed(key, getTeamProgression(uuid));
    }

    /**
     * Returns whether the provided advancement is unredeemed for the specified team.
     *
     * @param key The advancement key.
     * @param pro The {@link TeamProgression} of the team.
     * @return A {@link CompletableFuture} which provides a boolean value that is {@code true} if the
     *         provided advancement is unredeemed for the specified player, false otherwise.
     */
    @NotNull
    public CompletableFuture<Boolean> isUnredeemed(@NotNull AdvancementKey key, @NotNull TeamProgression pro) {
        Preconditions.checkNotNull(key, "AdvancementKey is null.");
        validateTeamProgression(pro);

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            boolean res;
            try {
                res = database.isUnredeemed(key, pro.getTeamId());
            } catch (SQLException e) {
                System.err.println("Cannot fetch unredeemed advancements of team " + pro.getTeamId() + ':');
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }
            completableFuture.complete(res);
        }, executor);

        return completableFuture;
    }

    /**
     * Sets an advancement unredeemed for the specified player.
     *
     * @param key The advancement key.
     * @param giveRewards Whether advancement rewards will be given on redeem.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#setUnredeemed(Advancement, UUID, boolean, Consumer)
     */
    @NotNull
    public CompletableFuture<Void> setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, @NotNull UUID uuid) throws UserNotLoadedException {
        return setUnredeemed(key, giveRewards, getTeamProgression(uuid));
    }

    /**
     * Sets an advancement unredeemed for the specified team.
     *
     * @param key The advancement key.
     * @param giveRewards Whether advancement rewards will be given on redeem.
     * @param pro The {@link TeamProgression} of the team.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     */
    @NotNull
    public CompletableFuture<Void> setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, @NotNull TeamProgression pro) {
        Preconditions.checkNotNull(key, "AdvancementKey is null.");
        validateTeamProgression(pro);

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                database.setUnredeemed(key, giveRewards, pro.getTeamId());
            } catch (SQLException e) {
                System.err.println("Cannot set unredeemed advancement " + key + " to team " + pro.getTeamId() + ':');
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }
            completableFuture.complete(null);
        }, executor);

        return completableFuture;
    }

    /**
     * Redeem the specified advancement for the provided player.
     *
     * @param key The advancement key.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#unsetUnredeemed(Advancement, UUID, Consumer)
     */
    @NotNull
    public CompletableFuture<Void> unsetUnredeemed(@NotNull AdvancementKey key, @NotNull UUID uuid) throws UserNotLoadedException {
        return unsetUnredeemed(key, getTeamProgression(uuid));
    }

    /**
     * Redeem the specified advancement for the provided team.
     *
     * @param key The advancement key.
     * @param pro The {@link TeamProgression} of the team.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     */
    @NotNull
    public CompletableFuture<Void> unsetUnredeemed(@NotNull AdvancementKey key, @NotNull TeamProgression pro) {
        Preconditions.checkNotNull(key, "AdvancementKey is null.");
        validateTeamProgression(pro);

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                database.unsetUnredeemed(key, pro.getTeamId());
            } catch (SQLException e) {
                System.err.println("Cannot set unredeemed advancement " + key + " to team " + pro.getTeamId() + ':');
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }
            completableFuture.complete(null);
        }, executor);

        return completableFuture;
    }

    /**
     * Gets the in-database stored name of the provided player.
     *
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} which provides the stored name of the player.
     * @see UltimateAdvancementAPI#getStoredPlayerName(UUID, Consumer)
     */
    @NotNull
    public CompletableFuture<String> getStoredPlayerName(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "UUID is null.");

        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            String name;
            try {
                name = database.getPlayerName(uuid);
            } catch (SQLException e) {
                System.err.println("Cannot fetch player name of " + uuid + ':');
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }

            completableFuture.complete(name);
        }, executor);

        return completableFuture;
    }

    /**
     * Loads the provided player from the database into the caching system.
     * <p>Different things happens based on the specified {@link CacheFreeingOption}:
     * <ul>
     *     <li><strong>{@link CacheFreeingOption#DONT_CACHE()}:</strong> the player isn't loaded in the caching system, but loads and returns only the player team's {@link TeamProgression};</li>
     *     <li><strong>{@link CacheFreeingOption#AUTOMATIC(Plugin, long)}:</strong> the player is loaded for a certain amount of ticks;</li>
     *     <li><strong>{@link CacheFreeingOption#MANUAL(Plugin)}:</strong> the player is loaded and kept until {@link #unloadOfflinePlayer(UUID, Plugin)} is called.</li>
     * </ul>
     *
     * @param uuid The {@link UUID} of the player to load.
     * @param option The chosen {@link CacheFreeingOption}.
     * @return A {@link CompletableFuture} which provides the player team's {@link TeamProgression}.
     * @see UltimateAdvancementAPI#loadOfflinePlayer(UUID, CacheFreeingOption, Consumer)
     */
    @NotNull
    public synchronized CompletableFuture<TeamProgression> loadOfflinePlayer(@NotNull UUID uuid, @NotNull CacheFreeingOption option) {
        Preconditions.checkNotNull(uuid, "UUID is null.");
        Preconditions.checkNotNull(option, "CacheFreeingOption is null.");
        TeamProgression pro = progressionCache.get(uuid);
        if (pro != null) {
            handleCacheFreeingOption(uuid, null, option); // Handle requests
            return CompletableFuture.completedFuture(pro);
        }
        pro = searchTeamProgressionDeeply(uuid);
        if (pro != null) {
            handleCacheFreeingOption(uuid, pro, option); // Direct caching and handle requests
            return CompletableFuture.completedFuture(pro);
        }

        CompletableFuture<TeamProgression> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            TeamProgression t;
            try {
                t = database.loadUUID(uuid);
            } catch (SQLException e) {
                System.err.println("Cannot load offline player " + uuid + ':');
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }
            handleCacheFreeingOption(uuid, t, option); // Direct caching and handle requests
            if (option.option != Option.DONT_CACHE) {
                t.inCache.set(true); // Set TeamProgression valid
                callEventCatchingExceptions(new AsyncTeamLoadEvent(t));
            }
            completableFuture.complete(t);
        }, executor);

        return completableFuture;
    }

    private void handleCacheFreeingOption(@NotNull UUID uuid, @Nullable TeamProgression pro, @NotNull CacheFreeingOption option) {
        switch (option.option) {
            case AUTOMATIC -> {
                runSync(main, option.ticks, () -> internalUnloadOfflinePlayer(uuid, option.requester, true));
                addCachingRequest(uuid, pro, option, true);
            }
            case MANUAL -> addCachingRequest(uuid, pro, option, false);
        }
    }

    // TeamProgression == null iff it doesn't need to be stored (since it is already stored)
    private synchronized void addCachingRequest(@NotNull UUID uuid, @Nullable TeamProgression pro, @NotNull CacheFreeingOption option, boolean auto) {
        TempUserMetadata meta = tempLoaded.computeIfAbsent(uuid, TempUserMetadata::new);
        meta.addRequest(option.requester, auto);
        if (pro != null) {
            progressionCache.put(uuid, pro);
        }
    }

    /**
     * Returns whether at least one loading request is currently active for the specified player.
     *
     * @param uuid The {@link UUID} of the player.
     * @return Whether at least one loading request for the specified player is currently active.
     */
    @Contract(pure = true, value = "null -> false")
    public synchronized boolean isOfflinePlayerLoaded(UUID uuid) {
        return tempLoaded.containsKey(uuid);
    }

    /**
     * Returns whether at least one loading request, done by the provided plugin, is currently active for the specified player.
     *
     * @param uuid The {@link UUID} of the player.
     * @param requester The plugin which requested the loading.
     * @return Whether at least one loading request, done by the provided plugin, for the specified player is currently active.
     * @see UltimateAdvancementAPI#isOfflinePlayerLoaded(UUID)
     */
    @Contract(pure = true, value = "null, null -> false; null, !null -> false; !null, null -> false")
    public synchronized boolean isOfflinePlayerLoaded(UUID uuid, Plugin requester) {
        TempUserMetadata t = tempLoaded.get(uuid);
        return t != null && Integer.compareUnsigned(t.getRequests(requester), 0) > 0;
    }

    /**
     * Unloads the provided player from the caching system.
     * <p>Note that this method will only unload players loaded with {@link CacheFreeingOption#MANUAL(Plugin)}.
     *
     * @param uuid The {@link UUID} of the player to unload.
     * @param requester The plugin which requested the loading.
     * @see UltimateAdvancementAPI#unloadOfflinePlayer(UUID)
     */
    public void unloadOfflinePlayer(@NotNull UUID uuid, @NotNull Plugin requester) {
        internalUnloadOfflinePlayer(uuid, requester, false);
    }

    private synchronized void internalUnloadOfflinePlayer(@NotNull UUID uuid, @NotNull Plugin requester, boolean auto) {
        Preconditions.checkNotNull(uuid, "UUID is null.");
        Preconditions.checkNotNull(requester, "Plugin is null.");
        TempUserMetadata meta = tempLoaded.get(uuid);
        if (meta != null) {
            meta.removeRequest(requester, auto);
            if (meta.canBeRemoved()) {
                tempLoaded.remove(uuid);
                if (!meta.isOnline) {
                    TeamProgression t = progressionCache.remove(uuid);
                    if (t != null && t.noMemberMatch(progressionCache::containsKey)) {
                        t.inCache.set(false); // Invalidate TeamProgression
                        callEventCatchingExceptions(new AsyncTeamUnloadEvent(t));
                    }
                }
            }
        }
    }

    private void replacePlayerTeam(@NotNull TeamProgression oldTeam, @NotNull TeamProgression newTeam, @NotNull UUID uuid) {
        callEventCatchingExceptions(new AsyncTeamUpdateEvent(oldTeam, uuid, Action.LEAVE));

        final boolean teamUnloaded, newTeamLoaded;
        synchronized (DatabaseManager.this) {
            // Replace team atomically
            oldTeam.movePlayer(newTeam, uuid);
            progressionCache.put(uuid, newTeam);
            newTeamLoaded = newTeam.inCache.getAndSet(true);

            // Check for team unloading
            teamUnloaded = oldTeam.noMemberMatch(progressionCache::containsKey);
        }

        if (newTeamLoaded) {
            callEventCatchingExceptions(new AsyncTeamLoadEvent(newTeam));
        }

        callEventCatchingExceptions(new AsyncTeamUpdateEvent(newTeam, uuid, Action.JOIN));

        if (teamUnloaded) {
            oldTeam.inCache.set(false); // Invalidate TeamProgression
            callEventCatchingExceptions(new AsyncTeamUnloadEvent(oldTeam));
        }
    }

    static <E extends Event> void callEventCatchingExceptions(@NotNull E event) {
        try {
            Bukkit.getPluginManager().callEvent(event);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static final class TempUserMetadata {

        // Integer format: first 16 bits for automatic requests count and 16 bits for plugin requests count
        final Map<Plugin, Integer> pluginRequests = new HashMap<>();
        boolean isOnline;

        public TempUserMetadata(UUID uuid) {
            this.isOnline = Bukkit.getPlayer(uuid) != null;
        }

        public void addRequest(@NotNull Plugin plugin, boolean auto) {
            pluginRequests.compute(plugin, (p, i) -> {
                if (i == null) {
                    i = 0;
                }
                return auto ? addAuto(i) : addManual(i);
            });
        }

        public void removeRequest(@NotNull Plugin plugin, boolean auto) {
            Integer i = pluginRequests.get(plugin);
            if (i != null) {
                i = auto ? removeAuto(i) : removeManual(i);
                if (Integer.compareUnsigned(i, 0) <= 0) {
                    pluginRequests.remove(plugin);
                } else {
                    pluginRequests.put(plugin, i);
                }
            }
        }

        public int getRequests(@NotNull Plugin plugin) {
            return pluginRequests.getOrDefault(plugin, 0);
        }

        public int getAuto(@NotNull Plugin plugin) {
            return getRequests(plugin) >>> 16;
        }

        public int getManual(@NotNull Plugin plugin) {
            return getRequests(plugin) & 0xFFFF;
        }

        public boolean canBeRemoved() {
            return pluginRequests.isEmpty();
        }

        private int addAuto(int i) {
            char tmp = (char) (i >>> 16);
            if (tmp == Character.MAX_VALUE) {
                throw new RuntimeException("Max per-plugin automatic simultaneous requests amount exceeded.");
            }
            return ((tmp + 1) << 16) | (i & 0xFFFF);
        }

        private int addManual(int i) {
            char tmp = (char) (i & 0xFFFF);
            if (tmp == Character.MAX_VALUE) {
                throw new RuntimeException("Max per-plugin manual simultaneous requests amount exceeded.");
            }
            return (tmp + 1) | (i & 0xFFFF0000);
        }

        private int removeAuto(int i) {
            char tmp = (char) (i >>> 16);
            return tmp == 0 ? (i & 0xFFFF) : ((tmp - 1) << 16) | (i & 0xFFFF);
        }

        private int removeManual(int i) {
            char tmp = (char) (i & 0xFFFF);
            return tmp == 0 ? (i & 0xFFFF0000) : (tmp - 1) | (i & 0xFFFF0000);
        }

        @Override
        public String toString() {
            return "TempUserMetadata{" +
                    "pluginRequests=" + pluginRequests +
                    ", isOnline=" + isOnline +
                    '}';
        }
    }
}
