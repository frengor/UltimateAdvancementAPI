package com.fren_gor.ultimateAdvancementAPI.advancement.multiParents;

import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.FakeAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * An implementation of {@link AbstractMultiParentsAdvancement}.
 */
public class MultiParentsAdvancement extends AbstractMultiParentsAdvancement {

    // Every parent goes here. Ignoring the parent in BaseAdvancement
    private final Map<BaseAdvancement, FakeAdvancement> parents;

    @LazyValue
    private PreparedAdvancementWrapper wrapper;

    /**
     * Creates a new {@code MultiParentsAdvancement} with a maximum progression of {@code 1}.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param parents The advancement parents. There must be at least one.
     */
    public MultiParentsAdvancement(@NotNull String key, @NotNull AbstractAdvancementDisplay display, @NotNull BaseAdvancement... parents) {
        this(key, display, 1, parents);
    }

    /**
     * Creates a new {@code MultiParentsAdvancement}.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param maxProgression The maximum advancement progression.
     * @param parents The advancement parents. There must be at least one.
     */
    public MultiParentsAdvancement(@NotNull String key, @NotNull AbstractAdvancementDisplay display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression, @NotNull BaseAdvancement... parents) {
        this(key, display, maxProgression, Sets.newHashSet(Objects.requireNonNull(parents)));
    }

    /**
     * Creates a new {@code MultiParentsAdvancement} with a maximum progression of {@code 1}.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param parents The advancement parents. There must be at least one.
     */
    public MultiParentsAdvancement(@NotNull String key, @NotNull AbstractAdvancementDisplay display, @NotNull Set<BaseAdvancement> parents) {
        this(key, display, 1, parents);
    }

