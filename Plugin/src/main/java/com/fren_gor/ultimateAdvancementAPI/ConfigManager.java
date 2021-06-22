package com.fren_gor.ultimateAdvancementAPI;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    //files for configs
    private final File configFile = new File(AdvancementPlugin.getInstance().getDataFolder() + "/config.yml");
    private final YamlConfiguration config = new YamlConfiguration();

    //db parameters
    private boolean isSqlLite;
    private String sqlLiteDbName;
    private String username;
    private String password;
    private String databaseName;
    private String host;
    private int port;
    private int poolSize;
    private long connectionTimeout;

    public ConfigManager() {

        saveDefault(false);
        loadVariables();
    }

    public void loadVariables() {

        try {
            config.load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        isSqlLite = config.getBoolean("isSqlLite");
        sqlLiteDbName = config.getString("sqlDbName") == null ? "database.db" : config.getString("sqlDbName");
        username = config.getString("username") == null ? "root" : config.getString("username");
        password = config.getString("password") == null ? "" : config.getString("password");
        databaseName = config.getString("databaseName") == null ? "database" : config.getString("databaseName");
        host = config.getString("host") == null ? "root" : config.getString("127.0.0.1");
        port = config.getInt("port");
        poolSize = config.getInt("poolSize");
        connectionTimeout = config.getLong("connectionTimeout");

    }

    private void saveDefault(boolean replace) {
        AdvancementPlugin.getInstance().saveResource("config.yml", replace);

    }

    public void enableDB(AdvancementMain main) {

        if (isSqlLite) {
            main.enable(new File(AdvancementPlugin.getInstance().getDataFolder(), sqlLiteDbName));

        } else {

            main.enable(username, password, databaseName, host, port, poolSize, connectionTimeout);

        }

    }
}