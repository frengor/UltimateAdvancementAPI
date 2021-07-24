package com.fren_gor.ultimateAdvancementAPI.advancement.multiParents;

import com.fren_gor.ultimateAdvancementAPI.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.FakeAdvancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_15_R1.Advancement;
import net.minecraft.server.v1_15_R1.AdvancementProgress;
import net.minecraft.server.v1_15_R1.Criterion;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.ADV_REWARDS;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementCriteria;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementProgress;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementRequirements;

public class MultiParentsAdvancement extends AbstractMultiParentsAdvancement {

    // Every parent goes here. Ignoring the parent in BaseAdvancement
    private final Map<BaseAdvancement, FakeAdvancement> parents;
    private final Map<String, Criterion> advCriteria = getAdvancementCriteria(maxCriteria);
    private final String[][] advRequirements = getAdvancementRequirements(advCriteria);
    private final net.minecraft.server.v1_15_R1.AdvancementDisplay mcDisplay;

    public MultiParentsAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull BaseAdvancement... parents) {
        this(key, display, 1, parents);
    }

    public MultiParentsAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria, @NotNull BaseAdvancement... parents) {
        this(key, display, maxCriteria, Sets.newHashSet(Objects.requireNonNull(parents)));
    }

    public MultiParentsAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull Set<BaseAdvancement> parents) {
        this(key, display, 1, parents);
    }

    public MultiParentsAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria, @NotNull Set<BaseAdvancement> parents) {
        super(key, display, validateAndGetFirst(parents), maxCriteria);

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
        mcDisplay = display.getMinecraftDisplay(this);
    }

    @Override
    public void onUpdate(@NotNull TeamProgression teamProgression, @NotNull Set<net.minecraft.server.v1_15_R1.Advancement> advancementList, @NotNull Map<MinecraftKey, AdvancementProgress> progresses, @NotNull Set<MinecraftKey> added) {
        if (isVisible(teamProgression)) {
            BaseAdvancement tmp = null;
            for (Entry<BaseAdvancement, FakeAdvancement> e : parents.entrySet()) {
                if (e.getKey().isVisible(teamProgression)) {
                    if (tmp == null)
                        tmp = e.getKey();
                    else
                        e.getValue().onUpdate(teamProgression, advancementList, progresses, added);
                }
            }
            if (tmp == null) {
                tmp = getParent();
            }
            net.minecraft.server.v1_15_R1.Advancement mcAdv = getMinecraftAdvancement(tmp);
            advancementList.add(mcAdv);
            MinecraftKey key = getMinecraftKey();
            added.add(key);
            progresses.put(key, getAdvancementProgress(mcAdv, getTeamCriteria(teamProgression)));
        }
    }

    @Override
    @NotNull
    @Unmodifiable
    public Set<@NotNull BaseAdvancement> getParents() {
        return Collections.unmodifiableSet(parents.keySet());
    }

    @Override
    public boolean isEveryParentGranted(@NotNull TeamProgression pro) {
        Validate.notNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (!advancement.isGranted(pro)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAnyParentGranted(@NotNull TeamProgression pro) {
        Validate.notNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (advancement.isGranted(pro)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEveryGrandparentGranted(@NotNull TeamProgression pro) {
        Validate.notNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (!advancement.isGranted(pro)) {
                if (advancement instanceof AbstractMultiParentsAdvancement && !((AbstractMultiParentsAdvancement) advancement).isEveryParentGranted(pro)) {
                    return false;
                } else if (!advancement.getParent().isGranted(pro)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isAnyGrandparentGranted(@NotNull TeamProgression pro) {
        Validate.notNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (advancement.isGranted(pro)) {
                return true;
            } else if (advancement instanceof AbstractMultiParentsAdvancement && ((AbstractMultiParentsAdvancement) advancement).isAnyParentGranted(pro)) {
                return true;
            } else if (advancement.getParent().isGranted(pro)) {
                return true;
            }
        }
        return false;
    }

    // Not currently used
    /*public boolean isAnyParentStarted(@NotNull TeamProgression pro) {
        Validate.notNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (advancement.getTeamCriteria(pro) > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyGrandparentStarted(@NotNull TeamProgression pro) {
        Validate.notNull(pro, "TeamProgression cannot be null.");

        for (BaseAdvancement advancement : parents.keySet()) {
            if (advancement.getTeamCriteria(pro) > 0 || isParentStarted(pro, advancement)) {
                return true;
            }
        }
        return false;
    }

    private boolean isParentStarted(@NotNull TeamProgression pro, @NotNull BaseAdvancement adv) {
        // Avoid merging if to improve readability
        if (adv instanceof AbstractMultiParentsAdvancement && ((AbstractMultiParentsAdvancement) adv).isAnyParentStarted(pro)) {
            return true;
        } else
            return adv.getParent().getTeamCriteria(pro) > 0;
    }*/

    @Override
    public void validateRegister() throws InvalidAdvancementException {
        for (BaseAdvancement advancement : parents.keySet()) {
            if (!advancement.isValid())
                throw new InvalidAdvancementException("A parent advancement is not valid (" + advancement.getKey() + ").");
        }
    }

    @Override
    public void onDispose() {
        for (FakeAdvancement f : parents.values()) {
            f.onDispose();
        }
        super.onDispose();
    }

    @Override
    @NotNull
    public BaseAdvancement getParent() {
        return (BaseAdvancement) parent;
    }

    @NotNull
    @Override
    public Advancement getMinecraftAdvancement() {
        System.out.println("WARNING: Calls to MultiParentsAdvancement#getMinecraftAdvancement() results in an undefined behaviour, since there are more than one parents. This is not an error, though.");
        return super.getMinecraftAdvancement();
    }

    @NotNull
    protected net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement(@NotNull BaseAdvancement advancement) {
        return new net.minecraft.server.v1_15_R1.Advancement(getMinecraftKey(), advancement.getMinecraftAdvancement(), mcDisplay, ADV_REWARDS, advCriteria, advRequirements);
    }
}
