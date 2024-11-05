package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UAAPIExtension.class)
public class TeamProgressionTest {

    @Test
    void emptyTeamTest() {
        TeamProgression pro = TeamProgressionFactory.createTeamProgression(0);
        assertEquals(0, pro.getSize());
        assertNull(pro.getAMember());
    }

    @Test
    void creationTest(Player player1, Player player2, Player player3) {
        TeamProgression pro = TeamProgressionFactory.createTeamProgression(0, player1.getUniqueId());
        assertEquals(0, pro.getTeamId());
        assertEquals(1, pro.getSize());
        assertTrue(pro.contains(player1));
        assertTrue(pro.contains(player1.getUniqueId()));
        assertFalse(pro.contains(player2));
        assertFalse(pro.contains(player2.getUniqueId()));
        assertFalse(pro.isValid());
        pro.inCache.set(true);
        assertTrue(pro.isValid());
        pro.addMember(player2.getUniqueId());
        assertTrue(pro.contains(player1));
        assertTrue(pro.contains(player1.getUniqueId()));
        assertTrue(pro.contains(player2));
        assertTrue(pro.contains(player2.getUniqueId()));
        assertFalse(pro.contains(player3));
        assertFalse(pro.contains(player3.getUniqueId()));
        assertTrue(List.of(player1.getUniqueId(), player2.getUniqueId()).contains(pro.getAMember()));
    }

    @Test
    void immutabilityTest(UUID uuid, UUID uuid1, UUID uuid2) {
        TeamProgression pro = TeamProgressionFactory.createTeamProgression(0);
        Set<UUID> members = pro.getMembers();

        assertThrows(UnsupportedOperationException.class, () -> members.add(uuid1));
        assertThrows(UnsupportedOperationException.class, () -> members.remove(uuid2));

        pro.addMember(uuid);
        Set<UUID> members1 = pro.getMembers();
        assertThrows(UnsupportedOperationException.class, () -> members1.add(uuid1));
        assertThrows(UnsupportedOperationException.class, () -> members1.remove(uuid2));

        pro.removeMember(uuid);
        Set<UUID> members2 = pro.getMembers();
        assertThrows(UnsupportedOperationException.class, () -> members2.add(uuid1));
        assertThrows(UnsupportedOperationException.class, () -> members2.remove(uuid2));
    }

    @Test
    void addRemoveTest(UUID uuid1, UUID uuid2, UUID uuid3) {
        Consumer<Set<UUID>> assertContains1 = set -> {
            assertEquals(1, set.size());
            assertTrue(set.contains(uuid1));
            assertFalse(set.contains(uuid2));
            assertFalse(set.contains(uuid3));
        };

        Consumer<Set<UUID>> assertContains2 = set -> {
            assertEquals(2, set.size());
            assertTrue(set.contains(uuid1));
            assertTrue(set.contains(uuid2));
            assertFalse(set.contains(uuid3));
        };

        Consumer<Set<UUID>> assertContains3 = set -> {
            assertEquals(3, set.size());
            assertTrue(set.contains(uuid1));
            assertTrue(set.contains(uuid2));
            assertTrue(set.contains(uuid3));
        };

        Consumer<Set<UUID>> assertContains4 = set -> {
            assertEquals(2, set.size());
            assertTrue(set.contains(uuid1));
            assertFalse(set.contains(uuid2));
            assertTrue(set.contains(uuid3));
        };

        Consumer<Set<UUID>> assertContains5 = set -> {
            assertEquals(1, set.size());
            assertFalse(set.contains(uuid1));
            assertFalse(set.contains(uuid2));
            assertTrue(set.contains(uuid3));
        };

        TeamProgression pro = TeamProgressionFactory.createTeamProgression(0);

        Set<UUID> beforeAdd = pro.getMembers();
        assertTrue(beforeAdd.isEmpty());

        pro.addMember(uuid1);
        Set<UUID> afterAdd1 = pro.getMembers();
        assertContains1.accept(afterAdd1);
        assertNotSame(beforeAdd, afterAdd1);

        pro.addMember(uuid2);
        Set<UUID> afterAdd2 = pro.getMembers();
        assertContains1.accept(afterAdd1);
        assertContains2.accept(afterAdd2);
        assertNotSame(beforeAdd, afterAdd2);
        assertNotSame(afterAdd1, afterAdd2);

        pro.addMember(uuid3);
        Set<UUID> afterAdd3 = pro.getMembers();
        assertContains1.accept(afterAdd1);
        assertContains2.accept(afterAdd2);
        assertContains3.accept(afterAdd3);
        assertNotSame(beforeAdd, afterAdd3);
        assertNotSame(afterAdd1, afterAdd3);
        assertNotSame(afterAdd2, afterAdd3);

        pro.removeMember(uuid2);
        Set<UUID> afterRemove3 = pro.getMembers();
        assertContains4.accept(afterRemove3);
        assertContains1.accept(afterAdd1);
        assertContains2.accept(afterAdd2);
        assertContains3.accept(afterAdd3);
        assertNotSame(afterAdd3, afterRemove3);

        pro.removeMember(uuid1);
        Set<UUID> afterRemove2 = pro.getMembers();
        assertContains4.accept(afterRemove3);
        assertContains5.accept(afterRemove2);
        assertContains1.accept(afterAdd1);
        assertContains2.accept(afterAdd2);
        assertContains3.accept(afterAdd3);
        assertNotSame(afterAdd3, afterRemove2);
        assertNotSame(afterRemove3, afterRemove2);

        pro.removeMember(uuid3);
        Set<UUID> afterRemove1 = pro.getMembers();
        assertContains4.accept(afterRemove3);
        assertContains5.accept(afterRemove2);
        assertTrue(afterRemove1.isEmpty());
        assertContains1.accept(afterAdd1);
        assertContains2.accept(afterAdd2);
        assertContains3.accept(afterAdd3);
        assertNotSame(afterAdd3, afterRemove1);
        assertNotSame(afterRemove3, afterRemove1);
        assertNotSame(afterRemove2, afterRemove1);
    }

    @Test
    void advancementProgressionTest(AdvancementKey key1, AdvancementKey key2, AdvancementKey key3) {
        TeamProgression pro = TeamProgressionFactory.createTeamProgression(0, Map.of(key1, 10, key2, 5), Collections.emptyList());

        assertEquals(10, pro.getRawProgression(key1));
        assertEquals(5, pro.getRawProgression(key2));
        assertEquals(0, pro.getRawProgression(key3));
    }
}
