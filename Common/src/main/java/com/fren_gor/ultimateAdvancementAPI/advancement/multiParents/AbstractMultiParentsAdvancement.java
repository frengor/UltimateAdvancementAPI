package com.fren_gor.ultimateAdvancementAPI.advancement.multiParents;

import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
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

/**
 * The {@code AbstractMultiParentsAdvancement} class abstracts the implementation of any multi-parent advancement,
 * providing a standard supported by the API.
 * <p>A multi-parent advancement is an advancement that has more than one parent.
 * <p>A default implementation for {@code AbstractMultiParentsAdvancement} is {@link MultiParentsAdvancement}.
 */
public abstract class AbstractMultiParentsAdvancement extends BaseAdvancement {

    /**
     * Creates a new {@code AbstractMultiParentsAdvancement}.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param aParent One of the parents of this advancement.
     */
    public AbstractMultiParentsAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull BaseAdvancement aParent) {
        super(key, display, aParent);
    }

    /**
     * Creates a new {@code AbstractMultiParentsAdvancement}.
     *
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param aParent One of the parents of this advancement.
     * @param maxCriteria The maximum criteria of the task.
     */
    public AbstractMultiParentsAdvancement(@NotNull String key, @NotNull AdvancementDisplay display, @NotNull BaseAdvancement aParent, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(key, display, aParent, maxCriteria);
    }

    /**
     * Get a {@link Set} of the parents.
     *
     * @return A {@link Set} of the parents.
     */
    @NotNull
    public abstract Set<@NotNull BaseAdvancement> getParents();

    /**
     * Returns whether every parent advancement is granted for the provided player's team.
     *
     * @param player The player.
     * @return Whether every parent advancement is granted for the provided player's team.
     */
    public boolean isEveryParentGranted(@NotNull Player player) {
        return isEveryParentGranted(uuidFromPlayer(player));
    }

    /**
     * Returns whether every parent advancement is granted for the provided player's team.
     *
     * @param uuid The {@link UUID} of the player.
     * @return Whether every parent advancement is granted for the provided player's team.
     */
    public boolean isEveryParentGranted(@NotNull UUID uuid) {
        return isEveryParentGranted(progressionFromUUID(uuid, this));
    }

    /**
     * Returns whether every parent advancement is granted for the provided team.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return Whether every parent advancement is granted for the provided team.
     */
    public abstract boolean isEveryParentGranted(@NotNull TeamProgression progression);

    /**
     * Returns whether any parent advancement is granted for the provided player's team.
     *
     * @param player The player.
     * @return Whether any parent advancement is granted for the provided player's team.
     */
    public boolean isAnyParentGranted(@NotNull Player player) {
        return isAnyParentGranted(uuidFromPlayer(player));
    }

    /**
     * Returns whether any parent advancement is granted for the provided player's team.
     *
     * @param uuid The {@link UUID} of the player.
     * @return Whether any parent advancement is granted for the provided player's team.
     */
    public boolean isAnyParentGranted(@NotNull UUID uuid) {
        return isAnyParentGranted(progressionFromUUID(uuid, this));
    }

    /**
     * Returns whether any parent advancement is granted for the provided team.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return Whether any parent advancement is granted for the provided team.
     */
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
