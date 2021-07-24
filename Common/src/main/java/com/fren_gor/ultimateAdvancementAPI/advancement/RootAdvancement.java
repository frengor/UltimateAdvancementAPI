package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import lombok.Getter;
import net.minecraft.server.v1_15_R1.Criterion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.ADV_REWARDS;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementCriteria;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementRequirements;

public class RootAdvancement extends Advancement {

    @Getter
    @NotNull
    private final String backgroundTexture;
    private net.minecraft.server.v1_15_R1.Advancement mcAdvancement;

    public RootAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull String backgroundTexture) {
        this(advancementTab, key, display, backgroundTexture, 1);
    }

    public RootAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull String backgroundTexture, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(advancementTab, key, display, maxCriteria);
        this.backgroundTexture = Objects.requireNonNull(backgroundTexture, "Background texture is null.");
    }

    @NotNull
    public net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement() {
        if (mcAdvancement != null) {
            return mcAdvancement;
        }

        Map<String, Criterion> advCrit = getAdvancementCriteria(maxCriteria);
        return mcAdvancement = new net.minecraft.server.v1_15_R1.Advancement(getMinecraftKey(), null, display.getMinecraftDisplay(this), ADV_REWARDS, advCrit, getAdvancementRequirements(advCrit));
    }

    @Override
    @Contract("_ -> true")
    public final boolean isVisible(@NotNull Player player) {
        return true;
    }

    @Override
    @Contract("_ -> true")
    public final boolean isVisible(@NotNull UUID uuid) {
        return true;
    }

    @Override
    @Contract("_ -> true")
    public final boolean isVisible(@NotNull TeamProgression progression) {
        return true;
    }
}
