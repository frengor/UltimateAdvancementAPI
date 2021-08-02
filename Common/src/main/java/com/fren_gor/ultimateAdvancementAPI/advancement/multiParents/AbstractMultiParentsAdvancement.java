package com.fren_gor.ultimateAdvancementAPI.advancement.multiParents;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromUUID;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;

public abstract class AbstractMultiParentsAdvancement extends BaseAdvancement {

    public AbstractMultiParentsAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull BaseAdvancement aParent) {
        super(key, display, aParent);
    }

    public AbstractMultiParentsAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull BaseAdvancement aParent, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(key, display, aParent, maxCriteria);
    }

    @NotNull
    public abstract Set<@NotNull BaseAdvancement> getParents();

    public boolean isEveryParentGranted(@NotNull Player player) {
        return isEveryParentGranted(uuidFromPlayer(player));
    }

    public boolean isEveryParentGranted(@NotNull UUID uuid) {
        return isEveryParentGranted(progressionFromUUID(uuid, this));
    }

    public abstract boolean isEveryParentGranted(@NotNull TeamProgression progression);

    public boolean isAnyParentGranted(@NotNull Player player) {
        return isAnyParentGranted(uuidFromPlayer(player));
    }

    public boolean isAnyParentGranted(@NotNull UUID uuid) {
        return isAnyParentGranted(progressionFromUUID(uuid, this));
    }

    public abstract boolean isAnyParentGranted(@NotNull TeamProgression progression);

    public boolean isEveryGrandparentGranted(@NotNull Player player) {
        return isEveryGrandparentGranted(uuidFromPlayer(player));
    }

    public boolean isEveryGrandparentGranted(@NotNull UUID uuid) {
        return isEveryGrandparentGranted(progressionFromUUID(uuid, this));
    }

    public abstract boolean isEveryGrandparentGranted(@NotNull TeamProgression progression);

    public boolean isAnyGrandparentGranted(@NotNull Player player) {
        return isAnyGrandparentGranted(uuidFromPlayer(player));
    }

    public boolean isAnyGrandparentGranted(@NotNull UUID uuid) {
        return isAnyGrandparentGranted(progressionFromUUID(uuid, this));
    }

    public abstract boolean isAnyGrandparentGranted(@NotNull TeamProgression progression);

    @NotNull
    public static <E extends BaseAdvancement> E validateAndGetFirst(Set<E> advs) {
        Validate.notNull(advs, "Parent advancements are null.");
        Validate.isTrue(advs.size() > 0, "There must be at least 1 parent.");
        return Objects.requireNonNull(advs.iterator().next());
    }
}
