package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Internal class used to support Paper-only events in {@code AdvancementMain}.
 */
final class PaperEvents {

    // ServerResourcesReloadedEvent was added in Paper 1.16.4
    public static final boolean IS_SERVER_RESOURCES_RELOADED_EVENT_SUPPORTED = ReflectionUtil.VERSION > 16 || (ReflectionUtil.VERSION == 16 && ReflectionUtil.MINOR_VERSION >= 4);

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
