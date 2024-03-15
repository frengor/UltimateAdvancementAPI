package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.packets;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutSelectAdvancementTabWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PacketPlayOutSelectAdvancementTabWrapper_mocked0_0_R1 extends PacketPlayOutSelectAdvancementTabWrapper {

    public static final Map<UUID, List<PacketPlayOutSelectAdvancementTabWrapper_mocked0_0_R1>> PACKETS_SENT = Collections.synchronizedMap(new LinkedHashMap<>());

    private final MinecraftKeyWrapper key;

    public PacketPlayOutSelectAdvancementTabWrapper_mocked0_0_R1() {
        this.key = null;
    }

    public PacketPlayOutSelectAdvancementTabWrapper_mocked0_0_R1(@NotNull MinecraftKeyWrapper key) {
        this.key = Objects.requireNonNull(key);
    }

    @Override
    public void sendTo(@NotNull Player player) {
        PACKETS_SENT.computeIfAbsent(player.getUniqueId(), uuid -> Collections.synchronizedList(new ArrayList<>())).add(this);
    }

    @Nullable
    public MinecraftKeyWrapper getKey() {
        return key;
    }

    public boolean isTabDeselection() {
        return key == null;
    }
}
