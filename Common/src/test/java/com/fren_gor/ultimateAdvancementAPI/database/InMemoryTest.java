package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.exceptions.TeamNotRegisteredException;
import com.fren_gor.ultimateAdvancementAPI.tests.database.FallibleDBImpl.RuntimePlannedFailureException;
import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import com.fren_gor.ultimateAdvancementAPI.tests.AutoInject;
import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UAAPIExtension.class)
public class InMemoryTest {

    private InMemory db;
    @AutoInject
    private AdvancementMain advancementMain;
    @AutoInject
    private UUID uuid;

    @BeforeEach
    void init() throws Exception {
        db = new InMemory(advancementMain);
        db.setUp();
    }

    @AfterEach
    void tearDown() throws Exception {
        db.close();
        db = null;
    }

    @Test
    void inMemoryTest(AdvancementKey key) throws Exception {
        var res = db.loadOrRegisterPlayer(uuid, "Dummy");
        assertEquals(0, res.getKey().getSize());
        assertFalse(res.getKey().contains(uuid));
        assertTrue(res.getKey().noMemberMatch(u -> u.equals(uuid)));
        assertTrue(res.getValue()); // This is a new team

        {
            TeamProgression loaded = db.loadUUID(uuid);
            assertEquals(res.getKey().getTeamId(), loaded.getTeamId());
            assertTrue(loaded.contains(uuid));
        }

        {
            TeamProgression loaded = db.loadTeam(res.getKey().getTeamId());
            assertEquals(res.getKey().getTeamId(), loaded.getTeamId());
            assertTrue(loaded.contains(uuid));
        }

        res.getKey().addMember(uuid);
        assertTrue(res.getKey().contains(uuid));

        db.updateAdvancement(key, res.getKey().getTeamId(), 10);

        boolean executedAtLeastOnce = false;
        for (var entry : db.getTeamAdvancements(res.getKey().getTeamId()).entrySet()) {
            executedAtLeastOnce = true;
            assertEquals(key, entry.getKey());
            assertEquals(10, entry.getValue());
        }
        assertTrue(executedAtLeastOnce);
    }

    @Test
    void testTransactionAtomicity(AdvancementKey key1, AdvancementKey key2, AdvancementKey key3, AdvancementKey key4) throws Exception {
        var loadResult = db.loadOrRegisterPlayer(uuid, "Dummy");
        TeamProgression progression = loadResult.getKey();
        progression.addMember(uuid);

        final Entry<AdvancementKey, Boolean> entry1 = new SimpleEntry<>(key1, true);
        final Entry<AdvancementKey, Boolean> entry2 = new SimpleEntry<>(key2, false);
        final Entry<AdvancementKey, Boolean> entry3 = new SimpleEntry<>(key3, false);
        final Entry<AdvancementKey, Boolean> entry4 = new SimpleEntry<>(key4, true);

        final List<Entry<AdvancementKey, Boolean>> all = List.of(entry1, entry2, entry3, entry4);

        for (var entry : all)
            db.updateAdvancement(entry.getKey(), progression.getTeamId(), 1);

        for (var entry : all)
            db.setUnredeemed(entry.getKey(), entry.getValue(), progression.getTeamId());

        assertTrue(db.openConnection().getAutoCommit());
        assertThrows(RuntimePlannedFailureException.class, () -> {
            db.unsetUnredeemed(new FallibleList<>(all, 2), progression.getTeamId());
        });
        assertTrue(db.openConnection().getAutoCommit());

        assertEquals(all, db.getUnredeemed(progression.getTeamId()));

        assertTrue(db.openConnection().getAutoCommit());
        db.unsetUnredeemed(all, progression.getTeamId());
        assertTrue(db.openConnection().getAutoCommit());

        assertTrue(db.getUnredeemed(progression.getTeamId()).isEmpty());
    }

    @Test
    void testPermanentTeams() throws Exception {
        TeamProgression team1 = db.createNewTeam();
        TeamProgression team2 = db.createNewTeam();
        TeamProgression team3 = db.createNewTeam();

        var teamId1 = team1.getTeamId();
        var teamId2 = team2.getTeamId();
        var teamId3 = team3.getTeamId();

        assertNotEquals(teamId1, teamId2);

        assertFalse(db.isTeamPermanent(teamId1));
        assertFalse(db.isTeamPermanent(teamId2));
        assertFalse(db.isTeamPermanent(teamId3));

        assertTrue(db.getPermanentTeams().isEmpty());

        db.setTeamPermanent(teamId1, true);

        assertTrue(db.isTeamPermanent(teamId1));
        assertFalse(db.isTeamPermanent(teamId2));
        assertFalse(db.isTeamPermanent(teamId3));

        List<Integer> teams = db.getPermanentTeams();
        assertEquals(1, teams.size());
        assertTrue(teams.contains(teamId1));

        db.setTeamPermanent(teamId2, true);

        assertTrue(db.isTeamPermanent(teamId1));
        assertTrue(db.isTeamPermanent(teamId2));
        assertFalse(db.isTeamPermanent(teamId3));

        teams = db.getPermanentTeams();
        assertEquals(2, teams.size());
        assertTrue(teams.contains(teamId1));
        assertTrue(teams.contains(teamId2));

        db.setTeamPermanent(teamId2, false);

        assertTrue(db.isTeamPermanent(teamId1));
        assertFalse(db.isTeamPermanent(teamId2));
        assertFalse(db.isTeamPermanent(teamId3));

        teams = db.getPermanentTeams();
        assertEquals(1, teams.size());
        assertTrue(teams.contains(teamId1));
    }

    @Test
    void testClearUpTeams(UUID uuid2) throws Exception {
        var team1 = db.loadOrRegisterPlayer(uuid, "Dummy").getKey().getTeamId();
        var team2 = db.loadOrRegisterPlayer(uuid2, "Dummy").getKey().getTeamId();
        db.setTeamPermanent(team2, true);
        var team3 = db.createNewTeam().getTeamId();
        db.setTeamPermanent(team3, true);
        var team4 = db.createNewTeam().getTeamId();

        db.clearUpTeams();

        assertTrue(db.loadTeam(team1).contains(uuid));
        assertTrue(db.loadTeam(team2).contains(uuid2));
        assertEquals(0, db.loadTeam(team3).getSize());
        assertThrows(TeamNotRegisteredException.class, () -> db.loadTeam(team4));
    }

    private static class FallibleList<T> extends LinkedList<T> {
        private int untilFail;

        public FallibleList(List<T> base, int untilFail) {
            this.addAll(base);
            this.untilFail = untilFail;
        }

        @Override
        @NotNull
        public Iterator<T> iterator() {
            Iterator<T> t = super.iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return t.hasNext();
                }

                @Override
                public T next() {
                    if (untilFail-- <= 0) {
                        throw new RuntimePlannedFailureException();
                    }
                    return t.next();
                }
            };
        }
    }
}
