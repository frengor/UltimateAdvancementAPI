package com.fren_gor.ultimateAdvancementAPI;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final File configFile;
    private final AdvancementPlugin plugin;
    private final YamlConfiguration config = new YamlConfiguration();

    // db parameters
    private String storageType;
    private String sqlLiteDbName;
    private String username;
    private String password;
    private String databaseName;
    private String host;
    private int port;
    private int poolSize;
    private long connectionTimeout;

    public ConfigManager(AdvancementPlugin plugin) {
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public void loadVariables() {

        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            System.out.println("An error occurred, shutting down " + plugin.getName() + '.');
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        storageType = config.getString("storage-type");
        if (storageType == null) {
            System.out.println("Could not find storage-type. Shutting down " + plugin.getName() + '.');
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        if (storageType.equalsIgnoreCase("SQLite")) {
            sqlLiteDbName = getOrDefault("sqlite.file", "database.db");
        } else if (storageType.equalsIgnoreCase("MySQL")) {
            username = getOrDefault("mysql.username", "root");
            password = getOrDefault("mysql.password", "");
            databaseName = getOrDefault("mysql.databaseName", "advancements");
            host = getOrDefault("mysql.host", "127.0.0.1");
            port = getOrDefault("mysql.port", 3306);
            poolSize = getOrDefault("mysql.advanced-settings.poolSize", 5);
            connectionTimeout = getOrDefault("mysql.advanced-settings.connectionTimeout", 6000L);
        } else {
            System.out.println("Invalid storage-type \"" + storageType + "\". Shutting down " + plugin.getName() + '.');
            Bukkit.getPluginManager().disablePlugin(plugin);
            return; // Keep to avoid future issues if code will be added below
        }

    }

    public void saveDefault(boolean replace) {
        plugin.saveResource("config.yml", replace);
    }

    public void enable(AdvancementMain main) {
        Validate.notNull(storageType, "Config has not been loaded.");

        if (storageType.equalsIgnoreCase("SQLite")) {
            main.enable(new File(plugin.getDataFolder(), sqlLiteDbName));
        } else if (storageType.equalsIgnoreCase("MySQL")) {
            main.enable(username, password, databaseName, host, port, poolSize, connectionTimeout);
        } // else case already handled in loadVariables()
    }

    private String getOrDefault(@NotNull String path, @NotNull String def) {
        Object obj = config.get(path);
        return obj instanceof String ? (String) obj : def;
    }

    private Integer getOrDefault(@NotNull String path, @NotNull Integer def) {
        Object obj = config.get(path);
        return obj instanceof Integer ? (Integer) obj : def;
    }

    private Long getOrDefault(@NotNull String path, @NotNull Long def) {
        Object obj = config.get(path);
        return obj instanceof Long ? (Long) obj : def;
    }
}