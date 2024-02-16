package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class PreparedAdvancementWrapper_mocked0_0_R1 extends PreparedAdvancementWrapper {

    private final MinecraftKeyWrapper key;
    private final PreparedAdvancementWrapper parent;
    private final int maxProgression;

    public PreparedAdvancementWrapper_mocked0_0_R1(@NotNull MinecraftKeyWrapper key, @Nullable PreparedAdvancementWrapper parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        this.key = key;
        this.parent = parent;
        this.maxProgression = maxProgression;
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
        return maxProgression;
    }

    @Override
    @NotNull
    public AdvancementWrapper toAdvancementWrapper(@NotNull AdvancementDisplayWrapper display) {
        return toAdvancementWrapperWithParent(display, parent);
    }

    @Override
    @NotNull
    public AdvancementWrapper toAdvancementWrapperWithParent(@NotNull AdvancementDisplayWrapper display, @Nullable PreparedAdvancementWrapper parent) {
        if (parent == null) {
            return new AdvancementWrapper_mocked0_0_R1(key, display, maxProgression);
        } else {
            return new AdvancementWrapper_mocked0_0_R1(key, parent, display, maxProgression);
        }
    }
}
