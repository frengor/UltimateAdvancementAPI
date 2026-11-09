package com.fren_gor.ultimateAdvancementAPI.chat.adventure;

import com.fren_gor.ultimateAdvancementAPI.chat.IDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordStyle;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Locale;
import java.util.Objects;

public class AdventureDefaultStyle implements IDefaultStyle {

    private static final boolean CHAT_COLOR_HAS_GET_COLOR = ReflectionUtil.hasMethod(ChatColor.class, "getColor");

    private final Style style;

    public AdventureDefaultStyle(@NotNull Style style) {
        this.style = Objects.requireNonNull(style, "Style is null.");
    }

    @Override
    @NotNull
    public IDefaultStyle merge(@NotNull IDefaultStyle style) {
        return new AdventureDefaultStyle(this.style.merge(style.toAdventureStyle()));
    }

    @Override
    @NotNull
    public Style toAdventureStyle() {
        return this.style;
    }

    @Override
    @NotNull
    public BungeeCordStyle toBungeeCordStyle() {
        var shadowColor = style.shadowColor();
        var font = style.font();
        return new BungeeCordStyle()
            .color(fromTextColor(style.color()))
            .shadowColor(shadowColor == null ? null : new Color(shadowColor.value(), true))
            .font(font == null ? null : font.asString())
            .bold(style.hasDecoration(TextDecoration.BOLD))
            .italic(style.hasDecoration(TextDecoration.ITALIC))
            .underlined(style.hasDecoration(TextDecoration.UNDERLINED))
            .strikethrough(style.hasDecoration(TextDecoration.STRIKETHROUGH))
            .obfuscated(style.hasDecoration(TextDecoration.OBFUSCATED));
    }

    @Override
    public boolean isEmpty() {
        return style.isEmpty();
    }

    private ChatColor fromTextColor(@Nullable TextColor color) {
        if (color == null) {
            return null;
        }
        if (CHAT_COLOR_HAS_GET_COLOR) {
            return ChatColor.of(new Color(color.value()));
        } else {
            return ChatColor.valueOf(NamedTextColor.nearestTo(color).toString().toUpperCase(Locale.ROOT));
        }
    }
}
