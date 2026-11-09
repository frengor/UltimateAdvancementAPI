package com.fren_gor.ultimateAdvancementAPI.chat.bungeecord;

import com.fren_gor.ultimateAdvancementAPI.chat.IDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentStyle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BungeeCordDefaultStyle implements IDefaultStyle {

    private static final boolean CHAT_COLOR_HAS_GET_COLOR = ReflectionUtil.hasMethod(ChatColor.class, "getColor");

    private final BungeeCordStyle style;

    public BungeeCordDefaultStyle(@NotNull BungeeCordStyle style) {
        this.style = Objects.requireNonNull(style, "BungeeCordStyle is null.");
    }

    public BungeeCordDefaultStyle(@NotNull ComponentStyle style) {
        this(new BungeeCordStyle(Objects.requireNonNull(style, "ComponentStyle is null.")));
    }

    @Override
    @NotNull
    public Style toAdventureStyle() {
        var builder = Style.style();
        if (style.getColor() != null) {
            TextColor color;
            if (CHAT_COLOR_HAS_GET_COLOR) {
                var rgbColor = style.getColor().getColor();
                color = TextColor.color(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue());
            } else {
                color = LegacyComponentSerializer.parseChar(style.getColor().toString().charAt(1)).color();
            }
            builder.color(color);
        }
        if (style.getShadowColor() != null) {
            var color = style.getShadowColor();
            builder.shadowColor(ShadowColor.shadowColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
        }
        if (style.getFont() != null) {
            builder.font(Key.key(style.getFont()));
        }
        if (style.isBold() != null) {
            builder.decorate(TextDecoration.BOLD);
        }
        if (style.isItalic() != null) {
            builder.decorate(TextDecoration.ITALIC);
        }
        if (style.isUnderlined() != null) {
            builder.decorate(TextDecoration.UNDERLINED);
        }
        if (style.isStrikethrough() != null) {
            builder.decorate(TextDecoration.STRIKETHROUGH);
        }
        if (style.isObfuscated() != null) {
            builder.decorate(TextDecoration.OBFUSCATED);
        }
        return builder.build();
    }

    @Override
    @NotNull
    public BungeeCordStyle toBungeeCordStyle() {
        return this.style;
    }

    @Override
    public boolean isEmpty() {
        return style.isEmpty();
    }

    @Override
    @NotNull
    public IDefaultStyle merge(@NotNull IDefaultStyle style) {
        return new BungeeCordDefaultStyle(this.style.merge(style.toBungeeCordStyle()));
    }
}
