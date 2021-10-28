package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R1;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import net.minecraft.server.v1_16_R1.MinecraftKey;
import net.minecraft.server.v1_16_R1.ResourceKeyInvalidException;
import org.jetbrains.annotations.NotNull;

public class MinecraftKeyWrapper_v1_16_R1 extends MinecraftKeyWrapper {

    private final MinecraftKey key;

    public MinecraftKeyWrapper_v1_16_R1(@NotNull Object key) {
        this.key = (MinecraftKey) key;
    }

    public MinecraftKeyWrapper_v1_16_R1(@NotNull String namespace, @NotNull String key) {
        try {
            this.key = new MinecraftKey(namespace, key);
        } catch (ResourceKeyInvalidException e) {
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
        return key.getKey();
    }

    @Override
    public int compareTo(@NotNull MinecraftKeyWrapper obj) {
        return key.compareTo(((MinecraftKeyWrapper_v1_16_R1) obj).key);
    }

    @Override
    @NotNull
    public MinecraftKey getNMSKey() {
        return key;
    }
}
