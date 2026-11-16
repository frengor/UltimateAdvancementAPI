package com.fren_gor.ultimateAdvancementAPI.chat.bungeecord;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A representation of the style of a {@link BaseComponent}.
 * <p>Present for cross-version compatibility, since {@link ComponentStyle} has been added in 1.20.3.
 * <p>Instances are <i>immutable</i>. Mutator methods do not modify the instance on which they are called,
 * instead they return a new instance with the necessary modifications applied.
 *
 * @see ComponentStyle
 * @see BaseComponent
 */
public final class BungeeCordComponentStyle {

    private static final boolean CHAT_COLOR_HAS_GET_COLOR = ReflectionUtil.hasMethod(ChatColor.class, "getColor");
    private static final boolean BASE_COMPONENT_HAS_FONT = ReflectionUtil.hasMethod(BaseComponent.class, "setFont", String.class);
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
     * Creates an empty {@code BungeeCordComponentStyle}.
     */
    public BungeeCordComponentStyle() {
        this(null, null, null, null, null, null, null, null);
    }

    /**
     * Creates a {@code BungeeCordComponentStyle} with the provided color.
     *
     * @param color The color. Must <i>not</i> be a formatting option.
     */
    public BungeeCordComponentStyle(@NotNull ChatColor color) {
        this(validateColor(Objects.requireNonNull(color, "ChatColor is null.")), null, null, null, null, null, null, null);
    }

