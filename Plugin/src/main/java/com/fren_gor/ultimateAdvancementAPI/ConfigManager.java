package com.fren_gor.ultimateAdvancementAPI;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ConfigManager {

    // TODO Improve config system

    private final File configFile;
    private final AdvancementPlugin plugin;
    private final YamlConfiguration config = new YamlConfiguration();

    private boolean disableVanillaAdvancements;
    private boolean disableVanillaAdvancementsRecipes;
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
     * Load the config.
     *
     * @return {@code true} if the loading failed, {@code false} otherwise.
     */
    public boolean loadVariables() {
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "An error occurred loading the config file.");
            e.printStackTrace();
            return true;
        }

        disableVanillaAdvancements = config.getBoolean("disable-vanilla-advancements");
        disableVanillaAdvancementsRecipes = config.getBoolean("disable-vanilla-advancements-recipes");

        String type = config.getString("storage-type");
        if (type == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not find \"storage-type\".");
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
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Invalid storage type \"" + type + "\".");
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

        switch (storageType) {
            case SQLITE -> main.enableSQLite(new File(plugin.getDataFolder(), sqlLiteDbName));
            case MYSQL -> main.enableMySQL(username, password, databaseName, host, port, poolSize, connectionTimeout);
            case IN_MEMORY -> main.enableInMemory();
        }
    }

    private String getOrDefault(@NotNull String path, @NotNull String def) {
        return config.get(path) instanceof String s ? s : def;
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

    public boolean getDisableVanillaAdvancementsRecipes() {
        return disableVanillaAdvancementsRecipes;
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
}