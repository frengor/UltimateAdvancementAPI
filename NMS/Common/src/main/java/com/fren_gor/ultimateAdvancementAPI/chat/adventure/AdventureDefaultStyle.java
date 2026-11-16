package com.fren_gor.ultimateAdvancementAPI.chat.adventure;

import com.fren_gor.ultimateAdvancementAPI.chat.IDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordStyle;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
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

/**
 * Implementation of {@link IDefaultStyle} for Adventure chat components.
 *
 * @see Style
 * @see AdventureComponent
 */
public class AdventureDefaultStyle implements IDefaultStyle {

    /**
     * An empty style.
     */
    public static IDefaultStyle EMPTY = of(Style.empty());

    private static final boolean CHAT_COLOR_HAS_GET_COLOR = ReflectionUtil.hasMethod(ChatColor.class, "getColor");
    private static final boolean STYLE_HAS_SHADOW_COLOR = ReflectionUtil.hasMethod(Style.class, "shadowColor");

    private final Style style;

    @LazyValue
    private BungeeCordStyle bungeeCordStyle;

    private AdventureDefaultStyle(@NotNull Style style) {
        this.style = Objects.requireNonNull(style, "Style is null.");
    }

    /**
     * Creates a new {@code AdventureDefaultStyle} with the provided color.
     *
     * @param color The color.
     * @return A new {@code AdventureDefaultStyle} with the provided color.
     */
    @NotNull
    public static IDefaultStyle of(@NotNull TextColor color) {
        return of(Style.style(color));
    }

    /**
     * Creates a new {@code AdventureDefaultStyle} with the provided style.
     *
     * @param style The style.
     * @return A new {@code AdventureDefaultStyle} with the provided style.
     */
    @NotNull
    public static IDefaultStyle of(@NotNull Style style) {
        return new AdventureDefaultStyle(style);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This method requires no conversion and simply returns the wrapped value.
     */
    @Override
    @NotNull
    public Style toAdventureStyle() {
        return this.style;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The returned value is <a href="https://en.wikipedia.org/wiki/Memoization">memoized</a>,
     *     i.e. it is computed on the first call and cached for subsequent ones.
     */
    @Override
    @NotNull
    public BungeeCordStyle toBungeeCordStyle() {
        if (bungeeCordStyle != null) {
            return bungeeCordStyle;
        }
        var shadowColor = STYLE_HAS_SHADOW_COLOR ? style.shadowColor() : null;
        var font = style.font();
        return bungeeCordStyle = new BungeeCordStyle()
            .color(fromTextColor(style.color()))
            .shadowColor(shadowColor == null ? null : new Color(shadowColor.value(), true))
            .font(font == null ? null : font.asString())
            .bold(style.hasDecoration(TextDecoration.BOLD))
            .italic(style.hasDecoration(TextDecoration.ITALIC))
            .underlined(style.hasDecoration(TextDecoration.UNDERLINED))
            .strikethrough(style.hasDecoration(TextDecoration.STRIKETHROUGH))
            .obfuscated(style.hasDecoration(TextDecoration.OBFUSCATED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return style.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IDefaultStyle merge(@NotNull IDefaultStyle style) {
        return new AdventureDefaultStyle(this.style.merge(style.toAdventureStyle()));
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AdventureDefaultStyle that = (AdventureDefaultStyle) o;
        return Objects.equals(style, that.style);
    }

    @Override
    public int hashCode() {
        return style.hashCode();
    }

    @Override
    public String toString() {
        return "AdventureDefaultStyle{" +
            "style=" + style +
            '}';
    }
}
