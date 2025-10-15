package com.fren_gor.ultimateAdvancementAPI.nms.wrappers;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.google.common.base.Preconditions;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Class to disable vanilla advancements.
 */
public class VanillaAdvancementDisablerWrapper {

    private static final Method disableMethod;

    static {
        var clazz = ReflectionUtil.getWrapperClass(VanillaAdvancementDisablerWrapper.class);
        Preconditions.checkNotNull(clazz, "VanillaAdvancementDisablerWrapper implementation not found.");
        try {
            disableMethod = clazz.getDeclaredMethod("disableVanillaAdvancements", boolean.class, boolean.class);
            Preconditions.checkArgument(Modifier.isPublic(disableMethod.getModifiers()), "Method disableVanillaAdvancements(boolean, boolean) is not public.");
            Preconditions.checkArgument(Modifier.isStatic(disableMethod.getModifiers()), "Method disableVanillaAdvancements(boolean, boolean) is not static.");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Couldn't initialize VanillaAdvancementDisablerWrapper.", e);
        }
    }

    /**
     * Disables vanilla advancements.
     *
     * @throws Exception If disabling fails.
     * @deprecated Use {@link #disableVanillaAdvancements(boolean, boolean) disableVanillaAdvancements(true, false)} instead.
     */
    @Deprecated(forRemoval = true)
    public static void disableVanillaAdvancements() throws Exception {
        disableVanillaAdvancements(true, false);
    }

    /**
     * Disables vanilla advancements.
     *
     * @param vanillaAdvancements Whether to disable vanilla advancements.
     * @param vanillaRecipeAdvancements Whether to disable vanilla recipe advancements (i.e. the advancements which unlock recipes).
     * @throws Exception If disabling fails.
     */
    public static void disableVanillaAdvancements(boolean vanillaAdvancements, boolean vanillaRecipeAdvancements) throws Exception {
        if (vanillaAdvancements || vanillaRecipeAdvancements) { // Don't execute if there is nothing to disable
            disableMethod.invoke(null, vanillaAdvancements, vanillaRecipeAdvancements);
        }
    }
}
