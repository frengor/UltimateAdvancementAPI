package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@Data
@EqualsAndHashCode(callSuper=false)
public class AdvancementCriteriaUpdateEvent extends Event {

    @Range(from = 0, to = Integer.MAX_VALUE)
    private final int oldCriteria, newCriteria;
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