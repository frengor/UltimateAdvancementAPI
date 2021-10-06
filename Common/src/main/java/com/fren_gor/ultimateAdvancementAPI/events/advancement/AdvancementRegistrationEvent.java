package com.fren_gor.ultimateAdvancementAPI.events.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Called when an advancement is registered in an {@link AdvancementTab}.
 *
 * @see AdvancementTab#registerAdvancements(RootAdvancement, Set)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdvancementRegistrationEvent extends Event {

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