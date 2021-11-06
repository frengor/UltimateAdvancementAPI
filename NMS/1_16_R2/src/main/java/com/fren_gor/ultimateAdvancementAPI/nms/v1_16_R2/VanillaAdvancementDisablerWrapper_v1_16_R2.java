package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R2;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_16_R2.Advancement;
import net.minecraft.server.v1_16_R2.Advancements;
import net.minecraft.server.v1_16_R2.MinecraftKey;
import net.minecraft.server.v1_16_R2.PacketPlayOutAdvancements;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class VanillaAdvancementDisablerWrapper_v1_16_R2 extends VanillaAdvancementDisablerWrapper {

    private static Field advancementRoots, advancementTasks;

    static {
        try {
            advancementRoots = Advancements.class.getDeclaredField("c");
            advancementRoots.setAccessible(true);
            advancementTasks = Advancements.class.getDeclaredField("d");
            advancementTasks.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void disableVanillaAdvancements() throws Exception {
        Advancements registry = ((CraftServer) Bukkit.getServer()).getServer().getAdvancementData().REGISTRY;

        if (registry.advancements.isEmpty()) {
            return;
        }

        final Set<Advancement> advRoots = (Set<Advancement>) advancementRoots.get(registry);
        final Set<Advancement> advTasks = (Set<Advancement>) advancementTasks.get(registry);

        Set<MinecraftKey> removed = Sets.newHashSetWithExpectedSize(registry.advancements.size());

        Iterator<Entry<MinecraftKey, Advancement>> it = registry.advancements.entrySet().iterator();
        while (it.hasNext()) {
            Entry<MinecraftKey, Advancement> e = it.next();

            if (e.getKey().getNamespace().equals("minecraft")) {
                // Unregister it
                Advancement adv = e.getValue();
                if (adv.b() == null) {
                    // If parent is null then the adv is root
                    advRoots.remove(adv);
                } else {
                    advTasks.remove(adv);
                }
                it.remove();
                removed.add(e.getKey());
            }
        }

        // Remove advancements from players
        PacketPlayOutAdvancements removePacket = new PacketPlayOutAdvancements(false, Collections.emptySet(), removed, Collections.emptyMap());

        for (Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(removePacket);
        }
    }

    private VanillaAdvancementDisablerWrapper_v1_16_R2() {
        throw new UnsupportedOperationException("Utility class.");
    }
}