package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called just before an advancement is disposed.
 * <p>Note that the {@link Advancement#onDispose()} method has not been already called.
 */
public class AdvancementDisposeEvent extends Event {

    private final Advancement advancement;

    /**
     * Creates a new {@code AdvancementDisposeEvent}.
     *
     * @param advancement The {@link Advancement} which is being disposed.
     */
    public AdvancementDisposeEvent(@NotNull Advancement advancement) {
        this.advancement = Objects.requireNonNull(advancement, "Advancement is null.");
    }

    /**
     * Gets the {@link Advancement} which is being disposed.
     *
     * @return The {@link Advancement} which is being disposed.
     */
    @NotNull
    public Advancement getAdvancement() {
        return advancement;
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
        return "AdvancementDisposeEvent{" +
                "advancement=" + advancement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdvancementDisposeEvent that = (AdvancementDisposeEvent) o;

        return advancement.equals(that.advancement);
    }

    @Override
    public int hashCode() {
        return advancement.hashCode();
    }
}