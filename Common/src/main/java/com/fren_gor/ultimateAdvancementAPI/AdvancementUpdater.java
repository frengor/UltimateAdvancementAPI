package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractPerPlayerAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractPerTeamAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DuplicatedException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class AdvancementUpdater {

    private final MinecraftKeyWrapper tabRootKey;
    private PreparedAdvancementWrapper rootWrapper;
    private AbstractAdvancementDisplay rootDisplay;
    private int rootProgression;
    private final Map<PreparedAdvancementWrapper, Entry<AbstractAdvancementDisplay, Integer>> immutableAdvancements;
    private final Map<PreparedAdvancementWrapper, Entry<AbstractPerTeamAdvancementDisplay, Integer>> perTeamAdvancements;
    private final Map<PreparedAdvancementWrapper, Entry<AbstractPerPlayerAdvancementDisplay, Integer>> perPlayerAdvancements;
    private final Set<MinecraftKeyWrapper> keys;

    AdvancementUpdater(@NotNull AdvancementKey tabRootKey, int sizeApprox) {
        this.tabRootKey = tabRootKey.getNMSWrapper();
        keys = Sets.newHashSetWithExpectedSize(sizeApprox);

        // Don't allocate sizeApprox for every map, to avoid wasting (too much) space in the average case
        sizeApprox = sizeApprox / 2;
        immutableAdvancements = Maps.newHashMapWithExpectedSize(sizeApprox);
        perTeamAdvancements = Maps.newHashMapWithExpectedSize(sizeApprox);
        perPlayerAdvancements = Maps.newHashMapWithExpectedSize(sizeApprox);
    }

    public void addBaseAdvancement(@NotNull PreparedAdvancementWrapper advancementWrapper, @NotNull AbstractAdvancementDisplay display, @Range(from = 0, to = Integer.MAX_VALUE) int progression) throws DuplicatedException {
        validateParams(advancementWrapper, display, progression, false);

        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            perPlayerAdvancements.put(advancementWrapper, Map.entry(perPlayer, progression));
        } else if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            perTeamAdvancements.put(advancementWrapper, Map.entry(perTeam, progression));
        } else {
            // Not per-player and not per-team
            immutableAdvancements.put(advancementWrapper, Map.entry(display, progression));
        }
    }

    public void addRootAdvancement(@NotNull PreparedAdvancementWrapper advancementWrapper, @NotNull AbstractAdvancementDisplay display, @Range(from = 0, to = Integer.MAX_VALUE) int progression) throws DuplicatedException {
        validateParams(advancementWrapper, display, progression, true);

        this.rootWrapper = advancementWrapper;
        this.rootDisplay = display;
        this.rootProgression = progression;
    }

    private void validateParams(@NotNull PreparedAdvancementWrapper advancementWrapper, @NotNull AbstractAdvancementDisplay display, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean root) {
        Preconditions.checkNotNull(advancementWrapper, "Advancement wrapper is null.");
        Preconditions.checkNotNull(display, "Display is null.");
        AdvancementUtils.validateProgressionValue(progression);

        if (root) {
            if (advancementWrapper.getParent() != null) {
                throw new IllegalArgumentException("Wrapper of root advancement " + advancementWrapper.getKey() + " has advancement " + advancementWrapper.getParent().getKey() + " as parent.");
            }
            Preconditions.checkArgument(tabRootKey.equals(advancementWrapper.getKey()), "The key of the provided root wrapper (" + advancementWrapper.getKey() + ") is different from the one of the registered RootAdvancement (" + tabRootKey + ").");

            if (this.rootWrapper != null) {
                throw new DuplicatedException("Another root advancement is already present, cannot add " + advancementWrapper.getKey());
            }
        } else {
            Preconditions.checkNotNull(advancementWrapper.getParent(), "Wrapper of base advancement " + advancementWrapper.getKey() + " has no parent.");
        }

        if (!keys.add(advancementWrapper.getKey())) {
            throw new DuplicatedException("Advancement " + advancementWrapper.getKey() + " has already been added.");
        }
    }

    @NotNull
    PreparedAdvancementWrapper getRootWrapper() {
        return rootWrapper;
    }

    @NotNull
    AbstractAdvancementDisplay getRootDisplay() {
        return rootDisplay;
    }

    int getRootProgression() {
        return rootProgression;
    }

    @NotNull
    Map<PreparedAdvancementWrapper, Entry<AbstractAdvancementDisplay, Integer>> getImmutableAdvancements() {
        return immutableAdvancements;
    }

    @NotNull
    Map<PreparedAdvancementWrapper, Entry<AbstractPerTeamAdvancementDisplay, Integer>> getPerTeamAdvancements() {
        return perTeamAdvancements;
    }

    @NotNull
    Map<PreparedAdvancementWrapper, Entry<AbstractPerPlayerAdvancementDisplay, Integer>> getPerPlayerAdvancements() {
        return perPlayerAdvancements;
    }

    @NotNull
    @UnmodifiableView
    Set<MinecraftKeyWrapper> getKeys() {
        return Collections.unmodifiableSet(keys);
    }
}
