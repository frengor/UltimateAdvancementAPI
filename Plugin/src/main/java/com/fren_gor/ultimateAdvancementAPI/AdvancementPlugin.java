package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager;
import com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager.ILoadable;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidVersionException;
import com.fren_gor.ultimateAdvancementAPI.metrics.BStats;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;

public class AdvancementPlugin extends JavaPlugin {

    /**
     * Spigot resource id
     */
    private static final int RESOURCE_ID = 95585;

    private static AdvancementPlugin instance;

    private AdvancementMain main;
    private ConfigManager configManager;
    private boolean correctVersion = true, commandsEnabled = false;
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
        if (commandAPIManager != null) { // In case commands couldn't be loaded
            try {
                commandAPIManager.onLoad(main, this);
            } catch (Throwable t) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[UltimateAdvancementAPI] An exception occurred while loading commands for UltimateAdvancementAPI, continuing without them.");
                getLogger().log(Level.WARNING, "An exception occurred while loading commands for UltimateAdvancementAPI", t);
                commandAPIManager = null;
            }
        }
    }

    @Override
    public void onEnable() {
        if (!correctVersion) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(this);
        configManager.saveDefault(false);
        if (configManager.loadVariables()) {
            main.disable();
            configManager = null;
            return;
        }
        try {
            configManager.enable(main);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[UltimateAdvancementAPI] An exception occurred while enabling UltimateAdvancementAPI.");
            getLogger().log(Level.SEVERE, "An exception occurred while enabling UltimateAdvancementAPI", e);
            // main.disable() is already called by AdvancementMain#failEnable(Exception)
            return;
        }

        if (commandAPIManager != null) { // In case commands couldn't be loaded
            try {
                commandAPIManager.onEnable();
                commandsEnabled = true;
            } catch (Throwable t) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[UltimateAdvancementAPI] An exception occurred while enabling commands for UltimateAdvancementAPI, continuing without them.");
                getLogger().log(Level.WARNING, "An exception occurred while enabling commands for UltimateAdvancementAPI", t);
            }
        }

        if (configManager.getDisableVanillaAdvancements() || configManager.getDisableVanillaRecipeAdvancements()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        VanillaAdvancementDisablerWrapper.disableVanillaAdvancements(configManager.getDisableVanillaAdvancements(), configManager.getDisableVanillaRecipeAdvancements());
                    } catch (Exception e) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[UltimateAdvancementAPI] Cannot disable vanilla advancements.");
                        getLogger().log(Level.WARNING, "Cannot disable vanilla advancements", e);
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
        if (commandAPIManager != null && commandsEnabled) { // In case commands are not loaded/enabled
            try {
                commandAPIManager.onDisable();
            } catch (Throwable t) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[UltimateAdvancementAPI] An exception occurred while disabling commands for UltimateAdvancementAPI:");
                getLogger().log(Level.WARNING, "An exception occurred while disabling commands for UltimateAdvancementAPI", t);
            }
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
                            getLogger().info("A new version of " + this.getDescription().getName() + " is out! Download it at https://modrinth.com/plugin/ultimateadvancementapi");
                        });
                    }
                }
            } catch (Exception e) {
                AdvancementUtils.runSync(this, () -> {
                    getLogger().info("Cannot look for updates: " + e.getMessage());
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

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
