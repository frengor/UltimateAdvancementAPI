package com.fren_gor.ultimateAdvancementAPI.tests;

import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter.Coord;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoordAdapterTest {

    @Test
    public void basicCoordTest() {
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
    public void NaNCoordTest() {
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
    public void infiniteCoordTest() {
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
    public void testDocCode() {
        Plugin myPlugin = InterfaceImplementer.newFakePlugin("myPlugin");
        Utils.mockServer(() -> {
            // Keys of the advancements to create
            var advKey1 = new AdvancementKey(myPlugin, "first_advancement");
            var advKey2 = new AdvancementKey(myPlugin, "second_advancement");
            var advKey3 = new AdvancementKey(myPlugin, "third_advancement");

            // Create the CoordAdapter instance
            CoordAdapter adapter = CoordAdapter.builder()
                    .add(advKey1, 0, 0)  // Will become (0, 1)
                    .add(advKey2, 1, -1) // Will become (1, 0)
                    .add(advKey3, 1, 1)  // Will become (1, 2)
                    .build();

            // Create the AdvancementDisplays
            var advDisplay1 = new AdvancementDisplay.Builder(Material.GRASS_BLOCK, "Title1").coords(adapter, advKey1).build();
            var advDisplay2 = new AdvancementDisplay.Builder(Material.GRASS_BLOCK, "Title2").coords(adapter, advKey2).build();
            var advDisplay3 = new AdvancementDisplay.Builder(Material.GRASS_BLOCK, "Title3").coords(adapter, advKey3).build();

            // Create the advancements
            var adv1 = new RootAdvancement(Utils.newAdvancementTab(myPlugin, advKey1.getNamespace()), advKey1.getKey(), advDisplay1, "textures/block/stone.png");
            var adv2 = new BaseAdvancement(advKey2.getKey(), advDisplay2, adv1);
            var adv3 = new BaseAdvancement(advKey3.getKey(), advDisplay3, adv1, 5);

            // Just to be sure the comments above are correct
            assertEquals(new Coord(0, 0), adapter.getOriginalXAndY(0, 1));
            assertEquals(new Coord(1, -1), adapter.getOriginalXAndY(1, 0));
            assertEquals(new Coord(1, 1), adapter.getOriginalXAndY(1, 2));
        });
    }
}
