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
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateCriteria;

public final class TeamProgression {

    @Getter
    private final int teamId;
    private final Set<UUID> players;
    private final Map<AdvancementKey, Integer> advancements;

    public TeamProgression(int teamId, @NotNull UUID member) {
        Validate.notNull(member, "Member is null.");
        this.advancements = new ConcurrentHashMap<>();
        this.teamId = teamId;
        players = new HashSet<>();
        players.add(member);
    }

    public TeamProgression(@NotNull Map<AdvancementKey, Integer> advancements, int teamId, @NotNull Collection<UUID> members) {
        Validate.notNull(advancements, "Advancements is null.");
        Validate.notNull(members, "Members is null.");
        this.advancements = new ConcurrentHashMap<>(advancements);
        this.teamId = teamId;
        players = Sets.newHashSetWithExpectedSize(members.size() + 4);
        players.addAll(members);
    }

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

    @Contract(pure = true)
    public boolean contains(@NotNull Player player) {
        return contains(uuidFromPlayer(player));
    }

    @Contract(pure = true, value = "null -> false")
    public boolean contains(UUID uuid) {
        synchronized (players) {
            return players.contains(uuid);
        }
    }

    @Contract(pure = true, value = "-> new")
    public Set<@NotNull UUID> getMembersCopy() {
        synchronized (players) {
            return new HashSet<>(players);
        }
    }

    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getSize() {
        synchronized (players) {
            return players.size();
        }
    }

    public void forEachMember(@NotNull Consumer<UUID> action) {
        Validate.notNull(action, "Consumer is null.");
        synchronized (players) {
            for (UUID u : players) {
                action.accept(u);
            }
        }
    }

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

    int updateCriteria(@NotNull AdvancementKey key, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        validateCriteria(criteria);
        Integer i = advancements.put(key, criteria);
        return i == null ? 0 : i;
    }

    void removeMember(UUID uuid) {
        synchronized (players) {
            players.remove(uuid);
        }
    }

    boolean addMember(@NotNull UUID uuid) {
        Validate.notNull(uuid, "UUID is null.");
        synchronized (players) {
            return players.add(uuid);
        }
    }

    @Nullable
    public UUID getAMember() {
        synchronized (players) {
            return Iterables.getFirst(players, null);
        }
    }

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
}
