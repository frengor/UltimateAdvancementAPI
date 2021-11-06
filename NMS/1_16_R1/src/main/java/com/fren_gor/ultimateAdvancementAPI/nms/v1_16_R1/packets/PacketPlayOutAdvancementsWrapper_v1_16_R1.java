package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R1.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ListSet;
import com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R1.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_16_R1.Advancement;
import net.minecraft.server.v1_16_R1.AdvancementProgress;
import net.minecraft.server.v1_16_R1.MinecraftKey;
import net.minecraft.server.v1_16_R1.PacketPlayOutAdvancements;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PacketPlayOutAdvancementsWrapper_v1_16_R1 extends PacketPlayOutAdvancementsWrapper {

    private final PacketPlayOutAdvancements packet;

    public PacketPlayOutAdvancementsWrapper_v1_16_R1() {
        this.packet = new PacketPlayOutAdvancements(true, Collections.emptyList(), Collections.emptySet(), Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public PacketPlayOutAdvancementsWrapper_v1_16_R1(@NotNull Map<AdvancementWrapper, Integer> toSend) {
        Map<MinecraftKey, AdvancementProgress> map = Maps.newHashMapWithExpectedSize(toSend.size());
        for (Entry<AdvancementWrapper, Integer> e : toSend.entrySet()) {
            AdvancementWrapper adv = e.getKey();
            map.put((MinecraftKey) adv.getKey().toNMS(), Util.getAdvancementProgress((Advancement) adv.toNMS(), e.getValue()));
        }
        this.packet = new PacketPlayOutAdvancements(false, (Collection<Advancement>) ListSet.fromWrapperSet(toSend.keySet()), Collections.emptySet(), map);
    }

    @SuppressWarnings("unchecked")
    public PacketPlayOutAdvancementsWrapper_v1_16_R1(@NotNull Set<MinecraftKeyWrapper> toRemove) {
        this.packet = new PacketPlayOutAdvancements(false, Collections.emptyList(), (Set<MinecraftKey>) ListSet.fromWrapperSet(toRemove), Collections.emptyMap());
    }

    @Override
    public void sendTo(@NotNull Player player) {
        Util.sendTo(player, packet);
    }
}
