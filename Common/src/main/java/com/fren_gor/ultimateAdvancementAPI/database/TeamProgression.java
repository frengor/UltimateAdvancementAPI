package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalOperationException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateTeamProgression;

/**
 * The {@code TeamProgression} class stores information about a team and its advancement progressions.
 * <p>{@code TeamProgression} is used to cache team information by the caching system.
 * <p>This class is thread safe.
 */
public final class TeamProgression {

    final AtomicBoolean inCache = new AtomicBoolean(false);
    private final int teamId;
    private final Set<UUID> players;
    final ProgressionUpdaterManager progressionUpdaterManager = new ProgressionUpdaterManager();

    // Should be updated only from inside ProgressionUpdate having synchronized over ProgressionUpdate.this
    private final Map<AdvancementKey, Integer> advancements;

    /**
     * Creates a new TeamProgression for a team with one player in it.
     * <p><strong>Note:</strong> TeamProgression should be instantiated only by database-related classes.
     * Any illegal instantiation will throw an {@link IllegalOperationException}.
     *
     * @param teamId The team id.
     * @param member The member of the team.
     * @throws IllegalOperationException If this constructor is called by a class not in the
     *         {@code com.fren_gor.ultimateAdvancementAPI.database} package or in one of its sub-packages.
     */
    @Internal
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
     * @throws IllegalOperationException If this constructor is called by a class not in the
     *         {@code com.fren_gor.ultimateAdvancementAPI.database} package or in one of its sub-packages.
     */
    @Internal
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
        if (!caller.getPackage().getName().startsWith("com.fren_gor.ultimateAdvancementAPI.database")) {
            throw new IllegalOperationException("TeamProgression can be instantiated only by classes inside the com.fren_gor.ultimateAdvancementAPI.database package or its sub-packages.");
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

    int getRawProgression(@NotNull AdvancementKey key) {
        Preconditions.checkNotNull(key, "AdvancementKey is null.");

        Integer progression = advancements.get(key);
        return progression != null ? progression : 0;
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
     * Moves a player from this team to another atomically.
     *
     * @param newTeam The new team's {@link TeamProgression}.
     * @param uuid The player's uuid.
     */
    void movePlayer(@NotNull TeamProgression newTeam, @NotNull UUID uuid) {
        validateTeamProgression(newTeam);
        Preconditions.checkNotNull(uuid, "UUID is null.");

        // Synchronize on both this.players and newTeam.players to perform the move atomically
        synchronized (this.players) {
            synchronized (newTeam.players) {
                players.remove(uuid);
                newTeam.players.add(uuid);
            }
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

    final class ProgressionUpdaterManager {
        private final Map<AdvancementKey, ProgressionUpdate> progressionUpdates = new HashMap<>();

        public ProgressionUpdaterManager() {
        }

        public ScheduleResult scheduleSetUpdate(AdvancementKey key, int newValue) {
            return scheduleUpdate(key, new ProgressionUpdateInfo(ProgressionUpdateInfo.SET, newValue));
        }

        public ScheduleResult scheduleIncrementUpdate(AdvancementKey key, int increment) {
            return scheduleUpdate(key, new ProgressionUpdateInfo(ProgressionUpdateInfo.INCREMENT, increment));
        }

        private ScheduleResult scheduleUpdate(AdvancementKey key, ProgressionUpdateInfo updateInfo) {
            synchronized (progressionUpdates) {
                ProgressionUpdate update = progressionUpdates.computeIfAbsent(key, advancementKey -> {
                    return new ProgressionUpdate(advancementKey, getRawProgression(advancementKey));
                });
                return update.scheduleUpdate(updateInfo);
            }
        }

        final class ProgressionUpdate {
            final AdvancementKey advancementKey;
            private int oldValue;
            final ArrayDeque<ProgressionUpdateInfo> updateQueue = new ArrayDeque<>();

            public ProgressionUpdate(AdvancementKey key, int oldValue) {
                this.advancementKey = key;
                this.oldValue = oldValue;
            }

            private synchronized ScheduleResult scheduleUpdate(ProgressionUpdateInfo info) {
                updateQueue.addFirst(info);
                int value = info.applyUpdate(oldValue);
                Integer oldValue = advancements.put(advancementKey, value);
                return new ScheduleResult(this, oldValue == null ? 0 : oldValue, value);
            }

            public synchronized int updateEndedSuccessfully() {
                ProgressionUpdateInfo info = updateQueue.removeLast();
                oldValue = info.applyUpdate(oldValue);
                return oldValue;
            }

            public synchronized int updateEndedExceptionally() {
                int value = oldValue;
                updateQueue.removeLast();
                for (ProgressionUpdateInfo info : updateQueue) {
                    value = info.applyUpdate(value);
                }
                advancements.put(advancementKey, value);
                return value;
            }

            public void checkDisposal() {
                synchronized (progressionUpdates) {
                    synchronized (ProgressionUpdate.this) {
                        if (updateQueue.isEmpty()) {
                            progressionUpdates.remove(advancementKey);
                        }
                    }
                }
            }

            public synchronized int getOldValue() {
                return oldValue;
            }
        }

        // value is an increment when operation==INCREMENT and the progression to set when operation==SET
        private record ProgressionUpdateInfo(int operation, int value) {
            public static final int INCREMENT = 0;
            public static final int SET = 1;

            public int applyUpdate(int currentValue) {
                if (operation == INCREMENT) {
                    return Math.max(currentValue + value, 0);
                } else { // operation == SET
                    return value;
                }
            }
        }

        record ScheduleResult(ProgressionUpdate progressionUpdate, int oldValue, int newCachedValue) {}
    }
}
