package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * Called when an advancement is registered in an {@link AdvancementTab}.
 *
 * @see AdvancementTab#registerAdvancements(RootAdvancement, Set)
 */
public class AdvancementRegistrationEvent extends Event {

    private final Advancement advancement;

    /**
     * Creates a new {@code AdvancementRegistrationEvent}.
     *
     * @param advancement The registered {@link Advancement}.
     */
    public AdvancementRegistrationEvent(@NotNull Advancement advancement) {
        this.advancement = Objects.requireNonNull(advancement, "Advancement is null.");
    }

    /**
     * Gets the registered {@link Advancement}.
     *
     * @return The registered {@link Advancement}.
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
        return "AdvancementRegistrationEvent{" +
                "advancement=" + advancement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdvancementRegistrationEvent that = (AdvancementRegistrationEvent) o;

        return advancement.equals(that.advancement);
    }

    @Override
    public int hashCode() {
        return advancement.hashCode();
    }
}