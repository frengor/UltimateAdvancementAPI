package com.fren_gor.ultimateAdvancementAPI.chat.bungeecord;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for conversions between BungeeCord and Adventure components.
 * <p>
 * NO NOT USE unless Adventure is known to be present!
 */
final class AdventureConverter {

    private static final boolean CHAT_COLOR_HAS_GET_COLOR = ReflectionUtil.hasMethod(ChatColor.class, "getColor");
    private static final boolean STYLE_HAS_SHADOW_COLOR = ReflectionUtil.hasMethod(Style.class, "shadowColor");

    @NotNull
    public static Component toAdventureComponent(@NotNull BaseComponent component) {
        // TODO: check old versions compatibility
        return GsonComponentSerializer.gson().deserializeFromTree(ComponentSerializer.toJson(component));
    }

    @NotNull
    public static Style toAdventureStyle(@NotNull BungeeCordStyle style) {
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
        if (STYLE_HAS_SHADOW_COLOR && style.getShadowColor() != null) {
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

    private AdventureConverter() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
