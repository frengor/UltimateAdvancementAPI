package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.commands.UltimateAdvancementAPICommand;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidVersionException;
import com.fren_gor.ultimateAdvancementAPI.metrics.BStats;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class AdvancementPlugin extends JavaPlugin {

    /**
     * Spigot resource id
     */
    private static final int RESOURCE_ID = 95585;

    @Getter
    private static AdvancementPlugin instance;
    @Getter
    private AdvancementMain main;
    private boolean correctVersion = true;

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
        CommandAPIConfig config = new CommandAPIConfig();
        config.setVerboseOutput(false);
        CommandAPI.onLoad(config);
        new UltimateAdvancementAPICommand(main).register();
    }

    @Override
    public void onEnable() {
        if (!correctVersion) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        CommandAPI.onEnable(this);

        ConfigManager configManager = new ConfigManager(this);
        configManager.saveDefault(false);
        configManager.loadVariables();
        configManager.enable(main);

        if (configManager.getDisableVanillaAdvancements()) {
            try {
                AdvancementUtils.disableVanillaAdvancements();
            } catch (Exception e) {
                System.out.println("Couldn't disable vanilla advancements:");
                e.printStackTrace();
            }
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
}
