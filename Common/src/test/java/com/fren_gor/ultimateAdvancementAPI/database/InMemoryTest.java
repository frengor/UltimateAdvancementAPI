package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
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
