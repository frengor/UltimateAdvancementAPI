package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalOperationException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateProgressionValue;

/**
 * The {@code TeamProgression} class stores information about a team and its advancement progressions.
 * <p>{@code TeamProgression} is used to cache team information by the caching system.
 * <p>This class is thread safe.
 */
public final class TeamProgression {

    final AtomicBoolean inCache = new AtomicBoolean(false);
    private final int teamId;
    private final Set<UUID> players;
    private final Map<AdvancementKey, Integer> advancements;

    /**
     * Creates a new TeamProgression for a team with one player in it.
     * <p><strong>Note:</strong> TeamProgression should be instantiated only by database-related classes.
     * Any illegal instantiation will throw an {@link IllegalOperationException}.
     *
     * @param teamId The team id.
     * @param member The member of the team.
     * @throws IllegalOperationException If this constructor is called by a class which is not an instance of {@link IDatabase}.
     */
    public TeamProgression(int teamId, @NotNull UUID member) {
        validateCaller(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass());
        Preconditions.checkNotNull(member, "Member is null.");
        this.advancements = new ConcurrentHashMap<>();
        this.teamId = teamId;
        players = new HashSet<>();
        players.add(member);
    }

    /**
     * Creates a new TeamProgression for a team with more than one player in it.
     * <p><strong>Note:</strong> TeamProgression should be instantiated only by database-related classes.
     * Any illegal instantiation will throw an {@link IllegalOperationException}.
     *
     * @param advancements All the advancement keys with their progression.
     * @param teamId The team id.
     * @param members A collection of team members.
     * @throws IllegalOperationException If this constructor is called by a class which is not an instance of {@link IDatabase}.
     */
    public TeamProgression(@NotNull Map<AdvancementKey, Integer> advancements, int teamId, @NotNull Collection<UUID> members) {
        validateCaller(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass());
        Preconditions.checkNotNull(advancements, "Advancements is null.");
        Preconditions.checkNotNull(members, "Members is null.");
        this.advancements = new ConcurrentHashMap<>(advancements);
        this.teamId = teamId;
        players = Sets.newHashSetWithExpectedSize(members.size() + 4);
        players.addAll(members);
    }

    private void validateCaller(@NotNull Class<?> caller) throws IllegalOperationException {
        if (!IDatabase.class.isAssignableFrom(caller)) {
            throw new IllegalOperationException("TeamProgression can be instantiated only by database implementations (IDatabase).");
        }
    }

    /**
     * Gets the progression of the provided advancement for the team.
     *
     * @param advancement The advancement.
     * @return The current progression of the team for the provided advancement.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getProgression(@NotNull Advancement advancement) {
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        Integer progression = advancements.get(advancement.getKey());

        if (progression != null) {
            if (progression <= advancement.getMaxProgression())
                return progression;
            else
                return advancement.getMaxProgression();
        } else {
            return 0;
        }
    }

    /**
     * Returns whether the provided player is part of the team.
     *
     * @param player The player.
     * @return Whether the provided player is part of the team.
     */
    @Contract(pure = true)
    public boolean contains(@NotNull Player player) {
        return contains(uuidFromPlayer(player));
    }

    /**
     * Returns whether the provided player is part of the team.
     *
     * @param uuid The {@link UUID} of the player.
     * @return Whether the provided player is part of the team.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean contains(UUID uuid) {
        synchronized (players) {
            return players.contains(uuid);
        }
    }

    /**
     * Returns a modifiable copy of the team members set.
     *
     * @return A modifiable copy of the team members set.
     */
    @Contract(pure = true, value = "-> new")
    public Set<@NotNull UUID> getMembersCopy() {
        synchronized (players) {
            return new HashSet<>(players);
        }
    }

    /**
     * Returns the team members count.
     *
     * @return The team members count.
     */
    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getSize() {
        synchronized (players) {
            return players.size();
        }
    }

    /**
     * Runs the provided {@link Consumer} for each member of the team.
     *
     * @param action The {@link Consumer} to run for each member.
     */
    public void forEachMember(@NotNull Consumer<UUID> action) {
        Preconditions.checkNotNull(action, "Consumer is null.");
        synchronized (players) {
            for (UUID u : players) {
                action.accept(u);
            }
        }
    }

