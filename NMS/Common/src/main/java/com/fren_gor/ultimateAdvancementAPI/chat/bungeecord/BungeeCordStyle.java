package com.fren_gor.ultimateAdvancementAPI.chat.bungeecord;

import com.fren_gor.ultimateAdvancementAPI.chat.IStyle;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import net.kyori.adventure.text.format.Style;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentStyle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Implementation of {@link IStyle} for BungeeCord chat components.
 *
 * @see BungeeCordComponentStyle
 * @see ComponentStyle
 * @see BungeeCordComponent
 */
public class BungeeCordStyle implements IStyle {

    /**
     * An empty style.
     */
    public static IStyle EMPTY = of(new BungeeCordComponentStyle());

    private final BungeeCordComponentStyle style;

    @LazyValue
    private Object adventureStyle; // Use Object to avoid loading issues on Spigot

    private BungeeCordStyle(@NotNull BungeeCordComponentStyle style) {
        this.style = Objects.requireNonNull(style, "BungeeCordComponentStyle is null.");
    }

    /**
     * Creates a new {@code BungeeCordStyle} with the provided color.
     *
     * @param color The color.
     * @return A new {@code BungeeCordStyle} with the provided color.
     */
    @NotNull
    public static IStyle of(@NotNull ChatColor color) {
        return of(new BungeeCordComponentStyle(color));
    }

    /**
     * Creates a new {@code BungeeCordStyle} with the provided {@link ComponentStyle}.
     *
     * @param style The style.
     * @return A new {@code BungeeCordStyle} with the provided {@link ComponentStyle}.
     */
    @NotNull
    public static IStyle of(@NotNull ComponentStyle style) {
        return of(new BungeeCordComponentStyle(style));
    }

    /**
     * Creates a new {@code BungeeCordStyle} with the provided {@link BungeeCordComponentStyle}.
     *
     * @param style The style.
     * @return A new {@code BungeeCordStyle} with the provided {@link BungeeCordComponentStyle}.
     */
    @NotNull
    public static IStyle of(@NotNull BungeeCordComponentStyle style) {
        return new BungeeCordStyle(style);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The returned value is <a href="https://en.wikipedia.org/wiki/Memoization">memoized</a>,
     *     i.e. it is computed on the first call and cached for subsequent ones.
     */
    @Override
    @NotNull
    public Style toAdventureStyle() {
        if (adventureStyle != null) {
            return (Style) adventureStyle;
        }
        return (Style) (adventureStyle = AdventureConverter.toAdventureStyle(style));
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This method requires no conversion and simply returns the wrapped value.
     */
    @Override
    @NotNull
    public BungeeCordComponentStyle toBungeeCordStyle() {
        return this.style;
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
    public IStyle merge(@NotNull IStyle style) {
        return new BungeeCordStyle(this.style.merge(style.toBungeeCordStyle()));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        BungeeCordStyle that = (BungeeCordStyle) o;
        return Objects.equals(style, that.style);
    }

    @Override
    public int hashCode() {
        return style.hashCode();
    }

    @Override
    public String toString() {
        return "BungeeCordStyle{" +
            "style=" + style +
            '}';
    }
}
