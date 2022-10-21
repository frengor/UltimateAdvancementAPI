package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class AdvancementWrapper_mocked0_0_R1 extends AdvancementWrapper {

    private final int maxProgression;
    private final MinecraftKeyWrapper key;
    private final AdvancementWrapper parent;
    private final AdvancementDisplayWrapper display;

    public AdvancementWrapper_mocked0_0_R1(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        this.maxProgression = maxProgression;
        this.key = key;
        this.parent = null;
        this.display = display;
    }

    public AdvancementWrapper_mocked0_0_R1(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        this.maxProgression = maxProgression;
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

    @Override
    @Range(from = 1, to = Integer.MAX_VALUE)
    public int getMaxProgression() {
        return maxProgression;
    }

    @Override
    @NotNull
    public Object toNMS() {
        return this;
    }
}
