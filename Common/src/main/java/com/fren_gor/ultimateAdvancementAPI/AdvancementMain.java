package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DuplicatedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidVersionException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import lombok.Getter;
import net.byteflux.libby.BukkitLibraryManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;

public final class AdvancementMain {

    private final static AtomicBoolean LOADED = new AtomicBoolean(false), ENABLED = new AtomicBoolean(false), INVALID_VERSION = new AtomicBoolean(false);

    @Getter
    private final Plugin owningPlugin;
    @Getter
    private EventManager eventManager;
    @Getter
    private DatabaseManager databaseManager;
    @Getter
    private BukkitLibraryManager libbyManager;
    private final String libFolder;
    private final Map<String, AdvancementTab> tabs = new HashMap<>();
    private final Map<Plugin, List<AdvancementTab>> pluginMap = new HashMap<>();

    public AdvancementMain(@NotNull Plugin owningPlugin) {
        Validate.notNull(owningPlugin, "Plugin is null.");
        this.owningPlugin = owningPlugin;
        this.libFolder = ".libs";
    }

    public AdvancementMain(@NotNull Plugin owningPlugin, String libFolder) {
        Validate.notNull(owningPlugin, "Plugin is null.");
        Validate.notNull(libFolder, "Lib folder is null.");
        this.owningPlugin = owningPlugin;
        this.libFolder = libFolder;
    }

    public void load() throws InvalidVersionException {
        checkPrimaryThread();
        if (!LOADED.compareAndSet(false, true)) {
            throw new IllegalStateException("UltimateAdvancementAPI is getting loaded twice.");
        }
        // Check mc version
        final String actual = Versions.getNMSVersion();
        if (!Versions.getSupportedNMSVersions().contains(actual)) {
            INVALID_VERSION.set(true);
            String fancy = Versions.getSupportedNMSVersions().stream().map(Versions::getNMSVersionsRange).collect(Collectors.joining(", ", "[", "]"));
            throw new InvalidVersionException(fancy, actual, "Invalid minecraft version, couldn't load UltimateAdvancementAPI. Supported versions are " + fancy + '.');
        }
    }

    public void enableSQLite(File SQLiteDatabase) {
        commonEnablePreDatabase();

        try {
            // Run it sync to avoid using a not initialized database
            databaseManager = new DatabaseManager(this, SQLiteDatabase);
        } catch (Exception e) {
            failEnable(e);
        }

        commonEnablePostDatabase();
    }

    public void enableMySQL(String username, String password, String databaseName, String host, int port, int poolSize, long connectionTimeout) {
        commonEnablePreDatabase();

        try {
            // Run it sync to avoid using a not initialized database
            databaseManager = new DatabaseManager(this, username, password, databaseName, host, port, poolSize, connectionTimeout);
        } catch (Exception e) {
            failEnable(e);
        }

        commonEnablePostDatabase();
    }

    private void commonEnablePreDatabase() {
        checkPrimaryThread();
        if (INVALID_VERSION.get()) {
            throw new InvalidVersionException("Incorrect minecraft version. Couldn't enable UltimateAdvancementAPI.");
        }
        if (!isLoaded()) {
            throw new IllegalStateException("UltimateAdvancementAPI is not loaded.");
        }
        if (!owningPlugin.isEnabled()) {
            throw new IllegalStateException(owningPlugin.getName() + " is not enabled, cannot enable UltimateAdvancementAPI.");
        }
        if (!ENABLED.compareAndSet(false, true)) {
            throw new IllegalStateException("UltimateAdvancementAPI is getting enabled twice.");
        }

        libbyManager = new BukkitLibraryManager(owningPlugin, libFolder);
        libbyManager.addMavenCentral();

        eventManager = new EventManager(owningPlugin);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
    }

    private void commonEnablePostDatabase() {
        eventManager.register(this, PluginDisableEvent.class, EventPriority.HIGHEST, e -> unregisterAdvancementTabs(e.getPlugin()));

        // Resend advancements if /minecraft:reload is called
        eventManager.register(this, ServerCommandEvent.class, e -> {
            if (isMcReload(e.getCommand()))
                runSync(this, 20, () -> Bukkit.getOnlinePlayers().forEach(this::updatePlayer));
        });
        eventManager.register(this, PlayerCommandPreprocessEvent.class, e -> {
            if (isMcReload(e.getMessage()))
                runSync(this, 20, () -> Bukkit.getOnlinePlayers().forEach(this::updatePlayer));
        });

        UltimateAdvancementAPI.main = this;
    }

    @Contract("_ -> fail")
    private void failEnable(Exception e) {
        e.printStackTrace();
        Bukkit.getPluginManager().disablePlugin(owningPlugin);
        throw new RuntimeException("Exception setting up database.", e);
    }

