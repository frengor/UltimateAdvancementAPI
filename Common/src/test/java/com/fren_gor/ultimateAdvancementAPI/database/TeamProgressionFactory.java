package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalOperationException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public final class TeamProgressionFactory {

    public static TeamProgression createTeamProgression(int teamId, @NotNull UUID member) {
        return new TeamProgression(teamId, member);
    }

    public static TeamProgression createTeamProgression(int teamId, @NotNull Map<AdvancementKey, Integer> advancements, @NotNull Collection<UUID> members) {
        return new TeamProgression(advancements, teamId, members);
    }

    private TeamProgressionFactory() {
        throw new IllegalOperationException("TeamProgressionFactory is a utility class!");
    }
}
