package com.fren_gor.ultimateAdvancementAPI.nms.v1_21_R6.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.v1_21_R6.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Optional;

public class AdvancementWrapper_v1_21_R6 extends AdvancementWrapper {

    private final AdvancementHolder advancementHolder;
    private final MinecraftKeyWrapper key;
    private final PreparedAdvancementWrapper parent;
    private final AdvancementDisplayWrapper display;

    public AdvancementWrapper_v1_21_R6(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = Util.getAdvancementCriteria(maxProgression);
        Advancement advancement = new Advancement(Optional.empty(), Optional.of((DisplayInfo) display.toNMS()), AdvancementRewards.EMPTY, advCriteria, Util.getAdvancementRequirements(advCriteria), false, Optional.empty());
        this.advancementHolder = new AdvancementHolder((ResourceLocation) key.toNMS(), advancement);
        this.key = key;
        this.parent = null;
        this.display = display;
    }

    public AdvancementWrapper_v1_21_R6(@NotNull MinecraftKeyWrapper key, @NotNull PreparedAdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion<?>> advCriteria = Util.getAdvancementCriteria(maxProgression);
        Advancement advancement = new Advancement(Optional.of((ResourceLocation) parent.getKey().toNMS()), Optional.of((DisplayInfo) display.toNMS()), AdvancementRewards.EMPTY, advCriteria, Util.getAdvancementRequirements(advCriteria), false, Optional.empty());
        this.advancementHolder = new AdvancementHolder((ResourceLocation) key.toNMS(), advancement);
        this.key = key;
        this.parent = parent;
        this.display = display;
    }

    protected AdvancementWrapper_v1_21_R6(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @NotNull Map<String, Criterion<?>> advCriteria, @NotNull AdvancementRequirements advRequirements) {
        Advancement advancement = new Advancement(Optional.empty(), Optional.of((DisplayInfo) display.toNMS()), AdvancementRewards.EMPTY, advCriteria, advRequirements, false, Optional.empty());
        this.advancementHolder = new AdvancementHolder((ResourceLocation) key.toNMS(), advancement);
        this.key = key;
        this.parent = null;
        this.display = display;
    }

    protected AdvancementWrapper_v1_21_R6(@NotNull MinecraftKeyWrapper key, @NotNull PreparedAdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @NotNull Map<String, Criterion<?>> advCriteria, @NotNull AdvancementRequirements advRequirements) {
        Advancement advancement = new Advancement(Optional.of((ResourceLocation) parent.getKey().toNMS()), Optional.of((DisplayInfo) display.toNMS()), AdvancementRewards.EMPTY, advCriteria, advRequirements, false, Optional.empty());
        this.advancementHolder = new AdvancementHolder((ResourceLocation) key.toNMS(), advancement);
        this.key = key;
        this.parent = parent;
        this.display = display;
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
    @NotNull
    public AdvancementDisplayWrapper getDisplay() {
        return display;
    }

    @Override
    @Range(from = 1, to = Integer.MAX_VALUE)
    public int getMaxProgression() {
        return this.advancementHolder.value().requirements().size();
    }

    @Override
    @NotNull
    public AdvancementHolder toNMS() {
        return advancementHolder;
    }
}
