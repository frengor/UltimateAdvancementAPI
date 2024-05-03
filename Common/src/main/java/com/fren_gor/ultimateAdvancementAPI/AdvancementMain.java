package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.exceptions.AsyncExecutionException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DuplicatedException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidVersionException;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import com.google.common.base.Preconditions;
import net.byteflux.libby.BukkitLibraryManager;
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
import org.jetbrains.annotations.Range;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;

/**
 * Main class of the API. It is used to instantiate the API.
 */
public final class AdvancementMain {

    // Don't use AdvancementUtils here until having checked that the current mc version is supported

    private final static AtomicBoolean LOADED = new AtomicBoolean(false), ENABLED = new AtomicBoolean(false), INVALID_VERSION = new AtomicBoolean(false);

    private final Plugin owningPlugin;
    private EventManager eventManager;
    private DatabaseManager databaseManager;
    private BukkitLibraryManager libbyManager;
    private final String libFolder;
    private final Map<String, AdvancementTab> tabs = new HashMap<>();
    private final Map<Plugin, List<AdvancementTab>> pluginMap = new HashMap<>();

    /**
     * Creates a new {@code AdvancementMain}.
     * <p>The library folder is {@code "plugins/pluginDirectory/.libs"}.
     * Use {@link #AdvancementMain(Plugin, String)} to customize.
     *
     * @param owningPlugin The plugin instantiating the API.
     */
    public AdvancementMain(@NotNull Plugin owningPlugin) {
        // Don't use AdvancementUtils here until having checked that the current mc version is supported
        Preconditions.checkNotNull(owningPlugin, "Plugin is null.");
        this.owningPlugin = owningPlugin;
        this.libFolder = ".libs";
    }

    /**
     * Creates a new {@code AdvancementMain}.
     *
     * @param owningPlugin The plugin instantiating the API.
     * @param libFolder The name of the folder when additional libraries will be stored into.
     *         The folder is created into the plugin directory.
     */
    public AdvancementMain(@NotNull Plugin owningPlugin, String libFolder) {
        // Don't use AdvancementUtils here until having checked that the current mc version is supported
        Preconditions.checkNotNull(owningPlugin, "Plugin is null.");
        Preconditions.checkNotNull(libFolder, "Lib folder is null.");
        this.owningPlugin = owningPlugin;
        this.libFolder = libFolder;
    }

    /**
     * Loads the API.
     * <p><strong>Cannot be called twice</strong> until {@link #disable()} is called.
     *
     * @throws InvalidVersionException If the minecraft version in use is not supported by this API version.
     * @throws IllegalStateException If it is called at an invalid moment.
     */
    public void load() throws InvalidVersionException {
        checkSync(); // This is not the AdvancementUtils' one, since AdvancementUtils cannot be used before checking that the current mc version is supported
        if (!LOADED.compareAndSet(false, true)) {
            throw new IllegalStateException("UltimateAdvancementAPI is getting loaded twice.");
        }
        // Check mc version

        Optional<String> version = Versions.getNMSVersion();

        if (version.isEmpty()) {
            INVALID_VERSION.set(true);
            String fancy = Versions.getSupportedNMSVersions().stream().map(Versions::getNMSVersionsRange).collect(Collectors.joining(", ", "[", "]"));
            throw new InvalidVersionException(fancy, ReflectionUtil.MINECRAFT_VERSION, "Invalid minecraft version, couldn't load UltimateAdvancementAPI. Supported versions are " + fancy + '.');

        }

        libbyManager = new BukkitLibraryManager(owningPlugin, libFolder);
        libbyManager.addMavenCentral();
    }

    /**
     * Enables the API using a SQLite database.
     * <p><strong>Must be called after {@link #load()} and cannot be called twice</strong> until {@link #disable()} is called.
     * Also, only one <i>enable</i> method can be called per loading.
     *
     * @param SQLiteDatabase The SQLite database file.
     * @throws RuntimeException If the enabling fails. It is a wrapper for the real exception.
     * @throws InvalidVersionException If the minecraft version in use is not supported by this API version.
     * @throws IllegalStateException If it is called at an invalid moment.
     */
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

