package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R2;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_16_R2.AdvancementProgress;
import net.minecraft.server.v1_16_R2.ChatComponentText;
import net.minecraft.server.v1_16_R2.Criterion;
import net.minecraft.server.v1_16_R2.CriterionProgress;
import net.minecraft.server.v1_16_R2.CriterionTriggerImpossible;
import net.minecraft.server.v1_16_R2.IChatBaseComponent;
import net.minecraft.server.v1_16_R2.Packet;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;

public final class Util {

    @NotNull
    public static Map<String, Criterion> getAdvancementCriteria(@Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Preconditions.checkArgument(maxProgression >= 1, "Max progression must be >= 1.");

        Map<String, Criterion> advCriteria = Maps.newHashMapWithExpectedSize(maxProgression);
        for (int i = 0; i < maxProgression; i++) {
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
    public static net.minecraft.server.v1_16_R2.AdvancementProgress getAdvancementProgress(@NotNull net.minecraft.server.v1_16_R2.Advancement mcAdv, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
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

    @NotNull
    public static IChatBaseComponent fromString(@NotNull String string) {
        if (string == null || string.isEmpty()) {
            return ChatComponentText.d;
        }
        return CraftChatMessage.fromStringOrNull(string, true);
    }

    public static void sendTo(@NotNull Player player, @NotNull Packet<?> packet) {
        Preconditions.checkNotNull(player, "Player is null.");
        Preconditions.checkNotNull(packet, "Packet is null.");
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    private Util() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
