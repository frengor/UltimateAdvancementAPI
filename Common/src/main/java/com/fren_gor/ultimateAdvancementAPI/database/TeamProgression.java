package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.commons.lang.Validate;
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
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateCriteria;

/**
 * TeamProgression objects stores information about a team and its advancement progressions.
 * <p>TeamProgression is used to cache team information from the database.
 */
public final class TeamProgression {

    final AtomicBoolean inCache = new AtomicBoolean(false);
    private final int teamId;
    private final Set<UUID> players;
    private final Map<AdvancementKey, Integer> advancements;

    /**
     * Create a new TeamProgression.
     *
     * @param teamId The unique teamId value to be stored in the database.
     * @param member A member to be added in a new team.
     */
    public TeamProgression(int teamId, @NotNull UUID member) {
        Validate.notNull(member, "Member is null.");
        this.advancements = new ConcurrentHashMap<>();
        this.teamId = teamId;
        players = new HashSet<>();
        players.add(member);
    }

    /**
     * Create a new TeamProgression.
     *
     * @param advancements All the advancement keys with their progression.
     * @param teamId The team id stored in the database.
     * @param members Team member list.
     */
    public TeamProgression(@NotNull Map<AdvancementKey, Integer> advancements, int teamId, @NotNull Collection<UUID> members) {
        Validate.notNull(advancements, "Advancements is null.");
        Validate.notNull(members, "Members is null.");
        this.advancements = new ConcurrentHashMap<>(advancements);
        this.teamId = teamId;
        players = Sets.newHashSetWithExpectedSize(members.size() + 4);
        players.addAll(members);
    }

    /**
     * Returns the progression of the advancement.
     *
     * @param advancement An advancement.
     * @return The criteria progression.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getCriteria(@NotNull Advancement advancement) {
        Validate.notNull(advancement, "Advancement is null.");
        Integer progression = advancements.get(advancement.getKey());

        if (progression != null) {
            if (progression <= advancement.getMaxCriteria())
                return progression;
            else
                return advancement.getMaxCriteria();
        } else {
            return 0;
        }
    }

    /**
     * Returns whether a player is contained by the team.
     *
     * @param player A player.
     * @return Whether a player is contained by the team.
     */
    @Contract(pure = true)
    public boolean contains(@NotNull Player player) {
        return contains(uuidFromPlayer(player));
    }

    /**
     * Returns whether a UUID player is contained by the team.
     *
     * @param uuid A UUID player.
     * @return Whether a UUID player is contained by the team.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean contains(UUID uuid) {
        synchronized (players) {
            return players.contains(uuid);
        }
    }

    /**
     * Returns a copy of the member list.
     *
     * @return A copy of the member list.
     */
    @Contract(pure = true, value = "-> new")
    public Set<@NotNull UUID> getMembersCopy() {
        synchronized (players) {
            return new HashSet<>(players);
        }
    }

    /**
     * Returns the number of members.
     *
     * @return The number of members.
     */
    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getSize() {
        synchronized (players) {
            return players.size();
        }
    }

    /**
     * Run code for each member.
     *
     * @param action The code to run for each member.
     */
    public void forEachMember(@NotNull Consumer<UUID> action) {
        Validate.notNull(action, "Consumer is null.");
        synchronized (players) {
            for (UUID u : players) {
                action.accept(u);
            }
        }
    }

    /**
     * Run code for each member and checks if the code returns {@code true} for every member.
     *
     * @param action The code to run.
     * @return If the code returns {@code true} for every member.
     */
    public boolean everyMemberMatch(@NotNull Predicate<UUID> action) {
        Validate.notNull(action, "Predicate is null.");
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
     * Run code for each member and checks if the code returns {@code true} at least one.
     *
     * @param action The code to run.
     * @return If the code returns {@code true} at least one.
     */
    public boolean anyMemberMatch(@NotNull Predicate<UUID> action) {
        Validate.notNull(action, "Predicate is null.");
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
     * Run code for each member and checks if the code returns {@code false} for every member.
     *
     * @param action The code to run.
     * @return If the code returns {@code false} for every member.
     */
    public boolean noMemberMatch(@NotNull Predicate<UUID> action) {
        Validate.notNull(action, "Predicate is null.");
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
     * Gets whether the current TeamProgression object is valid. A TeamProgression object is valid if and only if it is stored
     * into the caching system (see {@link DatabaseManager}).
     *
     * @return Whether the current TeamProgression object is valid.
     */
    public boolean isValid() {
        return inCache.get();
    }

    /**
     * Sets the criteria of an advancement.
     *
     * @param key The key of the advancement.
     * @param criteria The new criteria progression to be set.
     * @return The previous criteria progress.
     */
    int updateCriteria(@NotNull AdvancementKey key, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        validateCriteria(criteria);
        Integer i = advancements.put(key, criteria);
        return i == null ? 0 : i;
    }

    /**
     * Removes a player from the team.
     *
     * @param uuid The UUID player to be removed.
     */
    void removeMember(UUID uuid) {
        synchronized (players) {
            players.remove(uuid);
        }
    }

    /**
     * Adds a player in the team.
     *
     * @param uuid The UUID player to be added.
     * @return {@code true} if the member list did not already contain the specified member, {@code false} otherwise.
     */
    boolean addMember(@NotNull UUID uuid) {
        Validate.notNull(uuid, "UUID is null.");
        synchronized (players) {
            return players.add(uuid);
        }
    }

    /**
     * Returns a random player of the team.
     *
     * @return A random UUID player of the team.
     */
    @Nullable
    public UUID getAMember() {
        synchronized (players) {
            return Iterables.getFirst(players, null);
        }
    }

    /**
     * Returns an online player of the team.
     *
     * @param manager The database manager.
     * @return An online player of the team, {@code null} if there are none.
     */
    @Nullable
    public Player getAnOnlineMember(@NotNull DatabaseManager manager) {
        Validate.notNull(manager, "DatabaseManager is null.");
        synchronized (players) {
            for (UUID u : players) {
                if (manager.isLoadedAndOnline(u)) {
                    return Bukkit.getPlayer(u);
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TeamProgression{" +
                "teamId=" + teamId +
                '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamProgression that = (TeamProgression) o;

        return teamId == that.teamId;
    }

    /**
     * {@inheritDoc}
     */
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
