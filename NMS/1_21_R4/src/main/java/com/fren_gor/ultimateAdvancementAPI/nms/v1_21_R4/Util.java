package com.fren_gor.ultimateAdvancementAPI.nms.v1_21_R4;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.ImpossibleTrigger.TriggerInstance;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R4.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Util {

    public static final Logger ERROR = Logger.getLogger("UltimateAdvancementAPI-NMS");

    @NotNull
    public static Map<String, Criterion<?>> getAdvancementCriteria(@Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Preconditions.checkArgument(maxProgression >= 1, "Max progression must be >= 1.");

        Map<String, Criterion<?>> advCriteria = Maps.newHashMapWithExpectedSize(maxProgression);
        for (int i = 0; i < maxProgression; i++) {
            advCriteria.put(String.valueOf(i), new Criterion<>(new ImpossibleTrigger(), new TriggerInstance()));
        }

        return advCriteria;
    }

    @NotNull
    public static AdvancementRequirements getAdvancementRequirements(@NotNull Map<String, Criterion<?>> advCriteria) {
        Preconditions.checkNotNull(advCriteria, "Advancement criteria map is null.");

        List<List<String>> list = new ArrayList<>(advCriteria.size());
        for (String name : advCriteria.keySet()) {
            list.add(List.of(name));
        }

        return new AdvancementRequirements(list);
    }

    @NotNull
    public static AdvancementProgress getAdvancementProgress(@NotNull AdvancementHolder mcAdv, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        Preconditions.checkNotNull(mcAdv, "NMS Advancement is null.");
        Preconditions.checkArgument(progression >= 0, "Progression must be >= 0.");

        AdvancementProgress advPrg = new AdvancementProgress();
        advPrg.update(mcAdv.value().requirements());

        for (int i = 0; i < progression; i++) {
            CriterionProgress criteriaPrg = advPrg.getCriterion(String.valueOf(i));
            if (criteriaPrg != null) {
                criteriaPrg.grant();
            }
        }

        return advPrg;
    }

    @NotNull
    public static Component fromString(@NotNull String string) {
        if (string == null || string.isEmpty()) {
            return CommonComponents.EMPTY;
        }
        return CraftChatMessage.fromStringOrNull(string, true);
    }

    @NotNull
    public static Component fromComponent(@NotNull BaseComponent component) {
        if (component == null) {
            return CommonComponents.EMPTY;
        }
        Component base = CraftChatMessage.fromJSONOrNull(ComponentSerializer.toString(component));
        return base == null ? CommonComponents.EMPTY : base;
    }

    @Nullable
    public static ClientAsset parseBackgroundTexture(@Nullable String backgroundTexture) {
        if (backgroundTexture == null) {
            return null;
        }

        ResourceLocation texturePath = ResourceLocation.parse(backgroundTexture);
        if (!texturePath.getPath().startsWith("textures/") || !texturePath.getPath().endsWith(".png")) {
            ERROR.severe("Invalid background texture \"" + backgroundTexture + "\" (the path should be in the form \"textures/**.png\")");
            return null;
        }

        ResourceLocation id = texturePath.withPath(path -> {
            return path.substring("textures/".length(), path.length() - ".png".length());
        });
        return new ClientAsset(id, texturePath);
    }

    public static void sendTo(@NotNull Player player, @NotNull Packet<?> packet) {
        Preconditions.checkNotNull(player, "Player is null.");
        Preconditions.checkNotNull(packet, "Packet is null.");
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    private Util() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
