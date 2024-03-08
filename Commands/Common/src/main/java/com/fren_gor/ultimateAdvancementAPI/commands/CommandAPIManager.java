package com.fren_gor.ultimateAdvancementAPI.commands;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import com.google.common.base.Preconditions;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

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
                .artifactId("commandapi-bukkit-shade")
                .version(ver.getVersion()).checksum(ver.getChecksum())
                .relocate("dev{}jorel{}commandapi", "dev.jorel.commandapi") // Should be changed by shading
                .build();
        try {
            libbyManager.loadLibrary(commandAPILibrary);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[UltimateAdvancementAPI-Commands] Cannot load library " + commandAPILibrary.toString() + '!', e);
            return null;
        }

        String manager = "com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v" + ver.getClasspathSuffix() + ".CommandAPIManager";
        Class<?> clazz;
        try {
            clazz = Class.forName(manager);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().log(Level.WARNING, "[UltimateAdvancementAPI-Commands] Cannot find CommandAPIManager Class! (" + manager + ")", e);
            return null;
        }

        try {
            return new CommonLoadable((ILoadable) clazz.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            Bukkit.getLogger().log(Level.WARNING, "[UltimateAdvancementAPI-Commands] Cannot load CommandAPIManager Class! (" + manager + ")", e);
            return null;
        }
    }

    /**
     * Interface implemented by loadable objects.
     */
    public interface ILoadable {

        /**
         * Loads the loadable object. Should be called into {@link Plugin#onLoad()}.
         *
         * @param main Already loaded {@link AdvancementMain} instance.
         * @param plugin The plugin which loaded the {@link AdvancementMain} instance.
         */
        void onLoad(@NotNull AdvancementMain main, @NotNull JavaPlugin plugin);

        /**
         * Enables the loadable object. Should be called into {@link Plugin#onEnable()}.
         */
        void onEnable();

        /**
         * Disables the loadable object. Should be called into {@link Plugin#onDisable()}.
         */
        void onDisable();
    }

    private static final class CommonLoadable implements ILoadable {

        private final ILoadable loadable;
        private JavaPlugin advancementMainOwner;
        private boolean enabled = false;

        public CommonLoadable(@NotNull ILoadable loadable) {
            Preconditions.checkNotNull(loadable, "ILoadable is null.");
            this.loadable = loadable;
        }

        @Override
        public void onLoad(@NotNull AdvancementMain main, @NotNull JavaPlugin plugin) {
            Preconditions.checkNotNull(plugin, "JavaPlugin is null.");
            Preconditions.checkArgument(plugin == main.getOwningPlugin(), "AdvancementMain owning plugin isn't the provided JavaPlugin.");
            Preconditions.checkArgument(AdvancementMain.isLoaded(), "AdvancementMain is not loaded.");
            this.advancementMainOwner = plugin;
            loadable.onLoad(main, plugin);
        }

        @Override
        public void onEnable() {
            if (advancementMainOwner == null) {
                throw new IllegalStateException("Not loaded.");
            }
            Preconditions.checkArgument(advancementMainOwner.isEnabled(), "Plugin is not enabled.");
            Preconditions.checkArgument(AdvancementMain.isEnabled(), "AdvancementMain is not enabled.");
            enabled = true;
            loadable.onEnable();
        }

        @Override
        public void onDisable() {
            if (advancementMainOwner == null) {
                throw new IllegalStateException("Not loaded and not enabled.");
            }
            if (!enabled) {
                throw new IllegalStateException("Not enabled.");
            }
            enabled = false;
            advancementMainOwner = null;
            loadable.onDisable();
        }
    }
}
