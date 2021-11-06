package com.fren_gor.ultimateAdvancementAPI.commands;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandAPIManager {

    @Nullable
    public static ILoadable loadManager(LibraryManager libbyManager) {
        CommandAPIVersion ver = CommandAPIVersion.getVersionToLoad(Versions.getNMSVersion());
        if (ver == null) {
            // Skip code down below if nms version is invalid
            return null;
        }

        // Download correct version of CommandAPI
        libbyManager.addJitPack();
        Library commandAPILibrary = Library.builder()
                .groupId("dev{}jorel{}CommandAPI")
                .artifactId("commandapi-shade")
                .version(ver.getVersion()).checksum(ver.getChecksum())
                .relocate("dev{}jorel{}commandapi", "dev.jorel.commandapi") // Should be changed by shading
                .build();
        try {
            libbyManager.loadLibrary(commandAPILibrary);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[UltimateAdvancementAPI-Commands] Can't load library " + commandAPILibrary.toString() + '!');
            e.printStackTrace();
            return null;
        }

        String manager = "com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v" + ver.getClasspathSuffix() + ".CommandAPIManager_v" + ver.getClasspathSuffix();
        Class<?> clazz;
        try {
            clazz = Class.forName(manager);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().info("[UltimateAdvancementAPI-Commands] Can't find CommandAPIManager Class! (" + manager + ")");
            e.printStackTrace();
            return null;
        }

        try {
            return (ILoadable) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface ILoadable {

        void onLoad(@NotNull AdvancementMain main);

        void onEnable(@NotNull Plugin plugin);
    }
}
