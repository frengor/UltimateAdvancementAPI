package com.fren_gor.ultimateAdvancementAPI.nms.v1_21_R3.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ListSet;
import com.fren_gor.ultimateAdvancementAPI.nms.v1_21_R3.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.google.common.collect.Maps;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PacketPlayOutAdvancementsWrapper_v1_21_R3 extends PacketPlayOutAdvancementsWrapper {

    private final ClientboundUpdateAdvancementsPacket packet;

    public PacketPlayOutAdvancementsWrapper_v1_21_R3() {
        this.packet = new ClientboundUpdateAdvancementsPacket(true, Collections.emptyList(), Collections.emptySet(), Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public PacketPlayOutAdvancementsWrapper_v1_21_R3(@NotNull Map<AdvancementWrapper, Integer> toSend) {
        Map<ResourceLocation, AdvancementProgress> map = Maps.newHashMapWithExpectedSize(toSend.size());
        for (Entry<AdvancementWrapper, Integer> e : toSend.entrySet()) {
            AdvancementWrapper adv = e.getKey();
            map.put((ResourceLocation) adv.getKey().toNMS(), Util.getAdvancementProgress((AdvancementHolder) adv.toNMS(), e.getValue()));
        }
        this.packet = new ClientboundUpdateAdvancementsPacket(false, (Collection<AdvancementHolder>) ListSet.fromWrapperSet(toSend.keySet()), Collections.emptySet(), map);
    }

    @SuppressWarnings("unchecked")
    public PacketPlayOutAdvancementsWrapper_v1_21_R3(@NotNull Set<MinecraftKeyWrapper> toRemove) {
        this.packet = new ClientboundUpdateAdvancementsPacket(false, Collections.emptyList(), (Set<ResourceLocation>) ListSet.fromWrapperSet(toRemove), Collections.emptyMap());
    }

    @Override
    public void sendTo(@NotNull Player player) {
        Util.sendTo(player, packet);
    }
}
