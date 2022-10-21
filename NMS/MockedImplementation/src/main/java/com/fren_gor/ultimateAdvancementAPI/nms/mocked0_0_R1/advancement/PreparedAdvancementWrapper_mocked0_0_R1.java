package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PreparedAdvancementWrapper_mocked0_0_R1 extends PreparedAdvancementWrapper {

    private final MinecraftKeyWrapper key;
    private final AdvancementDisplayWrapper display;
    private final int maxProgression;

    public PreparedAdvancementWrapper_mocked0_0_R1(@NotNull MinecraftKeyWrapper key, @NotNull AdvancementDisplayWrapper display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        this.key = key;
        this.display = display;
        this.maxProgression = maxProgression;
    }

    @NotNull
    public MinecraftKeyWrapper getKey() {
        return key;
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
    public AdvancementWrapper toRootAdvancementWrapper() {
        return new AdvancementWrapper_mocked0_0_R1(key, display, maxProgression);
    }

    @Override
    @NotNull
    public AdvancementWrapper toBaseAdvancementWrapper(@NotNull AdvancementWrapper parent) {
        return new AdvancementWrapper_mocked0_0_R1(key, parent, display, maxProgression);
    }
}
