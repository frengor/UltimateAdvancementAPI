package com.fren_gor.ultimateAdvancementAPI.nms.v1_18_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.v1_18_R1.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.google.common.base.Preconditions;
import net.minecraft.advancements.Criterion;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;

public class PreparedAdvancementWrapper_v1_18_R1 extends PreparedAdvancementWrapper {

    private final MinecraftKeyWrapper key;
    private final PreparedAdvancementWrapper parent;
    private final Map<String, Criterion> advCriteria;
    private final String[][] advRequirements;

    public PreparedAdvancementWrapper_v1_18_R1(@NotNull MinecraftKeyWrapper key, @Nullable PreparedAdvancementWrapper parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        this.key = key;
        this.parent = parent;
        this.advCriteria = Util.getAdvancementCriteria(maxProgression);
        this.advRequirements = Util.getAdvancementRequirements(advCriteria);
    }

    protected PreparedAdvancementWrapper_v1_18_R1(@NotNull MinecraftKeyWrapper key, @Nullable PreparedAdvancementWrapper parent, @NotNull Map<String, Criterion> advCriteria, @NotNull String[][] advRequirements) {
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
        return advRequirements.length;
    }

    @Override
    @NotNull
    public AdvancementWrapper toAdvancementWrapper(@NotNull AdvancementDisplayWrapper display) {
        Preconditions.checkNotNull(display, "AdvancementDisplayWrapper is null.");
        if (parent == null) {
            return new AdvancementWrapper_v1_18_R1(key, display, advCriteria, advRequirements);
        } else {
            return new AdvancementWrapper_v1_18_R1(key, parent, display, advCriteria, advRequirements);
        }
    }

    @Override
    @NotNull
    @Contract("_ -> new")
    public PreparedAdvancementWrapper withParent(@Nullable PreparedAdvancementWrapper parent) {
        return new PreparedAdvancementWrapper_v1_18_R1(key, parent, advCriteria, advRequirements);
    }
}
