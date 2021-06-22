package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.commands.UltimateAdvancementAPICommand;
import dev.jorel.commandapi.CommandAPI;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvancementPlugin extends JavaPlugin {

    @Getter
    private static AdvancementPlugin instance;
    private AdvancementMain main;
    private ConfigManager configManager;

    @Override
    public void onLoad() {
        instance = this;
        main = new AdvancementMain(this);
        main.load();
        CommandAPI.onLoad(false);
        new UltimateAdvancementAPICommand(main).register();
        configManager = new ConfigManager();
    }

    @Override
    public void onEnable() {
        configManager.enableDB(main);
    }

    @Override
    public void onDisable() {
        main.disable();
        main = null;
    }
}
