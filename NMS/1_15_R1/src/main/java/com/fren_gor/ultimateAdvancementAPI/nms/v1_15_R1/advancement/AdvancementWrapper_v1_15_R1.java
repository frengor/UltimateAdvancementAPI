package com.fren_gor.ultimateAdvancementAPI.nms.v1_15_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.v1_15_R1.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import net.minecraft.server.v1_15_R1.Advancement;
import net.minecraft.server.v1_15_R1.AdvancementDisplay;
import net.minecraft.server.v1_15_R1.AdvancementRewards;
import net.minecraft.server.v1_15_R1.Criterion;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;

public class AdvancementWrapper_v1_15_R1 extends AdvancementWrapper {

    private final Advancement advancement;
    private final MinecraftKeyWrapper key;
    private final PreparedAdvancementWrapper parent;
    private final AdvancementDisplayWrapper display;

    public AdvancementWrapper_v1_15_R1(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion> advCriteria = Util.getAdvancementCriteria(maxProgression);
        this.advancement = new Advancement((MinecraftKey) key.toNMS(), null, (AdvancementDisplay) display.toNMS(), AdvancementRewards.a, advCriteria, Util.getAdvancementRequirements(advCriteria));
        this.key = key;
        this.parent = null;
        this.display = display;
    }

    public AdvancementWrapper_v1_15_R1(@NotNull MinecraftKeyWrapper key, @NotNull PreparedAdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Map<String, Criterion> advCriteria = Util.getAdvancementCriteria(maxProgression);
        this.advancement = new Advancement((MinecraftKey) key.toNMS(), (Advancement) parent.toAdvancementWrapper(display).toNMS(), (AdvancementDisplay) display.toNMS(), AdvancementRewards.a, advCriteria, Util.getAdvancementRequirements(advCriteria));
        this.key = key;
        this.parent = parent;
        this.display = display;
    }

    protected AdvancementWrapper_v1_15_R1(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @NotNull Map<String, Criterion> advCriteria, @NotNull String[][] advRequirements) {
        this.advancement = new Advancement((MinecraftKey) key.toNMS(), null, (AdvancementDisplay) display.toNMS(), AdvancementRewards.a, advCriteria, advRequirements);
        this.key = key;
        this.parent = null;
        this.display = display;
    }

    protected AdvancementWrapper_v1_15_R1(@NotNull MinecraftKeyWrapper key, @NotNull PreparedAdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @NotNull Map<String, Criterion> advCriteria, @NotNull String[][] advRequirements) {
        this.advancement = new Advancement((MinecraftKey) key.toNMS(), (Advancement) parent.toAdvancementWrapper(display).toNMS(), (AdvancementDisplay) display.toNMS(), AdvancementRewards.a, advCriteria, advRequirements);
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
        return this.advancement.i().length;
    }

    @Override
    @NotNull
    public Advancement toNMS() {
        return advancement;
    }
}
