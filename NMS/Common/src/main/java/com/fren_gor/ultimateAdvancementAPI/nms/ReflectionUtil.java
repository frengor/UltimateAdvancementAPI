package com.fren_gor.ultimateAdvancementAPI.nms;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

/**
 * Reflection utility class.
 */
@UtilityClass
public class ReflectionUtil {

    /**
     * The complete NMS version (like {@code v1_17_R1}).
     */
    public static final String COMPLETE_VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];

    /**
     * The NMS version.
     * <p>For example, for {@code v1_17_R1} it is {@code 17}.
     */
    public static final int VERSION = Integer.parseInt(COMPLETE_VERSION.split("_")[1]);

    /**
     * The NMS release.
     * <p>For example, for {@code v1_17_R1} it is {@code 1}.
     */
    public static final int RELEASE = Integer.parseInt(COMPLETE_VERSION.split("R")[1]);
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
    public static Class<?> getNMSClass(String name, String mcPackage) {
        String path = "net.minecraft." + (IS_1_17 ? mcPackage : "server." + COMPLETE_VERSION) + '.' + name;
        try {
            return Class.forName(path);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().info("[UltimateAdvancementAPI] Can't find NMS Class! (" + path + ")");
            return null;
        }
    }

    /**
     * Gets a CraftBukkit class using reflections.
     *
     * @param name The name of the CraftBukkit class, starting with its package (relative to {@code org.bukkit.craftbukkit}).
     * @return The required CraftBukkit class, or {@code null} if the class couldn't be found.
     */
    public static Class<?> getCBClass(String name) {
        String cb = "org.bukkit.craftbukkit." + COMPLETE_VERSION + "." + name;
        try {
            return Class.forName(cb);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().info("[UltimateAdvancementAPI] Can't find CB Class! (" + cb + ")");
            return null;
        }
    }

    /**
     * Gets the NMS-specific wrapper class of the provided class.
     *
     * @param clazz The non-NMS-specific wrapper class.
     * @return The NMS-specific wrapper class of the provided class, or {@code null} if the class couldn't be found.
     */
    public static Class<?> getWrapperClass(Class<?> clazz) {
        String wrapper = "com.fren_gor.ultimateAdvancementAPI.nms." + COMPLETE_VERSION + "." + clazz.getSimpleName() + '_' + COMPLETE_VERSION;
        try {
            return Class.forName(wrapper);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().info("[UltimateAdvancementAPI] Can't find Wrapper Class! (" + wrapper + ")");
            return null;
        }
    }
}
