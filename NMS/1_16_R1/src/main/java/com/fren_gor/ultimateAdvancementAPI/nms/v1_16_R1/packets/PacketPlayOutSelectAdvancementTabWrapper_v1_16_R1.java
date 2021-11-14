package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R1.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R1.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutSelectAdvancementTabWrapper;
import net.minecraft.server.v1_16_R1.MinecraftKey;
import net.minecraft.server.v1_16_R1.PacketPlayOutSelectAdvancementTab;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PacketPlayOutSelectAdvancementTabWrapper_v1_16_R1 extends PacketPlayOutSelectAdvancementTabWrapper {

    private final PacketPlayOutSelectAdvancementTab packet;

    public PacketPlayOutSelectAdvancementTabWrapper_v1_16_R1() {
        packet = new PacketPlayOutSelectAdvancementTab();
    }

    public PacketPlayOutSelectAdvancementTabWrapper_v1_16_R1(@NotNull MinecraftKeyWrapper key) {
        packet = new PacketPlayOutSelectAdvancementTab((MinecraftKey) key.toNMS());
    }

    @Override
    public void sendTo(@NotNull Player player) {
        Util.sendTo(player, packet);
    }
}
