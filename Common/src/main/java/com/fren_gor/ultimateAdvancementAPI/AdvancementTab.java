package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractImmutableAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractPerPlayerAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractPerTeamAdvancementDisplay;
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
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.ISendable;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutSelectAdvancementTabWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.util.CompositeMap;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey.checkNamespace;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * The {@code AdvancementTab} class represents a tab in the advancement GUI.
 * <p>Every advancement tab is represented by a unique namespace (not visible in the advancement GUI) and has only one {@link RootAdvancement}.
 * <p>An instance can be obtained using {@link UltimateAdvancementAPI#createAdvancementTab(String, String)}.
 * The returned instance is not initialised, that is it doesn't contain any advancement yet.
 */
public final class AdvancementTab {

    private final Plugin owningPlugin;
    private final EventManager eventManager;
    private final String namespace;
    private final TabDisplay tabDisplay;
    private final DatabaseManager databaseManager;
    @Unmodifiable
    private volatile Map<AdvancementKey, Advancement> advancements = Collections.emptyMap();
    private final Map<Player, Set<MinecraftKeyWrapper>> players = new HashMap<>();
    private final AdvsUpdateRunnable updateManager;

    private RootAdvancement rootAdvancement;
    private volatile boolean initialised = false, disposed = false;
    private boolean automaticallyShown = false, automaticallyGrant = false, showToastToTeam = true, sendAnnouncementMessageOnlyToTeam = false;
    @LazyValue
    private Collection<String> advNamespacedKeys;
    @LazyValue
    private Collection<BaseAdvancement> advsWithoutRoot;

    AdvancementTab(@NotNull Plugin owningPlugin, @NotNull DatabaseManager databaseManager, @NotNull String namespace, @NotNull TabDisplay tabDisplay) {
        checkNamespace(namespace);
        this.namespace = namespace;
        this.tabDisplay = Objects.requireNonNull(tabDisplay, "Tab display is null.");
        this.owningPlugin = Objects.requireNonNull(owningPlugin, "Plugin is null.");
        this.eventManager = new EventManager(owningPlugin);
        this.databaseManager = Objects.requireNonNull(databaseManager, "DatabaseManager is null.");
        this.updateManager = new AdvsUpdateRunnable();
        eventManager.register(this, PlayerQuitEvent.class, e -> players.remove(e.getPlayer()));
    }

    /**
     * Returns whether the tab is initialised and not disposed.
     *
     * @return Whether the tab is initialised and not disposed.
     */
    public boolean isActive() {
        // IMPLEMENTATION DETAIL: this method must be thread-safe to call, see DatabaseManager#processUnredeemed and AdvancementMain#getAdvancement

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
        // IMPLEMENTATION DETAIL: this method must be thread-safe to call, see DatabaseManager#processUnredeemed and AdvancementMain#getAdvancement

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

        final Map<AdvancementKey, Advancement> map = new HashMap<>();

        this.rootAdvancement = rootAdvancement;
        map.put(rootAdvancement.getKey(), rootAdvancement);

        final PluginManager pluginManager = Bukkit.getPluginManager();

        callOnRegister(rootAdvancement);

        try {
            pluginManager.callEvent(new AdvancementRegistrationEvent(rootAdvancement));
        } catch (Exception e) {
            onRegisterFail();
            throw e;
        }

        for (BaseAdvancement adv : advancements) {
            if (map.put(adv.getKey(), adv) != null) {
                onRegisterFail();
                throw new DuplicatedException("Advancement " + adv.getKey() + " is duplicated.");
            }

            callOnRegister(adv);

            try {
                pluginManager.callEvent(new AdvancementRegistrationEvent(adv));
            } catch (Exception e) {
                onRegisterFail();
                throw e;
            }
        }

        this.advancements = Collections.unmodifiableMap(map);

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
        advancements = Collections.emptyMap();
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
                owningPlugin.getLogger().log(Level.SEVERE, "Couldn't show tab " + namespace + " to player " + p.getName(), e);
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
                owningPlugin.getLogger().log(Level.SEVERE, "Couldn't hide tab " + namespace + " to player " + p.getName(), e);
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
            owningPlugin.getLogger().log(Level.SEVERE, "Couldn't send remove packet to player " + player.getName(), e);
        }
    }

    void dispose() {
        AdvancementUtils.checkSync();
        if (disposed) { // Non-atomic read and write is fine here, since this method must be called on the main-thread
            throw new DisposedException("AdvancementTab is already disposed");
        }
        disposed = true;
        eventManager.disable();
        updateManager.dispose();
        if (!initialised) {
            return; // Just return if not initialized
        }

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
                } catch (Exception e) {
                    owningPlugin.getLogger().log(Level.WARNING, "An exception has occurred while calling AdvancementDisposeEvent for " + a, e);
                }
                // Dispose the advancement
                a.onDispose();
                // Trigger AdvancementDisposedEvent
                try {
                    pluginManager.callEvent(new AdvancementDisposedEvent(a.getKey()));
                } catch (Exception e) {
                    owningPlugin.getLogger().log(Level.WARNING, "An exception has occurred while calling AdvancementDisposedEvent for " + a, e);
                }
            } catch (Exception e) {
                owningPlugin.getLogger().log(Level.SEVERE, "An exception occurred disposing advancement " + a, e);
            }
        }
        advancements = Collections.emptyMap();
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
     * <p>This is more or less equivalent to calling:
     * <pre> {@code tab.registerEvent(PlayerLoadingCompletedEvent.class, EventPriority.LOWEST, e -> tab.showTab(e.getPlayer()));}</pre>
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

            // Show tab to already online players
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (databaseManager.isLoadedAndOnline(p)) {
                    showTab(p);
                }
            }
        }
        return this;
    }

    /**
     * Utility method which automatically grants the root advancement of this tab to every player after they have been loaded.
     * <p>This is more or less equivalent to calling:
     * <pre> {@code tab.registerEvent(PlayerLoadingCompletedEvent.class, EventPriority.LOW, e -> tab.grantRootAdvancement(e.getPlayer()));}</pre>
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

            // Grant root to already online players
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (databaseManager.isLoadedAndOnline(p)) {
                    grantRootAdvancement(p);
                }
            }
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
        // IMPLEMENTATION DETAIL: this method must be thread-safe to call, since used in AdvancementTab#getAdvancement

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
     * Gets the display of this tab.
     *
     * @return The display of this tab.
     */
    @NotNull
    public TabDisplay getTabDisplay() {
        return tabDisplay;
    }

    /**
     * Returns whether the toast notification should be sent to every team member on advancement grant.
     * <p>Defaults to {@code true} if not changed using {@link #setShowToastToTeam(boolean)}.
     *
     * @return Whether the toast notification should be sent to every team member on advancement grant.
     */
    public boolean doesShowToastToTeam() {
        return showToastToTeam;
    }

    /**
     * Sets whether the toast notification should be sent to every team member on advancement grant.
     *
     * @param showToastToTeam Whether the toast notification should be sent to every team member on advancement grant.
     */
    public void setShowToastToTeam(boolean showToastToTeam) {
        this.showToastToTeam = showToastToTeam;
    }

    /**
     * Returns whether the announcement message should be sent <i>only</i> to the team members on advancement grant.
     * <p>Defaults to {@code false} if not changed using {@link #setSendAnnouncementMessageOnlyToTeam(boolean)}.
     *
     * @return Whether the announcement message should be sent <i>only</i> to the team members on advancement grant.
     */
    public boolean doesSendAnnouncementMessageOnlyToTeam() {
        return sendAnnouncementMessageOnlyToTeam;
    }

    /**
     * Sets whether the announcement message should be sent <i>only</i> to the team members on advancement grant.
     *
     * @param sendAnnouncementMessageOnlyToTeam Whether the announcement message should be sent <i>only</i> to the
     *         team members on advancement grant.
     */
    public void setSendAnnouncementMessageOnlyToTeam(boolean sendAnnouncementMessageOnlyToTeam) {
        this.sendAnnouncementMessageOnlyToTeam = sendAnnouncementMessageOnlyToTeam;
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

        private final PreparedAdvancementWrapper internalRoot;

        @LazyValue
        private TabDisplayData immutableDisplayData;

        public AdvsUpdateRunnable() {
            try {
                MinecraftKeyWrapper internalRootKey = MinecraftKeyWrapper.craft(namespace, AdvancementKey.RESERVED_KEY_PREFIX + "root");
                internalRoot = PreparedAdvancementWrapper.craft(internalRootKey, 1);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

        }

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
            final int sizeApprox = advancements.size() + 16;

            if (immutableDisplayData == null && tabDisplay instanceof ImmutableTabDisplay display) {
                try {
                    immutableDisplayData = new TabDisplayData(display.getBackgroundTexture(), display.getIcon(), display.getTitle());
                } catch (Exception e) {
                    owningPlugin.getLogger().log(Level.SEVERE, "Error sending advancements", e);
                    return;
                }
            }

            for (TeamProgression pro : advsToUpdate) {
                if (!pro.isValid()) {
                    continue;
                }

                ImmutableAndPerTeamUpdateData update = null;
                for (UUID u : pro.getMembers()) {
                    try {
                        Player player = Bukkit.getPlayer(u);
                        if (player != null && AdvancementTab.this.players.containsKey(player) && databaseManager.isLoadedAndOnline(player)) {
                            if (update == null) {
                                try {
                                    // Create immutable and per-team update data only if a player of the team is online and loaded
                                    update = updateTeam(pro, immutableDisplayData, sizeApprox);
                                } catch (Exception e) {
                                    owningPlugin.getLogger().log(Level.SEVERE, "Error sending advancements to team with id " + pro.getTeamId(), e);
                                    break;
                                }
                            }

                            updatePlayer(player, update, sizeApprox);
                        }
                    } catch (Exception e) {
                        owningPlugin.getLogger().log(Level.SEVERE, "Error sending advancements to player " + u, e);
                    }
                }
            }
            task = null;
            advsToUpdate.clear();
            scheduled = false;
        }

        private ImmutableAndPerTeamUpdateData updateTeam(TeamProgression pro, TabDisplayData displayData, int sizeApprox) throws ReflectiveOperationException {
            if (displayData == null && tabDisplay instanceof PerTeamTabDisplay display) {
                displayData = new TabDisplayData(display.getBackgroundTexture(pro), display.getIcon(pro), display.getTitle(pro));
            }

            final AdvancementUpdater updater = new AdvancementUpdater(rootAdvancement.getKey(), sizeApprox);

            for (Advancement advancement : advancements.values()) {
                advancement.onUpdate(pro, updater);
            }

            final ISendable noTab = PacketPlayOutSelectAdvancementTabWrapper.craftSelectNone();
            final ISendable thisTab = PacketPlayOutSelectAdvancementTabWrapper.craftSelect(internalRoot.getKey());

            final Map<AdvancementWrapper, Integer> perTeamToSend = Maps.newHashMapWithExpectedSize(sizeApprox);

            // Handle root advancement
            final PreparedAdvancementDisplayWrapper rootDisplay; // Null if the root display is per-player
            if (updater.getRootDisplay() instanceof AbstractImmutableAdvancementDisplay immutable) {
                rootDisplay = immutable.getNMSWrapper();
            } else if (updater.getRootDisplay() instanceof AbstractPerTeamAdvancementDisplay perTeam) {
                rootDisplay = perTeam.getNMSWrapper(pro);
            } else {
                // Per-player root display
                rootDisplay = null;
            }

            if (rootDisplay != null) {
                perTeamToSend.put(updater.getRootWrapper().withParent(this.internalRoot).toAdvancementWrapper(rootDisplay.toBaseAdvancementDisplay()), updater.getRootProgression());
            }

            // Handle internal root display
            final AdvancementDisplayWrapper internalRootDisplay; // Null if the root display or displayData is per-player
            final boolean tabDisplayAndRootAreImmutableOrPerTeam = rootDisplay != null && displayData != null;
            if (tabDisplayAndRootAreImmutableOrPerTeam) {
                internalRootDisplay = displayData.toRootDisplayWrapper(rootDisplay.getX(), rootDisplay.getY(), rootDisplay::getIcon, rootDisplay::getTitle);
                perTeamToSend.put(this.internalRoot.toAdvancementWrapper(internalRootDisplay), 0);
            } else {
                internalRootDisplay = null;
            }

            // Handle base advancements
            for (var entry : updater.getImmutableAdvancements()) {
                AdvancementDisplayWrapper display = entry.display().getNMSWrapper().toBaseAdvancementDisplay();
                perTeamToSend.put(entry.advancementWrapper().toAdvancementWrapper(display), entry.progression());
            }
            for (var entry : updater.getPerTeamAdvancements()) {
                AdvancementDisplayWrapper display = entry.display().getNMSWrapper(pro).toBaseAdvancementDisplay();
                perTeamToSend.put(entry.advancementWrapper().toAdvancementWrapper(display), entry.progression());
            }

            return new ImmutableAndPerTeamUpdateData(updater, perTeamToSend, displayData, tabDisplayAndRootAreImmutableOrPerTeam, internalRootDisplay, rootDisplay, noTab, thisTab);
        }

        private void updatePlayer(Player player, ImmutableAndPerTeamUpdateData update, int sizeApprox) throws ReflectiveOperationException {
            TabDisplayData displayData = update.displayData;
            if (displayData == null) {
                if (tabDisplay instanceof PerPlayerTabDisplay display) {
                    displayData = new TabDisplayData(display.getBackgroundTexture(player), display.getIcon(player), display.getTitle(player));
                } else {
                    throw new RuntimeException("Invalid tab display " + tabDisplay.getClass().getName());
                }
            }

            final Map<AdvancementWrapper, Integer> perPlayerToSend = Maps.newHashMapWithExpectedSize(sizeApprox);

            // Handle root advancement
            PreparedAdvancementDisplayWrapper rootDisplay = update.rootDisplay;
            if (rootDisplay == null) {
                if (update.updater.getRootDisplay() instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
                    rootDisplay = perPlayer.getNMSWrapper(player);
                } else {
                    throw new RuntimeException("Invalid root display " + update.updater.getRootDisplay().getClass().getName());
                }

                perPlayerToSend.put(update.updater.getRootWrapper().withParent(this.internalRoot).toAdvancementWrapper(rootDisplay.toBaseAdvancementDisplay()), update.updater.getRootProgression());
            }

            // Handle internal root display
            AdvancementDisplayWrapper internalRootDisplay = update.internalRootDisplay;
            if (internalRootDisplay == null) {
                internalRootDisplay = displayData.toRootDisplayWrapper(rootDisplay.getX(), rootDisplay.getY(), rootDisplay::getIcon, rootDisplay::getTitle);
            }

            if (!update.internalRootAlreadyAddedToMap) {
                perPlayerToSend.put(this.internalRoot.toAdvancementWrapper(internalRootDisplay), 0);
            }

            // Handle base advancements
            for (var entry : update.updater.getPerPlayerAdvancements()) {
                AdvancementDisplayWrapper display = entry.display().getNMSWrapper(player).toBaseAdvancementDisplay();
                perPlayerToSend.put(entry.advancementWrapper().toAdvancementWrapper(display), entry.progression());
            }

            Map<AdvancementWrapper, Integer> toSendMap;
            if (perPlayerToSend.isEmpty()) {
                toSendMap = update.perTeamToSend;
            } else if (update.perTeamToSend.isEmpty()) {
                toSendMap = perPlayerToSend;
            } else {
                toSendMap = CompositeMap.of(update.perTeamToSend, perPlayerToSend);
            }

            ISendable sendPacket = PacketPlayOutAdvancementsWrapper.craftSendPacket(toSendMap);

            update.noTab.sendTo(player);

            @Nullable Set<MinecraftKeyWrapper> oldKeys = players.put(player, update.updater.getKeys());
            if (oldKeys != null && !oldKeys.isEmpty()) {
                try {
                    PacketPlayOutAdvancementsWrapper.craftRemovePacket(oldKeys).sendTo(player);
                } catch (ReflectiveOperationException e) {
                    players.put(player, oldKeys);
                    update.thisTab.sendTo(player); // Show again if sending the remove packet fails
                    throw e;
                }
            }

            sendPacket.sendTo(player);
            update.thisTab.sendTo(player);
        }

        private record TabDisplayData(
                @NotNull String backgroundTexture,
                @NotNull Optional<ItemStack> icon,
                @NotNull Optional<BaseComponent> title
        ) {
            @NotNull
            public AdvancementDisplayWrapper toRootDisplayWrapper(
                    float rootX,
                    float rootY,
                    Supplier<? extends ItemStack> rootIcon,
                    Supplier<? extends BaseComponent> rootTitle
            ) throws ReflectiveOperationException {
                ItemStack icon = this.icon.orElseGet(rootIcon);
                BaseComponent title = this.title.orElseGet(rootTitle);
                return AdvancementDisplayWrapper.craft(
                        icon, title, new TextComponent(""), AdvancementFrameTypeWrapper.TASK, rootX - 0.2f, rootY, false, false, true, this.backgroundTexture
                );
            }
        }

        private record ImmutableAndPerTeamUpdateData(
                @NotNull AdvancementUpdater updater,
                @NotNull Map<AdvancementWrapper, Integer> perTeamToSend,
                @Nullable TabDisplayData displayData,
                boolean internalRootAlreadyAddedToMap,
                @Nullable AdvancementDisplayWrapper internalRootDisplay,
                @Nullable PreparedAdvancementDisplayWrapper rootDisplay,
                @NotNull ISendable noTab,
                @NotNull ISendable thisTab) {
        }
    }

    /**
     * The display of an {@link AdvancementTab}.
     */
    // TODO Make sealed when switching to Java 17
    public interface TabDisplay {
    }

    /**
     * The display of an {@link AdvancementTab} which is immutable, that is every method call should always return the same value.
     */
    @FunctionalInterface
    public interface ImmutableTabDisplay extends TabDisplay {
        /**
         * Returns the path of the background texture image of the tab in the advancement GUI (like "textures/block/stone.png").
         *
         * @return The path of the background texture image of the tab in the advancement GUI.
         */
        @NotNull
        String getBackgroundTexture();

        /**
         * Returns the icon of the tab in the advancement GUI.
         *
         * @return The icon of the tab in the advancement GUI.
         */
        @NotNull
        default Optional<ItemStack> getIcon() {
            return Optional.empty();
        }

        /**
         * Returns the title of the tab in the advancement GUI.
         *
         * @return The title of the tab in the advancement GUI.
         */
        @NotNull
        default Optional<BaseComponent> getTitle() {
            return Optional.empty();
        }
    }

    /**
     * The display of an {@link AdvancementTab} which provides customized values based on the specified team.
     */
    @FunctionalInterface
    public interface PerTeamTabDisplay extends TabDisplay {
        /**
         * Returns the path of the background texture image of the tab in the advancement GUI (like "textures/block/stone.png").
         *
         * @param teamProgression The {@link TeamProgression} of the team.
         * @return The path of the background texture image of the tab in the advancement GUI.
         */
        @NotNull
        String getBackgroundTexture(@NotNull TeamProgression teamProgression);

        /**
         * Returns the icon of the tab in the advancement GUI.
         *
         * @param teamProgression The {@link TeamProgression} of the team.
         * @return The icon of the tab in the advancement GUI.
         */
        @NotNull
        default Optional<ItemStack> getIcon(@NotNull TeamProgression teamProgression) {
            return Optional.empty();
        }

        /**
         * Returns the title of the tab in the advancement GUI.
         *
         * @param teamProgression The {@link TeamProgression} of the team.
         * @return The title of the tab in the advancement GUI.
         */
        @NotNull
        default Optional<BaseComponent> getTitle(@NotNull TeamProgression teamProgression) {
            return Optional.empty();
        }
    }

    /**
     * The display of an {@link AdvancementTab} which provides customized values based on the specified player.
     */
    @FunctionalInterface
    public interface PerPlayerTabDisplay extends TabDisplay {
        /**
         * Returns the path of the background texture image of the tab in the advancement GUI (like "textures/block/stone.png").
         *
         * @param player The player.
         * @return The path of the background texture image of the tab in the advancement GUI.
         */
        @NotNull
        String getBackgroundTexture(@NotNull Player player);

        /**
         * Returns the icon of the tab in the advancement GUI.
         *
         * @param player The player.
         * @return The icon of the tab in the advancement GUI.
         */
        @NotNull
        default Optional<ItemStack> getIcon(@NotNull Player player) {
            return Optional.empty();
        }

        /**
         * Returns the title of the tab in the advancement GUI.
         *
         * @param player The player.
         * @return The title of the tab in the advancement GUI.
         */
        @NotNull
        default Optional<BaseComponent> getTitle(@NotNull Player player) {
            return Optional.empty();
        }
    }
}
