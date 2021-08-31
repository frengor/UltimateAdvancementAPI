package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementDisposeEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementDisposedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementRegistrationEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DisposedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DuplicatedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_15_R1.AdvancementProgress;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import net.minecraft.server.v1_15_R1.PacketPlayOutAdvancements;
import net.minecraft.server.v1_15_R1.PacketPlayOutSelectAdvancementTab;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey.checkNamespace;

/**
 * AdvancementTab objects represent a tab in the minecraft advancement GUI. They are used to register advancements in order to be sent to the players.
 */
public final class AdvancementTab {

    private final Plugin owningPlugin;

    private final EventManager eventManager;

    private final String namespace;
    @NotNull
    private final Map<AdvancementKey, Advancement> advancements = new HashMap<>();
    private RootAdvancement rootAdvancement;

    private final DatabaseManager databaseManager;

    private boolean initialised = false, disposed = false;
    private final Map<Player, Set<MinecraftKey>> players = new HashMap<>();
    private Collection<String> advNamespacedKeys;
    private Collection<BaseAdvancement> advsWithoutRoot;

    /**
     * Create a new AdvancementTab.
     *
     * @param owningPlugin The plugin that requires the creation of a new AdvancementTab.
     * @param databaseManager The database manager.
     * @param namespace The unique name of the advancement tab.
     */
    protected AdvancementTab(@NotNull Plugin owningPlugin, @NotNull DatabaseManager databaseManager, @NotNull String namespace) {
        checkNamespace(namespace);
        this.namespace = Objects.requireNonNull(namespace);
        this.owningPlugin = Objects.requireNonNull(owningPlugin);
        this.eventManager = new EventManager(owningPlugin);
        this.databaseManager = Objects.requireNonNull(databaseManager);
        eventManager.register(this, PlayerQuitEvent.class, e -> players.remove(e.getPlayer()));
    }

    /**
     * Gets whether the tab is initialised and not disposed.
     *
     * @return {@code true} if this tab is initialised and not disposed, {@code false} otherwise.
     */
    public boolean isActive() {
        return initialised && !disposed;
    }

    @NotNull
    @Contract(pure = true)
    public RootAdvancement getRootAdvancement() {
        checkInitialisation();
        return rootAdvancement;
    }

    /**
     * Returns all the advancements registered in the advancement tab.
     *
     * @return All the advancements registered in the advancement tab.
     */
    @UnmodifiableView
    @NotNull
    @Contract(pure = true)
    public Collection<@NotNull Advancement> getAdvancements() {
        checkInitialisation();
        return Collections.unmodifiableCollection(advancements.values());
    }

    /**
     * Returns all the advancements registered in the advancement tab without the root advancement.
     *
     * @return All the advancements registered in the advancement tab without the root advancement.
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
                if (!(a instanceof RootAdvancement)) {
                    list.add((BaseAdvancement) a);
                }
            }
            return advsWithoutRoot = Collections.unmodifiableList(list);
        }
    }

    /**
     * Returns all the advancements registered in the advancement tab filtered by class.
     *
     * @param filterClass The class filter.
     * @return All the advancements registered in the advancement tab filtered by class.
     */
    @Unmodifiable
    @NotNull
    @Contract(pure = true)
    public Collection<@NotNull Advancement> getAdvancementsByClass(Class<? extends Advancement> filterClass) {
        checkInitialisation();
        if (filterClass == null || filterClass == Advancement.class) {
            return Collections.unmodifiableCollection(advancements.values());
        }
        if (filterClass.isAssignableFrom(RootAdvancement.class)) {
            return Collections.singletonList(rootAdvancement);
        }
        if (filterClass.isAssignableFrom(BaseAdvancement.class)) {
            Collection<Advancement> coll = new ArrayList<>(advancements.size());
            for (Advancement a : advancements.values()) {
                if (a.getClass().isAssignableFrom(filterClass)) {
                    coll.add(a);
                }
            }
            return Collections.unmodifiableCollection(coll);
        }
        return Collections.emptyList();
    }

    /**
     * Returns an unmodifiable list of all {@link AdvancementKey} of the advancements.
     *
     * @return An unmodifiable list of all {@link AdvancementKey} of the advancements.
     */
    @UnmodifiableView
    @NotNull
    @Contract(pure = true)
    public Set<@NotNull AdvancementKey> getAdvancementsNamespacedKeys() {
        checkInitialisation();
        return Collections.unmodifiableSet(advancements.keySet());
    }

