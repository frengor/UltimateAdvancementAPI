package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CoordAdapter {

    private final Map<AdvancementKey, Coord> advancementCoords;
    private float lowestX, lowestY;

    public CoordAdapter(@NotNull Map<AdvancementKey, Coord> advancementCoords) {
        this.advancementCoords = Objects.requireNonNull(advancementCoords, "Advancement coords is null.");
        for (var e : advancementCoords.entrySet()) {
            Preconditions.checkNotNull(e.getKey(), "An AdvancementKey is null.");
            Coord coords = e.getValue();
            Preconditions.checkNotNull(coords, e.getKey() + "'s coords entry is null.");
            float x = coords.x, y = coords.y;
            Preconditions.checkArgument(Float.isFinite(x), e.getKey() + "'s x value is not finite.");
            Preconditions.checkArgument(Float.isFinite(y), e.getKey() + "'s y value is not finite.");

            if (x < lowestX) {
                lowestX = x;
            }
            if (y < lowestY) {
                lowestY = y;
            }
        }
    }

    public float getX(@NotNull AdvancementKey key) throws IllegalArgumentException {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        if (coord == null) {
            throw new IllegalArgumentException("Couldn't find key \"" + key + "\".");
        }
        return coord.x + lowestX;
    }

    public float getY(@NotNull AdvancementKey key) {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        if (coord == null) {
            throw new IllegalArgumentException("Couldn't find key \"" + key + "\".");
        }
        return coord.y + lowestY;
    }

    @NotNull
    public Coord getXAndY(@NotNull AdvancementKey key) {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        if (coord == null) {
            throw new IllegalArgumentException("Couldn't find key \"" + key + "\".");
        }
        return new Coord(coord.x + lowestX, coord.y + lowestY);
    }

    public float getOriginalX(@NotNull AdvancementDisplay display) {
        return getOriginalX(Objects.requireNonNull(display, "AdvancementDisplay is null.").getX());
    }

    public float getOriginalX(@NotNull AdvancementKey key) throws IllegalArgumentException {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        if (coord == null) {
            throw new IllegalArgumentException("Couldn't find key \"" + key + "\".");
        }
        return coord.x;
    }

    public float getOriginalX(float x) {
        return x - lowestX;
    }

    public float getOriginalY(@NotNull AdvancementDisplay display) {
        return getOriginalY(Objects.requireNonNull(display, "AdvancementDisplay is null.").getY());
    }

    public float getOriginalY(@NotNull AdvancementKey key) {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        if (coord == null) {
            throw new IllegalArgumentException("Couldn't find key \"" + key + "\".");
        }
        return coord.y;
    }

    public float getOriginalY(float y) {
        return y - lowestY;
    }

    @NotNull
    public Coord getOriginalXAndY(@NotNull AdvancementDisplay display) {
        Preconditions.checkNotNull(display, "AdvancementDisplay is null.");
        return getOriginalXAndY(display.getX(), display.getY());
    }

    @NotNull
    public Coord getOriginalXAndY(@NotNull AdvancementKey key) {
        Coord coord = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        if (coord == null) {
            throw new IllegalArgumentException("Couldn't find key \"" + key + "\".");
        }
        return coord;
    }

    @NotNull
    public Coord getOriginalXAndY(@NotNull Coord coord) {
        Preconditions.checkNotNull(coord, "Coord is null.");
        return getOriginalXAndY(coord.x, coord.y);
    }

    @NotNull
    public Coord getOriginalXAndY(float x, float y) {
        return new Coord(getOriginalX(x), getOriginalY(y));
    }

    @NotNull
    public static CoordAdapterBuilder builder() {
        return new CoordAdapterBuilder();
    }

    public static final class CoordAdapterBuilder {
        private final Map<AdvancementKey, Coord> advancementCoords = new HashMap<>();

        public CoordAdapterBuilder() {
        }

        @NotNull
        public CoordAdapterBuilder add(@NotNull AdvancementKey key, float x, float y) {
            Preconditions.checkNotNull(key, "Key is null");
            Preconditions.checkArgument(Float.isFinite(x), key + "'s x value is not finite.");
            Preconditions.checkArgument(Float.isFinite(y), key + "'s y value is not finite.");

            advancementCoords.put(key, new Coord(x, y));
            return this;
        }

        @NotNull
        public CoordAdapterBuilder offset(@NotNull AdvancementKey key, @NotNull AdvancementKey keyOfParent, float offsetX, float offsetY) {
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

        @NotNull
        public CoordAdapter build() {
            return new CoordAdapter(advancementCoords);
        }
    }

    public record Coord(float x, float y) {
    }
}
