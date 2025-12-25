package com.fren_gor.ultimateAdvancementAPI.nms.v1_21_R7.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.v1_21_R7.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutSelectAdvancementTabWrapper;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.resources.Identifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PacketPlayOutSelectAdvancementTabWrapper_v1_21_R7 extends PacketPlayOutSelectAdvancementTabWrapper {

    private final ClientboundSelectAdvancementsTabPacket packet;

    public PacketPlayOutSelectAdvancementTabWrapper_v1_21_R7() {
        packet = new ClientboundSelectAdvancementsTabPacket((Identifier) null);
    }

    public PacketPlayOutSelectAdvancementTabWrapper_v1_21_R7(@NotNull MinecraftKeyWrapper key) {
        packet = new ClientboundSelectAdvancementsTabPacket((Identifier) key.toNMS());
    }

    @Override
    public void sendTo(@NotNull Player player) {
        Util.sendTo(player, packet);
    }
}
