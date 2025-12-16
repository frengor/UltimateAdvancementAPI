package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementUpdater;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;

/**
 * BaseAdvancement directly extends Advancement. It represents an advancement with a parent advancement.
 */
public non-sealed class BaseAdvancement extends Advancement {

    /**
     * The advancement parent of the advancement.
     * <p>The advancement is visually linked with the parent in the advancement GUI.
     */
    @NotNull
    protected final Advancement parent;

    @LazyValue
    private PreparedAdvancementWrapper wrapper;

    /**
     * Creates a new {@code BaseAdvancement} with a maximum progression of {@code 1}.
     * <p>The tab of this advancement will be the parent one.
     *
     * @param parent The parent of this advancement.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param display The display information of this advancement.
     */
    public BaseAdvancement(@NotNull Advancement parent, @NotNull String key, @NotNull AbstractAdvancementDisplay display) {
        this(parent, key, 1, display);
    }

    /**
     * Creates a new {@code BaseAdvancement}.
     * <p>The tab of this advancement will be the parent one.
     *
     * @param parent The parent of this advancement.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     *         Should not start with {@link AdvancementKey#RESERVED_KEY_PREFIX}.
     * @param maxProgression The maximum advancement progression.
     * @param display The display information of this advancement.
     */
    public BaseAdvancement(@NotNull Advancement parent, @NotNull String key, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression, @NotNull AbstractAdvancementDisplay display) {
        super(Objects.requireNonNull(parent, "Parent advancement is null.").advancementTab, key, maxProgression, display);
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
    public void onUpdate(@NotNull TeamProgression teamProgression, @NotNull AdvancementUpdater advancementUpdater) throws ReflectiveOperationException {
        if (isVisible(teamProgression)) {
            advancementUpdater.addBaseAdvancement(getNMSWrapper(), display, getProgression(teamProgression));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public PreparedAdvancementWrapper getNMSWrapper() throws ReflectiveOperationException {
        if (wrapper != null) {
            return wrapper;
        }

        return wrapper = PreparedAdvancementWrapper.craft(key.getNMSWrapper(), parent.getNMSWrapper(), maxProgression);
    }

    /**
     * Gets the parent of the advancement.
     *
     * @return The parent of the advancement.
     */
    @NotNull
    public Advancement getParent() {
        return parent;
    }
}