package com.fren_gor.ultimateAdvancementAPI.util.display;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Class representing the default style of a {@link BaseComponent}.
 * <p>If a property is {@code null}, then Minecraft's default should be used instead.
 */
public final class DefaultStyle {

    private static final boolean CHAT_COLOR_HAS_GET_COLOR = ReflectionUtil.hasMethod(ChatColor.class, "getColor");
    private static final boolean BASE_COMPONENT_HAS_FONT = ReflectionUtil.hasMethod(BaseComponent.class, "setFont");
    private static final boolean COMPONENT_STYLE_CLASS_EXISTS = ReflectionUtil.classExists("net.md_5.bungee.api.chat.ComponentStyle");
    private static final boolean COMPONENT_STYLE_HAS_SHADOW_COLOR = COMPONENT_STYLE_CLASS_EXISTS && ReflectionUtil.hasMethod(ComponentStyle.class, "getShadowColor");

    /**
     * A {@code DefaultStyle} which makes no modification to the default Minecraft style.
     */
    @NotNull
    public static final DefaultStyle MINECRAFT_DEFAULTS = new DefaultStyle();

    @Nullable
    private final ChatColor color;
    @Nullable
    private final Color shadowColor;
    @Nullable
    private final String font;
    @Nullable
    private final Boolean bold;
    @Nullable
    private final Boolean italic;
    @Nullable
    private final Boolean underlined;
    @Nullable
    private final Boolean strikethrough;
    @Nullable
    private final Boolean obfuscated;

    /**
     * Creates a {@code DefaultStyle} which makes no modification to the default Minecraft style.
     */
    public DefaultStyle() {
        this(null, null, null, null, null, null, null, null);
    }

    /**
     * Creates a {@code DefaultStyle} which makes no modification to the default Minecraft style
     * except for the color of the text.
     *
     * @param color The color of the text. Must represent a color and not a formatting option.
     */
    public DefaultStyle(@NotNull ChatColor color) {
        this(validateColor(Objects.requireNonNull(color, "ChatColor is null.")), null, null, null, null, null, null, null);
    }

    /**
     * Creates a {@code DefaultStyle} which makes no modification to the default Minecraft style
     * except for the modifications present in the provided {@link ComponentStyle}.
     *
     * @param style The {@link ComponentStyle}.
     */
    public DefaultStyle(@NotNull ComponentStyle style) {
        this(
                validateColor(Objects.requireNonNull(style, "ComponentStyle is null.").getColor()),
                COMPONENT_STYLE_HAS_SHADOW_COLOR ? style.getShadowColor() : null,
                // No need to check for BASE_COMPONENT_HAS_FONT, font was added in 1.16, but ComponentStyle was added in 1.18
                style.getFont(),
                style.isBoldRaw(),
                style.isItalicRaw(),
                style.isUnderlinedRaw(),
                style.isStrikethroughRaw(),
                style.isObfuscatedRaw()
        );
    }

