package com.fren_gor.ultimateAdvancementAPI.nms.v1_20_R3;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.AdvancementTree.Listener;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class VanillaAdvancementDisablerWrapper_v1_20_R3 extends VanillaAdvancementDisablerWrapper {

    private static Logger LOGGER = null;
    private static Field listener, firstPacket;

    static {
        try {
            listener = Arrays.stream(AdvancementTree.class.getDeclaredFields()).filter(f -> f.getType() == AdvancementTree.Listener.class).findFirst().orElseThrow();
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
            Field logger = Arrays.stream(AdvancementTree.class.getDeclaredFields()).filter(f -> f.getType() == org.slf4j.Logger.class).findFirst().orElseThrow();
            logger.setAccessible(true);
            org.slf4j.Logger slf4jLogger = (org.slf4j.Logger) logger.get(null);
            LOGGER = Arrays.stream(slf4jLogger.getClass().getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).map(f -> {
                try {
                    f.setAccessible(true);
                    if (f.get(slf4jLogger) instanceof Logger log) {
                        return log;
                    }
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).findFirst().orElseThrow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableVanillaAdvancements(boolean disableVanillaAdvancementsRecipes) throws Exception {
        ServerAdvancementManager serverAdvancements = ((CraftServer) Bukkit.getServer()).getServer().getAdvancements();
        AdvancementTree tree = serverAdvancements.tree();

        if (serverAdvancements.advancements.isEmpty()) {
            return;
        }

        final Set<ResourceLocation> removed = Sets.newHashSetWithExpectedSize(serverAdvancements.advancements.size());

        // Inject AdvancementTree listener
        final Listener old = (Listener) listener.get(tree);
        try {
            listener.set(tree, new Listener() {
                @Override
                public void onAddAdvancementRoot(AdvancementNode advancement) {
                    if (old != null)
                        old.onAddAdvancementRoot(advancement);
                }

                @Override
                public void onRemoveAdvancementRoot(AdvancementNode advancement) {
                    removed.add(advancement.holder().id());
                    if (old != null)
                        old.onRemoveAdvancementRoot(advancement);
                }

                @Override
                public void onAddAdvancementTask(AdvancementNode advancement) {
                    if (old != null)
                        old.onAddAdvancementTask(advancement);
                }

                @Override
                public void onRemoveAdvancementTask(AdvancementNode advancement) {
                    removed.add(advancement.holder().id());
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
            ImmutableMap.Builder<ResourceLocation, AdvancementHolder> builder = ImmutableMap.builder();
            for (var entry : serverAdvancements.advancements.entrySet()) {
                ResourceLocation key = entry.getKey();
                if (key.getNamespace().equals("minecraft") && (disableVanillaAdvancementsRecipes || !key.getPath().startsWith("recipes/"))) {
                    locations.add(key);
                } else {
                    builder.put(key, entry.getValue());
                }
            }

            serverAdvancements.advancements = builder.buildOrThrow();

            final Level oldLevel = disableLogger();
            try {
                tree.remove(locations);
            } finally {
                // Always restore old logger
                enableLogger(oldLevel);
            }
        } finally {
            // Always uninject advancement listener
            listener.set(tree, old);
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
        if (LOGGER == null) // Fail-safe if LOGGER could not be found by reflections
            return null;

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
        if (LOGGER == null || toSet == null) // Fail-safe if LOGGER could not be found by reflections
            return;

        // Method setLevel is not present in Logger interface
        if (LOGGER instanceof org.apache.logging.log4j.core.Logger coreLogger) {
            coreLogger.setLevel(toSet);
        } else if (LOGGER instanceof SimpleLogger simple) {
            simple.setLevel(toSet);
        }
    }

    private VanillaAdvancementDisablerWrapper_v1_20_R3() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
