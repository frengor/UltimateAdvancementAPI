package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v8_4_0;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager.*;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class CommandAPIManager implements ILoadable {

    public CommandAPIManager() {
    }

    @Override
    public void onLoad(@NotNull AdvancementMain main) {
        CommandAPI.onLoad(new CommandAPIConfig().verboseOutput(false).silentLogs(true));

        new UltimateAdvancementAPICommand(main).register();
    }

    @Override
    public void onEnable(@NotNull Plugin plugin) {
        CommandAPI.onEnable(plugin);
    }

    @Override
    public void onDisable(@NotNull Plugin plugin) {
        for (var command : CommandAPI.getRegisteredCommands()) {
            CommandAPI.unregister(command.commandName());
            for (var alias : command.aliases())
                CommandAPI.unregister(alias);
        }
    }
}