    // Assumes color has already been validated
    private DefaultStyle(
            @Nullable ChatColor color,
            @Nullable Color shadowColor,
            @Nullable String font,
            @Nullable Boolean bold,
            @Nullable Boolean italic,
            @Nullable Boolean underlined,
            @Nullable Boolean strikethrough,
            @Nullable Boolean obfuscated
    ) {
        this.color = color;
        this.shadowColor = shadowColor;
        this.font = font;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified color.
     *
     * @param color The color, or {@code null} to use Minecraft's default.
     *         Must represent a color and not a formatting option.
     * @return A copy of this {@code DefaultStyle} with the specified color.
     */
    @NotNull
    public DefaultStyle color(@Nullable ChatColor color) {
        return new DefaultStyle(validateColor(color), shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified shadow color.
     *
     * @param shadowColor The shadow color, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified shadow color.
     */
    @NotNull
    public DefaultStyle shadowColor(@Nullable Color shadowColor) {
        return new DefaultStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified font.
     *
     * @param font The font, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified font.
     */
    @NotNull
    public DefaultStyle font(@Nullable String font) {
        return new DefaultStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified bold value.
     *
     * @param bold The bold value, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified bold value.
     */
    @NotNull
    public DefaultStyle bold(@Nullable Boolean bold) {
        return new DefaultStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified italic value.
     *
     * @param italic The italic value, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified italic value.
     */
    @NotNull
    public DefaultStyle italic(@Nullable Boolean italic) {
        return new DefaultStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified underlined value.
     *
     * @param underlined The underlined value, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified underlined value.
     */
    @NotNull
    public DefaultStyle underlined(@Nullable Boolean underlined) {
        return new DefaultStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified strikethrough value.
     *
     * @param strikethrough The strikethrough value, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified strikethrough value.
     */
    @NotNull
    public DefaultStyle strikethrough(@Nullable Boolean strikethrough) {
        return new DefaultStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified obfuscated value.
     *
     * @param obfuscated The obfuscated value, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified obfuscated value.
     */
    @NotNull
    public DefaultStyle obfuscated(@Nullable Boolean obfuscated) {
        return new DefaultStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Merges two default styles into a new {@code DefaultStyle}, applying the modifications of this style
     * on top of the provided base style.
     *
     * @param baseStyle The base {@code DefaultStyle}.
     */
    @NotNull
    public DefaultStyle mergeWith(@NotNull DefaultStyle baseStyle) {
        Preconditions.checkNotNull(baseStyle, "DefaultStyle is null.");
        if (this == MINECRAFT_DEFAULTS) {
            // Common case, no need for baseStyle since it almost always won't be MINECRAFT_DEFAULTS
            return baseStyle;
        }
        return new DefaultStyle(
                this.color != null ? this.color : baseStyle.color,
                this.shadowColor != null ? this.shadowColor : baseStyle.shadowColor,
                this.font != null ? this.font : baseStyle.font,
                this.bold != null ? this.bold : baseStyle.bold,
                this.italic != null ? this.italic : baseStyle.italic,
                this.underlined != null ? this.underlined : baseStyle.underlined,
                this.strikethrough != null ? this.strikethrough : baseStyle.strikethrough,
                this.obfuscated != null ? this.obfuscated : baseStyle.obfuscated
        );
    }

    /**
     * Applies this {@code DefaultStyle} to the provided {@link BaseComponent}.
     *
     * @param component The component to which the default style will be applied to.
     */
    public void applyTo(@NotNull BaseComponent component) {
        Preconditions.checkNotNull(component, "BaseComponent is null.");
        if (this == MINECRAFT_DEFAULTS) {
            return; // Common case
        }
        if (this.color != null) {
            component.setColor(this.color);
        }
        if (COMPONENT_STYLE_HAS_SHADOW_COLOR && this.shadowColor != null) {
            component.setShadowColor(this.shadowColor);
        }
        if (BASE_COMPONENT_HAS_FONT && this.font != null) {
            component.setFont(this.font);
        }
        if (this.bold != null) {
            component.setBold(this.bold);
        }
        if (this.italic != null) {
            component.setItalic(this.italic);
        }
        if (this.underlined != null) {
            component.setUnderlined(this.underlined);
        }
        if (this.strikethrough != null) {
            component.setStrikethrough(this.strikethrough);
        }
        if (this.obfuscated != null) {
            component.setObfuscated(this.obfuscated);
        }
    }

    /**
     * Applies this {@code DefaultStyle} to the provided {@link ComponentBuilder}.
     *
     * @param builder The component builder to which the default style will be applied to.
     */
    public void applyTo(@NotNull ComponentBuilder builder) {
        Preconditions.checkNotNull(builder, "ComponentBuilder is null.");
        if (this == MINECRAFT_DEFAULTS) {
            return; // Common case
        }
        if (COMPONENT_STYLE_HAS_SHADOW_COLOR && this.shadowColor != null) {
            // Shadow color has no way to be set except for style(...)
            builder.style(this.toComponentStyle());
            return;
        }

        if (this.color != null) {
            builder.color(this.color);
        }
        if (BASE_COMPONENT_HAS_FONT && this.font != null) {
            builder.font(this.font);
        }
        if (this.bold != null) {
            builder.bold(this.bold);
        }
        if (this.italic != null) {
            builder.italic(this.italic);
        }
        if (this.underlined != null) {
            builder.underlined(this.underlined);
        }
        if (this.strikethrough != null) {
            builder.strikethrough(this.strikethrough);
        }
        if (this.obfuscated != null) {
            builder.obfuscated(this.obfuscated);
        }
    }

    /**
     * Converts this {@code DefaultStyle} in a {@link ComponentStyle}.
     *
     * @return This {@code DefaultStyle} converted into a {@link ComponentStyle}.
     */
    @NotNull
    public ComponentStyle toComponentStyle() {
        var builder = ComponentStyle.builder()
                .color(color)
                // No need to check for BASE_COMPONENT_HAS_FONT, font was added in 1.16, but ComponentStyle was added in 1.18
                .font(font)
                .bold(bold)
                .italic(italic)
                .underlined(underlined)
                .strikethrough(strikethrough)
                .obfuscated(obfuscated);

        if (COMPONENT_STYLE_HAS_SHADOW_COLOR) {
            builder.shadowColor(shadowColor);
        }

        return builder.build();
    }

    /**
     * Gets the color of the text.
     *
     * @return The color of the text, or {@code null} if Minecraft's default should be used.
     */
    @Nullable
    public ChatColor getColor() {
        return color;
    }

    /**
     * Gets the shadow color of the text.
     *
     * @return The shadow color of the text, or {@code null} if Minecraft's default should be used.
     */
    @Nullable
    public Color getShadowColor() {
        return shadowColor;
    }

    /**
     * Gets the font of the text.
     *
     * @return The font of the text, or {@code null} if Minecraft's default should be used.
     */
    @Nullable
    public String getFont() {
        return font;
    }

    /**
     * Gets whether to make the text bold.
     *
     * @return Whether to make the text bold, or {@code null} if Minecraft's default should be used.
     */
    @Nullable
    public Boolean isBold() {
        return bold;
    }

    /**
     * Gets whether to make the text italic.
     *
     * @return Whether to make the text italic, or {@code null} if Minecraft's default should be used.
     */
    @Nullable
    public Boolean isItalic() {
        return italic;
    }

    /**
     * Gets whether to make the text underlined.
     *
     * @return Whether to make the text underlined, or {@code null} if Minecraft's default should be used.
     */
    @Nullable
    public Boolean isUnderlined() {
        return underlined;
    }

    /**
     * Gets whether to make the text strikethrough.
     *
     * @return Whether to make the text strikethrough, or {@code null} if Minecraft's default should be used.
     */
    @Nullable
    public Boolean isStrikethrough() {
        return strikethrough;
    }

    /**
     * Gets whether to make the text obfuscated.
     *
     * @return Whether to make the text obfuscated, or {@code null} if Minecraft's default should be used.
     */
    @Nullable
    public Boolean isObfuscated() {
        return obfuscated;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "DefaultStyle{", "}");
        joiner.setEmptyValue("DefaultStyle{minecraft defaults}");

        if (this.color != null) {
            joiner.add("color=" + this.color.toString().replace(ChatColor.COLOR_CHAR, '&'));
        }
        if (this.shadowColor != null) {
            joiner.add("shadowColor=#" + String.format("%08X", this.shadowColor.getRGB()).substring(2));
        }
        if (this.font != null) {
            joiner.add("font='" + this.font + '\'');
        }
        if (this.bold != null) {
            joiner.add("bold=" + this.bold);
        }
        if (this.italic != null) {
            joiner.add("italic=" + this.italic);
        }
        if (this.underlined != null) {
            joiner.add("underlined=" + this.underlined);
        }
        if (this.strikethrough != null) {
            joiner.add("strikethrough=" + this.strikethrough);
        }
        if (this.obfuscated != null) {
            joiner.add("obfuscated=" + this.obfuscated);
        }

        return joiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultStyle that)) return false;

        return Objects.equals(color, that.color) &&
                Objects.equals(shadowColor, that.shadowColor) &&
                Objects.equals(font, that.font) &&
                Objects.equals(bold, that.bold) &&
                Objects.equals(italic, that.italic) &&
                Objects.equals(underlined, that.underlined) &&
                Objects.equals(strikethrough, that.strikethrough) &&
                Objects.equals(obfuscated, that.obfuscated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(color);
        result = 31 * result + Objects.hashCode(shadowColor);
        result = 31 * result + Objects.hashCode(font);
        result = 31 * result + Objects.hashCode(bold);
        result = 31 * result + Objects.hashCode(italic);
        result = 31 * result + Objects.hashCode(underlined);
        result = 31 * result + Objects.hashCode(strikethrough);
        result = 31 * result + Objects.hashCode(obfuscated);
        return result;
    }

    @Nullable
    private static ChatColor validateColor(@Nullable ChatColor color) {
        Preconditions.checkArgument(
                !CHAT_COLOR_HAS_GET_COLOR || color == null || color.getColor() != null,
                "Invalid ChatColor, expected a color but found a formatting option"
        );
        return color;
    }
}
