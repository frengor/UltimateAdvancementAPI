package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.commands.UltimateAdvancementAPICommand;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidVersionException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import dev.jorel.commandapi.CommandAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvancementPlugin extends JavaPlugin {

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
    }

    @Override
    public void onDisable() {
        if (!correctVersion) {
            return;
        }
        main.disable();
        main = null;
    }
}
