package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public class AdvancementWrapper_mocked0_0_R1 extends AdvancementWrapper {

    private final int maxProgression;
    private final MinecraftKeyWrapper key;
    private final PreparedAdvancementWrapper parent;
    private final AdvancementDisplayWrapper display;

    public AdvancementWrapper_mocked0_0_R1(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Preconditions.checkArgument(maxProgression >= 1, "maxProgression < 1");
        this.maxProgression = maxProgression;
        this.key = Objects.requireNonNull(key);
        this.parent = null;
        this.display = Objects.requireNonNull(display);
    }

    public AdvancementWrapper_mocked0_0_R1(@NotNull MinecraftKeyWrapper key, @NotNull PreparedAdvancementWrapper parent, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Preconditions.checkArgument(maxProgression >= 1, "maxProgression < 1");
        this.maxProgression = maxProgression;
        this.key = Objects.requireNonNull(key);
        this.parent = Objects.requireNonNull(parent);
        this.display = Objects.requireNonNull(display);
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
        return maxProgression;
    }

    @Override
    @NotNull
    public Object toNMS() {
        return this;
    }
}