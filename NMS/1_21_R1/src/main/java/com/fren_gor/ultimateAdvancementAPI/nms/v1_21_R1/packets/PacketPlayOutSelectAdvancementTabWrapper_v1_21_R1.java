package com.fren_gor.ultimateAdvancementAPI.nms.v1_21_R1.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.v1_21_R1.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutSelectAdvancementTabWrapper;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PacketPlayOutSelectAdvancementTabWrapper_v1_21_R1 extends PacketPlayOutSelectAdvancementTabWrapper {

    private final ClientboundSelectAdvancementsTabPacket packet;

    public PacketPlayOutSelectAdvancementTabWrapper_v1_21_R1() {
        packet = new ClientboundSelectAdvancementsTabPacket((ResourceLocation) null);
    }

    public PacketPlayOutSelectAdvancementTabWrapper_v1_21_R1(@NotNull MinecraftKeyWrapper key) {
        packet = new ClientboundSelectAdvancementsTabPacket((ResourceLocation) key.toNMS());
    }

    @Override
    public void sendTo(@NotNull Player player) {
        Util.sendTo(player, packet);
    }
}
