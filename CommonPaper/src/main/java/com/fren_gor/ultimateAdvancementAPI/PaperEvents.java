package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.eventManagerAPI.EventManager;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Internal class used to support Paper-only events in {@code AdvancementMain}.
 */
final class PaperEvents {

    private final EventManager eventManager;

    public PaperEvents(@NotNull EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void registerServerResourcesReloadedEvent(
            @NotNull Object listener,
            @NotNull Runnable onServerResourcesReloaded
    ) {
        eventManager.register(listener, ServerResourcesReloadedEvent.class, e -> {
            onServerResourcesReloaded.run();
        });
    }
}
