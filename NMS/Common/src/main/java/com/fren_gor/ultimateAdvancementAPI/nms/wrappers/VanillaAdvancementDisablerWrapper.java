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
            disableMethod = clazz.getDeclaredMethod("disableVanillaAdvancements");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Couldn't initialize VanillaAdvancementDisablerWrapper.", e);
        }
        Preconditions.checkArgument(Modifier.isPublic(disableMethod.getModifiers()), "Method disableVanillaAdvancements() is not public.");
        Preconditions.checkArgument(Modifier.isStatic(disableMethod.getModifiers()), "Method disableVanillaAdvancements() is not static.");
    }

    /**
     * Disable vanilla advancement.
     *
     * @throws Exception If disabling goes wrong.
     */
    public static void disableVanillaAdvancements() throws Exception {
        disableMethod.invoke(null);
    }
}
