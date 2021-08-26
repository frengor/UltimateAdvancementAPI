package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import net.minecraft.server.v1_15_R1.Criterion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Objects;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.ADV_REWARDS;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementCriteria;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementRequirements;

/**
 * BaseAdvancement directly extends Advancement. It represents an advancement with a parent advancement.
 */
public class BaseAdvancement extends Advancement {

    /**
     * The advancement parent of the advancement.
     * <p>The advancement is visually linked with the parent in the advancement GUI.
     */
    @NotNull
    protected final Advancement parent;

    private net.minecraft.server.v1_15_R1.Advancement mcAdvancement;

    /**
     * Create a new BaseAdvancement.
     *
     * @param key The unique key of the advancement.
     * @param display The {@link AdvancementDisplay} instance of the advancement.
     * @param parent The advancement parent of the advancement.
     */
    public BaseAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent) {
        this(key, display, parent, 1);
    }

    /**
     * Create a new BaseAdvancement.
     *
     * @param key The unique key of the advancement.
     * @param display The {@link AdvancementDisplay} instance of the advancement.
     * @param parent The advancement parent of the advancement.
     * @param maxCriteria The times the advancement action should be done.
     */
    public BaseAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(Objects.requireNonNull(parent, "Parent advancement is null.").advancementTab, key, display, maxCriteria);
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateRegister() throws InvalidAdvancementException {
        if (!parent.isValid()) {
            throw new InvalidAdvancementException("Parent advancement is not valid (" + parent.getKey() + ").");
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement() {
        if (mcAdvancement != null) {
            return mcAdvancement;
        }

        Map<String, Criterion> advCriteria = getAdvancementCriteria(maxCriteria);
        return mcAdvancement = new net.minecraft.server.v1_15_R1.Advancement(getMinecraftKey(), parent.getMinecraftAdvancement(), display.getMinecraftDisplay(this), ADV_REWARDS, advCriteria, getAdvancementRequirements(advCriteria));
    }

    /**
     * Returns the advancement parent.
     *
     * @return The advancement parent.
     */
    public Advancement getParent() {
        return parent;
    }
}