package com.fren_gor.ultimateAdvancementAPI.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @hidden
 */
@Internal
public final class TextUtil {

    public static final int MAX_DEPTH = 512;

    @Internal
    public enum SearchResult {
        FOUND,
        NOT_FOUND,
        EMPTY,
    }

    @NotNull
    public static SearchResult startsWithNewLine(@Nullable BaseComponent component, int depth) {
        if (depth <= 0) {
            throw new IllegalStateException("Exceeded maximum depth of " + MAX_DEPTH + " while processing component.");
        }
        SearchResult r;
        if (component == null) {
            return SearchResult.EMPTY;
        } else if (component instanceof TextComponent textComponent) {
            r = startsWithNewLine(textComponent.getText());
            if (r != SearchResult.EMPTY) {
                return r;
            }
        } else {
            // Use toPlainText() for any other component kind
            return startsWithNewLine(component.toPlainText());
        }

        var extra = component.getExtra();
        if (extra == null) {
            return SearchResult.EMPTY;
        }
        for (var e : extra) {
            if (e != null && (r = startsWithNewLine(e, depth - 1)) != SearchResult.EMPTY) {
                return r;
            }
        }
        return SearchResult.EMPTY;
    }

    @NotNull
    public static SearchResult startsWithNewLine(@Nullable String text) {
        if (text == null) {
            return SearchResult.EMPTY;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                return SearchResult.FOUND;
            } else if (c == ChatColor.COLOR_CHAR) {
                i++; // Skip next character since it is a color code
            } else if (!Character.isWhitespace(c)) { // Continue if c is a white space
                return SearchResult.NOT_FOUND;
            }
        }
        return SearchResult.EMPTY;
    }

    private TextUtil() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
