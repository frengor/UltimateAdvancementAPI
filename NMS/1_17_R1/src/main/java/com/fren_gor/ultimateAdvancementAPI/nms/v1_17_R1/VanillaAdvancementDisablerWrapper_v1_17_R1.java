package com.fren_gor.ultimateAdvancementAPI.nms.v1_17_R1;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;
import com.google.common.collect.Sets;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class VanillaAdvancementDisablerWrapper_v1_17_R1 extends VanillaAdvancementDisablerWrapper {

    private static Field advancementRoots, advancementTasks;

    static {
        try {
            advancementRoots = AdvancementList.class.getDeclaredField("c");
            advancementRoots.setAccessible(true);
            advancementTasks = AdvancementList.class.getDeclaredField("d");
            advancementTasks.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void disableVanillaAdvancements() throws Exception {
        AdvancementList registry = ((CraftServer) Bukkit.getServer()).getServer().getAdvancements().advancements;

        if (registry.advancements.isEmpty()) {
            return;
        }

        final Set<Advancement> advRoots = (Set<Advancement>) advancementRoots.get(registry);
        final Set<Advancement> advTasks = (Set<Advancement>) advancementTasks.get(registry);

        Set<ResourceLocation> removed = Sets.newHashSetWithExpectedSize(registry.advancements.size());

        Iterator<Entry<ResourceLocation, Advancement>> it = registry.advancements.entrySet().iterator();
        while (it.hasNext()) {
            Entry<ResourceLocation, Advancement> e = it.next();

            if (e.getKey().getNamespace().equals("minecraft")) {
                // Unregister it
                Advancement adv = e.getValue();
                if (adv.getParent() == null) {
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
        ClientboundUpdateAdvancementsPacket removePacket = new ClientboundUpdateAdvancementsPacket(false, Collections.emptyList(), removed, Collections.emptyMap());

        for (Player player : Bukkit.getOnlinePlayers()) {
            Util.sendTo(player, removePacket);
        }
    }

    private VanillaAdvancementDisablerWrapper_v1_17_R1() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
