package com.fren_gor.ultimateAdvancementAPI.nms.v1_20_R4;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MinecraftKeyWrapper_v1_20_R4 extends MinecraftKeyWrapper {

    private final ResourceLocation key;

    public MinecraftKeyWrapper_v1_20_R4(@NotNull Object key) {
        this.key = (ResourceLocation) key;
    }

    public MinecraftKeyWrapper_v1_20_R4(@NotNull String namespace, @NotNull String key) {
        try {
            this.key = new ResourceLocation(namespace, key);
        } catch (ResourceLocationException e) {
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
        return key.compareTo((ResourceLocation) obj.toNMS());
    }

    @Override
    @NotNull
    public ResourceLocation toNMS() {
        return key;
    }
}
