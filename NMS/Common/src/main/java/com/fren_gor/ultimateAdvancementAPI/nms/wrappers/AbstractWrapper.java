package com.fren_gor.ultimateAdvancementAPI.nms.wrappers;

import org.jetbrains.annotations.NotNull;

/**
 * A generic NMS wrapper.
 */
public abstract class AbstractWrapper {

    /**
     * Returns the NMS object associated with this wrapper.
     *
     * @return The NMS object associated with this wrapper.
     */
    @NotNull
    public abstract Object toNMS();

    @Override
    public String toString() {
        return toNMS().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractWrapper that = (AbstractWrapper) o;

        return toNMS().equals(that.toNMS());
    }

    @Override
    public int hashCode() {
        return toNMS().hashCode();
    }
}
