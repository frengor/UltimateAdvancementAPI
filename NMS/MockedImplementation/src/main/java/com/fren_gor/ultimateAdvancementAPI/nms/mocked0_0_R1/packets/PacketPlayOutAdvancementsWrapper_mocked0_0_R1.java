package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class PacketPlayOutAdvancementsWrapper_mocked0_0_R1 extends PacketPlayOutAdvancementsWrapper {

    public PacketPlayOutAdvancementsWrapper_mocked0_0_R1() {
    }

    public PacketPlayOutAdvancementsWrapper_mocked0_0_R1(@NotNull Map<AdvancementWrapper, Integer> toSend) {
    }

    public PacketPlayOutAdvancementsWrapper_mocked0_0_R1(@NotNull Set<MinecraftKeyWrapper> toRemove) {
    }

    @Override
    public void sendTo(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }
}
