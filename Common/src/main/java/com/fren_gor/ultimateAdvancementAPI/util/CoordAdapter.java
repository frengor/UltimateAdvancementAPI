package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The {@code CoordAdapter} class adds support for advancement negative coordinates.
 * <p>An example usage is shown below:
 * <blockquote><pre>
 * // Keys of the advancements to create
 * var advKey1 = new AdvancementKey(myPlugin, "first_advancement");
 * var advKey2 = new AdvancementKey(myPlugin, "second_advancement");
 * var advKey3 = new AdvancementKey(myPlugin, "third_advancement");
 *
 * // Create the CoordAdapter instance
 * CoordAdapter adapter = CoordAdapter.builder()
 *         .add(advKey1, 0, 0)  // Will become (0, 1)
 *         .add(advKey2, 1, -1) // Will become (1, 0)
 *         .add(advKey3, 1, 1)  // Will become (1, 2)
 *         .build();
 *
 * // Create the AdvancementDisplays
 * var advDisplay1 = new AdvancementDisplay(icon1, title1, ... , adapter.getX(advKey1), adapter.getY(advKey1));
 * var advDisplay2 = new AdvancementDisplay(icon2, title2, ... , adapter.getX(advKey2), adapter.getY(advKey2));
 * var advDisplay3 = new AdvancementDisplay(icon3, title3, ... , adapter.getX(advKey3), adapter.getY(advKey3));
 *
 * // Create the advancements
 * var adv1 = new RootAdvancement(tab, advKey1.getKey(), advDisplay1, "textures/block/stone.png");
 * var adv2 = new BaseAdvancement(advKey2.getKey(), advDisplay2, adv1);
 * var adv3 = new BaseAdvancement(advKey3.getKey(), advDisplay3, adv1, 5);</pre></blockquote>
 */
public final class CoordAdapter {

    private final Map<AdvancementKey, Coord> advancementCoords;
    private float lowestX, lowestY;

    /**
     * Creates a new {@code CoordAdapter}.
     *
     * @param advancementCoords The map of every {@link AdvancementKey} with its position. Negative values are accepted.
     * @throws NullPointerException If {@code advancementCoords} or any of its values are {@code null}.
     * @see Coord
     */
    public CoordAdapter(@NotNull Map<AdvancementKey, Coord> advancementCoords) {
        this.advancementCoords = Objects.requireNonNull(advancementCoords, "Advancement coords is null.");
        for (var e : advancementCoords.entrySet()) {
            Preconditions.checkNotNull(e.getKey(), "An AdvancementKey is null.");
            Coord coords = e.getValue();
            Preconditions.checkNotNull(coords, e.getKey() + "'s coords entry is null.");

            if (coords.x < lowestX) {
                lowestX = coords.x;
            }
            if (coords.y < lowestY) {
                lowestY = coords.y;
            }
        }
        if (lowestX < 0) {
            lowestX = -lowestX;
        }
        if (lowestY < 0) {
            lowestY = -lowestY;
        }
    }

    /**
     * Gets the converted x coordinate to be used in a {@link AdvancementDisplay}.
     *
     * @param key The {@link AdvancementKey} of the coordinate.
     * @return The converted x coordinate to be used in a {@link AdvancementDisplay}.
     * @throws IllegalArgumentException If the provided {@link AdvancementKey} is not present in the adapter.
     */
    public float getX(@NotNull AdvancementKey key) throws IllegalArgumentException {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        Preconditions.checkArgument(coord != null, "Couldn't find key \"" + key + "\".");
        return coord.x + lowestX;
    }

    /**
     * Gets the converted y coordinate to be used in a {@link AdvancementDisplay}.
     *
     * @param key The {@link AdvancementKey} of the coordinate.
     * @return The converted y coordinate to be used in a {@link AdvancementDisplay}.
     * @throws IllegalArgumentException If the provided {@link AdvancementKey} is not present in the adapter.
     */
    public float getY(@NotNull AdvancementKey key) throws IllegalArgumentException {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        Preconditions.checkArgument(coord != null, "Couldn't find key \"" + key + "\".");
        return coord.y + lowestY;
    }

