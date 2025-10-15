package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import com.fren_gor.ultimateAdvancementAPI.database.impl.MySQL;
import com.fren_gor.ultimateAdvancementAPI.database.impl.SQLite;
import com.google.common.base.Preconditions;
import net.byteflux.libby.Library;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ConfigManager {

    // TODO Improve config system

    public static final int CONFIG_VERSION = 2;
    public static final Library CONFIG_UPDATER = Library.builder()
            .groupId("com.tchristofferson")
            .artifactId("ConfigUpdater")
            .version("2.2")
            .checksum("3LHINAggl0kkNC3nROgEWBXVu0aiFlofmFjq5aNAi3A=")
            .build();

    private final File configFile;
    private final AdvancementPlugin plugin;
    private final YamlConfiguration config = new YamlConfiguration();

    private boolean disableVanillaAdvancements;
    private boolean disableVanillaRecipeAdvancements;

    // db parameters
    private DB_TYPE storageType;
    private String sqlLiteDbName;
    private String username;
    private String password;
    private String databaseName;
    private String host;
    private int port;
    private int poolSize;
    private long connectionTimeout;

    public ConfigManager(@NotNull AdvancementPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin is null.");
        configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    /**
     * Loads the config.
     *
     * @return {@code true} if the loading failed, {@code false} otherwise.
     */
    public boolean loadVariables() {
        try {
            loadAndUpdateConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load configuration", e);
            return true;
        }

        disableVanillaAdvancements = getOrDefault("disable-vanilla-advancements", false);
        disableVanillaRecipeAdvancements = getOrDefault("disable-vanilla-recipe-advancements", false);

        String type = config.getString("storage-type");
        if (type == null) {
            plugin.getLogger().log(Level.SEVERE, "Could not find \"storage-type\" in config.yml");
            return true;
        }

        if (type.equalsIgnoreCase("SQLite")) {
            storageType = DB_TYPE.SQLITE;
            sqlLiteDbName = getOrDefault("sqlite.file", "database.db");
        } else if (type.equalsIgnoreCase("MySQL")) {
            storageType = DB_TYPE.MYSQL;
            username = getOrDefault("mysql.username", "root");
            password = getOrDefault("mysql.password", "");
            databaseName = getOrDefault("mysql.databaseName", "advancements");
            host = getOrDefault("mysql.host", "127.0.0.1");
            port = getOrDefault("mysql.port", 3306);
            poolSize = getOrDefault("mysql.advanced-settings.poolSize", 5);
            connectionTimeout = getOrDefault("mysql.advanced-settings.connectionTimeout", 6000L);
        } else if (type.equalsIgnoreCase("InMemory")) {
            storageType = DB_TYPE.IN_MEMORY;
        } else {
            plugin.getLogger().log(Level.SEVERE, "Invalid storage type \"" + type + "\"");
            return true;
        }

        return false;
    }

    public void saveDefault(boolean replace) {
        if (!configFile.exists() || replace) {
            plugin.saveResource("config.yml", replace);
        }
    }

    public void enable(@NotNull AdvancementMain main) {
        Preconditions.checkNotNull(storageType, "Config has not been loaded.");

        main.enable(() -> switch (storageType) {
            case SQLITE -> new SQLite(main, new File(plugin.getDataFolder(), sqlLiteDbName));
            case MYSQL -> new MySQL(main, username, password, databaseName, host, port, poolSize, connectionTimeout);
            case IN_MEMORY -> new InMemory(main);
        });
    }

    private String getOrDefault(@NotNull String path, @NotNull String def) {
        return config.get(path) instanceof String s ? s : def;
    }

    private Boolean getOrDefault(@NotNull String path, @NotNull Boolean def) {
        return config.get(path) instanceof Boolean b ? b : def;
    }

    private Integer getOrDefault(@NotNull String path, @NotNull Integer def) {
        return config.get(path) instanceof Integer i ? i : def;
    }

    private Long getOrDefault(@NotNull String path, @NotNull Long def) {
        return config.get(path) instanceof Long l ? l : def;
    }

    public boolean getDisableVanillaAdvancements() {
        return disableVanillaAdvancements;
    }

    public boolean getDisableVanillaRecipeAdvancements() {
        return disableVanillaRecipeAdvancements;
    }

    public DB_TYPE getStorageType() {
        return storageType;
    }

    public enum DB_TYPE {
        SQLITE("SQLite"),
        MYSQL("MySQL"),
        IN_MEMORY("In Memory");

        private final String fancyName;

        DB_TYPE(@NotNull String fancyName) {
            this.fancyName = Objects.requireNonNull(fancyName);
        }

        @NotNull
        public String getFancyName() {
            return fancyName;
        }
    }

    private void loadAndUpdateConfig() throws Exception {
        loadConfig();

        int configVersion = getOrDefault("config-version", -1);
        if (configVersion != CONFIG_VERSION) {
            // CONFIG_UPDATER will always keep the old value of config-version.
            // Thus, we delete the field before updating the config as a workaround.
            // We could set it to CONFIG_VERSION, however that would prevent future updates in case updateConfig() fails
            try {
                config.set("config-version", null);
                config.save(configFile);
            } catch (Exception e) {
                throw new RuntimeException("Could not remove config-version from the configuration file.", e);
            }

            try {
                updateConfig();
            } catch (Exception e) {
                try {
                    config.set("config-version", configVersion);
                    config.save(configFile);
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.SEVERE, "Could not restore config-version to its previous value", ex);
                }
                throw new RuntimeException("Could not update the configuration file.", e);
            }
            loadConfig();
        }
    }

    private void loadConfig() {
        try {
            config.load(configFile);
        } catch (Exception e) {
            throw new RuntimeException("Could not load the configuration file.", e);
        }
    }

    private void updateConfig() throws Exception {
        var path = plugin.getMain().getLibbyManager().downloadLibrary(CONFIG_UPDATER);
        try (var cl = new URLClassLoader(new URL[]{path.toUri().toURL()}, this.getClass().getClassLoader())) {
            Class<?> updater = cl.loadClass("com.tchristofferson.configupdater.ConfigUpdater");
            // ConfigUpdater#update(Plugin plugin, String resourceName, File toUpdate, List<String> ignoredSections)
            var updateMethod = updater.getDeclaredMethod("update", Plugin.class, String.class, File.class, List.class);
            updateMethod.invoke(null, plugin, "config.yml", configFile, List.of());
        }
    }
}