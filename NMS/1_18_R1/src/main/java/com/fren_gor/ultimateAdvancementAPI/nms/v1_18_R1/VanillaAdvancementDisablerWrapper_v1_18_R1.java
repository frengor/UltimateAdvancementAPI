package com.fren_gor.ultimateAdvancementAPI.nms.v1_18_R1;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;
import com.google.common.collect.Sets;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementList.Listener;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VanillaAdvancementDisablerWrapper_v1_18_R1 extends VanillaAdvancementDisablerWrapper {

    private static Logger LOGGER = null;
    private static Field listener, firstPacket;

    static {
        try {
            listener = Arrays.stream(AdvancementList.class.getDeclaredFields()).filter(f -> f.getType() == AdvancementList.Listener.class).findFirst().orElseThrow();
            listener.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            firstPacket = Arrays.stream(PlayerAdvancements.class.getDeclaredFields()).filter(f -> f.getType() == boolean.class).findFirst().orElseThrow();
            firstPacket.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Field logger = Arrays.stream(AdvancementList.class.getDeclaredFields()).filter(f -> f.getType() == Logger.class).findFirst().orElseThrow();
            logger.setAccessible(true);
            LOGGER = (Logger) logger.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableVanillaAdvancements(boolean disableVanillaAdvancementsRecipes) throws Exception {
        ServerAdvancementManager serverAdvancements = ((CraftServer) Bukkit.getServer()).getServer().getAdvancements();
        AdvancementList registry = serverAdvancements.advancements;

        if (registry.advancements.isEmpty()) {
            return;
        }

        final Set<ResourceLocation> removed = Sets.newHashSetWithExpectedSize(registry.advancements.size());

        // Inject advancement listener
        final AdvancementList.Listener old = (Listener) listener.get(registry);
        try {
            listener.set(registry, new AdvancementList.Listener() {
                @Override
                public void onAddAdvancementRoot(Advancement advancement) {
                    if (old != null)
                        old.onAddAdvancementRoot(advancement);
                }

                @Override
                public void onRemoveAdvancementRoot(Advancement advancement) {
                    removed.add(advancement.getId());
                    if (old != null)
                        old.onRemoveAdvancementRoot(advancement);
                }

                @Override
                public void onAddAdvancementTask(Advancement advancement) {
                    if (old != null)
                        old.onAddAdvancementTask(advancement);
                }

                @Override
                public void onRemoveAdvancementTask(Advancement advancement) {
                    removed.add(advancement.getId());
                    if (old != null)
                        old.onRemoveAdvancementTask(advancement);
                }

                @Override
                public void onAdvancementsCleared() {
                    if (old != null)
                        old.onAdvancementsCleared();
                }
            });

            Set<ResourceLocation> locations = new HashSet<>();
            for (Advancement root : registry.getRoots()) {
                if (root.getId().getNamespace().equals("minecraft")) {
                    locations.add(root.getId());
                }
            }

            final Level oldLevel = disableLogger();
            try {
                registry.remove(locations);
            } finally {
                // Always restore old logger
                enableLogger(oldLevel);
            }
        } finally {
            // Always uninject advancement listener
            listener.set(registry, old);
        }

        final var removePacket = new ClientboundUpdateAdvancementsPacket(false, Collections.emptyList(), removed, Collections.emptyMap());

        // Remove advancements from players
        for (Player player : Bukkit.getOnlinePlayers()) {
            var mcPlayer = ((CraftPlayer) player).getHandle();
            var advs = mcPlayer.getAdvancements();
            advs.reload(serverAdvancements);
            firstPacket.setBoolean(advs, false); // Don't clear every client advancement
            mcPlayer.connection.send(removePacket);
        }
    }

    private static Level disableLogger() {
        Level old = LOGGER.getLevel();

        // Method setLevel is not present in Logger interface
        if (LOGGER instanceof org.apache.logging.log4j.core.Logger coreLogger) {
            coreLogger.setLevel(Level.OFF);
        } else if (LOGGER instanceof SimpleLogger simple) {
            simple.setLevel(Level.OFF);
        }

        return old;
    }

    private static void enableLogger(Level toSet) {
        // Method setLevel is not present in Logger interface
        if (LOGGER instanceof org.apache.logging.log4j.core.Logger coreLogger) {
            coreLogger.setLevel(toSet);
        } else if (LOGGER instanceof SimpleLogger simple) {
            simple.setLevel(toSet);
        }
    }

    private VanillaAdvancementDisablerWrapper_v1_18_R1() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
