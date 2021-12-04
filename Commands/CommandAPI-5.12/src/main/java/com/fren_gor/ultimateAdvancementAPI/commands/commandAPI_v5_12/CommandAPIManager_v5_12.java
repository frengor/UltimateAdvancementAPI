package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v5_12;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager.ILoadable;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class CommandAPIManager_v5_12 implements ILoadable {

    public CommandAPIManager_v5_12() {
    }

    @Override
    public void onLoad(@NotNull AdvancementMain main) {
        CommandAPIConfig config = new CommandAPIConfig();
        config.setVerboseOutput(false);
        CommandAPI.onLoad(config);

        new UltimateAdvancementAPICommand_v5_12(main).register();
    }

    @Override
    public void onEnable(@NotNull Plugin plugin) {
        CommandAPI.onEnable(plugin);
    }

    @Override
    public void onDisable(@NotNull Plugin plugin) {
    }
}
