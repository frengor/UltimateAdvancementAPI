package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called just before an advancement is being disposed.
 * <p>Note that the {@link Advancement#onDispose() method has not been already called).
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdvancementDisposeEvent extends Event {

    @NotNull
    private final Advancement advancement;

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