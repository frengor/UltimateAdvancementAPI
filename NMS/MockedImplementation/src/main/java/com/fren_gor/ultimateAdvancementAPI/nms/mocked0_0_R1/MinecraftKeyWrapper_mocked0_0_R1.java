package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class MinecraftKeyWrapper_mocked0_0_R1 extends MinecraftKeyWrapper {

    private static final Pattern VALID_NAMESPACE = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9_.-/]+");

    private final String namespace, key;

    public MinecraftKeyWrapper_mocked0_0_R1(@NotNull Object key) {
        throw new UnsupportedOperationException();
    }

    public MinecraftKeyWrapper_mocked0_0_R1(@NotNull String namespace, @NotNull String key) {
        Preconditions.checkArgument(VALID_NAMESPACE.matcher(namespace).matches());
        Preconditions.checkArgument(VALID_KEY.matcher(key).matches());
        this.namespace = namespace;
        this.key = key;
    }

    @Override
    @NotNull
    public Object toNMS() {
        return this;
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