    /**
     * Returns if the advancement belongs to this advancement tab.
     *
     * @param advancement The advancements of this tab. Cannot include any {@code null} advancement.
     * @return If the advancement belongs to this advancement tab.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean hasAdvancement(Advancement advancement) {
        checkInitialisation();
        if (advancement == null)
            return false;
        return advancements.containsKey(advancement.getKey());
    }

    /**
     * Returns if the advancement belongs to this advancement tab.
     *
     * @param namespacedKey The {@link  AdvancementKey} of the advancement of this tab.
     * @return If the advancement belongs to this advancement tab.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean hasAdvancement(AdvancementKey namespacedKey) {
        checkInitialisation();
        return advancements.containsKey(namespacedKey);
    }

    /**
     * Returns the wanted advancement.
     *
     * @param namespacedKey The {@link AdvancementKey} of the wanted advancement of this tab.
     * @return The wanted advancement, or {@code null}.
     */
    @Nullable
    @Contract(pure = true, value = "null -> null")
    public Advancement getAdvancement(AdvancementKey namespacedKey) {
        checkInitialisation();
        return advancements.get(namespacedKey);
    }

    /**
     * Returns an unmodifiable list of the player that are registered in the tab.
     *
     * @return An unmodifiable list of the player that are registered in the tab.
     */
    @UnmodifiableView
    @NotNull
    @Contract(pure = true)
    public Set<@NotNull Player> getPlayers() {
        checkInitialisation();
        return Collections.unmodifiableSet(players.keySet());
    }

    /**
     * Grants the root advancement of the tab to a specified player.
     * <p>This method will call {@link RootAdvancement#giveReward(Player)}
     *
     * @param player The player.
     */
    public void grantRootAdvancement(@NotNull Player player) {
        checkInitialisation();
        rootAdvancement.setCriteriaTeamProgression(player, rootAdvancement.getMaxCriteria());
    }

    /**
     * Grants the root advancement of the tab to a specified player.
     * <p>This method will call {@link RootAdvancement#giveReward(Player)}
     *
     * @param uuid The UUID player.
     */
    public void grantRootAdvancement(@NotNull UUID uuid) {
        checkInitialisation();
        rootAdvancement.setCriteriaTeamProgression(uuid, rootAdvancement.getMaxCriteria());
    }

    /**
     * Grants the root advancement of the tab to a specified player.
     *
     * @param player The player.
     * @param giveRewards Whether call {@link RootAdvancement#giveReward(Player)} or not.
     */
    public void grantRootAdvancement(@NotNull Player player, boolean giveRewards) {
        checkInitialisation();
        rootAdvancement.setCriteriaTeamProgression(player, rootAdvancement.getMaxCriteria(), giveRewards);
    }

    /**
     * Grants the root advancement of the tab to a specified player.
     *
     * @param uuid The player.
     * @param giveRewards Whether call {@link RootAdvancement#giveReward(Player)} or not.
     */
    public void grantRootAdvancement(@NotNull UUID uuid, boolean giveRewards) {
        checkInitialisation();
        rootAdvancement.setCriteriaTeamProgression(uuid, rootAdvancement.getMaxCriteria(), giveRewards);
    }

    /**
     * Update an advancement to a specified team.
     *
     * @param player A player of the team.
     */
    public void updateAdvancementsToTeam(@NotNull Player player) {
        updateAdvancementsToTeam(AdvancementUtils.uuidFromPlayer(player));
    }

    /**
     * Update an advancement to a specified team.
     *
     * @param uuid A UUID player of the team.
     */
    public void updateAdvancementsToTeam(@NotNull UUID uuid) {
        updateAdvancementsToTeam(databaseManager.getProgression(uuid));
    }

