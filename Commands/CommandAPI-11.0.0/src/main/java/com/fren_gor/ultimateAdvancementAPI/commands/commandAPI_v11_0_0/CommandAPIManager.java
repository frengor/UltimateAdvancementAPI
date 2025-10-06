package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v11_0_0;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager.*;
import com.fren_gor.ultimateAdvancementAPI.commands.MojangMappingsHandler;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.RegisteredCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandAPIManager implements ILoadable {

    public CommandAPIManager() {
    }

    @Override
    public void onLoad(@NotNull AdvancementMain main, @NotNull JavaPlugin plugin) {
        // Use CommandAPIPaperConfig on paper and Mojang mapped, CommandAPISpigotConfig on spigot

        CommandAPIBukkitConfig<?> config;
        try {
            if (MojangMappingsHandler.isMojangMapped()) {
                Class<?> lifecycleEventOwnerClass = Class.forName("io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner");
                Class<?> paperConfigClass = Class.forName("dev.jorel.commandapi.CommandAPIPaperConfig");
                config = (CommandAPIBukkitConfig<?>) paperConfigClass.getDeclaredConstructor(lifecycleEventOwnerClass).newInstance(plugin);
            } else {
                Class<?> spigotConfigClass = Class.forName("dev.jorel.commandapi.CommandAPISpigotConfig");
                config = (CommandAPIBukkitConfig<?>) spigotConfigClass.getDeclaredConstructor(JavaPlugin.class).newInstance(plugin);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create CommandAPI config", e);
        }

        CommandAPI.onLoad(config
                .verboseOutput(false)
                .silentLogs(true)
                .setNamespace(plugin.getName().toLowerCase(Locale.ENGLISH)) // Plugin names contain only latin characters present in english
        );

        new UltimateAdvancementAPICommand(main).register();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
    }

    @Override
    public void onDisable() {
        Stream.concat(
                CommandAPI.getRegisteredCommands().stream().map(RegisteredCommand::commandName),
                CommandAPI.getRegisteredCommands().stream().flatMap(cmd -> Arrays.stream(cmd.aliases()))
        ).distinct().forEach(command -> {
            CommandAPI.unregister(command, true);
        });
        CommandAPI.onDisable();
    }
}
