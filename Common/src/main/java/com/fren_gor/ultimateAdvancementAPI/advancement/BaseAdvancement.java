package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Objects;

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

    @LazyValue
    private PreparedAdvancementWrapper wrapper;

    /**
     * Creates a new {@code BaseAdvancement} with a maximum progression of {@code 1}.
     * <p>The tab of this advancement will be the parent one.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param parent The parent of this advancement.
     */
    public BaseAdvancement(@NotNull String key, @NotNull AbstractAdvancementDisplay display, @NotNull Advancement parent) {
        this(key, display, parent, 1);
    }

    /**
     * Creates a new {@code BaseAdvancement}.
     * <p>The tab of this advancement will be the parent one.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param parent The parent of this advancement.
     * @param maxProgression The maximum advancement progression.
     */
    public BaseAdvancement(@NotNull String key, @NotNull AbstractAdvancementDisplay display, @NotNull Advancement parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        super(Objects.requireNonNull(parent, "Parent advancement is null.").advancementTab, key, display, maxProgression);
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
    public void onUpdate(@NotNull Player player, @NotNull TeamProgression teamProgression, @NotNull Map<AdvancementWrapper, Integer> addedAdvancements) {
        if (isVisible(teamProgression)) {
            addedAdvancements.put(getNMSWrapper().toBaseAdvancementWrapper(parent.getNMSWrapper(), display.dispatchGetNMSWrapper(this, player, teamProgression)), getProgression(teamProgression));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public PreparedAdvancementWrapper getNMSWrapper() {
        if (wrapper != null) {
            return wrapper;
        }

        try {
            return wrapper = PreparedAdvancementWrapper.craft(key.getNMSWrapper(), maxProgression);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
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