package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementDisposeEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementDisposedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementRegistrationEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DisposedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DuplicatedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.ISendable;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutSelectAdvancementTabWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey.checkNamespace;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * The {@code AdvancementTab} class represents a tab in the advancement GUI.
 * <p>Every advancement tab is represented by a unique namespace (not visible in the advancement GUI) and has only one {@link RootAdvancement}.
 * <p>An instance can be obtained using {@link UltimateAdvancementAPI#createAdvancementTab(String)}.
 * The returned instance is not initialised, that is it doesn't contain any advancement yet.
 */
public final class AdvancementTab {

    private final Plugin owningPlugin;
    private final EventManager eventManager;
    private final String namespace;
    private final DatabaseManager databaseManager;
    private final Map<AdvancementKey, Advancement> advancements = new HashMap<>();
    private final Map<Player, Set<MinecraftKeyWrapper>> players = new HashMap<>();
    private final AdvsUpdateRunnable updateManager;

    private RootAdvancement rootAdvancement;
    private boolean initialised = false, disposed = false, automaticallyShown = false, automaticallyGrant = false;
    @LazyValue
    private Collection<String> advNamespacedKeys;
    @LazyValue
    private Collection<BaseAdvancement> advsWithoutRoot;

    AdvancementTab(@NotNull Plugin owningPlugin, @NotNull DatabaseManager databaseManager, @NotNull String namespace) {
        checkNamespace(namespace);
        this.namespace = Objects.requireNonNull(namespace);
        this.owningPlugin = Objects.requireNonNull(owningPlugin);
        this.eventManager = new EventManager(owningPlugin);
        this.databaseManager = Objects.requireNonNull(databaseManager);
        this.updateManager = new AdvsUpdateRunnable();
        eventManager.register(this, PlayerQuitEvent.class, e -> players.remove(e.getPlayer()));
    }

    /**
     * Returns whether the tab is initialised and not disposed.
     *
     * @return Whether the tab is initialised and not disposed.
     */
    public boolean isActive() {
        return initialised && !disposed;
    }

    /**
     * Returns the root advancement of this tab.
     *
     * @return The root advancement of this tab.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @NotNull
    @Contract(pure = true)
    public RootAdvancement getRootAdvancement() {
        checkInitialisation();
        return rootAdvancement;
    }

    /**
     * Gets an unmodifiable {@link Collection} of all the tab's advancements.
     *
     * @return An unmodifiable {@link Collection} of all the tab's advancements.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @UnmodifiableView
    @NotNull
    @Contract(pure = true)
    public Collection<@NotNull Advancement> getAdvancements() {
        checkInitialisation();
        return Collections.unmodifiableCollection(advancements.values());
    }

    /**
     * Gets an unmodifiable {@link Collection} of all the tab's advancements but the root.
     *
     * @return An unmodifiable {@link Collection} of all the tab's advancements but the root.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @Unmodifiable
    @NotNull
    public Collection<@NotNull BaseAdvancement> getAdvancementsWithoutRoot() {
        checkInitialisation();
        if (advsWithoutRoot != null) {
            return advsWithoutRoot;
        } else {
            List<BaseAdvancement> list = new ArrayList<>(advancements.size());
            for (Advancement a : advancements.values()) {
                if (a instanceof BaseAdvancement base) {
                    list.add(base);
                }
            }
            return advsWithoutRoot = Collections.unmodifiableList(list);
        }
    }

    /**
     * Gets an unmodifiable {@link Collection} of all the tab's advancements
     * that are an instance of the provided class or any of its subclasses.
     * <p>If the provided class is {@code null}, then an empty {@link Collection} is returned.
     *
     * @param filterClass The filter class.
     * @return An unmodifiable {@link Collection} of all the tab's advancements
     *         that are an instance of the provided class or any of its subclasses.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @Unmodifiable
    @NotNull
    @Contract(pure = true)
    public Collection<@NotNull Advancement> getAdvancementsByClass(Class<? extends Advancement> filterClass) {
        checkInitialisation();
        if (filterClass == null) {
            return Collections.emptyList();
        }
        if (filterClass == Advancement.class) {
            return Collections.unmodifiableCollection(advancements.values());
        }
        if (rootAdvancement.getClass().isInstance(filterClass)) {
            return Collections.singletonList(rootAdvancement);
        }
        if (filterClass.isInstance(BaseAdvancement.class)) {
            Collection<Advancement> coll = new ArrayList<>(advancements.size());
            for (Advancement a : advancements.values()) {
                if (a.getClass().isInstance(filterClass)) {
                    coll.add(a);
                }
            }
            return Collections.unmodifiableCollection(coll);
        }
        return Collections.emptyList();
    }

    /**
     * Gets an unmodifiable {@link Set} of the {@link AdvancementKey}s of all the tab's advancements.
     *
     * @return An unmodifiable {@link Set} of the {@link AdvancementKey}s of all the tab's advancements.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @UnmodifiableView
    @NotNull
    @Contract(pure = true)
    public Set<@NotNull AdvancementKey> getAdvancementsNamespacedKeys() {
        checkInitialisation();
        return Collections.unmodifiableSet(advancements.keySet());
    }

    /**
     * Returns whether the provided advancement belongs to the tab.
     *
     * @param advancement The advancement.
     * @return Whether the provided advancement belongs to the tab.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean hasAdvancement(Advancement advancement) {
        checkInitialisation();
        if (advancement == null)
            return false;
        return advancements.containsKey(advancement.getKey());
    }

    /**
     * Returns whether an advancement with the provided {@link AdvancementKey} belongs to the tab.
     *
     * @param namespacedKey The {@link AdvancementKey} of the advancement.
     * @return Whether an advancement with the provided {@link AdvancementKey} belongs to the tab.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean hasAdvancement(AdvancementKey namespacedKey) {
        checkInitialisation();
        return advancements.containsKey(namespacedKey);
    }

    /**
     * Gets the advancement of the tab with the provided {@link AdvancementKey}.
     *
     * @param namespacedKey The {@link AdvancementKey} of the wanted advancement of this tab.
     * @return The advancement of the tab with the provided {@link AdvancementKey}, or {@code null} if there's not.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @Nullable
    @Contract(pure = true, value = "null -> null")
    public Advancement getAdvancement(AdvancementKey namespacedKey) {
        checkInitialisation();
        return advancements.get(namespacedKey);
    }

    /**
     * Returns an unmodifiable {@link Set} of the players the tab is currently shown to.
     *
     * @return An unmodifiable {@link Set} of the players the tab is currently shown to.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @UnmodifiableView
    @NotNull
    @Contract(pure = true)
    public Set<@NotNull Player> getPlayers() {
        checkInitialisation();
        return Collections.unmodifiableSet(players.keySet());
    }

    /**
     * Grants the root advancement of the tab to the specified player giving rewards.
     *
     * @param player The player who is completing the root advancement.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    public void grantRootAdvancement(@NotNull Player player) {
        checkInitialisation();
        rootAdvancement.setProgression(player, rootAdvancement.getMaxProgression());
    }

    /**
     * Grants the root advancement of the tab to the specified player giving rewards.
     *
     * @param uuid The {@link UUID} of the player who is completing the root advancement.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    public void grantRootAdvancement(@NotNull UUID uuid) {
        checkInitialisation();
        rootAdvancement.setProgression(uuid, rootAdvancement.getMaxProgression());
    }

    /**
     * Grants the root advancement of the tab to the specified player.
     *
     * @param player The player who is completing the root advancement.
     * @param giveRewards Whether to give rewards.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    public void grantRootAdvancement(@NotNull Player player, boolean giveRewards) {
        checkInitialisation();
        rootAdvancement.setProgression(player, rootAdvancement.getMaxProgression(), giveRewards);
    }

    /**
     * Grants the root advancement of the tab to the specified player.
     *
     * @param uuid The {@link UUID} of the player who is completing the root advancement.
     * @param giveRewards Whether to give rewards.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    public void grantRootAdvancement(@NotNull UUID uuid, boolean giveRewards) {
        checkInitialisation();
        rootAdvancement.setProgression(uuid, rootAdvancement.getMaxProgression(), giveRewards);
    }

    /**
     * Sends or updates the advancements of the tab to the provided player's team members.
     *
     * @param player The player.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     * @throws UserNotLoadedException If the provided player's team is not loaded.
     */
    public void updateAdvancementsToTeam(@NotNull Player player) throws UserNotLoadedException {
        updateAdvancementsToTeam(AdvancementUtils.uuidFromPlayer(player));
    }

    /**
     * Sends or updates the advancements of the tab to the provided player's team members.
     *
     * @param uuid A {@link UUID} of the player.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     * @throws UserNotLoadedException If the provided player's team is not loaded.
     */
    public void updateAdvancementsToTeam(@NotNull UUID uuid) throws UserNotLoadedException {
        updateAdvancementsToTeam(databaseManager.getTeamProgression(uuid));
    }

    /**
     * Sends or updates the advancements of the tab to the provided team's members.
     *
     * @param pro The {@link TeamProgression} of the team.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    public void updateAdvancementsToTeam(@NotNull TeamProgression pro) {
        checkInitialisation();
        validateTeamProgression(pro);
        updateManager.schedule(pro);
    }

    /**
     * Sends or updates the advancements of the tab to the provided player.
     *
     * @param player The player.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     * @throws UserNotLoadedException If the provided player's team is not loaded.
     * @deprecated Use {@link #updateAdvancementsToTeam(Player)} instead.
     */
    @Deprecated(since = "2.2.0")
    public void updateEveryAdvancement(@NotNull Player player) throws UserNotLoadedException {
        updateAdvancementsToTeam(player);
    }

    /**
     * Register the advancements for this tab, initializing the tab. Thus, it cannot be called twice.
     *
     * @param rootAdvancement The root of this tab.
     * @param advancements The advancements of this tab. Cannot include any {@code null} advancement or {@link RootAdvancement}.
     * @throws IllegalStateException If the tab is already initialised.
     * @throws DisposedException If the tab is disposed.
     */
    public void registerAdvancements(@NotNull RootAdvancement rootAdvancement, @NotNull BaseAdvancement... advancements) {
        registerAdvancements(rootAdvancement, Sets.newHashSet(advancements));
    }

    /**
     * Register the advancements for this tab, initializing the tab. Thus, it cannot be called twice.
     *
     * @param rootAdvancement The root of this tab.
     * @param advancements The advancements of this tab. Cannot include any {@code null} advancement or {@link RootAdvancement}.
     * @throws IllegalStateException If the tab is already initialised.
     * @throws DisposedException If the tab is disposed.
     */
    public void registerAdvancements(@NotNull RootAdvancement rootAdvancement, @NotNull Set<BaseAdvancement> advancements) {
        if (disposed) {
            throw new DisposedException("AdvancementTab is disposed.");
        }
        if (initialised)
            throw new IllegalStateException("Tab is already initialised.");
        Preconditions.checkNotNull(rootAdvancement, "RootAdvancement is null.");
        Preconditions.checkArgument(isOwnedByThisTab(rootAdvancement), "RootAdvancement " + rootAdvancement + " is not owned by this tab.");

        for (BaseAdvancement a : advancements) {
            if (a == null) {
                throw new IllegalArgumentException("An advancement is null.");
            }
            if (!isOwnedByThisTab(a)) {
                throw new IllegalArgumentException("Advancement " + a.getKey().toString() + " is not owned by this tab.");
            }
        }

        // Just to be sure
        this.advancements.clear();

        this.rootAdvancement = rootAdvancement;
        this.advancements.put(rootAdvancement.getKey(), rootAdvancement);

        final PluginManager pluginManager = Bukkit.getPluginManager();

        callOnRegister(rootAdvancement);

        try {
            pluginManager.callEvent(new AdvancementRegistrationEvent(rootAdvancement));
        } catch (IllegalStateException e) {
            onRegisterFail();
            throw e;
        }

        for (BaseAdvancement adv : advancements) {
            if (this.advancements.put(adv.getKey(), adv) != null) {
                onRegisterFail();
                throw new DuplicatedException("Advancement " + adv.getKey() + " is duplicated.");
            }

            callOnRegister(adv);

            try {
                pluginManager.callEvent(new AdvancementRegistrationEvent(adv));
            } catch (IllegalStateException e) {
                onRegisterFail();
                throw e;
            }
        }

        // Initialise before validation since advancementTab's methods have to be called
        // Make sure to revert it in case of an invalid advancement is found. See onRegisterFail()
        initialised = true;

        for (Advancement adv : this.advancements.values()) {
            callValidation(adv);
        }
    }

    private void callOnRegister(Advancement adv) {
        try {
            adv.onRegister();
        } catch (Exception e) {
            onRegisterFail();
            throw new RuntimeException("Exception occurred while registering advancement " + adv.getKey() + ':', e);
        }
    }

    private void callValidation(Advancement adv) {
        try {
            adv.validateRegister();
        } catch (InvalidAdvancementException e) {
            onRegisterFail();
            throw new RuntimeException("Advancement " + adv.getKey() + " is not valid:", e);
        } catch (Exception e) {
            onRegisterFail();
            throw new RuntimeException("Exception occurred while validating advancement " + adv.getKey() + ':', e);
        }
    }

    private void onRegisterFail() {
        // Revert initialised to false in case of an invalid advancement is found
        initialised = false;
        advancements.clear();
        rootAdvancement = null;
    }

    /**
     * Shows the tab to the provided players and sends them the advancements of the tab.
     *
     * @param players The players the tab will be shown to.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    public void showTab(@NotNull Player... players) {
        checkInitialisation();
        Preconditions.checkNotNull(players, "Player[] is null.");
        for (Player p : players) {
            try {
                showTab(p);
            } catch (Exception e) {
                // Add other players anyway
                e.printStackTrace();
            }
        }
    }

    /**
     * Shows the tab to the provided player and sends them the advancements of the tab.
     *
     * @param player The player the tab will be shown to.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    public void showTab(@NotNull Player player) {
        checkInitialisation();
        Preconditions.checkNotNull(player, "Player is null.");
        if (!players.containsKey(player)) {
            players.put(player, Collections.emptySet());
            updateAdvancementsToTeam(player);
        }
    }

    /**
     * Hides the tab and its advancements to the provided players.
     *
     * @param players The players the tab will be hid to.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     * @since 1.0.2
     */
    public void hideTab(@NotNull Player... players) {
        checkInitialisation();
        Preconditions.checkNotNull(players, "Player[] is null.");
        for (Player p : players) {
            try {
                hideTab(p);
            } catch (Exception e) {
                // Remove other players anyway
                e.printStackTrace();
            }
        }
    }

    /**
     * Hides the tab and its advancements to the provided player.
     *
     * @param player The player the tab will be hid to.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    public void hideTab(@NotNull Player player) {
        checkInitialisation();
        Preconditions.checkNotNull(player, "Player is null.");

        if (isShownTo(player))
            removePlayer(player, players.remove(player));
    }

    private void removePlayer(@NotNull Player player, Set<MinecraftKeyWrapper> keys) {
        if (keys == null || keys.isEmpty())
            return;
        try {
            PacketPlayOutAdvancementsWrapper.craftRemovePacket(keys).sendTo(player);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    void dispose() {
        checkInitialisation();
        disposed = true;
        eventManager.disable();
        updateManager.dispose();
        var it = players.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Player, Set<MinecraftKeyWrapper>> e = it.next();
            removePlayer(e.getKey(), e.getValue());
            it.remove();
        }
        PluginManager pluginManager = Bukkit.getPluginManager();
        for (Advancement a : advancements.values()) {
            try {
                // Trigger AdvancementDisposeEvent
                try {
                    pluginManager.callEvent(new AdvancementDisposeEvent(a));
                } catch (IllegalStateException e) {
                    System.err.println("An exception has occurred while calling AdvancementDisposeEvent for " + a);
                    e.printStackTrace();
                }
                // Dispose the advancement
                a.onDispose();
                // Trigger AdvancementDisposedEvent
                try {
                    pluginManager.callEvent(new AdvancementDisposedEvent(a.getKey()));
                } catch (IllegalStateException e) {
                    System.err.println("An exception has occurred while calling AdvancementDisposedEvent for " + a);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        advancements.clear();
        rootAdvancement = null;
        advNamespacedKeys = null;
        advsWithoutRoot = null;
    }

    /**
     * Returns whether the tab is shown to the specified player.
     *
     * @param player The player.
     * @return Whether the tab is shown to the specified player.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean isShownTo(Player player) {
        checkInitialisation();
        return players.containsKey(player);
    }

    /**
     * Returns an unmodifiable {@link Collection} of all the advancement's keys of this tab as {@link String}s.
     *
     * @return An unmodifiable {@link Collection} of all the advancement's keys of this tab as {@link String}s.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     */
    @Unmodifiable
    @NotNull
    public Collection<@NotNull String> getAdvancementsAsStrings() {
        checkInitialisation();
        if (advNamespacedKeys != null) {
            return advNamespacedKeys;
        } else {
            List<String> list = new ArrayList<>(advancements.size());
            for (AdvancementKey key : advancements.keySet()) {
                list.add(key.toString());
            }
            return advNamespacedKeys = Collections.unmodifiableCollection(list);
        }
    }

    /**
     * Returns whether the namespace of the specified advancement's key is equals to this tab namespace.
     *
     * @param advancement The advancement.
     * @return Whether the namespace of the specified advancement's key is equals to this tab namespace.
     */
    @Contract(pure = true)
    public boolean isOwnedByThisTab(@NotNull Advancement advancement) {
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        return advancement.getKey().getNamespace().equals(namespace);
    }

    /**
     * Utility method which automatically shows this tab to every player just after they have been loaded.
     * <p>More formally, this is equivalent to calling:
     * <blockquote><pre>
     * tab.registerEvent(PlayerLoadingCompletedEvent.class, EventPriority.LOWEST, e -> tab.showTab(e.getPlayer()));
     * </pre></blockquote>
     *
     * @return This {@code AdvancementTab}.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     * @since 2.2.0
     */
    @NotNull
    @Contract("-> this")
    public AdvancementTab automaticallyShowToPlayers() {
        checkInitialisation();
        if (!automaticallyShown) {
            automaticallyShown = true;
            // Use LOWEST priority in order to show the tab as soon as possible
            registerEvent(PlayerLoadingCompletedEvent.class, EventPriority.LOWEST, e -> showTab(e.getPlayer()));
        }
        return this;
    }

    /**
     * Utility method which automatically grants the root advancement of this tab to every player after they have been loaded.
     * <p>More formally, this is equivalent to calling:
     * <blockquote><pre>
     * tab.registerEvent(PlayerLoadingCompletedEvent.class, EventPriority.LOW, e -> tab.grantRootAdvancement(e.getPlayer()));
     * </pre></blockquote>
     *
     * @return This {@code AdvancementTab}.
     * @throws IllegalStateException If the tab is not initialised.
     * @throws DisposedException If the tab is disposed.
     * @since 2.2.0
     */
    @NotNull
    @Contract("-> this")
    public AdvancementTab automaticallyGrantRootAdvancement() {
        checkInitialisation();
        if (!automaticallyGrant) {
            automaticallyGrant = true;
            // Use LOW priority since automaticallyShowToPlayers() uses LOWEST priority
            registerEvent(PlayerLoadingCompletedEvent.class, EventPriority.LOW, e -> grantRootAdvancement(e.getPlayer()));
        }
        return this;
    }

    /**
     * Registers the provided event into the {@link EventManager} of this tab.
     *
     * @param eventClass The class of the event to register.
     * @param consumer The code to run when the event occurs.
     * @param <E> The class of the event to register.
     * @throws DisposedException If the tab is disposed.
     * @throws IllegalArgumentException If any argument is null.
     * @since 2.2.0
     */
    public <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull Consumer<E> consumer) {
        try {
            eventManager.register(this, eventClass, consumer);
        } catch (IllegalStateException e) {
            throw new DisposedException(e);
        }
    }

    /**
     * Registers the provided event into the {@link EventManager} of this tab.
     *
     * @param eventClass The class of the event to register.
     * @param priority The priority of the event. See {@link EventPriority}.
     * @param consumer The code to run when the event occurs.
     * @param <E> The class of the event to register.
     * @throws DisposedException If the tab is disposed.
     * @throws IllegalArgumentException If any argument is null.
     * @since 2.2.0
     */
    public <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull EventPriority priority, @NotNull Consumer<E> consumer) {
        try {
            eventManager.register(this, eventClass, priority, consumer);
        } catch (IllegalStateException e) {
            throw new DisposedException(e);
        }
    }

    private void checkInitialisation() {
        if (disposed)
            throw new DisposedException("AdvancementTab is disposed");
        if (!initialised)
            throw new IllegalStateException("AdvancementTab has not been initialised yet.");
    }

    /**
     * Returns the namespace of the tab.
     *
     * @return The namespace of the tab.
     */
    @NotNull
    @Override
    public String toString() {
        return namespace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdvancementTab that = (AdvancementTab) o;

        return namespace.equals(that.namespace);
    }

    @Override
    public int hashCode() {
        return namespace.hashCode();
    }

    /**
     * Gets the plugin that created this advancement tab.
     *
     * @return The plugin that created this advancement tab.
     */
    @NotNull
    public Plugin getOwningPlugin() {
        return owningPlugin;
    }

    /**
     * Gets the {@link EventManager} of this tab.
     *
     * @return The {@link EventManager} of this tab.
     */
    @NotNull
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Gets the unique namespace of this tab.
     *
     * @return The unique namespace of this tab.
     */
    @NotNull
    public String getNamespace() {
        return namespace;
    }

    /**
     * Gets the {@link DatabaseManager} of this tab.
     *
     * @return The {@link DatabaseManager} of this tab.
     */
    @NotNull
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Gets whether the tab is initialized.
     *
     * @return Whether the tab is initialized.
     */
    public boolean isInitialised() {
        return initialised;
    }

    /**
     * Returns whether the tab is disposed.
     *
     * @return Whether the tab is disposed.
     */
    public boolean isDisposed() {
        return disposed;
    }

    private class AdvsUpdateRunnable implements Runnable {

        private final Set<TeamProgression> advsToUpdate = new HashSet<>();
        private boolean scheduled = false;
        private BukkitTask task;

        public void schedule(@NotNull TeamProgression progression) {
            if (!scheduled) {
                scheduled = true;
                task = Bukkit.getScheduler().runTaskLater(owningPlugin, this, 1L);
            }
            advsToUpdate.add(progression);
        }

        public void dispose() {
            if (task != null) {
                task.cancel();
                advsToUpdate.clear();
                scheduled = false;
            }
        }

        @Override
        public void run() {
            // Keep additional space for advancements that might be added by Advancement#onUpdate
            final int best = advancements.size() + 16;
            final Map<AdvancementWrapper, Integer> advs = Maps.newHashMapWithExpectedSize(best);

            for (TeamProgression pro : advsToUpdate) {
                for (Advancement advancement : advancements.values()) {
                    advancement.onUpdate(pro, advs);
                }

                final Set<MinecraftKeyWrapper> keys = Sets.newHashSetWithExpectedSize(advs.size());
                for (AdvancementWrapper wrapper : advs.keySet()) {
                    keys.add(wrapper.getKey());
                }

                ISendable sendPacket, noTab, thisTab;
                try {
                    sendPacket = PacketPlayOutAdvancementsWrapper.craftSendPacket(advs);
                    noTab = PacketPlayOutSelectAdvancementTabWrapper.craftSelectNone();
                    thisTab = PacketPlayOutSelectAdvancementTabWrapper.craftSelect(rootAdvancement.getKey().getNMSWrapper());
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    continue;
                }

                pro.forEachMember(u -> {
                    Player player = Bukkit.getPlayer(u);
                    if (player != null) {
                        noTab.sendTo(player);

                        @Nullable Set<MinecraftKeyWrapper> set = players.put(player, keys);
                        if (set != null && !set.isEmpty()) {
                            try {
                                PacketPlayOutAdvancementsWrapper.craftRemovePacket(set).sendTo(player);
                            } catch (ReflectiveOperationException e) {
                                e.printStackTrace();
                                players.put(player, set);
                                thisTab.sendTo(player);
                                return; // TODO Check
                            }
                        }

                        sendPacket.sendTo(player);
                        thisTab.sendTo(player);
                    }
                });

                advs.clear();
            }
            task = null;
            advsToUpdate.clear();
            scheduled = false;
        }
    }
}