    /**
     * Gets the converted x and y coordinates to be used in a {@link AdvancementDisplay}.
     *
     * @param key The {@link AdvancementKey} of the coordinates.
     * @return The converted x and y coordinates to be used in a {@link AdvancementDisplay}.
     * @throws IllegalArgumentException If the provided {@link AdvancementKey} is not present in the adapter.
     */
    @NotNull
    public Coord getXAndY(@NotNull AdvancementKey key) throws IllegalArgumentException {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        Preconditions.checkArgument(coord != null, "Couldn't find key \"" + key + "\".");
        return new Coord(coord.x + lowestX, coord.y + lowestY);
    }

    /**
     * Converts back the provided {@link AdvancementDisplay}'s x coordinate to the original one.
     *
     * @param display The {@link AdvancementDisplay}.
     * @return The original x coordinate.
     */
    public float getOriginalX(@NotNull AdvancementDisplay display) {
        return getOriginalX(Objects.requireNonNull(display, "AdvancementDisplay is null.").getX());
    }

    /**
     * Converts back the provided {@link AdvancementKey}'s x coordinate to the original one passed to this {@link CoordAdapter}.
     *
     * @param key The {@link AdvancementKey} of the coordinate.
     * @return The original x coordinate.
     * @throws IllegalArgumentException If the provided {@link AdvancementKey} is not present in the adapter.
     */
    public float getOriginalX(@NotNull AdvancementKey key) throws IllegalArgumentException {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        Preconditions.checkArgument(coord != null, "Couldn't find key \"" + key + "\".");
        return coord.x;
    }

    /**
     * Converts back the provided x coordinate to the original one.
     *
     * @param x The coordinate to convert back.
     * @return The original x coordinate.
     * @throws IllegalArgumentException If the provided x coordinate is NaN or infinite.
     */
    public float getOriginalX(float x) {
        Preconditions.checkArgument(Float.isFinite(x), "y coordinate is not finite.");
        return x - lowestX;
    }

    /**
     * Converts back the provided {@link AdvancementDisplay}'s y coordinate to the original one.
     *
     * @param display The {@link AdvancementDisplay}.
     * @return The original y coordinate.
     */
    public float getOriginalY(@NotNull AdvancementDisplay display) {
        return getOriginalY(Objects.requireNonNull(display, "AdvancementDisplay is null.").getY());
    }

    /**
     * Converts back the provided {@link AdvancementKey}'s y coordinate to the original one passed to this {@link CoordAdapter}.
     *
     * @param key The {@link AdvancementKey} of the coordinate.
     * @return The original y coordinate.
     * @throws IllegalArgumentException If the provided {@link AdvancementKey} is not present in the adapter.
     */
    public float getOriginalY(@NotNull AdvancementKey key) throws IllegalArgumentException {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        Preconditions.checkArgument(coord != null, "Couldn't find key \"" + key + "\".");
        return coord.y;
    }

    /**
     * Converts back the provided y coordinate to the original one.
     *
     * @param y The coordinate to convert back.
     * @return The original y coordinate.
     * @throws IllegalArgumentException If the provided y coordinate is NaN or infinite.
     */
    public float getOriginalY(float y) {
        Preconditions.checkArgument(Float.isFinite(y), "y coordinate is not finite.");
        return y - lowestY;
    }

    /**
     * Converts back the provided {@link AdvancementDisplay}'s x and y coordinates to the original ones.
     *
     * @param display The {@link AdvancementDisplay}.
     * @return The original x and y coordinates.
     */
    @NotNull
    public Coord getOriginalXAndY(@NotNull AdvancementDisplay display) {
        Preconditions.checkNotNull(display, "AdvancementDisplay is null.");
        return getOriginalXAndY(display.getX(), display.getY());
    }

    /**
     * Converts back the provided {@link AdvancementKey}'s x and y coordinates to the original ones passed to this {@link CoordAdapter}.
     *
     * @param key The {@link AdvancementKey} of the coordinates.
     * @return The original x and y coordinates.
     * @throws IllegalArgumentException If the provided {@link AdvancementKey} is not present in the adapter.
     */
    @NotNull
    public Coord getOriginalXAndY(@NotNull AdvancementKey key) throws IllegalArgumentException {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        Preconditions.checkArgument(coord != null, "Couldn't find key \"" + key + "\".");
        return coord;
    }

    /**
     * Converts back the provided x and y coordinates to the original ones.
     *
     * @param coord The coordinates to convert back.
     * @return The original x and y coordinates.
     */
    @NotNull
    public Coord getOriginalXAndY(@NotNull Coord coord) {
        Preconditions.checkNotNull(coord, "Coord is null.");
        return getOriginalXAndY(coord.x, coord.y);
    }

