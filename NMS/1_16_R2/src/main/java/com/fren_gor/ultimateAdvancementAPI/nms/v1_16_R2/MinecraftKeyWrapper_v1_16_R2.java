package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R2;

import com.fren_gor.ultimateAdvancementAPI.nms.MinecraftKeyWrapper;
import net.minecraft.server.v1_16_R2.MinecraftKey;
import net.minecraft.server.v1_16_R2.ResourceKeyInvalidException;
import org.jetbrains.annotations.NotNull;

public class MinecraftKeyWrapper_v1_16_R2 extends MinecraftKeyWrapper {

    private final MinecraftKey key;

    public MinecraftKeyWrapper_v1_16_R2(@NotNull Object key) {
        if (key instanceof MinecraftKey m) {
            this.key = m;
        }
        throw new ClassCastException(key.getClass().getName() + " is not an instance of " + MinecraftKey.class.getName());
    }

    public MinecraftKeyWrapper_v1_16_R2(@NotNull String namespace, @NotNull String key) {
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
    public int compareTo(@NotNull MinecraftKeyWrapper o) {
        if (o instanceof MinecraftKeyWrapper_v1_16_R2 obj) {
            return key.compareTo(obj.key);
        }
        throw new ClassCastException();
    }
}
