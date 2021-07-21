package com.fren_gor.ultimateAdvancementAPI.advancement.multiParents;

import com.fren_gor.ultimateAdvancementAPI.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;

public abstract class AbstractMultiParentsAdvancement extends BaseAdvancement {

    public AbstractMultiParentsAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent) {
        super(advancementTab, key, display, parent);
    }

    public AbstractMultiParentsAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull Advancement parent, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(advancementTab, key, display, parent, maxCriteria);
    }

    public boolean isEveryParentGranted(@NotNull Player player) {
        return isEveryParentGranted(uuidFromPlayer(player));
    }

    public abstract boolean isEveryParentGranted(@NotNull UUID uuid);

    public boolean isAnyParentGranted(@NotNull Player player) {
        return isAnyParentGranted(uuidFromPlayer(player));
    }

    public abstract boolean isAnyParentGranted(@NotNull UUID uuid);

    public boolean isAnyParentStarted(@NotNull Player player) {
        return isAnyParentStarted(uuidFromPlayer(player));
    }

    public abstract boolean isAnyParentStarted(@NotNull UUID uuid);

    public boolean isEveryGrandparentGranted(@NotNull Player player) {
        return isEveryGrandparentGranted(uuidFromPlayer(player));
    }

    public abstract boolean isEveryGrandparentGranted(@NotNull UUID uuid);

    public boolean isAnyGrandparentGranted(@NotNull Player player) {
        return isAnyGrandparentGranted(uuidFromPlayer(player));
    }

    public abstract boolean isAnyGrandparentGranted(@NotNull UUID uuid);

    public boolean isAnyGrandparentStarted(@NotNull Player player) {
        return isAnyGrandparentStarted(uuidFromPlayer(player));
    }

    public abstract boolean isAnyGrandparentStarted(@NotNull UUID uuid);

    @NotNull
    protected static <E extends Advancement> E validateAndGetFirst(Set<E> advs) {
        Validate.notNull(advs, "Parent advancements are null.");
        Validate.isTrue(advs.size() > 1, "There must be at least 2 parents.");
        return Objects.requireNonNull(advs.iterator().next());
    }
}
