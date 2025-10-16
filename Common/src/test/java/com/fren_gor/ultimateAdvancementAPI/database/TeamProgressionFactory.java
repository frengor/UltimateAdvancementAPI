package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalOperationException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

public final class TeamProgressionFactory {

    private static final SimpleTeamProgressionFactory FACTORY;

    static {
        FACTORY = mock(
                SimpleTeamProgressionFactory.class,
                withSettings().defaultAnswer(i -> {
                    throw new UnsupportedOperationException("Mocked method.");
                })
        );
        doCallRealMethod().when(FACTORY).createTeamProgression(anyInt());
        doCallRealMethod().when(FACTORY).createTeamProgression(anyInt(), any(), any());
    }

    public static TeamProgression createTeamProgression(int teamId) {
        return FACTORY.createTeamProgression(teamId);
    }

    public static TeamProgression createTeamProgression(int teamId, @NotNull UUID member) {
        TeamProgression pro = FACTORY.createTeamProgression(teamId);
        pro.addMember(member);
        return pro;
    }

    public static TeamProgression createTeamProgression(int teamId, @NotNull Map<AdvancementKey, Integer> advancements, @NotNull Collection<UUID> members) {
        return FACTORY.createTeamProgression(teamId, advancements, members);
    }

    private TeamProgressionFactory() {
        throw new IllegalOperationException("TeamProgressionFactory is a utility class!");
    }

    private static abstract class SimpleTeamProgressionFactory implements IDatabase {
        public TeamProgression createTeamProgression(int teamId) {
            return new TeamProgression(teamId);
        }

        public TeamProgression createTeamProgression(int teamId, Map<AdvancementKey, Integer> advancements, Collection<UUID> members) {
            return new TeamProgression(advancements, teamId, members);
        }
    }
}
