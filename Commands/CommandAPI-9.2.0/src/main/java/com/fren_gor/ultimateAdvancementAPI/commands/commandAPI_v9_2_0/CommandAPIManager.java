package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v9_2_0;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager.*;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class CommandAPIManager implements ILoadable {

    public CommandAPIManager() {
    }

    @Override
    public void onLoad(@NotNull AdvancementMain main, @NotNull JavaPlugin plugin) {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin).verboseOutput(false).silentLogs(true));

        new UltimateAdvancementAPICommand(main).register();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
    }

    @Override
    public void onDisable() {
        for (var command : CommandAPI.getRegisteredCommands()) {
            CommandAPI.unregister(command.commandName());
            for (var alias : command.aliases())
                CommandAPI.unregister(alias);
        }
        CommandAPI.onDisable();
    }
}
