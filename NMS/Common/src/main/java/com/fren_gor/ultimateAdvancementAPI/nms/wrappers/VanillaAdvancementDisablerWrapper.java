package com.fren_gor.ultimateAdvancementAPI.nms.wrappers;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.google.common.base.Preconditions;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Class to disable vanilla advancements.
 */
public class VanillaAdvancementDisablerWrapper {

    private static Method method;

    static {
        var clazz = ReflectionUtil.getWrapperClass(VanillaAdvancementDisablerWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            method = clazz.getDeclaredMethod("disableVanillaAdvancements");
            Preconditions.checkArgument(Modifier.isPublic(method.getModifiers()), "Method disableVanillaAdvancements() is not public.");
            Preconditions.checkArgument(Modifier.isStatic(method.getModifiers()), "Method disableVanillaAdvancements() is not static.");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disable vanilla advancement.
     *
     * @throws Exception If disabling goes wrong.
     */
    public static void disableVanillaAdvancements() throws Exception {
        method.invoke(null);
    }
}