    /**
     * Enables the API using a MySQL database.
     * <p><strong>Must be called after {@link #load()} and cannot be called twice</strong> until {@link #disable()} is called.
     * Also, only one <i>enable</i> method can be called per loading.
     *
     * @param username The username.
     * @param password The password.
     * @param databaseName The name of the database.
     * @param host The MySQL host.
     * @param port The MySQL port. Must be greater than zero.
     * @param poolSize The pool size. Must be greater than zero.
     * @param connectionTimeout The connection timeout. Must be greater or equal to 250.
     * @throws RuntimeException If the enabling fails. It is a wrapper for the real exception.
     * @throws IllegalStateException If it is called at an invalid moment.
     */
    public void enableMySQL(String username, String password, String databaseName, String host, @Range(from = 1, to = Integer.MAX_VALUE) int port, @Range(from = 1, to = Integer.MAX_VALUE) int poolSize, @Range(from = 250, to = Long.MAX_VALUE) long connectionTimeout) {
        commonEnablePreDatabase();

        try {
            // Run it sync to avoid using a not initialized database
            databaseManager = new DatabaseManager(this, username, password, databaseName, host, port, poolSize, connectionTimeout);
        } catch (Exception e) {
            failEnable(e);
        }

        commonEnablePostDatabase();
    }

    /**
     * Enables the API using an in-memory database.
     * <p><strong>Must be called after {@link #load()} and cannot be called twice</strong> until {@link #disable()} is called.
     * Also, only one <i>enable</i> method can be called per loading.
     *
     * @throws RuntimeException If the enabling fails. It is a wrapper for the real exception.
     * @throws InvalidVersionException If the minecraft version in use is not supported by this API version.
     * @throws IllegalStateException If it is called at an invalid moment.
     */
    public void enableInMemory() {
        commonEnablePreDatabase();

        try {
            // Run it sync to avoid using a not initialized database
            databaseManager = new DatabaseManager(this);
        } catch (Exception e) {
            failEnable(e);
        }

        commonEnablePostDatabase();
    }

    private void commonEnablePreDatabase() {
        checkSync(); // This is not the AdvancementUtils' one, since AdvancementUtils cannot be used before checking that the current mc version is supported
        if (INVALID_VERSION.get()) {
            throw new InvalidVersionException("Incorrect minecraft version. Couldn't enable UltimateAdvancementAPI.");
        }
        if (!LOADED.get()) {
            throw new IllegalStateException("UltimateAdvancementAPI is not loaded.");
        }
        if (!owningPlugin.isEnabled()) {
            throw new IllegalStateException(owningPlugin.getName() + " is not enabled, cannot enable UltimateAdvancementAPI.");
        }
        if (!ENABLED.compareAndSet(false, true)) {
            throw new IllegalStateException("UltimateAdvancementAPI is getting enabled twice.");
        }

        //libbyManager = new BukkitLibraryManager(owningPlugin, libFolder);
        //libbyManager.addMavenCentral();

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
        disable(); // Disable everything we set up before failing
        throw new RuntimeException("Exception setting up database.", e);
    }

    /**
     * Disables the API.
     * <p>If not called after an <i>enable</i> method or {@link #load()} no action is done.
     *
     * @throws InvalidVersionException If the minecraft version in use is not supported by this API version.
     */
    public void disable() {
        checkSync(); // This is not the AdvancementUtils' one, since AdvancementUtils cannot be used before checking that the current mc version is supported
        if (INVALID_VERSION.get()) {
            throw new InvalidVersionException("Incorrect minecraft version. Couldn't disable UltimateAdvancementAPI.");
        }
        if (!LOADED.compareAndSet(true, false)) {
            // Old code
            // throw new IllegalStateException("UltimateAdvancementAPI is not loaded.");

            return; // Don't do anything if API is already disabled
        }

        UltimateAdvancementAPI.main = null;

        if (ENABLED.getAndSet(false)) {
            if (eventManager != null)
                eventManager.disable();
            pluginMap.clear();
            Iterator<AdvancementTab> it = tabs.values().iterator();
            while (it.hasNext()) {
                try {
                    AdvancementTab tab = it.next();
                    if (tab.isActive()) {
                        tab.dispose();
                    }
                    it.remove();
                } catch (Exception t) {
                    t.printStackTrace();
                }
            }
            if (databaseManager != null)
                databaseManager.unregister();
        }
    }

    /**
     * Creates a new {@link AdvancementTab} with the provided namespace. The namespace must be unique.
     *
     * @param plugin The owner of the new tab.
     * @param namespace The unique namespace of the tab.
     * @return The new {@link AdvancementTab}.
     * @throws DuplicatedException If another tab with the name already exists.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#createAdvancementTab(String)
     */
    @NotNull
    @Contract("_, _ -> new")
    public AdvancementTab createAdvancementTab(@NotNull Plugin plugin, @NotNull String namespace) throws DuplicatedException {
        checkInitialisation();
        Preconditions.checkNotNull(plugin, "Plugin is null.");
        Preconditions.checkNotNull(namespace, "Namespace is null.");
        if (tabs.containsKey(namespace)) {
            throw new DuplicatedException("An AdvancementTab with '" + namespace + "' namespace already exists.");
        }

        AdvancementTab tab = new AdvancementTab(plugin, databaseManager, namespace);
        tabs.put(namespace, tab);
        pluginMap.computeIfAbsent(plugin, p -> new LinkedList<>()).add(tab);
        return tab;
    }

