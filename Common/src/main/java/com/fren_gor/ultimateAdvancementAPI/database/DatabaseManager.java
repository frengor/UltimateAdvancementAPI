package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import com.fren_gor.ultimateAdvancementAPI.database.impl.MySQL;
import com.fren_gor.ultimateAdvancementAPI.database.impl.SQLite;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingFailedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.ProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncPlayerUnregisteredEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamLoadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.PlayerRegisteredEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DatabaseException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DatabaseManagerClosedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.SyncExecutionException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.Closeable;
import java.io.File;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateProgressionValue;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * The database manager. It handles the connection to the database and caches the requested values to improve performance.
 * <p>All the implemented operation are <a href="https://en.wikipedia.org/wiki/ACID">ACID</a>.
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
 * However, this behavior can be overridden through the {@link #loadAndAddLoadingRequestToPlayer(UUID, Plugin)} method,
 * which forces a player to stay in cache even if they quit. If the player is not online, they'll be loaded.
 * <p>This class is thread safe.
 */
public final class DatabaseManager implements Closeable {

    // In this class using `synchronized (DatabaseManager.this) {...}` is to prefer over `synchronized (this) {...}`,
    // since the latter can't be used inside lambdas, which are very commonly used in the code

    private static final int LOAD_EVENTS_DELAY = 3;

    // A single-thread executor is used to maintain executed queries sequential
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new DatabaseManagerThreadFactory());

    private final EventManager eventManager;
    private final IDatabase database; // Calls to the database must be done using the `executor` thread
    private final JoinEventWaiter joinEventWaiter;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Used inside DatabaseManager#close() to not leave uncompleted CompletableFutures
    private final Map<Integer, CompletableFuture<?>> uncompletedCompletableFutures = Collections.synchronizedMap(new HashMap<>());
    private final AtomicInteger keysOfUncompletedCFs = new AtomicInteger(0); // Used to generate the keys of uncompletedCompletableFutures map

    // All this fields must be accessed inside a `synchronized (DatabaseManager.this) {...}` block
    private final AdvancementMain main;
    private final Map<Integer, LoadedTeam> teamsLoaded = new HashMap<>();
    private final Map<UUID, LoadedPlayer> playersLoaded = new HashMap<>();

    /**
     * Lock to acquire in order to avoid team and progression updates in <i>async</i> code.
     * <p>Please note that <strong>using this lock on the main thread is useless</strong>
     * and trying to use it will throw a {@link SyncExecutionException}.
     *
     * @see ReentrantUpdaterLock
     * @since 3.0.0
     */
    public final ReentrantUpdaterLock updaterLock = new ReentrantUpdaterLock();
    private final PendingUpdatesManager pendingUpdatesManager = new PendingUpdatesManager();

    /**
     * Creates a new {@code DatabaseManager} which uses an in-memory database.
     *
     * @param main The {@link AdvancementMain}.
     * @throws Exception If anything goes wrong.
     */
    public DatabaseManager(@NotNull AdvancementMain main) throws Exception {
        this(main, new InMemory(main.getLogger()));
    }

    /**
     * Creates a new {@code DatabaseManager} which uses a SQLite database.
     *
     * @param main The {@link AdvancementMain}.
     * @param dbFile The SQLite database file.
     * @throws Exception If anything goes wrong.
     */
    public DatabaseManager(@NotNull AdvancementMain main, @NotNull File dbFile) throws Exception {
        this(main, new SQLite(Objects.requireNonNull(dbFile, "Database file is null."), main.getLogger()));
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
        this(main, new MySQL(username, password, databaseName, host, port, poolSize, connectionTimeout, main.getLogger(), main.getLibbyManager()));
    }

    private DatabaseManager(@NotNull AdvancementMain main, @NotNull IDatabase database) throws Exception {
        Preconditions.checkNotNull(main, "AdvancementMain is null.");
        this.main = main;
        this.eventManager = main.getEventManager();
        this.database = database;
        this.joinEventWaiter = new JoinEventWaiter(main.getOwningPlugin());
        commonSetUp();
    }

    private void commonSetUp() throws SQLException {
        // Run it sync to avoid using uninitialized database
        database.setUp();

        eventManager.register(this, PlayerLoginEvent.class, EventPriority.LOWEST, e -> {
            joinEventWaiter.onLogin(e.getPlayer().getUniqueId());

            final LoadedPlayer loadedPlayer;

            synchronized (DatabaseManager.this) {
                loadedPlayer = addPlayerToCache(e.getPlayer().getUniqueId(), true);

                // Keep in cache waiting for runAsyncOnExecutor(...)
                loadedPlayer.addInternalRequest();
            }

            runAsyncOnExecutor(() -> {
                synchronized (DatabaseManager.this) {
                    if (!loadedPlayer.isOnline()) {
                        // Make sure the player is still online. Shouldn't be a problem here, but better be sure
                        return;
                    }

                    // Return instantly if the player is already loaded
                    if (loadedPlayer.getPlayerTeam() != null) {
                        firePlayerLoadingCompletedEvent(e.getPlayer(), loadedPlayer);
                        return;
                    }

                    CompletableFuture<TeamProgression> registeringCF = loadedPlayer.getRegisteringCF();
                    if (registeringCF != null) {
                        // Wait for PlayerRegisteredEvent
                        loadedPlayer.addInternalRequest(); // Keep in cache while waiting for event
                        registeringCF.whenComplete((team, err) -> {
                            if (closed.get()) {
                                return;
                            }
                            try {
                                if (err != null) {
                                    firePlayerLoadingFailedEvent(e.getPlayer(), err);
                                } else {
                                    firePlayerLoadingCompletedEvent(e.getPlayer(), loadedPlayer);
                                }
                            } finally {
                                removeInternalRequest(loadedPlayer);
                            }
                        });
                        return;
                    }

                    // Search the player in already loaded teams to see if they're there.
                    // The searching is done here since more players from the same team may join at the same moment
                    // and running the searching on the executor thread increases the possibilities of a cache hit
                    LoadedTeam loadedTeam = searchPlayerTeam(e.getPlayer().getUniqueId());
                    if (loadedTeam != null) {
                        // Player team is already loaded
                        loadedPlayer.setPlayerTeam(loadedTeam);
                        loadedTeam.addInternalRequest(); // Add the request for the player
                        firePlayerLoadingCompletedEvent(e.getPlayer(), loadedPlayer);
                        return;
                    }
                }

                try {
                    // Player team isn't loaded yet
                    loadOrRegisterPlayerTeam(e.getPlayer().getUniqueId(), e.getPlayer(), loadedPlayer, false, pro -> {
                        firePlayerLoadingCompletedEvent(e.getPlayer(), loadedPlayer);
                    });
                } catch (Exception ex) {
                    System.err.println("Cannot load player " + e.getPlayer().getName() + ':');
                    ex.printStackTrace();
                    firePlayerLoadingFailedEvent(e.getPlayer(), ex);
                }
            }).whenComplete((v, k) -> {
                removeInternalRequest(loadedPlayer);
            });
        });
        eventManager.register(this, PlayerJoinEvent.class, e -> {
            joinEventWaiter.onJoin(e.getPlayer().getUniqueId());
        });
        eventManager.register(this, PlayerQuitEvent.class, EventPriority.MONITOR, e -> {
            joinEventWaiter.onQuit(e.getPlayer().getUniqueId());
            synchronized (DatabaseManager.this) {
                // loadedPlayer should be always non-null, but it's better to check it
                LoadedPlayer loadedPlayer = Objects.requireNonNull(playersLoaded.get(e.getPlayer().getUniqueId()));
                loadedPlayer.setOnline(false);
                removeInternalRequest(loadedPlayer);
            }
        });
        eventManager.register(this, PluginDisableEvent.class, EventPriority.HIGHEST, e -> {
            synchronized (DatabaseManager.this) {
                List<LoadedPlayer> list = new ArrayList<>(playersLoaded.size());
                for (LoadedPlayer loadedPlayer : playersLoaded.values()) {
                    if (loadedPlayer.__removeAllPluginRequests(e.getPlugin()) != 0 && loadedPlayer.canBeUnloaded()) {
                        // Player can be unloaded
                        list.add(loadedPlayer);
                    }
                }
                for (LoadedPlayer loadedPlayer : list) {
                    // Handle unload
                    unloadPlayer(loadedPlayer);
                }
            }
        });
        runAsyncOnExecutor(() -> {
            try {
                database.clearUpTeams();
            } catch (SQLException e) {
                System.err.println("Cannot clear up unused team ids:");
                e.printStackTrace();
            }
        });
    }

    /**
     * Closes the connection to the database and frees the cache.
     * <p>This method does not call {@link Event}s.
     */
    public void close() {
        if (closed.getAndSet(true)) {
            return; // Already closed
        }

        if (eventManager.isEnabled()) {
            eventManager.unregister(this);
        }

        joinEventWaiter.onClose();

        pendingUpdatesManager.unregisterTask();

        // Shutdown executor before database connection
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                // There are other tasks
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("It was not possible to terminate some tasks while closing DatabaseManager.");
                }
            }
        } catch (InterruptedException ignored) {
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // Preserve interrupt status
        } catch (Exception e) {
            System.err.println("An exception occurred shutting down the executor thread while closing DatabaseManager:");
            e.printStackTrace();
            executor.shutdownNow();
        }

        synchronized (DatabaseManager.this) {
            pendingUpdatesManager.clearUpdates();

            teamsLoaded.forEach((u, t) -> t.getTeamProgression().inCache.set(false)); // Invalidate TeamProgression
            for (LoadedPlayer player : playersLoaded.values()) {
                @Nullable var cf = player.getRegisteringCF();
                if (cf != null) {
                    player.unsetRegistering();
                    // The player was being registered, but the updater task hasn't run. Unregister they from the db
                    // so that next time they log in the PlayerRegisteredEvent will be called
                    cf.completeExceptionally(new DatabaseManagerClosedException());
                    try {
                        database.unregisterPlayer(player.getUuid());
                    } catch (Exception e) {
                        System.err.println("Couldn't unregister player " + player.getUuid() + " (waiting PlayerRegisteredEvent) while closing DatabaseManager:");
                        e.printStackTrace();
                    }
                }
            }
            playersLoaded.clear();
            teamsLoaded.clear();
            synchronized (uncompletedCompletableFutures) {
                for (CompletableFuture<?> uncompleted : uncompletedCompletableFutures.values()) {
                    uncompleted.completeExceptionally(new DatabaseManagerClosedException());
                }
                uncompletedCompletableFutures.clear();
            }
        }
        try {
            database.close();
        } catch (Exception e) {
            System.err.println("Couldn't close the database connection while closing DatabaseManager:");
            e.printStackTrace();
        }
    }

    private void firePlayerLoadingCompletedEvent(@NotNull Player player, @NotNull LoadedPlayer loadedPlayer) {
        loadedPlayer.addInternalRequest(); // Keep in cache while waiting for the BukkitRunnable

        joinEventWaiter.onFinishLoading(player.getUniqueId(), LOAD_EVENTS_DELAY, () -> {
            synchronized (DatabaseManager.this) {
                if (closed.get()) {
                    return;
                }

                if (!player.isOnline()) {
                    removeInternalRequest(loadedPlayer);
                    return;
                }

                LoadedTeam loadedTeam = loadedPlayer.getPlayerTeam();
                if (loadedTeam == null) {
                    removeInternalRequest(loadedPlayer);
                    return;
                }

                callEventCatchingExceptions(new PlayerLoadingCompletedEvent(player, loadedTeam.getTeamProgression()));

                // No need to call addInternalRequest here since the request can be reused and removed later
                processUnredeemed(loadedPlayer, player);
            }
            main.updatePlayer(player);
        }, () -> {
            // On cancel remove the internal request since the task didn't run
            removeInternalRequest(loadedPlayer);
        });
    }

    private void firePlayerLoadingFailedEvent(@NotNull Player player, @NotNull Throwable error) {
        joinEventWaiter.onFinishLoading(player.getUniqueId(), 0, () -> {
            if (!closed.get() && player.isOnline()) { // Make sure the player is still online
                callEventCatchingExceptions(new PlayerLoadingFailedEvent(player, error));
            }
        }, () -> {
            // Nothing to do on cancel
        });
    }

    @Nullable
    private synchronized LoadedTeam searchPlayerTeam(@NotNull UUID uuid) {
        // Search in every loaded TeamProgression if the player is in one of them
        for (LoadedTeam loadedTeam : teamsLoaded.values()) {
            if (loadedTeam.getTeamProgression().contains(uuid)) {
                // The player team is already loaded
                return loadedTeam;
            }
        }
        return null;
    }

    /**
     * Load the provided player from the database. If they are not present, this method registers they.
     * <p><strong>Should be called async.</strong>
     *
     * @param uuid The UUID of the player to be loaded or registered.
     * @param player The player to be loaded or registered.
     * @param loadedPlayer The {@link LoadedPlayer} object associated with player.
     * @param loadOnly Weather to only load the player and fail if not present in the database.
     * @param callback Callback called when the load/registration process ends. May be called either sync or async.
     * @throws SQLException If anything goes wrong.
     */
    private void loadOrRegisterPlayerTeam(@NotNull UUID uuid, Player player, @NotNull LoadedPlayer loadedPlayer, boolean loadOnly, @NotNull Consumer<TeamProgression> callback) throws SQLException {
        final TeamProgression progression;
        final boolean fireRegisteredEvent;
        if (loadOnly) {
            progression = database.loadUUID(uuid);
            fireRegisteredEvent = false;
        } else {
            // The player here is always online when called, otherwise it is better to not register the player at all
            String name = Objects.requireNonNull(player, "Player is null.").getName();
            Entry<TeamProgression, Boolean> e = database.loadOrRegisterPlayer(uuid, Objects.requireNonNull(name, "Player name is null."));
            progression = e.getKey();
            fireRegisteredEvent = e.getValue();
        }
        if (!fireRegisteredEvent && player != null && player.getName() != null) {
            updatePlayerName(player);
        }

        synchronized (DatabaseManager.this) {
            final LoadedTeam loadedTeam = addTeamToCache(progression);
            if (fireRegisteredEvent) {
                pendingUpdatesManager.registerPlayerRegisteredUpdate(loadedPlayer, loadedTeam, () -> callback.accept(progression));
            } else {
                loadedTeam.addInternalRequest();
                loadedPlayer.setPlayerTeam(loadedTeam);
                callback.accept(progression);
            }
        }
    }

    /**
     * Process unredeemed advancements for the provided player.
     * <p><strong>The caller must "pass" one internal request when calling this method.</strong>
     *
     * @param loadedPlayer The loaded player. Must be present in cache! That is not checked!
     * @param player The player.
     */
    private void processUnredeemed(final @NotNull LoadedPlayer loadedPlayer, final @NotNull Player player) {
        // addInternalRequest is not needed here since the caller must "pass" one to us

        // Used to keep track of whether removing the team internal request is needed
        // (the array is a hack to avoid "Variable used in lambda expression should be final or effectively final")
        //
        // To be sure, the array is accessed while synchronizing on DatabaseManager.this. Doing this doesn't affect
        // performance, since we're already synchronizing when setting the team and the removeInternalRequest(...)
        // method does synchronize on DatabaseManager.this
        final LoadedTeam[] teamToWhichRemoveInternalRequest = {null};

        runAsyncOnExecutor(() -> {
            final LoadedTeam loadedTeam;
            synchronized (DatabaseManager.this) {
                loadedTeam = loadedPlayer.getPlayerTeam();
                if (!loadedPlayer.isOnline() || loadedTeam == null) {
                    // The player is not online or in a team
                    return;
                }
                loadedTeam.addInternalRequest(); // Keep the team loaded
                teamToWhichRemoveInternalRequest[0] = loadedTeam; // Make sure the internal request will be removed
            }

            // Do all the computation in one go on the executor thread. This avoids granting the advancements more than
            // once per unredeemed (dupes), since other calls to processUnredeemed must wait for this to finish

            final LinkedList<Entry<AdvancementKey, Boolean>> list;
            try {
                list = database.getUnredeemed(loadedTeam.getTeamProgression().getTeamId());
            } catch (Exception e) {
                System.err.println("Cannot fetch unredeemed advancements:");
                e.printStackTrace();
                return;
            }

            if (list.isEmpty() || !loadedPlayer.isOnline()) {
                return;
            }

            final List<Entry<Advancement, Boolean>> advs = new ArrayList<>(list.size());
            Iterator<Entry<AdvancementKey, Boolean>> it = list.iterator();
            while (it.hasNext()) {
                Entry<AdvancementKey, Boolean> k = it.next();
                Advancement a = main.getAdvancement(k.getKey()); // getAdvancement is thread-safe to call
                if (a == null) {
                    it.remove();
                } else {
                    advs.add(new SimpleEntry<>(a, k.getValue()));
                }
            }
            if (closed.get() || advs.isEmpty() || !loadedPlayer.isOnline() || !loadedTeam.getTeamProgression().contains(loadedPlayer.getUuid())) {
                return;
            }

            try {
                database.unsetUnredeemed(Collections.unmodifiableList(list), loadedTeam.getTeamProgression().getTeamId());
            } catch (Exception e) {
                System.err.println("Couldn't unset unredeemed advancements for player " + player.getName() + ":");
                e.printStackTrace();
                return;
            }

            // Make sure the player and team stays loaded while waiting for the main thread
            loadedPlayer.addInternalRequest();
            loadedTeam.addInternalRequest();

            runSync(main, () -> {
                // No synchronized(DatabaseManager.this) is needed here since we are on the main thread
                try {
                    // Check if the player has gone offline or has changed team
                    // Also check if the DatabaseManager has been closed
                    if (closed.get() || !player.isOnline() || !loadedTeam.getTeamProgression().contains(loadedPlayer.getUuid())) {
                        // Then restore unredeemed advancements into the db
                        // If something goes wrong here, the unredeemed advs will be lost, but this is reasonable
                        // since this doesn't allow dupes

                        loadedPlayer.addInternalRequest();

                        // Don't use runAsyncOnExecutor since the following should always execute, even when DatabaseManager is closed
                        CompletableFuture.runAsync(() -> {
                            for (Entry<AdvancementKey, Boolean> entry : list) {
                                int teamId = loadedTeam.getTeamProgression().getTeamId();
                                try {
                                    database.setUnredeemed(entry.getKey(), entry.getValue(), teamId);
                                } catch (Exception e) {
                                    System.err.println("Error restoring unredeemed advancement '" + entry.getKey() + "' for team " + teamId + ':');
                                    e.printStackTrace();
                                }
                            }
                        }, executor).whenComplete((r, e) -> {
                            removeInternalRequest(loadedPlayer);
                        });
                        return;
                    }

                    for (Entry<Advancement, Boolean> e : advs) {
                        try {
                            e.getKey().onGrant(player, e.getValue());
                        } catch (Exception err) {
                            System.err.println("onGrant method of advancement '" + e.getKey().getKey() + "' has thrown an error:");
                            err.printStackTrace();
                        }
                    }
                } finally {
                    synchronized (DatabaseManager.this) {
                        removeInternalRequest(loadedPlayer);
                        removeInternalRequest(loadedTeam);
                    }
                }
            });
        }).whenComplete((r, e) -> {
            synchronized (DatabaseManager.this) {
                removeInternalRequest(loadedPlayer);

                // Remove the request to the team only if it has been added before,
                // in which case the team has been put into teamToWhichRemoveInternalRequest[0]
                if (teamToWhichRemoveInternalRequest[0] != null) {
                    removeInternalRequest(teamToWhichRemoveInternalRequest[0]);
                }
            }
        });
    }

    /**
     * Updates the name of the specified player in the database.
     *
     * @param player The player to update.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @see UltimateAdvancementAPI#updatePlayerName(Player)
     */
    @NotNull
    public CompletableFuture<Void> updatePlayerName(@NotNull Player player) {
        checkClosed();
        Preconditions.checkNotNull(player, "Player cannot be null.");
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
            try {
                database.updatePlayerName(player.getUniqueId(), player.getName());
            } catch (SQLException e) {
                System.err.println("Cannot update player " + player.getName() + " name:");
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }
            completableFuture.complete(null);
        });

        return completableFuture;
    }

    /**
     * Moves the provided player from their team to the second player's one.
     * <p>The player is not moved immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
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
     * <p>The player is not moved immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param playerToMove The {@link UUID} of the player to move.
     * @param otherTeamMember The {@link UUID} of a player of the destination team.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#updatePlayerTeam(UUID, UUID, Consumer)
     */
    @NotNull
    public CompletableFuture<Void> updatePlayerTeam(@NotNull UUID playerToMove, @NotNull UUID otherTeamMember) throws UserNotLoadedException {
        return updatePlayerTeam(playerToMove, getTeamProgression(otherTeamMember));
    }

    /**
     * Moves the provided player from their team to the specified one.
     * <p>The player is not moved immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param playerToMove The {@link UUID} of the player to move.
     * @param otherTeamProgression The {@link TeamProgression} of the target team.
     * @return A {@link CompletableFuture} which provides the result of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    @NotNull
    public CompletableFuture<Void> updatePlayerTeam(@NotNull UUID playerToMove, @NotNull TeamProgression otherTeamProgression) throws UserNotLoadedException {
        return updatePlayerTeam(playerToMove, Bukkit.getPlayer(playerToMove), otherTeamProgression);
    }

    /**
     * Moves the provided player from their team to the specified one.
     * <p>The player is not moved immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
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
        checkClosed();
        Preconditions.checkNotNull(playerToMove, "Player to move is null.");

        final LoadedTeam loadedNewTeam;
        final LoadedPlayer loadedPlayer;
        synchronized (DatabaseManager.this) {
            validateTeamProgression(otherTeamProgression);

            loadedNewTeam = teamsLoaded.get(otherTeamProgression.getTeamId());
            if (loadedNewTeam == null) {
                throw new IllegalArgumentException("Invalid TeamProgression.");
            }

            loadedPlayer = playersLoaded.get(playerToMove);
            if (loadedPlayer == null || loadedPlayer.getPlayerTeam() == null) {
                throw new UserNotLoadedException(playerToMove);
            }

            // Put these line here after the checks to avoid having to do calls to removeInternalRequest before throwing exceptions
            loadedNewTeam.addInternalRequest();
            loadedPlayer.addInternalRequest();
        }

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
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

            registerTeamUpdate(loadedPlayer, ptm, loadedNewTeam, true /*processUnredeemed*/, completableFuture, null /*completingObj*/);
        }).whenComplete((v, t) -> {
            synchronized (DatabaseManager.this) {
                removeInternalRequest(loadedPlayer);
                removeInternalRequest(loadedNewTeam);
            }
        });

        return completableFuture;
    }

    /**
     * Moves the provided player into a new team.
     * <p>The player is not moved immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
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
     * <p>The player is not moved immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
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
        checkClosed();
        Preconditions.checkNotNull(uuid, "UUID is null.");

        final LoadedPlayer loadedPlayer;
        synchronized (DatabaseManager.this) {
            loadedPlayer = playersLoaded.get(uuid);
            if (loadedPlayer == null || loadedPlayer.getPlayerTeam() == null) {
                throw new UserNotLoadedException(uuid);
            }

            loadedPlayer.addInternalRequest();
        }

        CompletableFuture<TeamProgression> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
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

            synchronized (DatabaseManager.this) {
                final LoadedTeam loadedTeam = addTeamToCache(newPro);
                registerTeamUpdate(loadedPlayer, ptr, loadedTeam, false /*processUnredeemed*/, completableFuture, newPro /*completingObj*/);
            }
        }).whenComplete((v, t) -> {
            removeInternalRequest(loadedPlayer);
        });

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
        checkClosed();
        Preconditions.checkNotNull(uuid, "UUID is null.");

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
            synchronized (DatabaseManager.this) {
                if (Bukkit.getPlayer(uuid) != null) {
                    completableFuture.completeExceptionally(new IllegalStateException("Player is online."));
                    return;
                }
                if (playersLoaded.containsKey(uuid)) {
                    completableFuture.completeExceptionally(new IllegalStateException("Player is temporary loaded."));
                    return;
                }
            }
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
        });

        return completableFuture;
    }

    /**
     * Sets the progression of the specified advancement.
     * <p>The progression is not updated immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param key The advancement key.
     * @param uuid The {@link UUID} of the player who made the advancement.
     * @param newProgression The new progression.
     * @return A {@link CompletableFuture} that will complete when the database update finishes.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    @NotNull
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull AdvancementKey key, @NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) throws UserNotLoadedException {
        return setProgression(key, getTeamProgression(uuid), newProgression);
    }

    /**
     * Sets the progression of the specified advancement.
     * <p>The progression is not updated immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param key The advancement key.
     * @param player The player who made the advancement.
     * @param newProgression The new progression.
     * @return A {@link CompletableFuture} that will complete when the database update finishes.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    @NotNull
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull AdvancementKey key, @NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) throws UserNotLoadedException {
        return setProgression(key, uuidFromPlayer(player), newProgression);
    }

    /**
     * Sets the progression of the specified advancement.
     * <p>The progression is not updated immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param key The advancement key.
     * @param progression The {@link TeamProgression} of the team which made the advancement.
     * @param newProgression The new progression.
     * @return A {@link CompletableFuture} that will complete when the database update finishes.
     */
    @NotNull
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull AdvancementKey key, @NotNull TeamProgression progression, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) {
        checkClosed();
        Preconditions.checkNotNull(key, "Key is null.");
        validateProgressionValue(newProgression);

        final LoadedTeam loadedNewTeam;
        synchronized (DatabaseManager.this) {
            validateTeamProgression(progression);

            loadedNewTeam = teamsLoaded.get(progression.getTeamId());
            if (loadedNewTeam == null) {
                throw new IllegalArgumentException("Invalid TeamProgression.");
            }

            loadedNewTeam.addInternalRequest();
        }

        CompletableFuture<ProgressionUpdateResult> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
            int old = pendingUpdatesManager.getCurrentValue(progression, key);
            try {
                database.updateAdvancement(key, progression.getTeamId(), newProgression);
            } catch (SQLException e) {
                System.err.println("Cannot set progression of advancement " + key + " to team " + progression.getTeamId() + ':');
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }
            registerProgressionUpdate(loadedNewTeam, key, old, newProgression, completableFuture);
        }).whenComplete((v, t) -> {
            removeInternalRequest(loadedNewTeam);
        });

        return completableFuture;
    }

    /**
     * Increments the progression of the specified advancement.
     * <p>The progression is not updated immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param key The advancement key.
     * @param uuid The {@link UUID} of the player who made the advancement.
     * @param increment The increment of the progression. May be negative.
     * @return A {@link CompletableFuture} that will complete when the database update finishes.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @since 3.0.0
     */
    @NotNull
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull AdvancementKey key, @NotNull UUID uuid, int increment) throws UserNotLoadedException {
        return incrementProgression(key, getTeamProgression(uuid), increment);
    }

    /**
     * Increments the progression of the specified advancement.
     * <p>The progression is not updated immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param key The advancement key.
     * @param player The player who made the advancement.
     * @param increment The increment of the progression. May be negative.
     * @return A {@link CompletableFuture} that will complete when the database update finishes.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @since 3.0.0
     */
    @NotNull
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull AdvancementKey key, @NotNull Player player, int increment) throws UserNotLoadedException {
        return incrementProgression(key, uuidFromPlayer(player), increment);
    }

    /**
     * Increments the progression of the specified advancement.
     * <p>The progression is not updated immediately in the cache, only when the database operation ends and the {@link CompletableFuture} returns.
     *
     * @param key The advancement key.
     * @param progression The {@link TeamProgression} of the team which made the advancement.
     * @param increment The increment of the progression. May be negative.
     * @return A {@link CompletableFuture} that will complete when the database update finishes.
     * @since 3.0.0
     */
    @NotNull
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull AdvancementKey key, @NotNull TeamProgression progression, int increment) {
        checkClosed();
        Preconditions.checkNotNull(key, "Key is null.");

        final LoadedTeam loadedNewTeam;
        synchronized (DatabaseManager.this) {
            validateTeamProgression(progression);

            loadedNewTeam = teamsLoaded.get(progression.getTeamId());
            if (loadedNewTeam == null) {
                throw new IllegalArgumentException("Invalid TeamProgression.");
            }

            loadedNewTeam.addInternalRequest();
        }

        CompletableFuture<ProgressionUpdateResult> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
            final int old = pendingUpdatesManager.getCurrentValue(loadedNewTeam.getTeamProgression(), key);
            int incremented = old;
            if (increment != 0) { // Don't update the db if the saved progression won't change
                incremented += increment;
                if (incremented < 0) {
                    // Don't throw an error if incremented < 0, simply put it to 0
                    incremented = 0;
                }
                try {
                    database.updateAdvancement(key, progression.getTeamId(), incremented);
                } catch (SQLException e) {
                    System.err.println("Cannot increment progression of advancement " + key + " to team " + progression.getTeamId() + ':');
                    e.printStackTrace();
                    completableFuture.completeExceptionally(new DatabaseException(e));
                    return;
                } catch (Exception e) {
                    completableFuture.completeExceptionally(new DatabaseException(e));
                    return;
                }
            }
            registerProgressionUpdate(loadedNewTeam, key, old, incremented, completableFuture);
        }).whenComplete((v, t) -> {
            removeInternalRequest(loadedNewTeam);
        });

        return completableFuture;
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
        checkClosed();
        Preconditions.checkNotNull(uuid, "UUID is null.");
        LoadedPlayer loadedPlayer = playersLoaded.get(uuid);
        if (loadedPlayer == null || loadedPlayer.getPlayerTeam() == null) {
            throw new UserNotLoadedException(uuid);
        }
        return loadedPlayer.getPlayerTeam().getTeamProgression();
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
        checkClosed();
        Preconditions.checkNotNull(key, "AdvancementKey is null.");

        final LoadedTeam loadedNewTeam;
        synchronized (DatabaseManager.this) {
            validateTeamProgression(pro);

            loadedNewTeam = teamsLoaded.get(pro.getTeamId());
            if (loadedNewTeam == null) {
                throw new IllegalArgumentException("Invalid TeamProgression.");
            }

            loadedNewTeam.addInternalRequest();
        }

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
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
        }).whenComplete((v, t) -> {
            removeInternalRequest(loadedNewTeam);
        });

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
        checkClosed();
        Preconditions.checkNotNull(key, "AdvancementKey is null.");

        final LoadedTeam loadedNewTeam;
        synchronized (DatabaseManager.this) {
            validateTeamProgression(pro);

            loadedNewTeam = teamsLoaded.get(pro.getTeamId());
            if (loadedNewTeam == null) {
                throw new IllegalArgumentException("Invalid TeamProgression.");
            }

            loadedNewTeam.addInternalRequest();
        }

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
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
        }).whenComplete((v, t) -> {
            removeInternalRequest(loadedNewTeam);
        });

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
        checkClosed();
        Preconditions.checkNotNull(key, "AdvancementKey is null.");

        final LoadedTeam loadedNewTeam;
        synchronized (DatabaseManager.this) {
            validateTeamProgression(pro);

            loadedNewTeam = teamsLoaded.get(pro.getTeamId());
            if (loadedNewTeam == null) {
                throw new IllegalArgumentException("Invalid TeamProgression.");
            }

            loadedNewTeam.addInternalRequest();
        }

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
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
        }).whenComplete((v, t) -> {
            removeInternalRequest(loadedNewTeam);
        });

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
        checkClosed();
        Preconditions.checkNotNull(uuid, "UUID is null.");

        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
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
        });

        return completableFuture;
    }

    /**
     * Loads the provided player from the database into the caching system (if they aren't already) and keeps they
     * loaded until {@link #removeLoadingRequestToPlayer(UUID, Plugin)} is called.
     * <p>Each time this method is called, the number of <i>loading requests</i> for the provided player and requester
     * plugin is incremented by one. Instead, the counterpart of this method, {@link #removeLoadingRequestToPlayer(UUID, Plugin)},
     * decrements by one the number of <i>loading requests</i> for the player and requester plugin every time it's called.
     * <br>Since the caching system ensures a player is always kept loaded while they have at least {@code 1} <i>loading request</i>,
     * this method can be used to make sure a player isn't unloaded, even if they were online and quit from the server.
     * <p>To know how many <i>loading requests</i> a plugin holds for a player, the method
     * {@link #getLoadingRequestsAmount(UUID, Plugin)} can be used.
     * <p>Also, a plugin should only use its own instance as requester to not interfere with other plugins.
     *
     * @param uuid The {@link UUID} of the player.
     * @param requester The plugin making the request.
     * @return A {@link CompletableFuture} which provides the player team's {@link TeamProgression}.
     * @see #removeLoadingRequestToPlayer(UUID, Plugin)
     * @see #getLoadingRequestsAmount(UUID, Plugin)
     */
    @NotNull
    public CompletableFuture<TeamProgression> loadAndAddLoadingRequestToPlayer(@NotNull UUID uuid, @NotNull Plugin requester) {
        checkClosed();
        Preconditions.checkNotNull(uuid, "UUID is null.");
        Preconditions.checkNotNull(requester, "Plugin is null.");

        CompletableFuture<TeamProgression> completableFuture = new CompletableFuture<>();

        final LoadedPlayer loadedPlayer;
        synchronized (DatabaseManager.this) {
            if (!requester.isEnabled()) {
                throw new IllegalStateException("Plugin is not enabled.");
            }

            loadedPlayer = playersLoaded.computeIfAbsent(uuid, LoadedPlayer::new);
            loadedPlayer.setOnline(Bukkit.getPlayer(uuid) != null); // Just to be sure

            // Don't do this here, it's better to do it before returning below in the runAsync lambda. This avoids
            // counting this plugin request in the return value of #getLoadingRequestsAmount(...) in case of a DB error
            // loadedPlayer.addPluginRequest(requester);

            // Keep in cache waiting for runAsyncOnExecutor(...)
            loadedPlayer.addInternalRequest();
        }

        runAsyncOnExecutor(completableFuture, () -> {
            synchronized (DatabaseManager.this) {
                // Re-do the "already loaded" check.
                // This is necessary to avoid running searchPlayerTeam(...) below, since that will call
                // updateLoadedPlayerTeam(...), which will call a (wrong) AsyncTeamUpdateEvent (with Action.LEAVE)
                if (loadedPlayer.getPlayerTeam() != null) {
                    loadedPlayer.addPluginRequest(requester);
                    completableFuture.complete(loadedPlayer.getPlayerTeam().getTeamProgression());
                    return;
                }

                CompletableFuture<TeamProgression> registeringCF = loadedPlayer.getRegisteringCF();
                if (registeringCF != null) {
                    // Wait for PlayerRegisteredEvent
                    loadedPlayer.addInternalRequest(); // Keep in cache while waiting for event
                    registeringCF.whenComplete((team, err) -> {
                        try {
                            if (err != null) {
                                completableFuture.completeExceptionally(err);
                            } else {
                                loadedPlayer.addPluginRequest(requester);
                                completableFuture.complete(team);
                            }
                        } finally {
                            removeInternalRequest(loadedPlayer);
                        }
                    });
                    return;
                }

                // Search the player in already loaded teams to see if it's there
                // The searching is done here since more players from the same team may join at the same moment
                // and running the searching on the executor's thread improves the possibility of a cache hit
                LoadedTeam loadedTeam = searchPlayerTeam(uuid);
                if (loadedTeam != null) {
                    // Player team is already loaded
                    loadedPlayer.addPluginRequest(requester);
                    loadedPlayer.setPlayerTeam(loadedTeam);
                    loadedTeam.addInternalRequest(); // Add the request for the player
                    completableFuture.complete(loadedTeam.getTeamProgression());
                    return;
                }
            }

            try {
                // Player team isn't loaded yet
                loadOrRegisterPlayerTeam(uuid, null /*fine since loadOnly is true*/, loadedPlayer, true /*loadOnly*/, pro -> {
                    loadedPlayer.addPluginRequest(requester);
                    completableFuture.complete(pro);
                });
            } catch (SQLException e) {
                System.err.println("Cannot load offline player " + uuid + ':');
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }
        }).whenComplete((v, t) -> {
            removeInternalRequest(loadedPlayer);
        });

        return completableFuture;
    }

    /**
     * Counterpart of {@link #loadAndAddLoadingRequestToPlayer(UUID, Plugin)}. Decrements by one the <i>loading requests</i>
     * count for the provided player and requester plugin.
     * <p>Since the caching system ensures a player is always kept loaded while they have at least {@code 1} <i>loading request</i>,
     * if after the decrement (done by this method) the total amount of <i>loading requests</i> drops to {@code 0}, the
     * caching system becomes able to unload the player at any time as soon as it can (for example, the player cannot be
     * unloaded if they are online).
     * <p>Also, a plugin should only use its own instance as requester to not interfere with other plugins.
     *
     * @param uuid The {@link UUID} of the player.
     * @param requester The plugin which requested the loading.
     * @see #loadAndAddLoadingRequestToPlayer(UUID, Plugin)
     * @see #getLoadingRequestsAmount(UUID, Plugin)
     */
    public void removeLoadingRequestToPlayer(@NotNull UUID uuid, @NotNull Plugin requester) {
        checkClosed();
        Preconditions.checkNotNull(uuid, "UUID is null.");
        Preconditions.checkNotNull(requester, "Plugin is null.");
        synchronized (DatabaseManager.this) {
            Preconditions.checkArgument(requester.isEnabled(), "Plugin isn't enabled.");

            LoadedPlayer loadedPlayer = playersLoaded.get(uuid);
            if (loadedPlayer != null)
                removePluginRequest(loadedPlayer, requester);
        }
    }

    /**
     * Creates a new empty team with {@code 1} <i>loading request</i> for the team and the provided requester plugin.
     * <p>The method {@link #removeLoadingRequestToTeam(TeamProgression, Plugin)} can be used to remove the <i>loading request</i>.
     * <p>For more information about <i>loading requests</i> see {@link #addLoadingRequestToTeam(TeamProgression, Plugin)},
     * {@link #removeLoadingRequestToTeam(TeamProgression, Plugin)} and {@link #getLoadingRequestsAmount(TeamProgression, Plugin)}.
     * <p>Also, a plugin should only use its own instance as requester to not interfere with other plugins.
     *
     * @param requester The plugin which requested to create the team.
     * @return A {@link CompletableFuture} which provides the {@link TeamProgression} of the new team.
     * @see #addLoadingRequestToTeam(TeamProgression, Plugin)
     * @see #removeLoadingRequestToTeam(TeamProgression, Plugin)
     * @see #getLoadingRequestsAmount(TeamProgression, Plugin)
     */
    @NotNull
    public CompletableFuture<TeamProgression> createNewTeamWithOneLoadingRequest(@NotNull Plugin requester) {
        checkClosed();
        Preconditions.checkNotNull(requester, "Plugin is null.");

        synchronized (DatabaseManager.this) {
            if (!requester.isEnabled()) {
                throw new IllegalStateException("Plugin is not enabled.");
            }
        }

        CompletableFuture<TeamProgression> completableFuture = new CompletableFuture<>();

        runAsyncOnExecutor(completableFuture, () -> {
            TeamProgression team;
            try {
                team = database.createNewTeam();
            } catch (SQLException e) {
                System.err.println("Cannot create a new team:");
                e.printStackTrace();
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            } catch (Exception e) {
                completableFuture.completeExceptionally(new DatabaseException(e));
                return;
            }

            synchronized (DatabaseManager.this) {
                // Check again if the plugin is enabled
                if (!requester.isEnabled()) {
                    completableFuture.completeExceptionally(new IllegalStateException("Plugin is not enabled."));
                    return;
                }

                LoadedTeam loadedTeam = addTeamToCache(team);
                loadedTeam.addPluginRequest(requester);
            }

            completableFuture.complete(team);
        });

        return completableFuture;
    }

    /**
     * Keeps the provided team loaded until {@link #removeLoadingRequestToTeam(TeamProgression, Plugin)} is called.
     * <p>Each time this method is called, the number of <i>loading requests</i> for the provided team and requester
     * plugin is incremented by one. Instead, the counterpart of this method, {@link #removeLoadingRequestToTeam(TeamProgression, Plugin)},
     * decrements by one the number of <i>loading requests</i> for the team and requester plugin every time it's called.
     * <br>Since the caching system ensures a team is always kept loaded while they have at least {@code 1} <i>loading request</i>,
     * this method can be used to make sure a team isn't unloaded, even if none of its members are loaded.
     * <p>To know how many <i>loading requests</i> a plugin holds for a team, the method
     * {@link #getLoadingRequestsAmount(TeamProgression, Plugin)} can be used.
     * <p>Also, a plugin should only use its own instance as requester to not interfere with other plugins.
     *
     * @param teamProgression The team to keep loaded.
     * @param requester The plugin which requested to keep the team loaded.
     * @throws IllegalArgumentException If the provided {@link TeamProgression} is invalid (see {@link TeamProgression#isValid()}).
     * @see #removeLoadingRequestToTeam(TeamProgression, Plugin)
     * @see #getLoadingRequestsAmount(TeamProgression, Plugin)
     */
    public void addLoadingRequestToTeam(@NotNull TeamProgression teamProgression, @NotNull Plugin requester) throws IllegalArgumentException {
        checkClosed();
        Preconditions.checkNotNull(requester, "Plugin is null.");

        synchronized (DatabaseManager.this) {
            if (!requester.isEnabled()) {
                throw new IllegalStateException("Plugin is not enabled.");
            }
            AdvancementUtils.validateTeamProgression(teamProgression);

            LoadedTeam team = teamsLoaded.get(teamProgression.getTeamId());
            Preconditions.checkArgument(team != null, "Team " + teamProgression.getTeamId() + " isn't loaded.");

            team.addPluginRequest(requester);
        }
    }

    /**
     * Counterpart of {@link #addLoadingRequestToTeam(TeamProgression, Plugin)}. Decrements by one the <i>loading requests</i>
     * count for the provided team and requester plugin.
     * <p>Since the caching system ensures a team is always kept loaded while they have at least {@code 1} <i>loading request</i>,
     * if after the decrement (done by this method) the total amount of <i>loading requests</i> drops to {@code 0}, the
     * caching system becomes able to unload the team at any time as soon as it can (for example, the team cannot be
     * unloaded if some of its members are loaded).
     * <p>Also, a plugin should only use its own instance as requester to not interfere with other plugins.
     *
     * @param teamProgression The team to keep loaded.
     * @param requester The plugin which requested to keep the team loaded.
     * @see #addLoadingRequestToTeam(TeamProgression, Plugin)
     * @see #getLoadingRequestsAmount(TeamProgression, Plugin)
     */
    public void removeLoadingRequestToTeam(@NotNull TeamProgression teamProgression, @NotNull Plugin requester) {
        checkClosed();
        Preconditions.checkNotNull(teamProgression, "TeamProgression is null.");
        Preconditions.checkNotNull(requester, "Plugin is null.");

        synchronized (DatabaseManager.this) {
            Preconditions.checkArgument(requester.isEnabled(), "Plugin isn't enabled.");

            LoadedTeam team = teamsLoaded.get(teamProgression.getTeamId());
            if (team != null)
                removePluginRequest(team, requester);
        }
    }

    /**
     * Returns whether the provided player is loaded into the cache.
     *
     * @param player The player.
     * @return Whether the provided player is loaded into the cache.
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
     */
    @Contract(pure = true, value = "null -> false")
    public synchronized boolean isLoaded(UUID uuid) {
        checkClosed();
        LoadedPlayer loadedPlayer = playersLoaded.get(uuid);
        return loadedPlayer != null && loadedPlayer.getPlayerTeam() != null;
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
        checkClosed();
        LoadedPlayer loadedPlayer = playersLoaded.get(uuid);
        return loadedPlayer != null && loadedPlayer.getPlayerTeam() != null && loadedPlayer.isOnline();
    }

    /**
     * Returns the number of <i>loading requests</i> that a plugin currently holds for the specified player.
     *
     * @param player The player.
     * @param requester The plugin.
     * @return The number of <i>loading requests</i> that a plugin currently holds for the specified player.
     * @see #loadAndAddLoadingRequestToPlayer(UUID, Plugin)
     * @see #removeLoadingRequestToPlayer(UUID, Plugin)
     */
    @Contract(pure = true)
    public int getLoadingRequestsAmount(@NotNull Player player, @NotNull Plugin requester) {
        return getLoadingRequestsAmount(uuidFromPlayer(player), requester);
    }

    /**
     * Returns the number of <i>loading requests</i> that a plugin currently holds for the specified player.
     *
     * @param player The player.
     * @param requester The plugin.
     * @return The number of <i>loading requests</i> that a plugin currently holds for the specified player.
     * @see #loadAndAddLoadingRequestToPlayer(UUID, Plugin)
     * @see #removeLoadingRequestToPlayer(UUID, Plugin)
     */
    @Contract(pure = true)
    public int getLoadingRequestsAmount(@NotNull OfflinePlayer player, @NotNull Plugin requester) {
        return getLoadingRequestsAmount(uuidFromPlayer(player), requester);
    }

    /**
     * Returns the number of <i>loading requests</i> that a plugin currently holds for the specified player.
     *
     * @param uuid The {@link UUID} of the player.
     * @param requester The plugin.
     * @return The number of <i>loading requests</i> that a plugin currently holds for the specified player.
     * @see #loadAndAddLoadingRequestToPlayer(UUID, Plugin)
     * @see #removeLoadingRequestToPlayer(UUID, Plugin)
     */
    @Contract(pure = true)
    public int getLoadingRequestsAmount(@NotNull UUID uuid, @NotNull Plugin requester) {
        checkClosed();
        Preconditions.checkNotNull(requester, "Plugin is null.");
        Preconditions.checkNotNull(uuid, "UUID is null.");

        synchronized (DatabaseManager.this) {
            Preconditions.checkArgument(requester.isEnabled(), "Plugin isn't enabled.");
            LoadedPlayer loadedPlayer = playersLoaded.get(uuid);
            if (loadedPlayer == null) {
                return 0;
            }
            return loadedPlayer.getPluginRequests(requester);
        }
    }

    /**
     * Returns the number of <i>loading requests</i> that a plugin currently holds for the specified team.
     *
     * @param teamProgression The team.
     * @param requester The plugin.
     * @return The number of <i>loading requests</i> that a plugin currently holds for the specified team.
     * @see #addLoadingRequestToTeam(TeamProgression, Plugin)
     * @see #removeLoadingRequestToTeam(TeamProgression, Plugin)
     */
    @Contract(pure = true)
    public int getLoadingRequestsAmount(@NotNull TeamProgression teamProgression, @NotNull Plugin requester) {
        checkClosed();
        Preconditions.checkNotNull(teamProgression, "TeamProgression is null.");
        Preconditions.checkNotNull(requester, "Plugin is null.");

        synchronized (DatabaseManager.this) {
            Preconditions.checkArgument(requester.isEnabled(), "Plugin isn't enabled.");

            LoadedTeam team = teamsLoaded.get(teamProgression.getTeamId());
            if (team == null) {
                return 0;
            }
            return team.getPluginRequests(requester);
        }
    }

    @NotNull
    private synchronized LoadedTeam addTeamToCache(TeamProgression progression) {
        LoadedTeam loadedTeam = teamsLoaded.computeIfAbsent(progression.getTeamId(), id -> new LoadedTeam(progression));
        if (!progression.inCache.getAndSet(true)) {
            callEventCatchingExceptions(new AsyncTeamLoadEvent(progression));
        }
        return loadedTeam;
    }

    @NotNull
    private synchronized LoadedPlayer addPlayerToCache(@NotNull UUID uuid, boolean isOnline) {
        LoadedPlayer loadedPlayer = playersLoaded.computeIfAbsent(uuid, LoadedPlayer::new);
        loadedPlayer.setOnline(isOnline);
        loadedPlayer.addInternalRequest(); // Make sure the player will not be removed from cache
        return loadedPlayer;
    }

    private synchronized void removeInternalRequest(@NotNull LoadedTeam loadedTeam) {
        if (loadedTeam.__removeInternalRequest() == 0 && loadedTeam.canBeUnloaded()) {
            unloadTeam(loadedTeam);
        }
    }

    private synchronized void removeInternalRequest(@NotNull LoadedPlayer loadedPlayer) {
        if (loadedPlayer.__removeInternalRequest() == 0 && loadedPlayer.canBeUnloaded()) {
            unloadPlayer(loadedPlayer);
        }
    }

    private synchronized void removePluginRequest(@NotNull LoadedTeam loadedTeam, @NotNull Plugin plugin) {
        if (loadedTeam.__removePluginRequest(plugin) == 0 && loadedTeam.canBeUnloaded()) {
            unloadTeam(loadedTeam);
        }
    }

    private synchronized void removePluginRequest(@NotNull LoadedPlayer loadedPlayer, @NotNull Plugin plugin) {
        if (loadedPlayer.__removePluginRequest(plugin) == 0 && loadedPlayer.canBeUnloaded()) {
            unloadPlayer(loadedPlayer);
        }
    }

    private synchronized void unloadTeam(@NotNull LoadedTeam loadedTeam) {
        final TeamProgression teamProgression = loadedTeam.getTeamProgression();
        teamProgression.inCache.set(false);
        teamsLoaded.remove(teamProgression.getTeamId());
        callEventCatchingExceptions(new AsyncTeamUnloadEvent(teamProgression));
    }

    private synchronized void unloadPlayer(@NotNull LoadedPlayer loadedPlayer) {
        playersLoaded.remove(loadedPlayer.getUuid());
        @Nullable final LoadedTeam loadedTeam = loadedPlayer.getPlayerTeam();
        loadedPlayer.setPlayerTeam(null);
        if (loadedTeam != null) {
            removeInternalRequest(loadedTeam);
        }
    }

    private synchronized void updateLoadedPlayerTeam(@NotNull LoadedPlayer player, @NotNull LoadedTeam newTeam) {
        // If this method will ever be modified to be called concurrently, see the comment inside TeamProgression#movePlayer
        AdvancementUtils.checkSync(); // Must be called sync since it calls TeamUpdateEvents

        LoadedTeam oldTeam = player.getPlayerTeam();
        Preconditions.checkArgument(oldTeam != null, "Player " + player.getUuid() + " was being moved but isn't part of a team.");

        // If this method will ever be modified to be called concurrently, see the comment inside TeamProgression#movePlayer
        oldTeam.getTeamProgression().movePlayer(newTeam.getTeamProgression(), player.getUuid());

        newTeam.addInternalRequest();
        player.setPlayerTeam(newTeam);

        callEventCatchingExceptions(new TeamUpdateEvent(oldTeam.getTeamProgression(), newTeam.getTeamProgression(), player.getUuid()));
        removeInternalRequest(oldTeam);
    }

    /**
     * Registers a progression update to execute on the main thread.
     * <p>An internal request is added to the team until the completableFuture is completed.
     *
     * @param team The team to update.
     * @param key The advancement which is updated.
     * @param oldProgr The old progression.
     * @param newProgr The new progression.
     * @param completableFuture The {@link CompletableFuture} to complete after the update ends.
     */
    private synchronized void registerProgressionUpdate(@NotNull LoadedTeam team, @NotNull AdvancementKey key, int oldProgr, int newProgr, @NotNull CompletableFuture<ProgressionUpdateResult> completableFuture) {
        pendingUpdatesManager.registerProgressionUpdate(team, key, oldProgr, newProgr, () -> {
            completableFuture.complete(new ProgressionUpdateResult(oldProgr, newProgr));
        });
    }

    /**
     * Registers a team update to execute on the main thread.
     * <p>An internal request is added to both the player and the team until the completableFuture is completed.
     *
     * @param player The player to move.
     * @param playerToMove The {@link Player} instance of the player to move. Can be {@code null}.
     * @param team The team in which the player will be moved.
     * @param processUnredeemed Whether to process unredeemed advancement after the player is moved.
     * @param completableFuture The {@link CompletableFuture} to complete after the update ends.
     * @param completingObj The object that will be used to complete the completableFuture.
     */
    private synchronized <T> void registerTeamUpdate(@NotNull LoadedPlayer player, @Nullable Player playerToMove, @NotNull LoadedTeam team, boolean processUnredeemed, CompletableFuture<T> completableFuture, T completingObj) {
        pendingUpdatesManager.registerTeamUpdate(player, team, () -> {
            completableFuture.complete(completingObj);

            AdvancementUtils.checkSync(); // To catch bugs, done after completing the completable future to avoid not completing it

            if (playerToMove != null && playerToMove.isOnline()) {
                // We are already on main thread here since we are in the callback
                main.updatePlayer(playerToMove);

                if (processUnredeemed) {
                    // processUnredeemed expects to have an internal request "passed" to it
                    player.addInternalRequest();
                    processUnredeemed(player, playerToMove);
                }
            }
        });
    }

    /**
     * Runs the provided runnable into the {@link DatabaseManager#executor} thread if the {@code DatabaseManager} isn't closed.
     *
     * @param runnable The {@link Runnable} to run.
     * @return A {@link CompletableFuture} which will complete once the runnable has been executed (or skipped if
     *         {@link DatabaseManager} gets closed).
     */
    private CompletableFuture<Void> runAsyncOnExecutor(Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            if (!closed.get()) {
                runnable.run();
            }
        }, executor);
    }

    /**
     * Runs the provided runnable into the {@link DatabaseManager#executor} thread if the {@code DatabaseManager} isn't closed.
     *
     * @param returnedCompletableFuture The {@link CompletableFuture} that is completed by the provided runnable when
     *         the operation ends or errors. It will be completed with a {@link DatabaseManagerClosedException} if the
     *         {@link DatabaseManager} gets closed.
     * @param runnable The {@link Runnable} to run.
     * @return A {@link CompletableFuture} which will complete once the runnable has been executed (or skipped if
     *         {@link DatabaseManager} gets closed).
     */
    private CompletableFuture<Void> runAsyncOnExecutor(CompletableFuture<?> returnedCompletableFuture, Runnable runnable) {
        final Integer key = keysOfUncompletedCFs.incrementAndGet();
        uncompletedCompletableFutures.put(key, returnedCompletableFuture);
        returnedCompletableFuture.whenComplete((res, err) -> {
            if (!closed.get()) {
                uncompletedCompletableFutures.remove(key);
            }
        });
        return CompletableFuture.runAsync(() -> {
            if (!closed.get()) {
                runnable.run();
            }
        }, executor);
    }

    private static <E extends Event> void callEventCatchingExceptions(@NotNull E event) {
        try {
            Bukkit.getPluginManager().callEvent(event);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void checkClosed() {
        if (closed.get()) {
            throw new DatabaseManagerClosedException();
        }
    }

    private static final class LoadedTeam extends CacheableEntry {
        private final TeamProgression teamProgression;

        public LoadedTeam(@NotNull TeamProgression progression) {
            this.teamProgression = progression;
        }

        @NotNull
        public TeamProgression getTeamProgression() {
            return teamProgression;
        }
    }

    private static final class LoadedPlayer extends CacheableEntry {

        @Nullable // Null here means the player is not loaded but probably online
        private volatile LoadedTeam playerTeam;

        private final UUID uuid;
        private volatile boolean online = false;

        // Used to make loading requests wait for PlayerRegisteredEvent if the player is being registered
        @Nullable // Null here means a PlayerRegisteredEvent for this player has not been scheduled
        private volatile CompletableFuture<TeamProgression> registeringCompletableFuture = null;

        public LoadedPlayer(@NotNull UUID uuid) {
            this.uuid = uuid;
        }

        @NotNull
        public UUID getUuid() {
            return uuid;
        }

        @Nullable
        public LoadedTeam getPlayerTeam() {
            return playerTeam;
        }

        public void setPlayerTeam(@Nullable LoadedTeam playerTeam) {
            this.playerTeam = playerTeam;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        public CompletableFuture<TeamProgression> getRegisteringCF() {
            return registeringCompletableFuture;
        }

        public void setRegistering() {
            this.registeringCompletableFuture = new CompletableFuture<>();
        }

        public void unsetRegistering() {
            this.registeringCompletableFuture = null;
        }
    }

    private static class CacheableEntry {
        private final Map<Plugin, Integer> pluginRequests = new HashMap<>();

        // Internal requests are used to keep the entry loaded
        private final AtomicInteger internalRequests = new AtomicInteger(0);

        public void addPluginRequest(@NotNull Plugin plugin) {
            synchronized (pluginRequests) {
                pluginRequests.merge(plugin, 1, Integer::sum);
            }
        }

        public int __removePluginRequest(@NotNull Plugin plugin) {
            synchronized (pluginRequests) {
                Integer res = pluginRequests.computeIfPresent(plugin, (key, value) -> {
                    // value shouldn't be null here since we don't put null values in the map
                    return value <= 1 ? null : value - 1;
                });
                return res == null ? 0 : res;
            }
        }

        public void addInternalRequest() {
            internalRequests.incrementAndGet();
        }

        public int __removeInternalRequest() {
            return internalRequests.decrementAndGet();
        }

        public int getInternalRequests() {
            return internalRequests.get();
        }

        public int getPluginRequests(@NotNull Plugin plugin) {
            synchronized (pluginRequests) {
                return pluginRequests.getOrDefault(plugin, 0);
            }
        }

        public int __removeAllPluginRequests(@NotNull Plugin plugin) {
            synchronized (pluginRequests) {
                Integer i = pluginRequests.remove(plugin);
                return i == null ? 0 : i;
            }
        }

        public boolean hasPluginRequests() {
            synchronized (pluginRequests) {
                return !pluginRequests.isEmpty();
            }
        }

        public boolean canBeUnloaded() {
            return getInternalRequests() == 0 && !hasPluginRequests();
        }
    }

    private final class PendingUpdatesManager {
        private final List<Update> progressionUpdates = new ArrayList<>(10);
        private final Map<Integer, Map<AdvancementKey, Integer>> realProgressions = new HashMap<>(); // Used to calculate increments

        // All this fields must be accessed inside a `synchronized (PendingUpdatesManager.this) {...}` block
        private boolean updaterTaskIsRegistered = false;
        private BukkitTask task = null;

        /**
         * Registers a team update to execute on the main thread.
         * <p>An internal request is added to both the player and the team until the callback returns.
         *
         * @param player The player to move.
         * @param team The team in which the player will be moved.
         * @param callback A callback called on the main thread after the update has been applied.
         */
        void registerTeamUpdate(@NotNull LoadedPlayer player, @NotNull LoadedTeam team, @NotNull Runnable callback) {
            synchronized(DatabaseManager.this) {
                // Keep in cache, removes are done after the callback returns
                player.addInternalRequest();
                team.addInternalRequest();

                progressionUpdates.add(new Update(UpdateType.TEAM_UPDATE, player, team, null, 0, 0, callback));
            }
            registerUpdaterTask();
        }

        /**
         * Registers a player registered update to execute on the main thread.
         * <p>An internal request is added to both the player and the team until the callback returns.
         *
         * @param player The player which has been registered.
         * @param team The (brand new) team of the player.
         * @param callback A callback called on the main thread after the update has been applied.
         */
        void registerPlayerRegisteredUpdate(@NotNull LoadedPlayer player, @NotNull LoadedTeam team, @NotNull Runnable callback) {
            synchronized(DatabaseManager.this) {
                // Keep in cache, removes are done after the callback returns
                player.addInternalRequest();
                team.addInternalRequest();

                player.setRegistering();
                progressionUpdates.add(new Update(UpdateType.PLAYER_REGISTERED_UPDATE, player, team, null, 0, 0, callback));
            }
            registerUpdaterTask();
        }

        /**
         * Registers a progression update to execute on the main thread.
         * <p>An internal request is added to the team until the callback returns.
         *
         * @param team The team to update.
         * @param key The advancement that is updated.
         * @param oldProgr The old progression.
         * @param newProgr The new progression.
         * @param callback A callback called on the main thread after the update has been applied.
         */
        void registerProgressionUpdate(@NotNull LoadedTeam team, @NotNull AdvancementKey key, int oldProgr, int newProgr, @NotNull Runnable callback) {
            synchronized(DatabaseManager.this) {
                // Keep in cache, removes are done after the callback returns
                team.addInternalRequest();

                var map = realProgressions.computeIfAbsent(team.getTeamProgression().getTeamId(), HashMap::new);
                map.put(key,newProgr);
                progressionUpdates.add(new Update(UpdateType.PROGRESSION_UPDATE, null, team, key, oldProgr, newProgr, callback));
            }
            registerUpdaterTask();
        }

        private synchronized void registerUpdaterTask() {
            if (!updaterTaskIsRegistered) {
                updaterTaskIsRegistered = true;
                task = Bukkit.getScheduler().runTaskTimer(main.getOwningPlugin(), () -> {
                    if (!updaterLock.tryLockExclusiveLock()) {
                        return; // Don't block the main thread while waiting for the update lock
                    }

                    try {
                        synchronized (PendingUpdatesManager.this) {
                            if (task == null) {
                                // DatabaseManager is closing, just return
                                return;
                            }
                            updaterTaskIsRegistered = false;
                            task.cancel(); // This is a timer task
                            task = null;
                        }

                        pendingUpdatesManager.applyUpdates();
                    } finally {
                        updaterLock.unlockExclusiveLock();
                    }
                }, 1, 1);
            }
        }

        synchronized void unregisterTask() {
            Preconditions.checkArgument(closed.get(), "DatabaseManager isn't closing.");

            if (updaterTaskIsRegistered) {
                if (task != null) {
                    task.cancel();
                    task = null;
                }
            } else {
                updaterTaskIsRegistered = true; // Don't register more tasks
            }
        }

        void clearUpdates() {
            Preconditions.checkArgument(closed.get(), "DatabaseManager isn't closing.");

            synchronized (DatabaseManager.this) {
                progressionUpdates.clear();
                realProgressions.clear();
            }
        }

        int getCurrentValue(@NotNull TeamProgression team, @NotNull AdvancementKey key) {
            synchronized(DatabaseManager.this) {
                var map = realProgressions.get(team.getTeamId());
                if (map == null) {
                    return team.getRawProgression(key);
                }
                Integer progr = map.get(key);
                return progr == null ? team.getRawProgression(key) : progr;
            }
        }

        void applyUpdates() {
            AdvancementUtils.checkSync();

            synchronized (DatabaseManager.this) {
                if (closed.get()) {
                    progressionUpdates.clear();
                    realProgressions.clear();
                    return;
                }

                for (Update update : progressionUpdates) {
                    switch (update.type) {
                        case TEAM_UPDATE -> {
                            updateLoadedPlayerTeam(update.player, update.team);
                        }
                        case PLAYER_REGISTERED_UPDATE -> {
                            if (update.player.getPlayerTeam() != null) {
                                // Player was already in a team, probably a bug
                                new RuntimeException("Player " + update.player.getUuid() + " was already loaded: " + update).printStackTrace();
                            } else {
                                update.team.getTeamProgression().addMember(update.player.getUuid());
                                update.team.addInternalRequest();
                                update.player.setPlayerTeam(update.team);

                                callEventCatchingExceptions(new PlayerRegisteredEvent(update.team.getTeamProgression(), update.player.getUuid()));
                            }

                            // Process possible loading requests waiting for the PlayerRegisteredEvent
                            @Nullable CompletableFuture<TeamProgression> cf = update.player.getRegisteringCF();
                            update.player.unsetRegistering();
                            if (cf != null) {
                                // No need to add internal requests since they have already been added before calling cf.whenComplete
                                cf.complete(update.player.getPlayerTeam().getTeamProgression());
                            }
                        }
                        case PROGRESSION_UPDATE -> {
                            update.team.getTeamProgression().updateProgression(update.key, update.newProgr);
                            callEventCatchingExceptions(new ProgressionUpdateEvent(update.team.getTeamProgression(), update.oldProgr, update.newProgr, update.key));
                        }
                    }

                    try {
                        update.callback.run();
                    } catch (Exception e) {
                        System.err.println("An exception occurred while executing the callback for update " + update + ':');
                        e.printStackTrace();
                    }

                    // Remove request since the callback returned
                    if (update.type == UpdateType.TEAM_UPDATE || update.type == UpdateType.PLAYER_REGISTERED_UPDATE) {
                        // In this case a request was added also to the player
                        removeInternalRequest(update.player);
                    }
                    removeInternalRequest(update.team);
                }
                progressionUpdates.clear();
                realProgressions.clear();
            }
        }

        private enum UpdateType {
            TEAM_UPDATE, // player is moved into team, key is null
            PLAYER_REGISTERED_UPDATE, // player is loaded, key is null
            PROGRESSION_UPDATE // player is null
        }

        private record Update(UpdateType type, LoadedPlayer player, LoadedTeam team, AdvancementKey key, int oldProgr, int newProgr, Runnable callback) {
            @Override
            public String toString() {
                return switch (type) {
                    case TEAM_UPDATE -> "TeamUpdate{" +
                            "player=" + player.getUuid() +
                            ", team=" + team.getTeamProgression().getTeamId() +
                            '}';
                    case PLAYER_REGISTERED_UPDATE -> "PlayerRegisteredUpdate{" +
                            "player=" + player.getUuid() +
                            ", team=" + team.getTeamProgression().getTeamId() +
                            '}';
                    case PROGRESSION_UPDATE -> "ProgressionUpdate{" +
                            "team=" + team.getTeamProgression().getTeamId() +
                            ", key=" + key +
                            ", oldProgr=" + oldProgr +
                            ", newProgr=" + newProgr +
                            '}';
                };
            }
        }
    }

    private static final class DatabaseManagerThreadFactory implements ThreadFactory {
        private final AtomicInteger id = new AtomicInteger(1);
        private final ThreadFactory factory = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            Thread t = factory.newThread(runnable);
            try {
                t.setName("DatabaseManager-thread-" + id.getAndIncrement());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return t;
        }
    }
}