    /**
     * Converts the provided {@link ComponentStyle} into a {@code BungeeCordComponentStyle}.
     *
     * @param style The {@link ComponentStyle}.
     */
    public BungeeCordComponentStyle(@NotNull ComponentStyle style) {
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
    private BungeeCordComponentStyle(
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
     * Returns this style with the specified color.
     *
     * @param color The color, or {@code null} to unset. Must <i>not</i> be a formatting option.
     * @return This style with the specified color.
     */
    @NotNull
    public BungeeCordComponentStyle color(@Nullable ChatColor color) {
        return new BungeeCordComponentStyle(validateColor(color), shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns this style with the specified shadow color.
     *
     * @param shadowColor The shadow color, or {@code null} to unset.
     * @return This style with the specified shadow color.
     */
    @NotNull
    public BungeeCordComponentStyle shadowColor(@Nullable Color shadowColor) {
        return new BungeeCordComponentStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns this style with the specified font.
     *
     * @param font The font, or {@code null} to unset.
     * @return This style with the specified font.
     */
    @NotNull
    public BungeeCordComponentStyle font(@Nullable String font) {
        return new BungeeCordComponentStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns this style with the specified bold state.
     *
     * @param bold The state to set, or {@code null} to unset.
     * @return This style with the specified bold state.
     */
    @NotNull
    public BungeeCordComponentStyle bold(@Nullable Boolean bold) {
        return new BungeeCordComponentStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns this style with the specified italic state.
     *
     * @param italic The state to set, or {@code null} to unset.
     * @return This style with the specified italic state.
     */
    @NotNull
    public BungeeCordComponentStyle italic(@Nullable Boolean italic) {
        return new BungeeCordComponentStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns this style with the specified underlined state.
     *
     * @param underlined The state to set, or {@code null} to unset.
     * @return This style with the specified underlined state.
     */
    @NotNull
    public BungeeCordComponentStyle underlined(@Nullable Boolean underlined) {
        return new BungeeCordComponentStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns this style with the specified strikethrough state.
     *
     * @param strikethrough The state to set, or {@code null} to unset.
     * @return This style with the specified strikethrough state.
     */
    @NotNull
    public BungeeCordComponentStyle strikethrough(@Nullable Boolean strikethrough) {
        return new BungeeCordComponentStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Returns this style with the specified obfuscated state.
     *
     * @param obfuscated The state to set, or {@code null} to unset.
     * @return This style with the specified obfuscated state.
     */
    @NotNull
    public BungeeCordComponentStyle obfuscated(@Nullable Boolean obfuscated) {
        return new BungeeCordComponentStyle(color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated);
    }

    /**
     * Merges the provided style on top of this style.
     *
     * @param that The style to merge.
     * @return This style with the provided style merged on top.
     */
    @NotNull
    public BungeeCordComponentStyle merge(@NotNull BungeeCordComponentStyle that) {
        Preconditions.checkNotNull(that, "BungeeCordComponentStyle is null.");
        return new BungeeCordComponentStyle(
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
     * Applies this style to the provided {@link BaseComponent}.
     *
     * @param component The component to be styled.
     */
    public void applyStyleTo(@NotNull BaseComponent component) {
        Preconditions.checkNotNull(component, "BaseComponent is null.");
        component.setColor(this.color);
        if (COMPONENT_STYLE_HAS_SHADOW_COLOR) {
            component.setShadowColor(this.shadowColor);
        }
        if (BASE_COMPONENT_HAS_FONT) {
            component.setFont(this.font);
        }
        component.setBold(this.bold);
        component.setItalic(this.italic);
        component.setUnderlined(this.underlined);
        component.setStrikethrough(this.strikethrough);
        component.setObfuscated(this.obfuscated);
    }

    /**
     * Applies this style to the provided {@link BaseComponent} without overriding the already present style.
     *
     * @param component The component to be styled.
     */
    public void applyDefaultStyleTo(@NotNull BaseComponent component) {
        Preconditions.checkNotNull(component, "BaseComponent is null.");
        if (this.color != null && component.getColorRaw() == null) {
            component.setColor(this.color);
        }
        if (COMPONENT_STYLE_HAS_SHADOW_COLOR && this.shadowColor != null && component.getShadowColorRaw() == null) {
            component.setShadowColor(this.shadowColor);
        }
        if (BASE_COMPONENT_HAS_FONT && this.font != null && component.getFontRaw() == null) {
            component.setFont(this.font);
        }
        if (this.bold != null && component.isBoldRaw() == null) {
            component.setBold(this.bold);
        }
        if (this.italic != null && component.isItalicRaw() == null) {
            component.setItalic(this.italic);
        }
        if (this.underlined != null && component.isUnderlinedRaw() == null) {
            component.setUnderlined(this.underlined);
        }
        if (this.strikethrough != null && component.isStrikethroughRaw() == null) {
            component.setStrikethrough(this.strikethrough);
        }
        if (this.obfuscated != null && component.isObfuscatedRaw() == null) {
            component.setObfuscated(this.obfuscated);
        }
    }

    /**
     * Converts this style into a {@link ComponentStyle}.
     *
     * @return This style converted into a {@link ComponentStyle}.
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
     * Gets the color.
     *
     * @return The color, or {@code null} if not set.
     */
    @Nullable
    public ChatColor getColor() {
        return color;
    }

    /**
     * Gets the shadow color.
     *
     * @return The shadow color, or {@code null} if not set.
     */
    @Nullable
    public Color getShadowColor() {
        return shadowColor;
    }

    /**
     * Gets the font.
     *
     * @return The font, or {@code null} if not set.
     */
    @Nullable
    public String getFont() {
        return font;
    }

    /**
     * Gets the bold state.
     *
     * @return The bold state, or {@code null} if not set.
     */
    @Nullable
    public Boolean isBold() {
        return bold;
    }

    /**
     * Gets the italic state.
     *
     * @return The italic state, or {@code null} if not set.
     */
    @Nullable
    public Boolean isItalic() {
        return italic;
    }

    /**
     * Gets the underlined state.
     *
     * @return The underlined state, or {@code null} if not set.
     */
    @Nullable
    public Boolean isUnderlined() {
        return underlined;
    }

    /**
     * Gets the strikethrough state.
     *
     * @return The strikethrough state, or {@code null} if not set.
     */
    @Nullable
    public Boolean isStrikethrough() {
        return strikethrough;
    }

    /**
     * Gets the obfuscated state.
     *
     * @return The obfuscated state, or {@code null} if not set.
     */
    @Nullable
    public Boolean isObfuscated() {
        return obfuscated;
    }

    /**
     * Returns whether this style is empty.
     *
     * @return Whether this style is empty.
     */
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
        StringJoiner joiner = new StringJoiner(", ", "BungeeCordComponentStyle{", "}");
        joiner.setEmptyValue("BungeeCordComponentStyle{empty}");

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
        if (!(o instanceof BungeeCordComponentStyle that)) return false;

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
