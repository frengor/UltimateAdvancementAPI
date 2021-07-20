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
import lombok.Getter;
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

public final class AdvancementTab {

    @Getter
    private final Plugin owningPlugin;
    @Getter
    private final EventManager eventManager;
    @Getter
    private final String namespace;
    @NotNull
    private final Map<AdvancementKey, Advancement> advancements = new HashMap<>();
    private RootAdvancement rootAdvancement;
    @Getter
    private final DatabaseManager databaseManager;
    @Getter
    private boolean initialised = false, disposed = false;
    private final Map<Player, Set<MinecraftKey>> players = new HashMap<>();
    private Collection<String> advNamespacedKeys;
    private Collection<BaseAdvancement> advsWithoutRoot;

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
     * @return true iff this tab is initialised and not disposed, false otherwise.
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

    @UnmodifiableView
    @NotNull
    @Contract(pure = true)
    public Collection<@NotNull Advancement> getAdvancements() {
        checkInitialisation();
        return Collections.unmodifiableCollection(advancements.values());
    }

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

    @UnmodifiableView
    @NotNull
    @Contract(pure = true)
    public Set<@NotNull AdvancementKey> getAdvancementsNamespacedKeys() {
        checkInitialisation();
        return Collections.unmodifiableSet(advancements.keySet());
    }

    @Nullable
    @Contract(pure = true, value = "null -> null")
    public Advancement getAdvancement(AdvancementKey namespacedKey) {
        checkInitialisation();
        return advancements.get(namespacedKey);
    }

    @UnmodifiableView
    @NotNull
    @Contract(pure = true)
    public Set<@NotNull Player> getPlayers() {
        checkInitialisation();
        return Collections.unmodifiableSet(players.keySet());
    }

    public void grantRootAdvancement(@NotNull Player player) {
        checkInitialisation();
        rootAdvancement.setCriteriaTeamProgression(player, rootAdvancement.getMaxCriteria());
    }

    public void grantRootAdvancement(@NotNull UUID uuid) {
        checkInitialisation();
        rootAdvancement.setCriteriaTeamProgression(uuid, rootAdvancement.getMaxCriteria());
    }

    public void grantRootAdvancement(@NotNull Player player, boolean giveRewards) {
        checkInitialisation();
        rootAdvancement.setCriteriaTeamProgression(player, rootAdvancement.getMaxCriteria(), giveRewards);
    }

    public void grantRootAdvancement(@NotNull UUID uuid, boolean giveRewards) {
        checkInitialisation();
        rootAdvancement.setCriteriaTeamProgression(uuid, rootAdvancement.getMaxCriteria(), giveRewards);
    }

    public void updateAdvancementsToTeam(@NotNull Player player) {
        updateAdvancementsToTeam(AdvancementUtils.uuidFromPlayer(player));
    }

    public void updateAdvancementsToTeam(@NotNull UUID uuid) {
        checkInitialisation();
        Validate.notNull(uuid, "UUID is null.");

        TeamProgression pro = databaseManager.getProgression(uuid);

        final int best = advancements.size() + 16;

        final Set<MinecraftKey> toRemove = Sets.newHashSetWithExpectedSize(best);

        final Set<net.minecraft.server.v1_15_R1.Advancement> advs = Sets.newHashSetWithExpectedSize(best);
        final Map<MinecraftKey, AdvancementProgress> progs = Maps.newHashMapWithExpectedSize(best);

        for (Advancement advancement : advancements.values()) {
            advancement.onUpdate(uuid, advs, progs, pro, toRemove);
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

    public void updateEveryAdvancement(@NotNull Player player) {
        checkInitialisation();
        Validate.notNull(player, "Player is null.");

        TeamProgression pro = databaseManager.getProgression(player);

        final int best = advancements.size() + 16;

        final Set<MinecraftKey> toRemove = Sets.newHashSetWithExpectedSize(best);
        final Set<net.minecraft.server.v1_15_R1.Advancement> advs = Sets.newHashSetWithExpectedSize(best);
        final Map<MinecraftKey, AdvancementProgress> progs = Maps.newHashMapWithExpectedSize(best);

        UUID uuid = player.getUniqueId();

        for (Advancement advancement : advancements.values()) {
            advancement.onUpdate(uuid, advs, progs, pro, toRemove);
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

        for (Advancement adv : this.advancements.values()) {
            callValidation(adv);
        }

        initialised = true;
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
        this.advancements.clear();
        this.rootAdvancement = null;
    }

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

    public void showTab(@NotNull Player player) {
        checkInitialisation();
        Validate.notNull(player, "Player is null.");
        if (!players.containsKey(player)) {
            players.put(player, Collections.emptySet());
            updateEveryAdvancement(player);
        }
    }

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

    @Contract(pure = true, value = "null -> false")
    public boolean isShownTo(Player player) {
        checkInitialisation();
        return players.containsKey(player);
    }

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
}