    /**
     * Converts back the provided x and y coordinates to the original ones.
     *
     * @param x The x coordinate to convert back.
     * @param y The y coordinate to convert back.
     * @return The original x and y coordinates.
     * @throws IllegalArgumentException If the provided x or y coordinates are NaN or infinite.
     */
    @NotNull
    public Coord getOriginalXAndY(float x, float y) {
        return new Coord(getOriginalX(x), getOriginalY(y));
    }

    /**
     * Creates a new {@link CoordAdapterBuilder}.
     *
     * @return A new {@link CoordAdapterBuilder}.
     */
    @NotNull
    @Contract(pure = true, value = "-> new")
    public static CoordAdapterBuilder builder() {
        return new CoordAdapterBuilder();
    }

    /**
     * A builder for {@link CoordAdapter}s.
     */
    public static final class CoordAdapterBuilder {
        private final Map<AdvancementKey, Coord> advancementCoords = new HashMap<>();

        /**
         * Creates a new {@code CoordAdapterBuilder}.
         */
        public CoordAdapterBuilder() {
        }

        /**
         * Adds the provided {@link AdvancementKey} with its coordinates to the builder.
         *
         * @param key The {@link AdvancementKey} to add.
         * @param x The x coordinate. Must be finite.
         * @param y The y coordinate. Must be finite.
         * @return This builder.
         */
        @NotNull
        public CoordAdapterBuilder add(@NotNull AdvancementKey key, float x, float y) {
            Preconditions.checkNotNull(key, "Key is null");
            Preconditions.checkArgument(Float.isFinite(x), key + "'s x value is not finite.");
            Preconditions.checkArgument(Float.isFinite(y), key + "'s y value is not finite.");

            advancementCoords.put(key, new Coord(x, y));
            return this;
        }

        /**
         * Adds the provided {@link AdvancementKey} to the builder.
         * <p>Its coordinates are based off the coordinates of the provided parent's {@link AdvancementKey}.
         * <p>In fact, they are calculated adding the offsets to the parent's coordinates.
         *
         * @param key The {@link AdvancementKey} to add.
         * @param keyOfParent The parent's {@link AdvancementKey}. Must be already added to the builder.
         * @param offsetX The offset on the x-axis. Must be finite.
         * @param offsetY The offset on the y-axis. Must be finite.
         * @return This builder.
         * @throws IllegalArgumentException If either of the provided offsets is NaN or infinite.
         */
        @NotNull
        public CoordAdapterBuilder offset(@NotNull AdvancementKey key, @NotNull AdvancementKey keyOfParent, float offsetX, float offsetY) throws IllegalArgumentException {
            Preconditions.checkNotNull(key, "Key is null");
            Preconditions.checkNotNull(keyOfParent, "Key of parent is null");
            Preconditions.checkArgument(Float.isFinite(offsetX), key + "'s offsetX value is not finite.");
            Preconditions.checkArgument(Float.isFinite(offsetY), key + "'s offsetY value is not finite.");

            Coord coord = advancementCoords.get(keyOfParent);
            if (coord == null) {
                throw new IllegalArgumentException("Cannot find key \"" + keyOfParent + "\".");
            }
            advancementCoords.put(key, new Coord(coord.x + offsetX, coord.y + offsetY));
            return this;
        }

        /**
         * Builds the {@link CoordAdapter}.
         *
         * @return The built {@link CoordAdapter}.
         */
        @NotNull
        public CoordAdapter build() {
            return new CoordAdapter(advancementCoords);
        }
    }

    /**
     * Record which represents a coordinate in the Cartesian plane.
     *
     * @param x The x coordinate. Can be any finite value.
     * @param y The y coordinate. Can be any finite value.
     * @see Float#isFinite(float)
     */
    public record Coord(float x, float y) {
        /**
         * Creates a new {@code Coord}.
         *
         * @param x The x coordinate. Can be any finite value.
         * @param y The y coordinate. Can be any finite value.
         * @throws IllegalArgumentException If {@code x} or {@code y} is NaN or infinite.
         * @see Float#isFinite(float)
         */
        public Coord {
            Preconditions.checkArgument(Float.isFinite(x), "x coordinate is not finite.");
            Preconditions.checkArgument(Float.isFinite(y), "y coordinate is not finite.");
        }
    }
}
