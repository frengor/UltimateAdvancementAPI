package com.fren_gor.ultimateAdvancementAPI.nms.util;

import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Reflection utility class.
 */
public class ReflectionUtil {

    /**
     * The Minecraft version.
     * <p>For example {@code 1.17.1}.
     */
    public static final String MINECRAFT_VERSION = Bukkit.getBukkitVersion().split("-")[0];

    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    /**
     * The NMS version.
     * <p>For example, for {@code v1_17_R1} it is {@code 17}.
     */
    public static final int VERSION = Integer.parseInt(MINECRAFT_VERSION.split("\\.")[1]);

    private static final boolean IS_1_17 = VERSION >= 17;

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
