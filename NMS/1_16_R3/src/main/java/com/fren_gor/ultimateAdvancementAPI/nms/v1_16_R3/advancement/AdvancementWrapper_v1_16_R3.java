package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R3.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R3.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import net.minecraft.server.v1_16_R3.Advancement;
import net.minecraft.server.v1_16_R3.AdvancementDisplay;
import net.minecraft.server.v1_16_R3.Criterion;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;

public class AdvancementWrapper_v1_16_R3 extends AdvancementWrapper {

    private final Advancement advancement;
    private final MinecraftKeyWrapper key;
    private final AdvancementWrapper parent;
    private final AdvancementDisplayWrapper display;

    public AdvancementWrapper_v1_16_R3(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        Map<String, Criterion> advCriteria = Util.getAdvancementCriteria(maxCriteria);
        this.advancement = new Advancement((MinecraftKey) key.getNMSKey(), null, (AdvancementDisplay) display.getNMSDisplay(), Util.ADV_REWARDS, advCriteria, Util.getAdvancementRequirements(advCriteria));
        this.key = key;
        this.parent = null;
        this.display = display;
    }

    public AdvancementWrapper_v1_16_R3(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        Map<String, Criterion> advCriteria = Util.getAdvancementCriteria(maxCriteria);
        this.advancement = new Advancement((MinecraftKey) key.getNMSKey(), (Advancement) parent.getNMSAdvancement(), (AdvancementDisplay) display.getNMSDisplay(), Util.ADV_REWARDS, advCriteria, Util.getAdvancementRequirements(advCriteria));
        this.key = key;
        this.parent = parent;
        this.display = display;
    }

    @NotNull
    public MinecraftKeyWrapper getKey() {
        return key;
    }

    @Nullable
    public AdvancementWrapper getParent() {
        return parent;
    }

    @NotNull
    public AdvancementDisplayWrapper getDisplay() {
        return display;
    }

    @Range(from = 1, to = Integer.MAX_VALUE)
    public int getMaxCriteria() {
        return this.advancement.i().length;
    }

    @Override
    @NotNull
    public Advancement getNMSAdvancement() {
        return advancement;
    }
}
