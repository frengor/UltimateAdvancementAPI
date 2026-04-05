package com.fren_gor.ultimateAdvancementAPI.nms.v26_1_R1;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class MinecraftKeyWrapper_v26_1_R1 extends MinecraftKeyWrapper {

    private final Identifier key;

    public MinecraftKeyWrapper_v26_1_R1(@NotNull Object key) {
        this.key = (Identifier) key;
    }

    public MinecraftKeyWrapper_v26_1_R1(@NotNull String namespace, @NotNull String key) {
        try {
            this.key = Identifier.fromNamespaceAndPath(namespace, key);
        } catch (IdentifierException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    @NotNull
    public String getNamespace() {
        return key.getNamespace();
    }

    @Override
    @NotNull
    public String getKey() {
        return key.getPath();
    }

    @Override
    public int compareTo(@NotNull MinecraftKeyWrapper obj) {
        return key.compareTo((Identifier) obj.toNMS());
    }

    @Override
    @NotNull
    public Identifier toNMS() {
        return key;
    }
}
