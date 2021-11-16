package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager;
import com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager.ILoadable;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidVersionException;
import com.fren_gor.ultimateAdvancementAPI.metrics.BStats;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class AdvancementPlugin extends JavaPlugin {

    /**
     * Spigot resource id
     */
    private static final int RESOURCE_ID = 95585;

    private static AdvancementPlugin instance;

    private AdvancementMain main;
    private boolean correctVersion = true;
    @Nullable
    private ILoadable commandAPIManager;

    @Override
    public void onLoad() {
        instance = this;
        main = new AdvancementMain(this);
        try {
            main.load();
        } catch (InvalidVersionException e) {
            ConsoleCommandSender sender = Bukkit.getConsoleSender();
            sender.sendMessage("§4================================================================================");
            sender.sendMessage("");
            sender.sendMessage("§cInvalid Minecraft Version!");
            if (e.getExpected() != null)
                sender.sendMessage("§eThis version of UltimateAdvancementAPI supports only " + e.getExpected());
            else
                sender.sendMessage("§eThis version of UltimateAdvancementAPI does not support your minecraft version");
            sender.sendMessage("§ePlease download and use the correct plugin version");
            sender.sendMessage("");
            sender.sendMessage("§4================================================================================");
            correctVersion = false;
            return;
        }

        commandAPIManager = CommandAPIManager.loadManager(main.getLibbyManager());
        if (commandAPIManager != null) // In case commands couldn't be loaded
            commandAPIManager.onLoad(main);
    }

    @Override
    public void onEnable() {
        if (!correctVersion) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (commandAPIManager != null) // In case commands couldn't be loaded
            commandAPIManager.onEnable(this);

        ConfigManager configManager = new ConfigManager(this);
        configManager.saveDefault(false);
        configManager.loadVariables();
        configManager.enable(main);

        if (configManager.getDisableVanillaAdvancements()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        AdvancementUtils.disableVanillaAdvancements();
                    } catch (Exception e) {
                        System.out.println("Couldn't disable vanilla advancements:");
                        e.printStackTrace();
                    }
                }
            }.runTaskLater(this, 20);
        }

        BStats.init(this);
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        if (!correctVersion) {
            return;
        }
        main.disable();
        main = null;
    }

    private void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID).openStream();
                 Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNextLine()) {
                    if (!this.getDescription().getVersion().equalsIgnoreCase(scanner.next())) {
                        AdvancementUtils.runSync(this, () -> {
                            this.getLogger().info("A new version of " + this.getDescription().getName() + " is out! Download it at https://www.spigotmc.org/resources/" + RESOURCE_ID);
                        });
                    }
                }
            } catch (Exception e) {
                AdvancementUtils.runSync(this, () -> {
                    this.getLogger().info("Cannot look for updates: " + e.getMessage());
                });
            }
        });
    }

    public static AdvancementPlugin getInstance() {
        return instance;
    }

    public AdvancementMain getMain() {
        return main;
    }
}
