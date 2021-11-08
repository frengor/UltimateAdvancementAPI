package com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.AbstractWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Wrapper class for NMS {@code FrameType}.
 */
public abstract class AdvancementFrameTypeWrapper extends AbstractWrapper {
    /**
     * A frame wrapper with squared shape.
     */
    public static AdvancementFrameTypeWrapper TASK;

    /**
     * A frame wrapper with rounded top and bottom.
     */
    public static AdvancementFrameTypeWrapper GOAL;

    /**
     * A frame wrapper with thorns at the corners.
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

    /**
     * Gets the {@link FrameType} of this frame wrapper.
     *
     * @return The {@link FrameType} of this frame wrapper.
     */
    @NotNull
    public abstract FrameType getFrameType();

    @Override
    @NotNull
    public String toString() {
        return getFrameType().name();
    }

    /**
     * Enumeration that represents the frame type of {@link AdvancementFrameTypeWrapper}s.
     */
    public enum FrameType {
        /**
         * A frame with squared shape.
         */
        TASK,
        /**
         * A frame with rounded top and bottom.
         */
        GOAL,
        /**
         * A frame with thorns at the corners.
         */
        CHALLENGE;
    }
}
