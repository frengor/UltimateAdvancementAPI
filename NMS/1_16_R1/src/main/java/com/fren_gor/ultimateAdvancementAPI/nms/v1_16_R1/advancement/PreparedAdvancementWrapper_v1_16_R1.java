package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R1.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import net.minecraft.server.v1_16_R1.Criterion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;

public class PreparedAdvancementWrapper_v1_16_R1 extends PreparedAdvancementWrapper {

    private final MinecraftKeyWrapper key;
    private final AdvancementDisplayWrapper display;
    private final Map<String, Criterion> advCriteria;
    private final String[][] advRequirements;

    public PreparedAdvancementWrapper_v1_16_R1(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        this.key = key;
        this.display = display;
        this.advCriteria = Util.getAdvancementCriteria(maxCriteria);
        this.advRequirements = Util.getAdvancementRequirements(advCriteria);
    }

    @NotNull
    public MinecraftKeyWrapper getKey() {
        return key;
    }

    @NotNull
    public AdvancementDisplayWrapper getDisplay() {
        return display;
    }

    @Range(from = 1, to = Integer.MAX_VALUE)
    public int getMaxCriteria() {
        return advRequirements.length;
    }

    @Override
    @NotNull
    public AdvancementWrapper toRootAdvancementWrapper() {
        return new AdvancementWrapper_v1_16_R1(key, display, advCriteria, advRequirements);
    }

    @Override
    @NotNull
    public AdvancementWrapper toBaseAdvancementWrapper(@NotNull AdvancementWrapper parent) {
        return new AdvancementWrapper_v1_16_R1(key, parent, display, advCriteria, advRequirements);
    }
}