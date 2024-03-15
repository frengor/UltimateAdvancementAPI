package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PacketPlayOutAdvancementsWrapper_mocked0_0_R1 extends PacketPlayOutAdvancementsWrapper {

    public static final Map<UUID, List<PacketPlayOutAdvancementsWrapper_mocked0_0_R1>> PACKETS_SENT = Collections.synchronizedMap(new LinkedHashMap<>());

    public final Map<AdvancementWrapper, Integer> added;
    public final Set<MinecraftKeyWrapper> removed;

    public PacketPlayOutAdvancementsWrapper_mocked0_0_R1() {
        added = Map.of();
        removed = Set.of();
    }

    public PacketPlayOutAdvancementsWrapper_mocked0_0_R1(@NotNull Map<AdvancementWrapper, Integer> toSend) {
        added = Map.copyOf(toSend);
        removed = Set.of();
    }

    public PacketPlayOutAdvancementsWrapper_mocked0_0_R1(@NotNull Set<MinecraftKeyWrapper> toRemove) {
        added = Map.of();
        removed = Set.copyOf(toRemove);
    }

    @Override
    public void sendTo(@NotNull Player player) {
        PACKETS_SENT.computeIfAbsent(player.getUniqueId(), uuid -> Collections.synchronizedList(new ArrayList<>())).add(this);
    }

    @NotNull
    @Unmodifiable
    public Map<AdvancementWrapper, Integer> getAdded() {
        return added;
    }

    @NotNull
    @Unmodifiable
    public Set<MinecraftKeyWrapper> getRemoved() {
        return removed;
    }
}
