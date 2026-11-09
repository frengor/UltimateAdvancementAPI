package com.fren_gor.ultimateAdvancementAPI.chat;

import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil.SearchResult;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.fren_gor.ultimateAdvancementAPI.chat.TextUtil.MAX_DEPTH;
import static org.junit.jupiter.api.Assertions.*;

public class TextUtilTest {

    private static final String WHITESPACE_SPAM = "\t " + ChatColor.DARK_PURPLE + "\r" + ChatColor.of(new java.awt.Color(52, 27, 244));

    private static final List<String> NEW_LINES_STRINGS = List.of(
        "\n",
        "\n\n",
        WHITESPACE_SPAM + "\n" + WHITESPACE_SPAM + "\n",
        WHITESPACE_SPAM + "\nsomething"
    );
    private static final List<String> NO_NEW_LINES_STRINGS = List.of(
        "a",
        "a\nb",
        "a" + WHITESPACE_SPAM + "\n" + WHITESPACE_SPAM + "\n",
        "a" + WHITESPACE_SPAM + "\n" + WHITESPACE_SPAM + "\nb"
    );
    private static final List<TextComponent> NEW_LINES_COMPONENTS = NEW_LINES_STRINGS.stream().map(TextComponent::new).toList();
    private static final List<TextComponent> NO_NEW_LINES_COMPONENTS = NO_NEW_LINES_STRINGS.stream().map(TextComponent::new).toList();

    @Test
    void startsWithNewLineEmptyTest() {
        Assertions.assertEquals(SearchResult.EMPTY, TextUtil.startsWithNewLine((BaseComponent) null, MAX_DEPTH));
        assertEquals(SearchResult.EMPTY, TextUtil.startsWithNewLine(new TextComponent(""), MAX_DEPTH));
        assertEquals(SearchResult.EMPTY, TextUtil.startsWithNewLine(new TextComponent(WHITESPACE_SPAM), MAX_DEPTH));
        assertEquals(SearchResult.EMPTY, TextUtil.startsWithNewLine((String) null));
        assertEquals(SearchResult.EMPTY, TextUtil.startsWithNewLine(""));
        assertEquals(SearchResult.EMPTY, TextUtil.startsWithNewLine(WHITESPACE_SPAM));
    }

    @Test
    void startsWithNewLineEmptyComplexTest() {
        for (var i : new int[]{0, 1, 2, 5, 10}) {
            assertEquals(SearchResult.EMPTY, TextUtil.startsWithNewLine(nest(new TextComponent(""), i), MAX_DEPTH));
            assertEquals(SearchResult.EMPTY, TextUtil.startsWithNewLine(nest(new TextComponent(WHITESPACE_SPAM), i), MAX_DEPTH));
        }
    }

    @Test
    void startsWithNewLineSimpleTest() {
        NEW_LINES_STRINGS.forEach(l -> assertEquals(SearchResult.FOUND, TextUtil.startsWithNewLine(l)));
        NEW_LINES_COMPONENTS.forEach(c -> assertEquals(SearchResult.FOUND, TextUtil.startsWithNewLine(c, MAX_DEPTH)));
    }

    @Test
    void startsWithNewLineComplexTest() {
        NEW_LINES_COMPONENTS.stream().flatMap(c -> Stream.of(
            nest(c, 0),
            nest(c, 1),
            nest(c, 2),
            nest(c, 5),
            nest(c, 10)
        )).forEach(c -> assertEquals(SearchResult.FOUND, TextUtil.startsWithNewLine(c, MAX_DEPTH)));
    }

    @Test
    void startsWithNewLineNoNewLineTest() {
        NO_NEW_LINES_STRINGS.forEach(l -> assertEquals(SearchResult.NOT_FOUND, TextUtil.startsWithNewLine(l)));
        NO_NEW_LINES_COMPONENTS.forEach(c -> assertEquals(SearchResult.NOT_FOUND, TextUtil.startsWithNewLine(c, MAX_DEPTH)));
        assertEquals(SearchResult.NOT_FOUND, TextUtil.startsWithNewLine(new KeybindComponent("key"), MAX_DEPTH));
    }

    @Test
    void startsWithNewLineNoNewLineComplexTest() {
        NO_NEW_LINES_COMPONENTS.stream().flatMap(c -> Stream.of(
            nest(c, 0),
            nest(c, 1),
            nest(c, 2),
            nest(c, 5),
            nest(c, 10)
        )).forEach(c -> assertEquals(SearchResult.NOT_FOUND, TextUtil.startsWithNewLine(c, MAX_DEPTH)));
        for (var i : new int[]{0, 1, 2, 5, 10}) {
            assertEquals(SearchResult.NOT_FOUND, TextUtil.startsWithNewLine(nest(new KeybindComponent("key"), i), MAX_DEPTH));
        }
    }

    @Test
    void maxDepthTest() {
        assertDoesNotThrow(() -> TextUtil.startsWithNewLine(nestVertically(new TextComponent(""), MAX_DEPTH - 1), MAX_DEPTH));
        assertThrows(IllegalStateException.class, () -> TextUtil.startsWithNewLine(nestVertically(new TextComponent(""), MAX_DEPTH), MAX_DEPTH));
    }

    private BaseComponent nest(BaseComponent component, int level) {
        TextComponent first = new TextComponent();
        TextComponent last = first;
        for (int i = 0; i < level; i++) {
            for (int n = 0; n <= (i / 2); n++)
                first.addExtra(new TextComponent(WHITESPACE_SPAM));
            first.addExtra(last = new TextComponent());
            for (int n = 0; n <= (i / 2); n++)
                first.addExtra(new TextComponent());
        }
        last.addExtra(component);
        return first;
    }

    private BaseComponent nestVertically(BaseComponent component, int level) {
        if (level == 0) {
            return component;
        }
        component.addExtra(nestVertically(component.duplicate(), level - 1));
        return component;
    }
}