    /**
     * Update an advancement to a specified team.
     *
     * @param pro The {@link TeamProgression} of the team.
     */
    public void updateAdvancementsToTeam(@NotNull TeamProgression pro) {
        checkInitialisation();
        Validate.notNull(pro, "TeamProgression is null.");

        final int best = advancements.size() + 16;

        final Set<MinecraftKey> toRemove = Sets.newHashSetWithExpectedSize(best);

        final Set<net.minecraft.server.v1_15_R1.Advancement> advs = Sets.newHashSetWithExpectedSize(best);
        final Map<MinecraftKey, AdvancementProgress> progs = Maps.newHashMapWithExpectedSize(best);

        for (Advancement advancement : advancements.values()) {
            advancement.onUpdate(pro, advs, progs, toRemove);
        }

        PacketPlayOutAdvancements sendPacket = new PacketPlayOutAdvancements(false, advs, Collections.emptySet(), progs);

        PacketPlayOutSelectAdvancementTab noTab = new PacketPlayOutSelectAdvancementTab(), thisTab = new PacketPlayOutSelectAdvancementTab(rootAdvancement.getMinecraftKey());

        pro.forEachMember(u -> {
            Player player = Bukkit.getPlayer(u);
            if (player != null) {
                PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
                connection.sendPacket(noTab);

                @Nullable Set<MinecraftKey> set = players.put(player, toRemove);
                if (set != null && !set.isEmpty()) {
                    PacketPlayOutAdvancements removePacket = new PacketPlayOutAdvancements(false, Collections.emptySet(), set, Collections.emptyMap());
                    connection.sendPacket(removePacket);
                }

                connection.sendPacket(sendPacket);
                //new BukkitRunnable() {
                //    @Override
                //    public void run() {
                connection.sendPacket(thisTab);
                ///    }
                //}.runTaskLaterAsynchronously(AdvancementMain.getInstance(), 5);
            }
        });
    }

    /**
     * Update all advancement of this tab to a specified team.
     *
     * @param player A player of the team.
     */
    public void updateEveryAdvancement(@NotNull Player player) {
        checkInitialisation();
        Validate.notNull(player, "Player is null.");

        TeamProgression pro = databaseManager.getProgression(player);

        final int best = advancements.size() + 16;

        final Set<MinecraftKey> toRemove = Sets.newHashSetWithExpectedSize(best);
        final Set<net.minecraft.server.v1_15_R1.Advancement> advs = Sets.newHashSetWithExpectedSize(best);
        final Map<MinecraftKey, AdvancementProgress> progs = Maps.newHashMapWithExpectedSize(best);

        for (Advancement advancement : advancements.values()) {
            advancement.onUpdate(pro, advs, progs, toRemove);
        }

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        PacketPlayOutSelectAdvancementTab noTab = new PacketPlayOutSelectAdvancementTab(), thisTab = new PacketPlayOutSelectAdvancementTab(rootAdvancement.getMinecraftKey());
        connection.sendPacket(noTab);

        @Nullable Set<MinecraftKey> set = players.put(player, toRemove);
        if (set != null && !set.isEmpty()) {
            PacketPlayOutAdvancements removePacket = new PacketPlayOutAdvancements(false, Collections.emptySet(), set, Collections.emptyMap());
            connection.sendPacket(removePacket);
        }

        PacketPlayOutAdvancements sendPacket = new PacketPlayOutAdvancements(false, advs, Collections.emptySet(), progs);
        connection.sendPacket(sendPacket);
        //new BukkitRunnable() {
        //    @Override
        //    public void run() {*/
        connection.sendPacket(thisTab);
        //    }
        //}.runTaskLaterAsynchronously(AdvancementMain.getInstance(), 5);*/
    }

    /**
     * Register the advancements for this tab, initializing the tab. Thus, it cannot be called twice.
     *
     * @param rootAdvancement The root of this tab.
     * @param advancements The advancements of this tab. Cannot include any {@code null} advancement or {@link RootAdvancement}.
     */
    public void registerAdvancements(@NotNull RootAdvancement rootAdvancement, @NotNull BaseAdvancement... advancements) {
        registerAdvancements(rootAdvancement, Sets.newHashSet(advancements));
    }

