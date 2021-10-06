package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after an advancement has been disposed.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdvancementDisposedEvent extends Event {

    @NotNull
    private final AdvancementKey advancementKey;

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
}