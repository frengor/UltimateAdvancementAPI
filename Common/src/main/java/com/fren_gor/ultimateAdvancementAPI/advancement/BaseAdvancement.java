package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import lombok.Getter;
import net.minecraft.server.v1_15_R1.Criterion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Objects;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.ADV_REWARDS;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementCriteria;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementRequirements;

public class BaseAdvancement extends Advancement {

    @Getter
    @NotNull
    protected final Advancement parent;
    private net.minecraft.server.v1_15_R1.Advancement mcAdvancement;

    public BaseAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent) {
        this(key, display, parent, 1);
    }

    public BaseAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(Objects.requireNonNull(parent, "Parent advancement is null.").advancementTab, key, display, maxCriteria);
        this.parent = parent;
    }

    @Override
    public void validateRegister() throws InvalidAdvancementException {
        if (!parent.isValid()) {
            throw new InvalidAdvancementException("Parent advancement is not valid (" + parent.getKey() + ").");
        }
    }

    @NotNull
    public net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement() {
        if (mcAdvancement != null) {
            return mcAdvancement;
        }

        Map<String, Criterion> advCriteria = getAdvancementCriteria(maxCriteria);
        return mcAdvancement = new net.minecraft.server.v1_15_R1.Advancement(getMinecraftKey(), parent.getMinecraftAdvancement(), display.getMinecraftDisplay(this), ADV_REWARDS, advCriteria, getAdvancementRequirements(advCriteria));
    }
}