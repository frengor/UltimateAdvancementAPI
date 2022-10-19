package com.fren_gor.ultimateAdvancementAPI.nms.serverVersion1_19_R1;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public class MinecraftKeyWrapper_serverVersion1_19_R1 extends MinecraftKeyWrapper {

    private final String namespace, key;

    public MinecraftKeyWrapper_serverVersion1_19_R1(@NotNull Object key) {
        throw new UnsupportedOperationException();
    }

    public MinecraftKeyWrapper_serverVersion1_19_R1(@NotNull String namespace, @NotNull String key) {
        AdvancementKey.checkNamespace(namespace);
        AdvancementKey.checkKey(key);
        Preconditions.checkArgument(AdvancementKey.VALID_NAMESPACE.matcher(namespace).matches());
        Preconditions.checkArgument(AdvancementKey.VALID_KEY.matcher(key).matches());
        this.namespace = namespace;
        this.key = key;
    }

    @Override
    @NotNull
    public Object toNMS() {
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public String getNamespace() {
        return namespace;
    }

    @Override
    @NotNull
    public String getKey() {
        return key;
    }

    @Override
    public int compareTo(@NotNull MinecraftKeyWrapper o) {
        Preconditions.checkNotNull(o);
        int value = namespace.compareTo(o.getNamespace());
        return value == 0 ? value : key.compareTo(o.getKey());
    }
}
