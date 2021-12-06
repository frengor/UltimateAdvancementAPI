package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.CacheFreeingOption.Option;
import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import com.fren_gor.ultimateAdvancementAPI.database.impl.MySQL;
import com.fren_gor.ultimateAdvancementAPI.database.impl.SQLite;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingFailedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.ProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamLoadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUpdateEvent.Action;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import org.apache.commons.lang.Validate;
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
import java.util.function.Consumer;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;

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
        Validate.notNull(main, "AdvancementMain is null.");
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
        Validate.notNull(main, "AdvancementMain is null.");
        Validate.notNull(dbFile, "Database file is null.");
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
        Validate.notNull(main, "AdvancementMain is null.");
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
                runSync(main, 2, () -> Bukkit.getPluginManager().callEvent(new PlayerLoadingFailedEvent(e.getPlayer(), ex)));
            }
        }));
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
                        Bukkit.getPluginManager().callEvent(new TeamUnloadEvent(t));
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
        });
    }

    /**
     * Closes the connection to the database and frees the cache.
     * <p>This method does not call {@link Event}s.
     */
    public void unregister() {
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
        runSync(main, 2, () -> {
            Bukkit.getPluginManager().callEvent(new PlayerLoadingCompletedEvent(player, pro));
            if (entry.getValue())
                Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(pro, player.getUniqueId(), Action.JOIN));
            main.updatePlayer(player);
            CompletableFuture.runAsync(() -> processUnredeemed(player, pro));
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
            // Don't let player to be unloaded from cache
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
        runSync(main, () -> Bukkit.getPluginManager().callEvent(new TeamLoadEvent(e.getKey())));
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
                    });
            });
    }

    /**
     * Updates the name of the specified player in the database.
     *
     * @param player The player to update.
     * @return A {@link CompletableFuture} which provides the {@link Result} of the operation.
     * @see UltimateAdvancementAPI#updatePlayerName(Player)
     */
    @NotNull
    public CompletableFuture<Result> updatePlayerName(@NotNull Player player) {
        Validate.notNull(player, "Player cannot be null.");
        return CompletableFuture.supplyAsync(() -> {
            try {
                database.updatePlayerName(player.getUniqueId(), player.getName());
            } catch (SQLException e) {
                System.err.println("Cannot update player " + player.getName() + " name:");
                e.printStackTrace();
                return new Result(e);
            } catch (Exception e) {
                return new Result(e);
            }
            return Result.SUCCESSFUL;
        });
    }

    /**
     * Moves the provided player from their team to the second player's one.
     *
     * @param playerToMove The player to move.
     * @param otherTeamMember A player of the destination team.
     * @return A {@link CompletableFuture} which provides the {@link Result} of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#updatePlayerTeam(Player, Player, Consumer)
     */
    @NotNull
    public CompletableFuture<Result> updatePlayerTeam(@NotNull Player playerToMove, @NotNull Player otherTeamMember) throws UserNotLoadedException {
        return updatePlayerTeam(playerToMove, getTeamProgression(otherTeamMember));
    }

    /**
     * Moves the provided player from their team to the second player's one.
     *
     * @param playerToMove The {@link UUID} of the player to move.
     * @param otherTeamMember The {@link UUID} of a player of the destination team.
     * @return A {@link CompletableFuture} which provides the {@link Result} of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#updatePlayerTeam(UUID, UUID, Consumer)
     */
    @NotNull
    public CompletableFuture<Result> updatePlayerTeam(@NotNull UUID playerToMove, @NotNull UUID otherTeamMember) throws UserNotLoadedException {
        return updatePlayerTeam(playerToMove, Bukkit.getPlayer(playerToMove), getTeamProgression(otherTeamMember));
    }

    /**
     * Moves the provided player from their team to the specified one.
     *
     * @param playerToMove The player to move.
     * @param otherTeamProgression The {@link TeamProgression} of the target team.
     * @return A {@link CompletableFuture} which provides the {@link Result} of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    @NotNull
    public CompletableFuture<Result> updatePlayerTeam(@NotNull Player playerToMove, @NotNull TeamProgression otherTeamProgression) throws UserNotLoadedException {
        return updatePlayerTeam(uuidFromPlayer(playerToMove), playerToMove, otherTeamProgression);
    }

    @NotNull
    private CompletableFuture<Result> updatePlayerTeam(@NotNull UUID playerToMove, @Nullable Player ptm, @NotNull TeamProgression otherTeamProgression) throws UserNotLoadedException {
        Validate.notNull(playerToMove, "Player to move is null.");
        validateTeamProgression(otherTeamProgression);

        synchronized (DatabaseManager.this) {
            if (!progressionCache.containsKey(playerToMove)) {
                throw new UserNotLoadedException(playerToMove);
            }
        }

        if (otherTeamProgression.contains(playerToMove)) {
            // Player is already in that team
            return CompletableFuture.completedFuture(Result.SUCCESSFUL);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                database.movePlayer(playerToMove, otherTeamProgression.getTeamId());
            } catch (SQLException e) {
                System.err.println("Cannot move player " + (ptm == null ? playerToMove : ptm) + " into team " + otherTeamProgression.getTeamId());
                e.printStackTrace();
                return new Result(e);
            } catch (Exception e) {
                return new Result(e);
            }

            final TeamProgression pro;
            boolean teamUnloaded;
            synchronized (DatabaseManager.this) {
                otherTeamProgression.addMember(playerToMove);

                pro = progressionCache.put(playerToMove, otherTeamProgression);

                if (pro != null) {
                    pro.removeMember(playerToMove);
                    teamUnloaded = pro.noMemberMatch(progressionCache::containsKey);
                    if (teamUnloaded) {
                        pro.inCache.set(false); // Invalidate TeamProgression
                    }
                } else {
                    teamUnloaded = false;
                }
            }

            runSync(main, () -> {
                if (pro != null)
                    Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(pro, playerToMove, Action.LEAVE));
                if (teamUnloaded)
                    Bukkit.getPluginManager().callEvent(new TeamUnloadEvent(pro));
                Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(otherTeamProgression, playerToMove, Action.JOIN));
                if (ptm != null)
                    main.updatePlayer(ptm);
            });

            if (ptm != null) {
                processUnredeemed(ptm, otherTeamProgression);
            }
            return Result.SUCCESSFUL;
        });
    }

    /**
     * Moves the provided player into a new team.
     *
     * @param player The player.
     * @return A {@link CompletableFuture}&lt;{@link ObjectResult}&gt; which provides the new player team's {@link TeamProgression}.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#movePlayerInNewTeam(Player)
     */
    public CompletableFuture<ObjectResult<@NotNull TeamProgression>> movePlayerInNewTeam(@NotNull Player player) throws UserNotLoadedException {
        return movePlayerInNewTeam(uuidFromPlayer(player), player);
    }

    /**
     * Moves the provided player into a new team.
     *
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture}&lt;{@link ObjectResult}&gt; which provides the new player team's {@link TeamProgression}.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#movePlayerInNewTeam(UUID)
     */
    public CompletableFuture<ObjectResult<@NotNull TeamProgression>> movePlayerInNewTeam(@NotNull UUID uuid) throws UserNotLoadedException {
        return movePlayerInNewTeam(uuid, Bukkit.getPlayer(uuid));
    }

    private CompletableFuture<ObjectResult<@NotNull TeamProgression>> movePlayerInNewTeam(@NotNull UUID uuid, @Nullable Player ptr) throws UserNotLoadedException {
        Validate.notNull(uuid, "UUID is null.");
        synchronized (DatabaseManager.this) {
            if (!progressionCache.containsKey(uuid)) {
                throw new UserNotLoadedException(uuid);
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            final TeamProgression newPro;
            try {
                newPro = database.movePlayerInNewTeam(uuid);
            } catch (SQLException e) {
                System.err.println("Cannot remove player " + (ptr == null ? uuid : ptr.getName()) + " from their team:");
                e.printStackTrace();
                return new ObjectResult<>(e);
            } catch (Exception e) {
                return new ObjectResult<>(e);
            }
            final TeamProgression pro;
            boolean teamUnloaded;
            synchronized (DatabaseManager.this) {
                newPro.inCache.set(true); // Set TeamProgression valid
                pro = progressionCache.put(uuid, newPro);

                if (pro != null) {
                    pro.removeMember(uuid);
                    teamUnloaded = pro.noMemberMatch(progressionCache::containsKey);
                    if (teamUnloaded) {
                        pro.inCache.set(false); // Invalidate TeamProgression
                    }
                } else {
                    teamUnloaded = false;
                }
            }

            runSync(main, () -> {
                if (pro != null)
                    Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(pro, uuid, Action.LEAVE));
                if (teamUnloaded)
                    Bukkit.getPluginManager().callEvent(new TeamUnloadEvent(pro));
                Bukkit.getPluginManager().callEvent(new TeamLoadEvent(newPro));
                Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(newPro, uuid, Action.JOIN));
                if (ptr != null)
                    main.updatePlayer(ptr);
            });
            return new ObjectResult<>(newPro);
        });
    }

    /**
     * Unregisters the provided player. The player must be offline and not loaded into the cache.
     *
     * @param player The player to unregister.
     * @return A {@link CompletableFuture} which provides the {@link Result} of the operation.
     * @throws IllegalStateException If the player is online or loaded into the cache.
     * @see UltimateAdvancementAPI#unregisterOfflinePlayer(OfflinePlayer)
     */
    public CompletableFuture<Result> unregisterOfflinePlayer(@NotNull OfflinePlayer player) throws IllegalStateException {
        return unregisterOfflinePlayer(uuidFromPlayer(player));
    }

    /**
     * Unregisters the provided player. The player must be offline and not loaded into the cache.
     *
     * @param uuid The {@link UUID} of the player to unregister.
     * @return A {@link CompletableFuture} which provides the {@link Result} of the operation.
     * @throws IllegalStateException If the player is online or loaded into the cache.
     * @see UltimateAdvancementAPI#unregisterOfflinePlayer(UUID)
     */
    public CompletableFuture<Result> unregisterOfflinePlayer(@NotNull UUID uuid) throws IllegalStateException {
        Validate.notNull(uuid, "UUID is null.");
        AdvancementUtils.checkSync();
        if (Bukkit.getPlayer(uuid) != null)
            throw new IllegalStateException("Player is online.");
        synchronized (DatabaseManager.this) {
            if (tempLoaded.containsKey(uuid))
                throw new IllegalStateException("Player is temporary loaded.");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                database.unregisterPlayer(uuid);
            } catch (SQLException e) {
                System.err.println("Cannot unregister player " + uuid + ':');
                e.printStackTrace();
                return new Result(e);
            } catch (Exception e) {
                return new Result(e);
            }

            return Result.SUCCESSFUL;
        });
    }

    /**
     * Updates the progression of the specified advancement.
     *
     * @param key The advancement key.
     * @param player The player who made the advancement.
     * @param newProgression The new progression.
     * @return The old progression.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    public int updateProgression(@NotNull AdvancementKey key, @NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) throws UserNotLoadedException {
        return updateProgression(key, uuidFromPlayer(player), newProgression);
    }

    /**
     * Updates the progression of the specified advancement.
     *
     * @param key The advancement key.
     * @param uuid The {@link UUID} of the player who made the advancement.
     * @param newProgression The new progression.
     * @return The old progression.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    public int updateProgression(@NotNull AdvancementKey key, @NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) throws UserNotLoadedException {
        return updateProgression(key, getTeamProgression(uuid), newProgression);
    }

    /**
     * Updates the progression of the specified advancement.
     *
     * @param key The advancement key.
     * @param progression The {@link TeamProgression} of the team which made the advancement.
     * @param newProgression The new progression.
     * @return The old progression.
     */
    public int updateProgression(@NotNull AdvancementKey key, @NotNull TeamProgression progression, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) {
        return updateProgressionWithCompletable(key, progression, newProgression).getKey();
    }

    /**
     * Updates the progression of the specified advancement.
     *
     * @param key The advancement key.
     * @param player The player who made the advancement.
     * @param newProgression The new progression.
     * @return A pair containing the old progression and a {@link CompletableFuture} which provides the {@link Result} of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    @NotNull
    public Entry<Integer, CompletableFuture<Result>> updateProgressionWithCompletable(@NotNull AdvancementKey key, @NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) throws UserNotLoadedException {
        return updateProgressionWithCompletable(key, uuidFromPlayer(player), newProgression);
    }

    /**
     * Updates the progression of the specified advancement.
     *
     * @param key The advancement key.
     * @param uuid The {@link UUID} of the player who made the advancement.
     * @param newProgression The new progression.
     * @return A pair containing the old progression and a {@link CompletableFuture} which provides the {@link Result} of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     */
    @NotNull
    public Entry<Integer, CompletableFuture<Result>> updateProgressionWithCompletable(@NotNull AdvancementKey key, @NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) throws UserNotLoadedException {
        return updateProgressionWithCompletable(key, getTeamProgression(uuid), newProgression);
    }

    /**
     * Updates the progression of the specified advancement.
     *
     * @param key The advancement key.
     * @param progression The {@link TeamProgression} of the team which made the advancement.
     * @param newProgression The new progression.
     * @return A pair containing the old progression and a {@link CompletableFuture} which provides the {@link Result} of the operation.
     */
    @NotNull
    public Entry<Integer, CompletableFuture<Result>> updateProgressionWithCompletable(@NotNull AdvancementKey key, @NotNull TeamProgression progression, @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) {
        Validate.notNull(key, "Key is null.");
        validateTeamProgression(progression);
        Validate.isTrue(progression.getSize() > 0, "TeamProgression doesn't contain any player.");
        AdvancementUtils.checkSync();

        int old = progression.updateProgression(key, newProgression);

        if (old != newProgression) { // Don't update if the progression isn't being changed
            try {
                Bukkit.getPluginManager().callEvent(new ProgressionUpdateEvent(progression, old, newProgression, key));
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            return new SimpleEntry<>(old, CompletableFuture.supplyAsync(() -> {
                try {
                    database.updateAdvancement(key, progression.getTeamId(), newProgression);
                } catch (SQLException e) {
                    System.err.println("Cannot update advancement " + key + " to team " + progression.getTeamId() + ':');
                    e.printStackTrace();
                    return new Result(e);
                } catch (Exception e) {
                    return new Result(e);
                }
                return Result.SUCCESSFUL;
            }));
        }
        return new SimpleEntry<>(old, CompletableFuture.completedFuture(Result.SUCCESSFUL));
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
        Validate.notNull(uuid, "UUID is null.");
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
        Validate.notNull(plugin, "Plugin is null.");
        Validate.notNull(uuid, "UUID is null.");
        Validate.notNull(type, "CacheFreeingOption.Option is null.");
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
     * @return A {@link CompletableFuture}&lt;{@link ObjectResult}&gt; which provides a boolean value that is {@code true} if the
     *         provided advancement is unredeemed for the specified player, false otherwise.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#isUnredeemed(Advancement, UUID, Consumer)
     */
    @NotNull
    public CompletableFuture<ObjectResult<@NotNull Boolean>> isUnredeemed(@NotNull AdvancementKey key, @NotNull UUID uuid) throws UserNotLoadedException {
        return isUnredeemed(key, getTeamProgression(uuid));
    }

    /**
     * Returns whether the provided advancement is unredeemed for the specified team.
     *
     * @param key The advancement key.
     * @param pro The {@link TeamProgression} of the team.
     * @return A {@link CompletableFuture}&lt;{@link ObjectResult}&gt; which provides a boolean value that is {@code true} if the
     *         provided advancement is unredeemed for the specified player, false otherwise.
     */
    @NotNull
    public CompletableFuture<ObjectResult<@NotNull Boolean>> isUnredeemed(@NotNull AdvancementKey key, @NotNull TeamProgression pro) {
        Validate.notNull(key, "AdvancementKey is null.");
        validateTeamProgression(pro);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new ObjectResult<>(database.isUnredeemed(key, pro.getTeamId()));
            } catch (SQLException e) {
                System.err.println("Cannot fetch unredeemed advancements of team " + pro.getTeamId() + ':');
                e.printStackTrace();
                return new ObjectResult<>(e);
            } catch (Exception e) {
                e.printStackTrace();
                return new ObjectResult<>(e);
            }
        });
    }

    /**
     * Sets an advancement unredeemed for the specified player.
     *
     * @param key The advancement key.
     * @param giveRewards Whether advancement rewards will be given on redeem.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} which provides the {@link Result} of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#setUnredeemed(Advancement, UUID, boolean, Consumer)
     */
    @NotNull
    public CompletableFuture<Result> setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, @NotNull UUID uuid) throws UserNotLoadedException {
        return setUnredeemed(key, giveRewards, getTeamProgression(uuid));
    }

    /**
     * Sets an advancement unredeemed for the specified team.
     *
     * @param key The advancement key.
     * @param giveRewards Whether advancement rewards will be given on redeem.
     * @param pro The {@link TeamProgression} of the team.
     * @return A {@link CompletableFuture} which provides the {@link Result} of the operation.
     */
    @NotNull
    public CompletableFuture<Result> setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, @NotNull TeamProgression pro) {
        Validate.notNull(key, "AdvancementKey is null.");
        validateTeamProgression(pro);
        return CompletableFuture.supplyAsync(() -> {
            try {
                database.setUnredeemed(key, giveRewards, pro.getTeamId());
            } catch (SQLException e) {
                System.err.println("Cannot set unredeemed advancement " + key + " to team " + pro.getTeamId() + ':');
                e.printStackTrace();
                return new Result(e);
            } catch (Exception e) {
                return new Result(e);
            }
            return Result.SUCCESSFUL;
        });
    }

    /**
     * Redeem the specified advancement for the provided player.
     *
     * @param key The advancement key.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} which provides the {@link Result} of the operation.
     * @throws UserNotLoadedException If the player was not loaded into the cache.
     * @see UltimateAdvancementAPI#unsetUnredeemed(Advancement, UUID, Consumer)
     */
    @NotNull
    public CompletableFuture<Result> unsetUnredeemed(@NotNull AdvancementKey key, @NotNull UUID uuid) throws UserNotLoadedException {
        return unsetUnredeemed(key, getTeamProgression(uuid));
    }

    /**
     * Redeem the specified advancement for the provided team.
     *
     * @param key The advancement key.
     * @param pro The {@link TeamProgression} of the team.
     * @return A {@link CompletableFuture} which provides the {@link Result} of the operation.
     */
    @NotNull
    public CompletableFuture<Result> unsetUnredeemed(@NotNull AdvancementKey key, @NotNull TeamProgression pro) {
        Validate.notNull(key, "AdvancementKey is null.");
        validateTeamProgression(pro);
        return CompletableFuture.supplyAsync(() -> {
            try {
                database.unsetUnredeemed(key, pro.getTeamId());
            } catch (SQLException e) {
                System.err.println("Cannot set unredeemed advancement " + key + " to team " + pro.getTeamId() + ':');
                e.printStackTrace();
                return new Result(e);
            } catch (Exception e) {
                return new Result(e);
            }
            return Result.SUCCESSFUL;
        });
    }

    /**
     * Gets the in-database stored name of the provided player.
     *
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture}&lt;{@link ObjectResult}&gt; which provides the stored name of the player.
     * @see UltimateAdvancementAPI#getStoredPlayerName(UUID, Consumer)
     */
    @NotNull
    public CompletableFuture<ObjectResult<@NotNull String>> getStoredPlayerName(@NotNull UUID uuid) {
        Validate.notNull(uuid, "UUID is null.");
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new ObjectResult<>(database.getPlayerName(uuid));
            } catch (SQLException e) {
                System.err.println("Cannot fetch player name of " + uuid + ':');
                e.printStackTrace();
                return new ObjectResult<>(e);
            } catch (Exception e) {
                return new ObjectResult<>(e);
            }
        });
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
     * @return A {@link CompletableFuture}&lt;{@link ObjectResult}&gt; which provides the player team's {@link TeamProgression}.
     * @see UltimateAdvancementAPI#loadOfflinePlayer(UUID, CacheFreeingOption, Consumer)
     */
    @NotNull
    public synchronized CompletableFuture<ObjectResult<@NotNull TeamProgression>> loadOfflinePlayer(@NotNull UUID uuid, @NotNull CacheFreeingOption option) {
        Validate.notNull(uuid, "UUID is null.");
        Validate.notNull(option, "CacheFreeingOption is null.");
        TeamProgression pro = progressionCache.get(uuid);
        if (pro != null) {
            handleCacheFreeingOption(uuid, null, option); // Handle requests
            return CompletableFuture.completedFuture(new ObjectResult<>(pro));
        }
        pro = searchTeamProgressionDeeply(uuid);
        if (pro != null) {
            handleCacheFreeingOption(uuid, pro, option); // Direct caching and handle requests
            return CompletableFuture.completedFuture(new ObjectResult<>(pro));
        }
        return CompletableFuture.supplyAsync(() -> {
            TeamProgression t;
            try {
                t = database.loadUUID(uuid);
            } catch (SQLException e) {
                System.err.println("Cannot load offline player " + uuid + ':');
                e.printStackTrace();
                return new ObjectResult<>(e);
            } catch (Exception e) {
                return new ObjectResult<>(e);
            }
            handleCacheFreeingOption(uuid, t, option); // Direct caching and handle requests
            if (option.option != Option.DONT_CACHE) {
                t.inCache.set(true); // Set TeamProgression valid
                runSync(main, () -> Bukkit.getPluginManager().callEvent(new TeamLoadEvent(t)));
            }
            return new ObjectResult<>(t);
        });
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
        Validate.notNull(uuid, "UUID is null.");
        Validate.notNull(requester, "Plugin is null.");
        TempUserMetadata meta = tempLoaded.get(uuid);
        if (meta != null) {
            meta.removeRequest(requester, auto);
            if (meta.canBeRemoved()) {
                tempLoaded.remove(uuid);
                if (!meta.isOnline) {
                    TeamProgression t = progressionCache.remove(uuid);
                    if (t != null && t.noMemberMatch(progressionCache::containsKey)) {
                        t.inCache.set(false); // Invalidate TeamProgression
                        Bukkit.getPluginManager().callEvent(new TeamUnloadEvent(t));
                    }
                }
            }
        }
    }

    private static void validateTeamProgression(@NotNull TeamProgression pro) {
        Validate.notNull(pro, "TeamProgression is null.");
        Validate.isTrue(pro.isValid(), "TeamProgression is not valid (this means it is not present in cache).");
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
