package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public class PreparedAdvancementWrapper_mocked0_0_R1 extends PreparedAdvancementWrapper {

    private final MinecraftKeyWrapper key;
    private final PreparedAdvancementWrapper parent;
    private final int maxProgression;

    public PreparedAdvancementWrapper_mocked0_0_R1(@NotNull MinecraftKeyWrapper key, @Nullable PreparedAdvancementWrapper parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        Preconditions.checkArgument(maxProgression >= 1, "maxProgression < 1");
        this.key = Objects.requireNonNull(key);
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
        Preconditions.checkNotNull(display, "AdvancementDisplayWrapper is null.");
        if (parent == null) {
            return new AdvancementWrapper_mocked0_0_R1(key, display, maxProgression);
        } else {
            return new AdvancementWrapper_mocked0_0_R1(key, parent, display, maxProgression);
        }
    }

    @Override
    @NotNull
    @Contract("_ -> new")
    public PreparedAdvancementWrapper withParent(@Nullable PreparedAdvancementWrapper parent) {
        return new PreparedAdvancementWrapper_mocked0_0_R1(key, parent, maxProgression);
    }
}
