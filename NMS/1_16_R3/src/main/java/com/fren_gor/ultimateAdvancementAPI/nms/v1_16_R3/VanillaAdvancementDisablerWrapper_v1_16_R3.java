package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R3;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_16_R3.Advancement;
import net.minecraft.server.v1_16_R3.AdvancementDataPlayer;
import net.minecraft.server.v1_16_R3.AdvancementDataWorld;
import net.minecraft.server.v1_16_R3.Advancements;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.PacketPlayOutAdvancements;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class VanillaAdvancementDisablerWrapper_v1_16_R3 extends VanillaAdvancementDisablerWrapper {

    private static Field advancementRoots, advancementTasks, firstPacket;

    static {
        try {
            advancementRoots = Advancements.class.getDeclaredField("c");
            advancementRoots.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            advancementTasks = Advancements.class.getDeclaredField("d");
            advancementTasks.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            firstPacket = AdvancementDataPlayer.class.getDeclaredField("m");
            firstPacket.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void disableVanillaAdvancements(boolean disableVanillaRecipeAdvancements) throws Exception {
        AdvancementDataWorld serverAdvancements = ((CraftServer) Bukkit.getServer()).getServer().getAdvancementData();
        Advancements registry = serverAdvancements.REGISTRY;

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

        PacketPlayOutAdvancements removePacket = new PacketPlayOutAdvancements(false, Collections.emptySet(), removed, Collections.emptyMap());

        // Remove advancements from players
        for (Player player : Bukkit.getOnlinePlayers()) {
            var mcPlayer = ((CraftPlayer) player).getHandle();
            var advs = mcPlayer.getAdvancementData();
            advs.a(serverAdvancements);
            firstPacket.setBoolean(advs, false); // Don't clear every client advancement
            mcPlayer.playerConnection.sendPacket(removePacket);
        }
    }

    private VanillaAdvancementDisablerWrapper_v1_16_R3() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
