package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.AbstractWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public abstract class AdvancementFrameTypeWrapper extends AbstractWrapper {
    /**
     * A frame with squared shape.
     */
    public static AdvancementFrameTypeWrapper TASK;

    /**
     * A frame with rounded top and bottom.
     */
    public static AdvancementFrameTypeWrapper GOAL;

    /**
     * A frame with thorns at the corners.
     */
    public static AdvancementFrameTypeWrapper CHALLENGE;

    static {
        var clazz = ReflectionUtil.getWrapperClass(AdvancementFrameTypeWrapper.class);
        assert clazz != null : "Wrapper class is null.";
        try {
            Constructor<? extends AdvancementFrameTypeWrapper> constructor = clazz.getDeclaredConstructor(FrameType.class);
            TASK = constructor.newInstance(FrameType.TASK);
            GOAL = constructor.newInstance(FrameType.GOAL);
            CHALLENGE = constructor.newInstance(FrameType.CHALLENGE);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public abstract FrameType getFrameType();

    @Override
    @NotNull
    public String toString() {
        return getFrameType().name();
    }

    public enum FrameType {
        TASK, GOAL, CHALLENGE;
    }
}
