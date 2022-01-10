package com.fren_gor.ultimateAdvancementAPI.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public final class CoordAdapter {

    private final Map<AdvancementKey, Entry<Float, Float>> advancementCoords;
    private float lowestX, lowestY;

    public CoordAdapter(@NotNull Map<AdvancementKey, Entry<Float, Float>> advancementCoords) {
        this.advancementCoords = Objects.requireNonNull(advancementCoords, "Advancement coords is null.");
        for (var e : advancementCoords.entrySet()) {
            Preconditions.checkNotNull(e.getKey(), "An AdvancementKey is null.");
            var coords = e.getValue();
            Preconditions.checkNotNull(coords, e.getKey() + "'s coords entry is null.");
            float x = coords.getKey(), y = coords.getValue();
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
        var e = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        if (e == null) {
            throw new IllegalArgumentException("Couldn't find key \"" + key + "\".");
        }
        return e.getKey() + lowestX;
    }

    public float getY(@NotNull AdvancementKey key) {
        var e = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        if (e == null) {
            throw new IllegalArgumentException("Couldn't find key \"" + key + "\".");
        }
        return e.getValue() + lowestY;
    }

    @NotNull
    public Entry<Float, Float> getXAndY(@NotNull AdvancementKey key) {
        var e = advancementCoords.get(Objects.requireNonNull(key, "Key is null."));
        if (e == null) {
            throw new IllegalArgumentException("Couldn't find key \"" + key + "\".");
        }
        return new SimpleEntry<>(e.getKey() + lowestX, e.getValue() + lowestY);
    }

    public float convertBackX(float x) {
        return x - lowestX;
    }

    public float convertBackY(float y) {
        return y - lowestY;
    }

    public static CoordAdapterBuilder builder() {
        return new CoordAdapterBuilder();
    }

    public static final class CoordAdapterBuilder {
        private final Map<AdvancementKey, Entry<Float, Float>> advancementCoords = new HashMap<>();

        public CoordAdapterBuilder() {
        }

        @NotNull
        public CoordAdapterBuilder add(@NotNull AdvancementKey key, float x, float y) {
            Preconditions.checkNotNull(key, "Key is null");
            Preconditions.checkArgument(Float.isFinite(x), key + "'s x value is not finite.");
            Preconditions.checkArgument(Float.isFinite(y), key + "'s y value is not finite.");

            advancementCoords.put(key, new SimpleEntry<>(x, y));
            return this;
        }

        @NotNull
        public CoordAdapterBuilder offset(@NotNull AdvancementKey key, @NotNull AdvancementKey keyOfParent, float offsetX, float offsetY) {
            Preconditions.checkNotNull(key, "Key is null");
            Preconditions.checkNotNull(keyOfParent, "Key of parent is null");
            Preconditions.checkArgument(Float.isFinite(offsetX), key + "'s offsetX value is not finite.");
            Preconditions.checkArgument(Float.isFinite(offsetY), key + "'s offsetY value is not finite.");

            var e = advancementCoords.get(keyOfParent);
            if (e == null) {
                throw new IllegalArgumentException("Cannot find key \"" + keyOfParent + "\".");
            }
            advancementCoords.put(key, new SimpleEntry<>(e.getKey() + offsetX, e.getValue() + offsetY));
            return this;
        }

        @NotNull
        public CoordAdapter build() {
            return new CoordAdapter(advancementCoords);
        }
    }
}