    /**
     * Returns whether every member of the team matches the provided {@link Predicate}.
     * <p>This method may not call the {@link Predicate} for every team member, since it stops on the first non-matching one.
     *
     * @param action The {@link Predicate} to run.
     * @return Whether the {@link Predicate} returns {@code true} for every member, or {@code true} if the team is empty.
     */
    public boolean everyMemberMatch(@NotNull Predicate<UUID> action) {
        Preconditions.checkNotNull(action, "Predicate is null.");
        synchronized (players) {
            for (UUID u : players) {
                if (!action.test(u)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Returns whether any member of the team matches the provided {@link Predicate}.
     * <p>This method may not call the {@link Predicate} for every team member, since it stops on the first matching one.
     *
     * @param action The {@link Predicate} to run.
     * @return Whether the {@link Predicate} returns {@code true} for at least one member, or {@code false} if the team is empty.
     */
    public boolean anyMemberMatch(@NotNull Predicate<UUID> action) {
        Preconditions.checkNotNull(action, "Predicate is null.");
        synchronized (players) {
            for (UUID u : players) {
                if (action.test(u)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Returns whether no member of the team matches the provided {@link Predicate}.
     * <p>This method may not call the {@link Predicate} for every team member, since it stops on the first matching one.
     *
     * @param action The {@link Predicate} to run.
     * @return Whether the {@link Predicate} returns {@code false} for every member, or {@code true} if the team is empty.
     */
    public boolean noMemberMatch(@NotNull Predicate<UUID> action) {
        Preconditions.checkNotNull(action, "Predicate is null.");
        synchronized (players) {
            for (UUID u : players) {
                if (action.test(u)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Gets whether the current TeamProgression object is valid. A TeamProgression object is valid if and only if
     * it is stored into the caching system (see {@link DatabaseManager}).
     *
     * @return Whether the current TeamProgression object is valid.
     * @since 1.0.2
     */
    public boolean isValid() {
        return inCache.get();
    }

    /**
     * Sets the progression of the provided advancement for the team.
     *
     * @param key The key of the advancement.
     * @param progression The new progression to be set.
     * @return The previous progression.
     */
    int updateProgression(@NotNull AdvancementKey key, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        validateProgressionValue(progression);
        Integer i = advancements.put(key, progression);
        return i == null ? 0 : i;
    }

    /**
     * Removes the provided player from the team.
     *
     * @param uuid The {@link UUID} of the player to be removed.
     */
    void removeMember(UUID uuid) {
        synchronized (players) {
            players.remove(uuid);
        }
    }

    /**
     * Adds the provided player to the team if it's not already part of it.
     *
     * @param uuid The {@link UUID} of the player to be added.
     */
    void addMember(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "UUID is null.");
        synchronized (players) {
            players.add(uuid);
        }
    }

    /**
     * Returns the {@link UUID} of a team member. No particular player is preferred by this operation and
     * the returned member may change from invocation to invocation.
     *
     * @return The {@link UUID} of a team member, or {@code null} if the team is empty.
     */
    @Nullable
    public UUID getAMember() {
        synchronized (players) {
            return Iterables.getFirst(players, null);
        }
    }

    /**
     * Returns an online team member, if possible.
     *
     * @param manager The database manager.
     * @return An online team member, or {@code null} if there are none.
     */
    @Nullable
    public Player getAnOnlineMember(@NotNull DatabaseManager manager) {
        Preconditions.checkNotNull(manager, "DatabaseManager is null.");
        synchronized (players) {
            for (UUID u : players) {
                if (manager.isLoadedAndOnline(u)) {
                    return Bukkit.getPlayer(u);
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "TeamProgression{" +
                "teamId=" + teamId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamProgression that = (TeamProgression) o;

        return teamId == that.teamId;
    }

    @Override
    public int hashCode() {
        return teamId;
    }

    /**
     * Returns the team unique id.
     *
     * @return The team unique id.
     */
    public int getTeamId() {
        return teamId;
    }
}
