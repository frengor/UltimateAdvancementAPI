package com.fren_gor.ultimateAdvancementAPI.nms.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper for JSON strings.
 * <p>Use in formal parameters instead of a plain {@link String} to avoid accidentally passing a non-JSON string where one is expected.
 *
 * @param jsonString The JSON string.
 */
public record JsonString(@NotNull String jsonString) {
    public JsonString {
        Preconditions.checkNotNull(jsonString, "String is null.");
    }
}