    /**
     * Creates a new {@code MultiParentsAdvancement}.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param maxProgression The maximum advancement progression.
     * @param parents The advancement parents. There must be at least one.
     */
    public MultiParentsAdvancement(@NotNull String key, @NotNull AbstractAdvancementDisplay display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression, @NotNull Set<BaseAdvancement> parents) {
        super(key, display, validateAndGetFirst(parents), maxProgression);

        this.parents = Maps.newHashMapWithExpectedSize(parents.size());

        for (BaseAdvancement advancement : parents) {
            if (advancement == null) {
                this.parents.clear();
                throw new IllegalArgumentException("A parent advancement is null.");
            }
            if (!advancementTab.isOwnedByThisTab(advancement)) {
                this.parents.clear();
                throw new IllegalArgumentException("A parent advancement (" + advancement.getKey() + ") is not owned by this tab (" + advancementTab + ").");
            }
            FakeAdvancement adv = new FakeAdvancement(advancement, display.getX(), display.getY());
            this.parents.put(advancement, adv);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(@NotNull TeamProgression teamProgression, @NotNull Map<AdvancementWrapper, Integer> addedAdvancements) {
        if (isVisible(teamProgression)) {
            BaseAdvancement tmp = null;
            for (Entry<BaseAdvancement, FakeAdvancement> e : parents.entrySet()) {
                if (e.getKey().isVisible(teamProgression)) {
                    if (tmp == null)
                        tmp = e.getKey();
                    else
                        e.getValue().onUpdate(teamProgression, addedAdvancements);
                }
            }
            if (tmp == null) {
                tmp = getParent();
            }
            addedAdvancements.put(getNMSWrapper(tmp), getProgression(teamProgression));
        }
    }

    /**
     * Gets an unmodifiable {@link Set} of the advancement parents.
     *
     * @return An unmodifiable {@link Set} of the advancement parents.
     */
    @Override
    @NotNull
    @Unmodifiable
    public Set<@NotNull BaseAdvancement> getParents() {
        return Collections.unmodifiableSet(parents.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEveryParentGranted(@NotNull TeamProgression pro) {
        Preconditions.checkNotNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (!advancement.isGranted(pro)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnyParentGranted(@NotNull TeamProgression pro) {
        Preconditions.checkNotNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (advancement.isGranted(pro)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEveryGrandparentGranted(@NotNull TeamProgression pro) {
        Preconditions.checkNotNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (!advancement.isGranted(pro)) { // If it is granted then continue to check the other parents
                if (advancement instanceof AbstractMultiParentsAdvancement multiParent) {
                    if (!multiParent.isEveryParentGranted(pro))
                        return false;
                } else if (!advancement.getParent().isGranted(pro)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnyGrandparentGranted(@NotNull TeamProgression pro) {
        validateTeamProgression(pro);

        for (BaseAdvancement advancement : parents.keySet()) {
            if (advancement.isGranted(pro)) {
                return true;
            } else if (advancement instanceof AbstractMultiParentsAdvancement multiParent) {
                if (multiParent.isAnyParentGranted(pro))
                    return true;
            } else if (advancement.getParent().isGranted(pro)) {
                return true;
            }
        }
        return false;
    }

    // Not currently used
    /*public boolean isAnyParentStarted(@NotNull TeamProgression pro) {
        Preconditions.checkNotNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (advancement.getProgression(pro) > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyGrandparentStarted(@NotNull TeamProgression pro) {
        Preconditions.checkNotNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (advancement.getProgression(pro) > 0 || isParentStarted(pro, advancement)) {
                return true;
            }
        }
        return false;
    }

    private boolean isParentStarted(@NotNull TeamProgression pro, @NotNull BaseAdvancement adv) {
        // Avoid merging if to improve readability
        if (adv instanceof AbstractMultiParentsAdvancement multiParent) {
            if (multiParent.isAnyParentStarted(pro))
                return true;
        } else
            return adv.getParent().getProgression(pro) > 0;
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateRegister() throws InvalidAdvancementException {
        for (BaseAdvancement advancement : parents.keySet()) {
            if (!advancement.isValid())
                throw new InvalidAdvancementException("A parent advancement is not valid (" + advancement.getKey() + ").");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDispose() {
        for (FakeAdvancement f : parents.values()) {
            f.onDispose();
        }
        super.onDispose();
    }

    /**
     * Returns the parent advancement.
     * <p><strong>Calls to this method results in an undefined behaviour</strong>, since there is more than one parent.
     * <p>Use when you don't need an exact parent.
     *
     * @return A parent advancement.
     */
    @Override
    @NotNull
    public BaseAdvancement getParent() {
        return (BaseAdvancement) parent;
    }

    /**
     * Returns the NMS wrapper of this advancement.
     * <p><strong>Calls to this method results in an undefined behaviour</strong>, since there is more than one parent
     * and the NMS wrapper can contain only one.
     * <p>Use when you don't need the NMS wrapper of an exact parent.
     * <p>Use {@link #getNMSWrapper(BaseAdvancement)} instead.
     *
     * @return The NMS wrapper of this advancement.
     * @see #getNMSWrapper(BaseAdvancement)
     */
    @Override
    @NotNull
    public AdvancementWrapper getNMSWrapper() {
        setUpWrapper();
        return wrapper.toBaseAdvancementWrapper(parent.getNMSWrapper());
    }

    /**
     * Returns the NMS wrapper of this advancement.
     * The parent of the returned advancement wrapper is the (NMS wrapper of the) provided one.
     *
     * @param advancement The parent advancement used as the parent of the NMS wrapper.
     * @return The NMS wrapper of this advancement.
     */
    @NotNull
    protected AdvancementWrapper getNMSWrapper(@NotNull BaseAdvancement advancement) {
        setUpWrapper();
        return wrapper.toBaseAdvancementWrapper(advancement.getNMSWrapper());
    }

    private void setUpWrapper() {
        if (wrapper == null) {
            try {
                wrapper = PreparedAdvancementWrapper.craft(this.key.getNMSWrapper(), this.display.getNMSWrapper(this), maxProgression);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
