package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R3;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R3.AdvancementProgress;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.Criterion;
import net.minecraft.server.v1_16_R3.CriterionProgress;
import net.minecraft.server.v1_16_R3.CriterionTriggerImpossible;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.Packet;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
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
    public static net.minecraft.server.v1_16_R3.AdvancementProgress getAdvancementProgress(@NotNull net.minecraft.server.v1_16_R3.Advancement mcAdv, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
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

    @NotNull
    public static IChatBaseComponent fromComponent(@NotNull BaseComponent component) {
        if (component == null) {
            return ChatComponentText.d;
        }
        return fromJSON(ComponentSerializer.toString(component)); // Should never throw JsonParseException
    }

    @NotNull
    public static IChatBaseComponent fromJSON(@NotNull String json) throws JsonParseException {
        if (json == null) {
            return ChatComponentText.d;
        }
        return CraftChatMessage.fromJSON(json);
    }

    @NotNull
    public static BaseComponent toComponent(@NotNull IChatBaseComponent component) {
        if (component == null) {
            return new TextComponent("");
        }
        BaseComponent[] parsed = ComponentSerializer.parse(CraftChatMessage.toJSON(component));
        return parsed.length == 1 ? parsed[0] : new TextComponent(parsed);
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
