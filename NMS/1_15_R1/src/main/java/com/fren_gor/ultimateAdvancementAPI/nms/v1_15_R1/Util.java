package com.fren_gor.ultimateAdvancementAPI.nms.v1_15_R1;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_15_R1.AdvancementProgress;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.Criterion;
import net.minecraft.server.v1_15_R1.CriterionProgress;
import net.minecraft.server.v1_15_R1.CriterionTriggerImpossible;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.Packet;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;

public class Util {

    private static final IChatBaseComponent EMPTY = new ChatComponentText("");

    @NotNull
    public static Map<String, Criterion> getAdvancementCriteria(@Range(from = 1, to = Integer.MAX_VALUE) int maxProgressions) {
        Preconditions.checkArgument(maxProgressions >= 1, "Max progressions must be >= 1.");

        Map<String, Criterion> advCriteria = Maps.newHashMapWithExpectedSize(maxProgressions);
        for (int i = 0; i < maxProgressions; i++) {
            advCriteria.put(String.valueOf(i), new Criterion(new CriterionTriggerImpossible.a()));
        }

        return advCriteria;
    }

    @NotNull
    public static String[][] getAdvancementRequirements(@NotNull Map<String, Criterion> advCriteria) {
        Preconditions.checkNotNull(advCriteria, "Advancement criteria map is null.");

        String[][] array = new String[advCriteria.size()][1];
        int index = 0;
        for (String name : advCriteria.keySet()) {
            array[index++][0] = name;
        }

        return array;
    }

    @NotNull
    public static net.minecraft.server.v1_15_R1.AdvancementProgress getAdvancementProgress(@NotNull net.minecraft.server.v1_15_R1.Advancement mcAdv, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        Preconditions.checkNotNull(mcAdv, "NMS Advancement is null.");
        Preconditions.checkArgument(progression >= 0, "Progression must be >= 0.");

        AdvancementProgress advPrg = new AdvancementProgress();
        advPrg.a(mcAdv.getCriteria(), mcAdv.i());

        for (int i = 0; i < progression; i++) {
            CriterionProgress criteriaPrg = advPrg.getCriterionProgress(String.valueOf(i));
            if (criteriaPrg != null) {
                criteriaPrg.b();
            }
        }

        return advPrg;
    }

    public static void sendTo(@NotNull Player player, @NotNull Packet<?> packet) {
        Preconditions.checkNotNull(player, "Player is null.");
        Preconditions.checkNotNull(packet, "Packet is null.");
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @NotNull
    public static IChatBaseComponent fromString(@NotNull String string) {
        if (string == null || string.isEmpty()) {
            return EMPTY;
        }
        var component = ChatSerializer.a(string);
        return component != null ? component : CraftChatMessage.fromStringOrNull(string, true);
    }

    private Util() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
