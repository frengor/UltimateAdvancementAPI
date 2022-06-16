package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v8_3_1;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager.*;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class CommandAPIManager_v8_3_1 implements ILoadable {

    public CommandAPIManager_v8_3_1() {
    }

    @Override
    public void onLoad(@NotNull AdvancementMain main) {
        CommandAPI.onLoad(new CommandAPIConfig().verboseOutput(false).silentLogs(true));

        new UltimateAdvancementAPICommand_v8_3_1(main).register();
    }

    @Override
    public void onEnable(@NotNull Plugin plugin) {
        CommandAPI.onEnable(plugin);
    }

    @Override
    public void onDisable(@NotNull Plugin plugin) {
        CommandAPI.unregister("ultimateadvancementapi");
    }
}
