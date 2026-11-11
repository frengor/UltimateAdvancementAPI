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

import java.awt.Color;

/**
 * Utility class for conversions between BungeeCord and Adventure components.
 * <p>
 * NO NOT USE unless Adventure is known to be present!
 */
final class AdventureConverter {

    private static final boolean COMPONENT_SERIALIZER_HAS_TO_JSON = ReflectionUtil.hasMethod(ComponentSerializer.class, "toJson", BaseComponent.class);
    private static final boolean CHAT_COLOR_HAS_GET_COLOR = ReflectionUtil.hasMethod(ChatColor.class, "getColor");
    private static final boolean STYLE_HAS_SHADOW_COLOR = ReflectionUtil.hasMethod(Style.class, "shadowColor");

    @NotNull
    public static Component toAdventureComponent(@NotNull BungeeCordComponent component) {
        if (COMPONENT_SERIALIZER_HAS_TO_JSON) {
            return GsonComponentSerializer.gson().deserializeFromTree(ComponentSerializer.toJson(component.toBungeeCordComponent()));
        } else {
            return GsonComponentSerializer.gson().deserialize(component.toJson().jsonString());
        }
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
            ShadowColorSetter.setShadowColor(style.getShadowColor(), builder);
        }
        if (style.getFont() != null) {
            builder.font(Key.key(style.getFont()));
        }
        if (style.isBold() != null) {
            builder.decoration(TextDecoration.BOLD, style.isBold());
        }
        if (style.isItalic() != null) {
            builder.decoration(TextDecoration.ITALIC, style.isItalic());
        }
        if (style.isUnderlined() != null) {
            builder.decoration(TextDecoration.UNDERLINED, style.isUnderlined());
        }
        if (style.isStrikethrough() != null) {
            builder.decoration(TextDecoration.STRIKETHROUGH, style.isStrikethrough());
        }
        if (style.isObfuscated() != null) {
            builder.decoration(TextDecoration.OBFUSCATED, style.isObfuscated());
        }
        return builder.build();
    }

    // Necessary for old versions compatibility (builder.shadowColor accepts an ARGBLike, but that class doesn't exist on old versions)
    private static final class ShadowColorSetter {
        private static void setShadowColor(@NotNull Color color, @NotNull Style.Builder builder) {
            builder.shadowColor(ShadowColor.shadowColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
        }

        private ShadowColorSetter() {
            throw new UnsupportedOperationException("Utility class.");
        }
    }

    private AdventureConverter() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
