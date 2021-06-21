package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.commands.UltimateAdvancementAPICommand;
import dev.jorel.commandapi.CommandAPI;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AdvancementPlugin extends JavaPlugin {

    @Getter
    private static AdvancementPlugin instance;
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
        main.enable(new File(getDataFolder(), "database.db"));
        //main.enable("root", "", "database", "127.0.0.1", 3306, 10, 6000);
    }

    @Override
    public void onDisable() {
        main.disable();
        main = null;
    }
}
