package com.fren_gor.ultimateAdvancementAPI.nms.util;

import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Reflection utility class.
 */
public class ReflectionUtil {

    /**
     * The Minecraft version.
     * <p>For example {@code 1.17.1} or {@code 26.1} (new year-based scheme introduced in 26.1).
     */
    public static final String MINECRAFT_VERSION = Bukkit.getBukkitVersion().split("-")[0];

    /**
     * The Minecraft minor version.
     * <p>For example, for {@code 1.19.2} it is {@code 2}. For new-format versions like {@code 26.1} it is {@code 0}.
     */
    public static final int MINOR_VERSION;

    static {
        var splitted = MINECRAFT_VERSION.split("\\.");
        if (splitted.length > 2) {
            MINOR_VERSION = Integer.parseInt(splitted[2]);
        } else {
            MINOR_VERSION = 0;
        }
    }

    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    // First segment of the version string: "1" for old format (1.x.y), year for new format (26.1)
    private static final int MAJOR_SEGMENT = Integer.parseInt(MINECRAFT_VERSION.split("\\.")[0]);

    /**
     * The NMS version.
     * <p>For old-format versions (1.x.y), this is the major release number (e.g., {@code 21} for 1.21.x).
     * For new year-based versions (e.g., {@code 26.1}), this is the drop number (e.g., {@code 1}).
     */
    public static final int VERSION = Integer.parseInt(MINECRAFT_VERSION.split("\\.")[1]);

    // True if the server uses Mojang-mapped class names (net.minecraft.xxx) rather than net.minecraft.server.vX_Y_RZ.
    // All new year-based versions (26.x+) and old 1.17+ versions use Mojang mappings.
    private static final boolean IS_1_17 = MAJOR_SEGMENT > 1 || VERSION >= 17;

    /**
     * Returns whether the provided class exists.
     *
     * @param className The fully qualified name of the class.
     * @return Whether the provided class exists.
     */
    public static boolean classExists(@NotNull String className) {
        Objects.requireNonNull(className, "ClassName cannot be null.");
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Gets an NMS class using reflections.
     * <p>For example, to get the NMS {@code Advancement} class this method should be called like:
     * {@code getNMSClass("Advancement", "advancements")}.
     * <p>Note that its fully qualified name in 1.17+ is {@code net.minecraft.advancements.Advancement}.
     *
     * @param name The NMS class name.
     * @param mcPackage The NMS class package (relative to {@code net.minecraft}).
     * @return The required NMS class, or {@code null} if the class couldn't be found.
     */
    @Nullable
    public static Class<?> getNMSClass(@NotNull String name, @NotNull String mcPackage) {
        String path;
        if (IS_1_17) {
            path = "net.minecraft." + mcPackage + '.' + name;
        } else {
            Optional<String> version = Versions.getNMSVersion();
            if (version.isEmpty()) {
                Bukkit.getLogger().severe("[UltimateAdvancementAPI] Unsupported Minecraft version! (" + MINECRAFT_VERSION + ")");
                return null;
            }
            path = "net.minecraft.server." + version.get() + '.' + name;
        }

        try {
            return Class.forName(path);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().severe("[UltimateAdvancementAPI] Can't find NMS Class! (" + path + ")");
            return null;
        }
    }

    /**
     * Gets a CraftBukkit class using reflections.
     *
     * @param name The name of the CraftBukkit class, starting with its package (relative to {@code org.bukkit.craftbukkit}).
     * @return The required CraftBukkit class, or {@code null} if the class couldn't be found.
     */
    @Nullable
    public static Class<?> getCBClass(@NotNull String name) {
        String cb = CRAFTBUKKIT_PACKAGE + "." + name;
        try {
            return Class.forName(cb);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().severe("[UltimateAdvancementAPI] Can't find CB Class! (" + cb + ")");
            return null;
        }
    }

    /**
     * Gets the NMS-specific wrapper class of the provided class.
     *
     * @param clazz The non-NMS-specific wrapper class.
     * @return The NMS-specific wrapper class of the provided class, or {@code null} if the class couldn't be found.
     */
    @Nullable
    public static <T> Class<? extends T> getWrapperClass(@NotNull Class<T> clazz) {
        Optional<String> version = Versions.getNMSVersion();
        if (version.isEmpty()) {
            Bukkit.getLogger().severe("[UltimateAdvancementAPI] Unsupported Minecraft version! (" + MINECRAFT_VERSION + ")");
            return null;
        }

        String name = clazz.getName();
        String validPackage = "com.fren_gor.ultimateAdvancementAPI.nms.wrappers.";
        if (!name.startsWith(validPackage)) {
            throw new IllegalArgumentException("Invalid class " + name + '.');
        }
        String wrapper = "com.fren_gor.ultimateAdvancementAPI.nms." + version.get() + "." + name.substring(validPackage.length()) + '_' + version.get();
        try {
            return Class.forName(wrapper).asSubclass(clazz);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().severe("[UltimateAdvancementAPI] Can't find Wrapper Class! (" + wrapper + ")");
            return null;
        }
    }

    private ReflectionUtil() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
