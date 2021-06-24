package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.impl.MySQL;
import com.fren_gor.ultimateAdvancementAPI.database.impl.SQLite;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingFailedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.CriteriaUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamLoadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUnloadEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUpdateEvent.Action;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
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

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;

public final class DatabaseManager {

    /**
     * Max possible loading requests a plugin can make simultaneously per offline player.
     * <p>Limit is applied to automatic and manual requests separately and doesn't apply to requests which doesn't cache.
     */
    public static final int MAX_SIMULTANEOUS_LOADING_REQUESTS = Character.MAX_VALUE;
    private final AdvancementMain main;
    private final Map<UUID, TeamProgression> progressionCache = new HashMap<>();
    private final Map<UUID, TempUserMetadata> tempLoaded = new HashMap<>();
    private final EventManager eventManager;
    private final IDatabase database;

    public DatabaseManager(AdvancementMain main, File dbFile) throws Exception {
        this.main = main;
        this.eventManager = main.getEventManager();

        database = new SQLite(dbFile, main.getLogger());
        commonSetUp();
    }

    public DatabaseManager(AdvancementMain main, String username, String password, String databaseName, String host, int port, int poolSize, long connectionTimeout) throws Exception {
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
            progressionCache.clear();
        }
    }

    // Must be called async
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

    // Must be called async
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

        pro = searchProgressionDeeplyInCache(uuid);
        if (pro != null) {
            progressionCache.put(uuid, pro); // Direct caching
            updatePlayerName(player);
            return new SimpleEntry<>(pro, false);
        }

        Entry<TeamProgression, Boolean> e = database.loadOrRegisterPlayer(uuid, player.getName());
        updatePlayerName(player);
        progressionCache.put(uuid, e.getKey());
        runSync(main, () -> Bukkit.getPluginManager().callEvent(new TeamLoadEvent(e.getKey())));
        return e;
    }

    @Nullable
    private synchronized TeamProgression searchProgressionDeeplyInCache(@NotNull UUID uuid) {
        for (TeamProgression progression : progressionCache.values()) {
            if (progression.contains(uuid)) {
                return progression;
            }
        }
        return null;
    }

    // Must be called async
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

    @NotNull
    public CompletableFuture<Result> updatePlayerTeam(@NotNull Player playerToMove, @NotNull Player otherTeamMember) throws UserNotLoadedException {
        return updatePlayerTeam(playerToMove, getProgression(otherTeamMember));
    }

    @NotNull
    public CompletableFuture<Result> updatePlayerTeam(@NotNull UUID playerToMove, @NotNull UUID otherTeamMember) throws UserNotLoadedException {
        return updatePlayerTeam(playerToMove, Bukkit.getPlayer(playerToMove), getProgression(otherTeamMember));
    }

    @NotNull
    public CompletableFuture<Result> updatePlayerTeam(@NotNull Player playerToMove, @NotNull TeamProgression otherTeamProgression) throws UserNotLoadedException {
        return updatePlayerTeam(uuidFromPlayer(playerToMove), playerToMove, otherTeamProgression);
    }

    @NotNull
    private CompletableFuture<Result> updatePlayerTeam(@NotNull UUID playerToMove, @Nullable Player ptm, @NotNull TeamProgression otherTeamProgression) throws UserNotLoadedException {
        Validate.notNull(playerToMove, "Player to move is null.");
        Validate.notNull(otherTeamProgression, "TeamProgression is null.");

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

    public CompletableFuture<ObjectResult<@NotNull TeamProgression>> movePlayerInNewTeam(@NotNull Player player) throws UserNotLoadedException {
        return movePlayerInNewTeam(uuidFromPlayer(player), player);
    }

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
                pro = progressionCache.put(uuid, newPro);

                if (pro != null) {
                    pro.removeMember(uuid);
                    teamUnloaded = pro.noMemberMatch(progressionCache::containsKey);
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

    public CompletableFuture<Result> unregisterOfflinePlayer(@NotNull OfflinePlayer player) throws IllegalStateException {
        return unregisterOfflinePlayer(uuidFromPlayer(player));
    }

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

    public int updateCriteria(@NotNull AdvancementKey key, @NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) throws UserNotLoadedException {
        return updateCriteria(key, uuidFromPlayer(player), criteria);
    }

    public int updateCriteria(@NotNull AdvancementKey key, @NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) throws UserNotLoadedException {
        return updateCriteria(key, getProgression(uuid), criteria);
    }

    public int updateCriteria(@NotNull AdvancementKey key, @NotNull TeamProgression progression, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        return updateCriteriaWithCompletable(key, progression, criteria).getKey();
    }

    @NotNull
    public Entry<Integer, CompletableFuture<Result>> updateCriteriaWithCompletable(@NotNull AdvancementKey key, @NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) throws UserNotLoadedException {
        return updateCriteriaWithCompletable(key, uuidFromPlayer(player), criteria);
    }

    @NotNull
    public Entry<Integer, CompletableFuture<Result>> updateCriteriaWithCompletable(@NotNull AdvancementKey key, @NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) throws UserNotLoadedException {
        return updateCriteriaWithCompletable(key, getProgression(uuid), criteria);
    }

    @NotNull
    public Entry<Integer, CompletableFuture<Result>> updateCriteriaWithCompletable(@NotNull AdvancementKey key, @NotNull TeamProgression progression, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        Validate.notNull(key, "Key is null.");
        Validate.notNull(progression, "TeamProgression is null.");
        Validate.isTrue(progression.getSize() > 0, "TeamProgression doesn't contain any player.");
        AdvancementUtils.checkSync();

        int old = progression.updateCriteria(key, criteria);

        if (old != criteria) { // Don't update if criteria isn't being changed
            try {
                Bukkit.getPluginManager().callEvent(new CriteriaUpdateEvent(old, criteria, key));
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            return new SimpleEntry<>(old, CompletableFuture.supplyAsync(() -> {
                try {
                    database.updateAdvancement(key, progression.getTeamId(), criteria);
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

    @NotNull
    public TeamProgression getProgression(@NotNull Player player) throws UserNotLoadedException {
        return getProgression(uuidFromPlayer(player));
    }

    @NotNull
    public synchronized TeamProgression getProgression(@NotNull UUID uuid) throws UserNotLoadedException {
        Validate.notNull(uuid, "UUID is null.");
        TeamProgression pro = progressionCache.get(uuid);
        AdvancementUtils.checkTeamProgressionNotNull(pro, uuid);
        return pro;
    }

    @Contract(pure = true)
    public boolean isLoaded(@NotNull Player player) {
        return isLoaded(uuidFromPlayer(player));
    }

    @Contract(pure = true, value = "null -> false")
    public synchronized boolean isLoaded(UUID uuid) {
        return progressionCache.containsKey(uuid);
    }

    @Contract(pure = true)
    public boolean isLoadedAndOnline(@NotNull Player player) {
        return isLoadedAndOnline(uuidFromPlayer(player));
    }

    @Contract(pure = true, value = "null -> false")
    public synchronized boolean isLoadedAndOnline(UUID uuid) {
        if (isLoaded(uuid)) {
            TempUserMetadata t = tempLoaded.get(uuid);
            return t == null || t.isOnline;
        }
        return false;
    }

    @NotNull
    public CompletableFuture<ObjectResult<@NotNull Boolean>> isUnredeemed(@NotNull AdvancementKey key, @NotNull UUID uuid) throws UserNotLoadedException {
        return isUnredeemed(key, getProgression(uuid));
    }

    @NotNull
    public CompletableFuture<ObjectResult<@NotNull Boolean>> isUnredeemed(@NotNull AdvancementKey key, @NotNull TeamProgression pro) {
        Validate.notNull(key, "AdvancementKey is null.");
        Validate.notNull(pro, "TeamProgression is null.");
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

    @NotNull
    public CompletableFuture<Result> setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, @NotNull UUID uuid) throws UserNotLoadedException {
        return setUnredeemed(key, giveRewards, getProgression(uuid));
    }

    @NotNull
    public CompletableFuture<Result> setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, @NotNull TeamProgression pro) {
        Validate.notNull(key, "AdvancementKey is null.");
        Validate.notNull(pro, "TeamProgression is null.");
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

    @NotNull
    public CompletableFuture<Result> unsetUnredeemed(@NotNull AdvancementKey key, @NotNull UUID uuid) throws UserNotLoadedException {
        return unsetUnredeemed(key, getProgression(uuid));
    }

    @NotNull
    public CompletableFuture<Result> unsetUnredeemed(@NotNull AdvancementKey key, @NotNull TeamProgression pro) {
        Validate.notNull(key, "AdvancementKey is null.");
        Validate.notNull(pro, "TeamProgression is null.");
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

    @NotNull
    public synchronized CompletableFuture<ObjectResult<@NotNull TeamProgression>> loadOfflinePlayer(@NotNull UUID uuid, @NotNull CacheFreeingOption option) {
        Validate.notNull(uuid, "UUID is null.");
        Validate.notNull(option, "CacheFreeingOption is null.");
        TeamProgression pro = progressionCache.get(uuid);
        if (pro != null) {
            handleCacheFreeingOption(uuid, null, option); // Handle requests
            return CompletableFuture.completedFuture(new ObjectResult<>(pro));
        }
        pro = searchProgressionDeeplyInCache(uuid);
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
            runSync(main, () -> Bukkit.getPluginManager().callEvent(new TeamLoadEvent(t)));
            return new ObjectResult<>(t);
        });
    }

    private void handleCacheFreeingOption(@NotNull UUID uuid, @Nullable TeamProgression pro, @NotNull CacheFreeingOption option) {
        switch (option.option) {
            case AUTOMATIC:
                runSync(main, option.ticks, () -> internalUnloadOfflinePlayer(uuid, option.requester, true));
                addCachingRequest(uuid, pro, option, true);
                break;
            case MANUAL:
                addCachingRequest(uuid, pro, option, false);
                break;
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

    @Contract(pure = true, value = "null -> false")
    public synchronized boolean isOfflinePlayerLoaded(UUID uuid) {
        return tempLoaded.containsKey(uuid);
    }

    @Contract(pure = true, value = "null, null -> false; null, !null -> false; !null, null -> false")
    public synchronized boolean isOfflinePlayerLoaded(UUID uuid, Plugin requester) {
        TempUserMetadata t = tempLoaded.get(uuid);
        return t != null && Integer.compareUnsigned(t.getRequests(requester), 0) > 0;
    }

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
                        Bukkit.getPluginManager().callEvent(new TeamUnloadEvent(t));
                    }
                }
            }
        }
    }

    @EqualsAndHashCode
    @ToString
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
    }

}
