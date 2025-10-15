package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.stream.Stream;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.EMPTY;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.NEW_LINE;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.NO_NEW_LINE;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UAAPIExtension.class)
public class AdvancementUtilsTest {

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
        assertEquals(EMPTY, AdvancementUtils.startsWithNewLine((BaseComponent) null));
        assertEquals(EMPTY, AdvancementUtils.startsWithNewLine(new TextComponent("")));
        assertEquals(EMPTY, AdvancementUtils.startsWithNewLine(new TextComponent(WHITESPACE_SPAM)));
        assertEquals(EMPTY, AdvancementUtils.startsWithNewLine((String) null));
        assertEquals(EMPTY, AdvancementUtils.startsWithNewLine(""));
        assertEquals(EMPTY, AdvancementUtils.startsWithNewLine(WHITESPACE_SPAM));
    }

    @Test
    void startsWithNewLineEmptyComplexTest() {
        for (var i : new int[]{0, 1, 2, 5, 10}) {
            assertEquals(EMPTY, AdvancementUtils.startsWithNewLine(nest(new TextComponent(""), i)));
            assertEquals(EMPTY, AdvancementUtils.startsWithNewLine(nest(new TextComponent(WHITESPACE_SPAM), i)));
        }
    }

    @Test
    void startsWithNewLineSimpleTest() {
        NEW_LINES_STRINGS.forEach(l -> assertEquals(NEW_LINE, AdvancementUtils.startsWithNewLine(l)));
        NEW_LINES_COMPONENTS.forEach(c -> assertEquals(NEW_LINE, AdvancementUtils.startsWithNewLine(c)));
    }

    @Test
    void startsWithNewLineComplexTest() {
        NEW_LINES_COMPONENTS.stream().flatMap(c -> Stream.of(
                nest(c, 0),
                nest(c, 1),
                nest(c, 2),
                nest(c, 5),
                nest(c, 10)
        )).forEach(c -> assertEquals(NEW_LINE, AdvancementUtils.startsWithNewLine(c)));
    }

    @Test
    void startsWithNewLineNoNewLineTest() {
        NO_NEW_LINES_STRINGS.forEach(l -> assertEquals(NO_NEW_LINE, AdvancementUtils.startsWithNewLine(l)));
        NO_NEW_LINES_COMPONENTS.forEach(c -> assertEquals(NO_NEW_LINE, AdvancementUtils.startsWithNewLine(c)));
        assertEquals(NO_NEW_LINE, AdvancementUtils.startsWithNewLine(new KeybindComponent("key")));
    }

    @Test
    void startsWithNewLineNoNewLineComplexTest() {
        NO_NEW_LINES_COMPONENTS.stream().flatMap(c -> Stream.of(
                nest(c, 0),
                nest(c, 1),
                nest(c, 2),
                nest(c, 5),
                nest(c, 10)
        )).forEach(c -> assertEquals(NO_NEW_LINE, AdvancementUtils.startsWithNewLine(c)));
        for (var i : new int[]{0, 1, 2, 5, 10}) {
            assertEquals(NO_NEW_LINE, AdvancementUtils.startsWithNewLine(nest(new KeybindComponent("key"), i)));
        }
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
}
