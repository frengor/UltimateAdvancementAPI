package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called after an advancement has been disposed.
 */
public class AdvancementDisposedEvent extends Event {

    private final AdvancementKey advancementKey;

    /**
     * Creates a new {@code AdvancementDisposedEvent}.
     *
     * @param advancementKey The {@link AdvancementKey} of the disposed {@link Advancement}.
     */
    public AdvancementDisposedEvent(@NotNull AdvancementKey advancementKey) {
        this.advancementKey = Objects.requireNonNull(advancementKey, "AdvancementKey is null.");
    }

    /**
     * Gets the {@link AdvancementKey} of the disposed {@link Advancement}.
     *
     * @return The {@link AdvancementKey} of the disposed {@link Advancement}.
     */
    @NotNull
    public AdvancementKey getAdvancementKey() {
        return advancementKey;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public String toString() {
        return "AdvancementDisposedEvent{" +
                "advancementKey=" + advancementKey +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdvancementDisposedEvent that = (AdvancementDisposedEvent) o;

        return advancementKey.equals(that.advancementKey);
    }

    @Override
    public int hashCode() {
        return advancementKey.hashCode();
    }
}