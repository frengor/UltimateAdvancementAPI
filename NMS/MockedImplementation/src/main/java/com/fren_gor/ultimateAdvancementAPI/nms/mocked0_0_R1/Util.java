package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1;

import com.google.gson.JsonParseException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class Util {

    @NotNull
    public static BaseComponent fromJSON(@NotNull String json) throws JsonParseException {
        if (json == null) {
            return new TextComponent("");
        }
        try {
            return ComponentSerializer.deserialize(json);
        } catch (IllegalArgumentException ex) {
            // Keep consistency with real NMS code, where a JsonParseException is thrown here instead of IllegalArgumentException
            throw new JsonParseException("Failed to parse JSON", ex);
        }
    }

    private Util() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
