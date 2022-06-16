package com.fren_gor.ultimateAdvancementAPI.commands;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import com.google.common.base.Preconditions;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a> manager, which loads the correct version of the API
 * and the correct implementation of the commands.
 */
public class CommandAPIManager {

    public static final String PERMISSION_MAIN_COMMAND = "ultimateadvancementapi.command";
    public static final String PERMISSION_PROGRESSION = "ultimateadvancementapi.progression";
    public static final String PERMISSION_PROGRESSION_GET = "ultimateadvancementapi.progression.get";
    public static final String PERMISSION_PROGRESSION_SET = "ultimateadvancementapi.progression.set";
    public static final String PERMISSION_GRANT = "ultimateadvancementapi.grant";
    public static final String PERMISSION_GRANT_ALL = "ultimateadvancementapi.grant.all";
    public static final String PERMISSION_GRANT_TAB = "ultimateadvancementapi.grant.tab";
    public static final String PERMISSION_GRANT_ONE = "ultimateadvancementapi.grant.one";
    public static final String PERMISSION_REVOKE = "ultimateadvancementapi.revoke";
    public static final String PERMISSION_REVOKE_ALL = "ultimateadvancementapi.revoke.all";
    public static final String PERMISSION_REVOKE_TAB = "ultimateadvancementapi.revoke.tab";
    public static final String PERMISSION_REVOKE_ONE = "ultimateadvancementapi.revoke.one";

    /**
     * Loads the correct version of the API and the correct implementation of the commands.
     *
     * @param libbyManager The {@link LibraryManager} lo load the <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a>.
     * @return The {@link ILoadable} to be loaded and enabled, or {@code null} if the NMS version is not supported.
     */
    @Nullable
    public static ILoadable loadManager(@NotNull LibraryManager libbyManager) {
        Preconditions.checkNotNull(libbyManager, "LibraryManager is null.");
        CommandAPIVersion ver = CommandAPIVersion.getVersionToLoad(Versions.getNMSVersion());
        if (ver == null) {
            // Skip code down below if nms version is invalid
            return null;
        }

        // Download correct version of CommandAPI
        libbyManager.addMavenCentral();
        Library commandAPILibrary = Library.builder()
                .groupId("dev{}jorel")
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
            return new CommonLoadable((ILoadable) clazz.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Interface implemented by loadable object.
     */
    public interface ILoadable {

        /**
         * Loads the loadable object. Should be called into {@link Plugin#onLoad()}.
         *
         * @param main Already loaded {@link AdvancementMain} instance.
         */
        void onLoad(@NotNull AdvancementMain main);

        /**
         * Enables the loadable object. Should be called into {@link Plugin#onEnable()}.
         *
         * @param plugin The plugin which loaded the {@link AdvancementMain} passed to {@link #onLoad(AdvancementMain)}.
         */
        void onEnable(@NotNull Plugin plugin);

        /**
         * Disables the loadable object. Should be called into {@link Plugin#onDisable()}.
         *
         * @param plugin The plugin which loaded the {@link AdvancementMain} passed to {@link #onLoad(AdvancementMain)}.
         */
        void onDisable(@NotNull Plugin plugin);
    }

    private static final class CommonLoadable implements ILoadable {

        private final ILoadable loadable;
        private Plugin advancementMainOwner;
        private boolean enabled = false;

        public CommonLoadable(@NotNull ILoadable loadable) {
            Preconditions.checkNotNull(loadable, "ILoadable is null.");
            this.loadable = loadable;
        }

        @Override
        public void onLoad(@NotNull AdvancementMain main) {
            Preconditions.checkArgument(AdvancementMain.isLoaded(), "AdvancementMain is not loaded.");
            this.advancementMainOwner = main.getOwningPlugin();
            loadable.onLoad(main);
        }

        @Override
        public void onEnable(@NotNull Plugin plugin) {
            if (advancementMainOwner == null) {
                throw new IllegalStateException("Not loaded.");
            }
            Preconditions.checkArgument(plugin == advancementMainOwner, "AdvancementMain owning plugin isn't the provided Plugin.");
            Preconditions.checkArgument(plugin.isEnabled(), "Plugin isn't enabled.");
            enabled = true;
            loadable.onEnable(plugin);
        }

        @Override
        public void onDisable(@NotNull Plugin plugin) {
            if (advancementMainOwner == null) {
                throw new IllegalStateException("Not loaded and not enabled.");
            }
            if (!enabled) {
                throw new IllegalStateException("Not enabled.");
            }
            Preconditions.checkArgument(plugin == advancementMainOwner, "AdvancementMain owning plugin isn't the provided Plugin.");
            enabled = false;
            advancementMainOwner = null;
            loadable.onDisable(plugin);
        }
    }
}
