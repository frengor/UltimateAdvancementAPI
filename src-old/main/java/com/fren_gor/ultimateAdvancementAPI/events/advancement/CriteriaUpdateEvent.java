package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import lombok.Data;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@Data
@EqualsAndHashCode(callSuper=false)
public class CriteriaUpdateEvent extends Event {

    @Range(from = 0, to = Integer.MAX_VALUE)
    private final int oldCriteria, newCriteria;
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