    /**
     * Register the advancements for this tab, initializing the tab. Thus, it cannot be called twice.
     *
     * @param rootAdvancement The root of this tab.
     * @param advancements The advancements of this tab. Cannot include any {@code null} advancement or {@link RootAdvancement}.
     */
    public void registerAdvancements(@NotNull RootAdvancement rootAdvancement, @NotNull Set<BaseAdvancement> advancements) {
        if (disposed) {
            throw new DisposedException("AdvancementTab is disposed.");
        }
        if (initialised)
            throw new IllegalStateException("Tab is already initialised.");
        Validate.notNull(rootAdvancement, "RootAdvancement is null.");
        Validate.isTrue(isOwnedByThisTab(rootAdvancement), "RootAdvancement " + rootAdvancement + " is not owned by this tab.");

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

        PluginManager pluginManager = Bukkit.getPluginManager();

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
     * Shows the tab to some players.
     *
     * @param players The players.
     */
    public void showTab(@NotNull Player... players) {
        checkInitialisation();
        Validate.notNull(players, "Player[] is null.");
        for (Player p : players) {
            try {
                showTab(p);
            } catch (NullPointerException e) {
                // Add other players anyway
                e.printStackTrace();
            }
        }
    }

    /**
     * Shows the tab to a players.
     *
     * @param player The player.
     */
    public void showTab(@NotNull Player player) {
        checkInitialisation();
        Validate.notNull(player, "Player is null.");
        if (!players.containsKey(player)) {
            players.put(player, Collections.emptySet());
            updateEveryAdvancement(player);
        }
    }

    /**
     * Hides the tab to a player.
     *
     * @param player The player.
     */
    public void hideTab(@NotNull Player player) {
        checkInitialisation();
        Validate.notNull(player, "Player is null.");
        checkPlayer(player);

        removePlayer(player, players.remove(player));
    }

    private void removePlayer(@NotNull Player player, @NotNull Set<MinecraftKey> keys) {
        PacketPlayOutAdvancements packet = new PacketPlayOutAdvancements(false, Collections.emptySet(), keys, Collections.emptyMap());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    void dispose() {
        checkInitialisation();
        disposed = true;
        eventManager.disable();
        Iterator<Entry<Player, Set<MinecraftKey>>> it = players.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Player, Set<MinecraftKey>> e = it.next();
            removePlayer(e.getKey(), e.getValue());
            it.remove();
        }
        PluginManager pluginManager = Bukkit.getPluginManager();
        for (Advancement a : advancements.values()) {
            try {
                // Trigger AdvancementDisposeEvent
                pluginManager.callEvent(new AdvancementDisposeEvent(a));
                // Dispose the advancement
                a.onDispose();
                // Trigger AdvancementDisposedEvent
                pluginManager.callEvent(new AdvancementDisposedEvent(a.getKey()));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        advancements.clear();
        rootAdvancement = null;
        advNamespacedKeys = null;
        advsWithoutRoot = null;
    }

    /**
     * Returns if the tab is visible to a specified team.
     *
     * @param player A player of a team.
     * @return If the tab is visible to a specified team.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean isShownTo(Player player) {
        checkInitialisation();
        return players.containsKey(player);
    }

    /**
     * Returns an unmodifiable list of all advancement keys of this tab.
     *
     * @return An unmodifiable list of all advancement keys of this tab.
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
     * Returns if a specified advancement belongs to this tab.
     *
     * @param advancement The advancement.
     * @return If a specified advancement belongs to this tab.
     */
    @Contract(pure = true)
    public boolean isOwnedByThisTab(@NotNull Advancement advancement) {
        Validate.notNull(advancement, "Advancement is null.");
        return advancement.getKey().getNamespace().equals(namespace);
    }

    private void checkInitialisation() {
        if (disposed)
            throw new DisposedException("AdvancementTab is disposed");
        if (!initialised)
            throw new IllegalStateException("AdvancementTab has not been initialised yet.");
    }

    private void checkPlayer(Player player) {
        if (!players.containsKey(player))
            throw new IllegalArgumentException("Tab isn't shown to " + (player == null ? "null" : player.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return namespace;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdvancementTab that = (AdvancementTab) o;

        return namespace.equals(that.namespace);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return namespace.hashCode();
    }

    /**
     * Returns the plugin that owns this advancement tab.
     *
     * @return The plugin that owns this advancement tab.
     */
    public Plugin getOwningPlugin() {
        return owningPlugin;
    }

    /**
     * Returns the {@link EventManager} of this tab.
     *
     * @return The {@link EventManager} of this tab.
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Returns the unique name of this tab.
     *
     * @return The unique name of this tab.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the {@link DatabaseManager} of this tab.
     *
     * @return The {@link DatabaseManager} of this tab.
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Returns whether this advancement tab has been initialized or not.
     *
     * @return {@code true} if the tab has been initialized, {@code false} otherwise.
     */
    public boolean isInitialised() {
        return initialised;
    }

    /**
     * Returns if the advancement tab is disposed.
     *
     * @return If the advancement tab is disposed.
     */
    public boolean isDisposed() {
        return disposed;
    }
}
