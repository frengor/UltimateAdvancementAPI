package com.fren_gor.ultimateAdvancementAPI.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.tests.AutoInject;
import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter.Coord;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UAAPIExtension.class)
public class CoordAdapterTest {

    @AutoInject
    private AdvancementMain main;

    @Test
    void basicCoordTest() {
        var coord = new Coord(0, 0);
        assertEquals(0, coord.x(), 0);
        assertEquals(0, coord.y(), 0);
        coord = new Coord(1, 5);
        assertEquals(1, coord.x(), 0);
        assertEquals(5, coord.y(), 0);
        coord = new Coord(-10, -3);
        assertEquals(-10, coord.x(), 0);
        assertEquals(-3, coord.y(), 0);
    }

    @Test
    void NaNCoordTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Coord(Float.NaN, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Coord(0, Float.NaN);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Coord(Float.NaN, Float.NaN);
        });
    }

    @Test
    void infiniteCoordTest() {
        // +infinite
        assertThrows(IllegalArgumentException.class, () -> {
            new Coord(Float.POSITIVE_INFINITY, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Coord(0, Float.POSITIVE_INFINITY);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Coord(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        });

        // -infinite
        assertThrows(IllegalArgumentException.class, () -> {
            new Coord(Float.NEGATIVE_INFINITY, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Coord(0, Float.NEGATIVE_INFINITY);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Coord(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        });
    }

    @Test
    void coordAdapterTest() {
        testCoordAdapterHelper(List.of(new Coord(0, 0), new Coord(5, 7), new Coord(-2, -11)));
        testCoordAdapterHelper(List.of(new Coord(0, 0), new Coord(0, 0), new Coord(0, 0)));
        testCoordAdapterHelper(List.of(new Coord(100, 98), new Coord(54, 43), new Coord(32, 8)));
        testCoordAdapterHelper(List.of(new Coord(-100, -98), new Coord(-54, -43), new Coord(-32, -8)));
        testCoordAdapterHelper(List.of(new Coord(100, 98), new Coord(54, 43), new Coord(32, 8), new Coord(-100, -98), new Coord(-54, -43), new Coord(-32, -8)));
    }

    private void testCoordAdapterHelper(@NotNull Collection<Coord> coordinates) {
        Map<AdvancementKey, Coord> map = Maps.newHashMapWithExpectedSize(coordinates.size());
        int i = 0;
        for (Coord c : coordinates) {
            map.put(new AdvancementKey("namespace", String.valueOf(i++)), c);
        }

        CoordAdapter adapter = new CoordAdapter(map);
        for (var entry : map.entrySet()) {
            assertEquals(entry.getValue(), adapter.getOriginalXAndY(entry.getKey()));
            assertEquals(entry.getValue(), new Coord(adapter.getOriginalX(entry.getKey()), adapter.getOriginalY(entry.getKey())));

            Coord coords = adapter.getXAndY(entry.getKey());
            assertEquals(coords, new Coord(adapter.getX(entry.getKey()), adapter.getY(entry.getKey())));
            assertTrue(coords.x() >= 0);
            assertTrue(coords.y() >= 0);
            assertEquals(entry.getValue().x(), adapter.getOriginalX(coords.x()), 0);
            assertEquals(entry.getValue().y(), adapter.getOriginalY(coords.y()), 0);
        }

        // Test converted values
        for (Set<AdvancementKey> set : Sets.combinations(map.keySet(), 2)) {
            assertEquals(2, set.size(), "Guava Sets#combinations bugged!");
            var iter = set.iterator();
            AdvancementKey key1 = iter.next();
            AdvancementKey key2 = iter.next();
            Coord c1 = map.get(key1);
            Coord c2 = map.get(key2);
            Coord c1A = adapter.getXAndY(key1);
            Coord c2A = adapter.getXAndY(key2);
            assertEquals(c1.x() - c2.x(), c1A.x() - c2A.x(), 0);
            assertEquals(c1.y() - c2.y(), c1A.y() - c2A.y(), 0);
        }
    }

    @Test
    void docCodeTest() {
        Plugin myPlugin = MockBukkit.createMockPlugin("myPlugin");
        AdvancementTab myTab = main.createAdvancementTab(myPlugin, "mytab", "textures/block/stone.png");

        // Keys of the advancements to create
        var advKey1 = new AdvancementKey(myTab.getNamespace(), "first_advancement");
        var advKey2 = new AdvancementKey(myTab.getNamespace(), "second_advancement");
        var advKey3 = new AdvancementKey(myTab.getNamespace(), "third_advancement");

        // Create the CoordAdapter instance
        CoordAdapter adapter = CoordAdapter.builder()
                .add(advKey1, 0, 0)  // Will become (0, 1)
                .add(advKey2, 1, -1) // Will become (1, 0)
                .add(advKey3, 1, 1)  // Will become (1, 2)
                .build();

        // Create the AdvancementDisplays
        var advDisplay1 = new AdvancementDisplayBuilder(Material.GRASS_BLOCK, "Title1").coords(adapter, advKey1).build();
        var advDisplay2 = new AdvancementDisplayBuilder(Material.GRASS_BLOCK, "Title2").coords(adapter, advKey2).build();
        var advDisplay3 = new AdvancementDisplayBuilder(Material.GRASS_BLOCK, "Title3").coords(adapter, advKey3).build();

        // Create the advancements
        var adv1 = new RootAdvancement(myTab, advKey1.getKey(), advDisplay1);
        var adv2 = new BaseAdvancement(advKey2.getKey(), advDisplay2, adv1);
        var adv3 = new BaseAdvancement(advKey3.getKey(), advDisplay3, adv1, 5);

        myTab.registerAdvancements(adv1, adv2, adv3);

        // Just to be sure the comments above are correct
        assertEquals(new Coord(0, 0), adapter.getOriginalXAndY(0, 1));
        assertEquals(new Coord(1, -1), adapter.getOriginalXAndY(1, 0));
        assertEquals(new Coord(1, 1), adapter.getOriginalXAndY(1, 2));
        assertEquals(new Coord(0, 0), adapter.getOriginalXAndY(advDisplay1));
        assertEquals(new Coord(1, -1), adapter.getOriginalXAndY(advDisplay2));
        assertEquals(new Coord(1, 1), adapter.getOriginalXAndY(advDisplay3));
    }

    @Test
    void offsetTest(AdvancementKey parent, AdvancementKey child) {
        assertThrows(IllegalArgumentException.class, () -> CoordAdapter.builder().offset(child, parent, 0, 0));
        for (int i = -10; i <= 10; i++) {
            for (int t = -10; t <= 10; t++) {
                for (int n = -10; n <= 10; n++) {
                    for (int j = -10; j <= 10; j++) {
                        CoordAdapter coordAdapter = CoordAdapter.builder().add(parent, i, t).offset(child, parent, n, j).build();
                        assertEquals(i, coordAdapter.getOriginalX(parent), 0);
                        assertEquals(i + n, coordAdapter.getOriginalX(child), 0);
                        assertEquals(t, coordAdapter.getOriginalY(parent), 0);
                        assertEquals(t + j, coordAdapter.getOriginalY(child), 0);
                    }
                }
            }
        }
    }
}
