package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.commands.UltimateAdvancementAPICommand;
import dev.jorel.commandapi.CommandAPI;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvancementPlugin extends JavaPlugin {

    @Getter
    private static AdvancementPlugin instance;
    @Getter
    private AdvancementMain main;

    @Override
    public void onLoad() {
        instance = this;
        main = new AdvancementMain(this);
        main.load();
        CommandAPI.onLoad(false);
        new UltimateAdvancementAPICommand(main).register();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable(this);

        ConfigManager configManager = new ConfigManager(this);
        configManager.saveDefault(false);
        configManager.loadVariables();
        configManager.enable(main);
    }

    @Override
    public void onDisable() {
        main.disable();
        main = null;
    }
}
