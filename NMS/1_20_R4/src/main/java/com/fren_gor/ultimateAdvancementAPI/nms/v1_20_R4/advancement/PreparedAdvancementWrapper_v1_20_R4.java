package com.fren_gor.ultimateAdvancementAPI.nms.v1_20_R4.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.v1_20_R4.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;

public class PreparedAdvancementWrapper_v1_20_R4 extends PreparedAdvancementWrapper {

    private final MinecraftKeyWrapper key;
    private final PreparedAdvancementWrapper parent;
    private final Map<String, Criterion<?>> advCriteria;
    private final AdvancementRequirements advRequirements;

    public PreparedAdvancementWrapper_v1_20_R4(@NotNull MinecraftKeyWrapper key, @Nullable PreparedAdvancementWrapper parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        this.key = key;
        this.parent = parent;
        this.advCriteria = Util.getAdvancementCriteria(maxProgression);
        this.advRequirements = Util.getAdvancementRequirements(advCriteria);
    }

    protected PreparedAdvancementWrapper_v1_20_R4(@NotNull MinecraftKeyWrapper key, @Nullable PreparedAdvancementWrapper parent, @NotNull Map<String, Criterion<?>> advCriteria, @NotNull AdvancementRequirements advRequirements) {
        this.key = key;
        this.parent = parent;
        this.advCriteria = advCriteria;
        this.advRequirements = advRequirements;
    }

    @Override
    @NotNull
    public MinecraftKeyWrapper getKey() {
        return key;
    }

    @Override
    @Nullable
    public PreparedAdvancementWrapper getParent() {
        return parent;
    }

    @Override
    @Range(from = 1, to = Integer.MAX_VALUE)
    public int getMaxProgression() {
        return advRequirements.size();
    }

    @Override
    @NotNull
    public AdvancementWrapper toAdvancementWrapper(@NotNull AdvancementDisplayWrapper display) {
        if (parent == null) {
            return new AdvancementWrapper_v1_20_R4(key, display, advCriteria, advRequirements);
        } else {
            return new AdvancementWrapper_v1_20_R4(key, parent, display, advCriteria, advRequirements);
        }
    }

    @Override
    @NotNull
    @Contract("_ -> new")
    public PreparedAdvancementWrapper withParent(@Nullable PreparedAdvancementWrapper parent) {
        return new PreparedAdvancementWrapper_v1_20_R4(key, parent, advCriteria, advRequirements);
    }
}