    /**
     * Gets an advancement tab by its namespace.
     *
     * @param namespace The namespace of the advancement tab.
     * @return The advancement tab with the provided namespace, or {@code null} if there isn't any tab with such namespace.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#getAdvancementTab(String)
     */
    @Nullable
    public AdvancementTab getAdvancementTab(@NotNull String namespace) {
        checkInitialisation();
        Preconditions.checkNotNull(namespace, "Namespace is null.");
        return tabs.get(namespace);
    }

    /**
     * Returns whether an advancement tab with the provided namespace has already been registered.
     *
     * @param namespace The namespace of the advancement tab.
     * @return Whether an advancement tab with the provided namespace has already been registered.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#isAdvancementTabRegistered(String)
     */
    public boolean isAdvancementTabRegistered(@NotNull String namespace) {
        checkInitialisation();
        Preconditions.checkNotNull(namespace, "Namespace is null.");
        return tabs.containsKey(namespace);
    }

    /**
     * Returns an unmodifiable {@link Collection} of the advancement tabs registered by the provided plugin.
     *
     * @param plugin The plugin that owns the returned advancement tabs.
     * @return An unmodifiable {@link Collection} of the advancement tabs registered by the provided plugin.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#getPluginAdvancementTabs()
     */
    @UnmodifiableView
    @NotNull
    public Collection<@NotNull AdvancementTab> getPluginAdvancementTabs(@NotNull Plugin plugin) {
        checkInitialisation();
        Preconditions.checkNotNull(plugin, "Plugin is null.");
        return Collections.unmodifiableCollection(pluginMap.getOrDefault(plugin, Collections.emptyList()));
    }

    /**
     * Unregisters an advancement tab.
     *
     * @param namespace The namespace of the advancement tab to be unregistered.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#unregisterAdvancementTab(String)
     */
    public void unregisterAdvancementTab(@NotNull String namespace) {
        checkInitialisation();
        Preconditions.checkNotNull(namespace, "Namespace is null.");
        AdvancementTab tab = tabs.remove(namespace);
        if (tab != null)
            tab.dispose();
    }

    /**
     * Unregisters all the advancement tabs owned by the provided plugin.
     *
     * @param plugin The plugin that owns the advancement tabs to be unregistered.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#unregisterPluginAdvancementTabs()
     */
    public void unregisterAdvancementTabs(@NotNull Plugin plugin) {
        checkInitialisation();
        Preconditions.checkNotNull(plugin, "Plugin is null.");
        List<AdvancementTab> tabs = pluginMap.remove(plugin);
        if (tabs != null) {
            for (AdvancementTab t : tabs)
                unregisterAdvancementTab(t.getNamespace());
        }
    }

    /**
     * Returns the advancement with the provided namespaced key.
     *
     * @param namespacedKey The namespaced key of the advancement. It must be in the format {@code "namespace:key"}.
     * @return The advancement with the provided namespaced key., or {@code null} if it doesn't exist.
     * @throws IllegalStateException If the API is not enabled.
     * @throws IllegalArgumentException If the namespaced key is malformed.
     * @see UltimateAdvancementAPI#getAdvancement(String)
     */
    @Nullable
    public Advancement getAdvancement(@NotNull String namespacedKey) {
        checkInitialisation();
        int colon = namespacedKey.indexOf(':');
        if (colon <= 0 || colon == namespacedKey.length() - 1) {
            throw new IllegalArgumentException("Malformed namespaced key '" + namespacedKey + "'");
        }
        return getAdvancement(namespacedKey.substring(0, colon), namespacedKey.substring(colon + 1));
    }

    /**
     * Returns the advancement with the provided namespaced key.
     *
     * @param namespace The namespace of the advancement's tab.
     * @param key The key of the advancement.
     * @return The advancement with the provided namespaced key, or {@code null} if it doesn't exist.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#getAdvancement(String, String)
     */
    @Nullable
    public Advancement getAdvancement(@NotNull String namespace, @NotNull String key) {
        checkInitialisation();
        Preconditions.checkNotNull(namespace, "Namespace is null.");
        Preconditions.checkNotNull(key, "Key is null.");
        return getAdvancement(new AdvancementKey(namespace, key));
    }

