package com.fren_gor.ultimateAdvancementAPI.chat.bungeecord;

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

// FIXME: Update javadoc
public final class BungeeCordStyle {

    private static final boolean CHAT_COLOR_HAS_GET_COLOR = ReflectionUtil.hasMethod(ChatColor.class, "getColor");
    private static final boolean BASE_COMPONENT_HAS_FONT = ReflectionUtil.hasMethod(BaseComponent.class, "setFont");
    private static final boolean COMPONENT_STYLE_CLASS_EXISTS = ReflectionUtil.classExists("net.md_5.bungee.api.chat.ComponentStyle");
    private static final boolean COMPONENT_STYLE_HAS_SHADOW_COLOR = COMPONENT_STYLE_CLASS_EXISTS && ReflectionUtil.hasMethod(ComponentStyle.class, "getShadowColor");

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
    public BungeeCordStyle() {
        this(null, null, null, null, null, null, null, null);
    }

    /**
     * Creates a {@code DefaultStyle} which makes no modification to the default Minecraft style
     * except for the color of the text.
     *
     * @param color The color of the text. Must represent a color and not a formatting option.
     */
    public BungeeCordStyle(@NotNull ChatColor color) {
        this(validateColor(Objects.requireNonNull(color, "ChatColor is null.")), null, null, null, null, null, null, null);
    }

    /**
     * Creates a {@code DefaultStyle} which makes no modification to the default Minecraft style
     * except for the modifications present in the provided {@link ComponentStyle}.
     *
     * @param style The {@link ComponentStyle}.
     */
    public BungeeCordStyle(@NotNull ComponentStyle style) {
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
    private BungeeCordStyle(
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
     *     Must represent a color and not a formatting option.
     * @return A copy of this {@code DefaultStyle} with the specified color.
     */
    @NotNull
    public BungeeCordStyle color(@Nullable ChatColor color) {
        return new BungeeCordStyle(validateColor(color), shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified shadow color.
     *
     * @param shadowColor The shadow color, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified shadow color.
     */
    @NotNull
    public BungeeCordStyle shadowColor(@Nullable Color shadowColor) {
        return new BungeeCordStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified font.
     *
     * @param font The font, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified font.
     */
    @NotNull
    public BungeeCordStyle font(@Nullable String font) {
        return new BungeeCordStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified bold value.
     *
     * @param bold The bold value, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified bold value.
     */
    @NotNull
    public BungeeCordStyle bold(@Nullable Boolean bold) {
        return new BungeeCordStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified italic value.
     *
     * @param italic The italic value, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified italic value.
     */
    @NotNull
    public BungeeCordStyle italic(@Nullable Boolean italic) {
        return new BungeeCordStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified underlined value.
     *
     * @param underlined The underlined value, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified underlined value.
     */
    @NotNull
    public BungeeCordStyle underlined(@Nullable Boolean underlined) {
        return new BungeeCordStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified strikethrough value.
     *
     * @param strikethrough The strikethrough value, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified strikethrough value.
     */
    @NotNull
    public BungeeCordStyle strikethrough(@Nullable Boolean strikethrough) {
        return new BungeeCordStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns a copy of this {@code DefaultStyle} with the specified obfuscated value.
     *
     * @param obfuscated The obfuscated value, or {@code null} to use Minecraft's default.
     * @return A copy of this {@code DefaultStyle} with the specified obfuscated value.
     */
    @NotNull
    public BungeeCordStyle obfuscated(@Nullable Boolean obfuscated) {
        return new BungeeCordStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    @NotNull
    public BungeeCordStyle merge(@NotNull BungeeCordStyle that) {
        Preconditions.checkNotNull(that, "BungeeCordStyle is null.");
        return new BungeeCordStyle(
            that.color != null ? that.color : this.color,
            that.shadowColor != null ? that.shadowColor : this.shadowColor,
            that.font != null ? that.font : this.font,
            that.bold != null ? that.bold : this.bold,
            that.italic != null ? that.italic : this.italic,
            that.underlined != null ? that.underlined : this.underlined,
            that.strikethrough != null ? that.strikethrough : this.strikethrough,
            that.obfuscated != null ? that.obfuscated : this.obfuscated
        );
    }

    /**
     * Applies this {@code DefaultStyle} to the provided {@link BaseComponent}.
     *
     * @param component The component to which the default style will be applied to.
     */
    public void applyTo(@NotNull BaseComponent component) {
        Preconditions.checkNotNull(component, "BaseComponent is null.");
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
            // No need to check for BASE_COMPONENT_HAS_FONT, font was added before ComponentStyle
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

    public boolean isEmpty() {
        return color == null &&
            shadowColor == null &&
            font == null &&
            bold == null &&
            italic == null &&
            underlined == null &&
            strikethrough == null &&
            obfuscated == null;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "BungeeCordStyle{", "}");
        joiner.setEmptyValue("BungeeCordStyle{minecraft defaults}");

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
        if (!(o instanceof BungeeCordStyle that)) return false;

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