    public void disable() {
        checkPrimaryThread();
        if (INVALID_VERSION.get()) {
            throw new InvalidVersionException("Incorrect minecraft version. Couldn't disable UltimateAdvancementAPI.");
        }
        checkInitialisation();
        LOADED.set(false);
        ENABLED.set(false);

        UltimateAdvancementAPI.main = null;
        // eventManager is disabled automatically since pl is disabling.
        pluginMap.clear();
        Iterator<AdvancementTab> it = tabs.values().iterator();
        while (it.hasNext()) {
            try {
                AdvancementTab tab = it.next();
                if (tab.isActive()) {
                    tab.dispose();
                }
                it.remove();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (databaseManager != null)
            databaseManager.unregister();
    }

    @NotNull
    @Contract("_, _ -> new")
    public AdvancementTab createAdvancementTab(@NotNull Plugin plugin, @NotNull String namespace) throws DuplicatedException {
        checkInitialisation();
        Validate.notNull(plugin, "Plugin is null.");
        Validate.notNull(namespace, "Namespace is null.");
        if (tabs.containsKey(namespace)) {
            throw new DuplicatedException("An AdvancementTab with '" + namespace + "' namespace already exists.");
        }

        AdvancementTab tab = new AdvancementTab(plugin, databaseManager, namespace);
        tabs.put(namespace, tab);
        pluginMap.computeIfAbsent(plugin, p -> new LinkedList<>()).add(tab);
        return tab;
    }

    @Nullable
    public AdvancementTab getAdvancementTab(@NotNull String namespace) {
        checkInitialisation();
        Validate.notNull(namespace, "Namespace is null.");
        return tabs.get(namespace);
    }

    public boolean isAdvancementTabRegistered(@NotNull String namespace) {
        checkInitialisation();
        Validate.notNull(namespace, "Namespace is null.");
        return tabs.containsKey(namespace);
    }

    @UnmodifiableView
    @NotNull
    public Collection<@NotNull AdvancementTab> getPluginAdvancementTabs(@NotNull Plugin plugin) {
        checkInitialisation();
        Validate.notNull(plugin, "Plugin is null.");
        return Collections.unmodifiableCollection(pluginMap.getOrDefault(plugin, Collections.emptyList()));
    }

    public void unregisterAdvancementTab(@NotNull String namespace) {
        checkInitialisation();
        Validate.notNull(namespace, "Namespace is null.");
        AdvancementTab tab = tabs.remove(namespace);
        if (tab != null)
            tab.dispose();
    }

    public void unregisterAdvancementTabs(@NotNull Plugin plugin) {
        checkInitialisation();
        Validate.notNull(plugin, "Plugin is null.");
        List<AdvancementTab> tabs = pluginMap.remove(plugin);
        if (tabs != null) {
            for (AdvancementTab t : tabs)
                unregisterAdvancementTab(t.getNamespace());
        }
    }

    @Nullable
    public Advancement getAdvancement(@NotNull String namespacedKey) {
        checkInitialisation();
        int colon = namespacedKey.indexOf(':');
        if (colon <= 0 || colon == namespacedKey.length() - 1) {
            throw new IllegalArgumentException("Malformed NamespacedKey '" + namespacedKey + "'");
        }
        return getAdvancement(namespacedKey.substring(0, colon), namespacedKey.substring(colon + 1));
    }

    @Nullable
    public Advancement getAdvancement(@NotNull String namespace, @NotNull String key) {
        checkInitialisation();
        Validate.notNull(namespace, "Namespace is null.");
        Validate.notNull(key, "Key is null.");
        return getAdvancement(new AdvancementKey(namespace, key));
    }

    @Nullable
    public Advancement getAdvancement(@NotNull AdvancementKey namespacedKey) {
        checkInitialisation();
        Validate.notNull(namespacedKey, "AdvancementKey is null.");
        AdvancementTab tab = tabs.get(namespacedKey.getNamespace());
        if (tab == null || !tab.isActive())
            return null;
        return tab.getAdvancement(namespacedKey);
    }

    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public Set<@NotNull String> getAdvancementTabNamespaces() {
        checkInitialisation();
        return Collections.unmodifiableSet(tabs.keySet());
    }

    @NotNull
    @Contract(pure = true, value = "_ -> new")
    public List<@NotNull String> filterNamespaces(@Nullable String input) {
        checkInitialisation();
        List<String> l = new LinkedList<>();
        if (input == null || input.isEmpty()) {
            for (Entry<String, AdvancementTab> e : tabs.entrySet()) {
                if (e.getValue().isActive()) {
                    l.addAll(e.getValue().getAdvancementsAsStrings());
                }
            }
        } else {
            int index = input.indexOf(':');
            if (index != -1) {
                String sub = input.substring(0, index);
                for (Entry<String, AdvancementTab> e : tabs.entrySet()) {
                    if (e.getValue().isActive() && e.getKey().equals(sub)) {
                        for (String s : e.getValue().getAdvancementsAsStrings()) {
                            if (s.startsWith(input))
                                l.add(s);
                        }
                    }
                }
            } else {
                for (Entry<String, AdvancementTab> e : tabs.entrySet()) {
                    if (e.getValue().isActive() && e.getKey().startsWith(input)) {
                        l.addAll(e.getValue().getAdvancementsAsStrings());
                    }
                }
            }
        }
        return l;
    }

    @UnmodifiableView
    @NotNull
    public Collection<@NotNull AdvancementTab> getTabs() {
        checkInitialisation();
        return Collections.unmodifiableCollection(tabs.values());
    }

    public void updatePlayer(@NotNull Player player) {
        checkInitialisation();
        Validate.notNull(player, "Player is null.");
        for (AdvancementTab tab : tabs.values()) {
            if (tab.isActive() && tab.isShownTo(player)) {
                tab.updateEveryAdvancement(player);
            }
        }
    }

    private static void checkInitialisation() {
        if (!isLoaded() || !isEnabled()) {
            throw new IllegalStateException("UltimateAdvancementAPI is not enabled.");
        }
    }

    private static boolean isMcReload(@NotNull String command) {
        return command.startsWith("/minecraft:reload") || command.startsWith("minecraft:reload");
    }

    private static void checkPrimaryThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Executing thread is not main thread.");
        }
    }

    // Duplicated methods from JavaPlugin

    public @NotNull Logger getLogger() {
        return owningPlugin.getLogger();
    }

    public File getDataFolder() {
        return owningPlugin.getDataFolder();
    }

    public static boolean isLoaded() {
        return LOADED.get() && !INVALID_VERSION.get();
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }
}