    /**
     * Returns the advancement with the provided namespaced key.
     *
     * @param namespacedKey The {@link AdvancementKey} of the advancement.
     * @return The advancement with the provided namespaced key, or {@code null} if it doesn't exist.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#getAdvancement(AdvancementKey)
     */
    @Nullable
    public Advancement getAdvancement(@NotNull AdvancementKey namespacedKey) {
        checkInitialisation();
        Preconditions.checkNotNull(namespacedKey, "AdvancementKey is null.");
        AdvancementTab tab = tabs.get(namespacedKey.getNamespace());
        if (tab == null || !tab.isActive())
            return null;
        return tab.getAdvancement(namespacedKey);
    }

    /**
     * Returns an unmodifiable {@link Set} containing the namespaces of every registered advancement tab.
     *
     * @return An unmodifiable {@link Set} containing the namespaces of every registered advancement tab.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#getAdvancementTabNamespaces()
     */
    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public Set<@NotNull String> getAdvancementTabNamespaces() {
        checkInitialisation();
        return Collections.unmodifiableSet(tabs.keySet());
    }

    /**
     * Returns the namespaced keys of every registered advancement which namespaced key starts with the provided one.
     *
     * @param input The partial namespaced key that acts as a filter.
     * @return A filtered {@link List} that contains only the matching namespaced keys of the registered advancements.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#filterNamespaces(String)
     */
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

    /**
     * Returns an unmodifiable {@link Collection} of every registered advancement tab.
     *
     * @return An unmodifiable {@link Collection} of every registered advancement tab.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#getTabs()
     */
    @UnmodifiableView
    @NotNull
    public Collection<@NotNull AdvancementTab> getTabs() {
        checkInitialisation();
        return Collections.unmodifiableCollection(tabs.values());
    }

    /**
     * Updates every advancement to a player.
     * <p>An advancement is updated only if its tab is shown to the player (see {@link AdvancementTab#isShownTo(Player)}).
     *
     * @param player The player to be updated.
     * @throws IllegalStateException If the API is not enabled.
     * @see UltimateAdvancementAPI#updatePlayer(Player)
     */
    public void updatePlayer(@NotNull Player player) {
        checkInitialisation();
        Preconditions.checkNotNull(player, "Player is null.");
        for (AdvancementTab tab : tabs.values()) {
            if (tab.isActive() && tab.isShownTo(player)) {
                tab.updateAdvancementsToTeam(player);
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

    /**
     * Gets the plugin that instantiated the API.
     *
     * @return The plugin that instantiated the API.
     */
    @NotNull
    public Plugin getOwningPlugin() {
        return owningPlugin;
    }

    /**
     * Gets the {@link EventManager} API global instance.
     *
     * @return The {@link EventManager} API global instance.
     */
    @NotNull
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Gets the {@link DatabaseManager}.
     *
     * @return The {@link DatabaseManager}.
     */
    @NotNull
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Gets the libby manager.
     * <p>Libby is a library to handle dependencies at runtime. <a href="https://github.com/AlessioDP/libby">Check it out here.</a>
     *
     * @return The libby manager.
     */
    @NotNull
    public BukkitLibraryManager getLibbyManager() {
        return libbyManager;
    }

    /**
     * Returns whether the API is loaded.
     *
     * @return Whether the API is loaded.
     */
    public static boolean isLoaded() {
        return LOADED.get() && !INVALID_VERSION.get();
    }

    /**
     * Returns whether the API is enabled.
     *
     * @return Whether the API is enabled.
     */
    public static boolean isEnabled() {
        return ENABLED.get();
    }

    // Shortcuts for JavaPlugin methods

    /**
     * Gets the owning plugin {@link Logger}.
     * <p>This method is equivalent to {@code getOwningPlugin().getLogger()}.
     *
     * @return The owning plugin {@link Logger}.
     */
    @NotNull
    public Logger getLogger() {
        return owningPlugin.getLogger();
    }

    /**
     * Gets the owning plugin data folder.
     * <p>This method is equivalent to {@code getOwningPlugin().getDataFolder()}.
     *
     * @return The owning plugin data folder.
     */
    public File getDataFolder() {
        return owningPlugin.getDataFolder();
    }

    // Just a copy-paste from AdvancementUtils to avoid loading it before checking that the current mc version is supported
    private static void checkSync() {
        if (!Bukkit.isPrimaryThread())
            throw new AsyncExecutionException("Illegal async method call. This method can be called only from the main thread.");
    }
}
