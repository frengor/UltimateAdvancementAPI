package com.fren_gor.ultimateAdvancementAPI.chat;

import com.fren_gor.ultimateAdvancementAPI.chat.adventure.AdventureStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordComponentStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordStyle;
import net.kyori.adventure.text.format.Style;
import net.md_5.bungee.api.chat.ComponentStyle;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper-like abstraction over platform-specific chat component styles like {@link ComponentStyle} or Adventure's {@link Style}.
 * <p>Instances are <i>immutable</i>. Mutator methods do not modify the instance on which they are called,
 * instead they return a new instance with the necessary modifications applied.
 * <p>For provided implementations see {@link BungeeCordStyle} and {@link AdventureStyle}.
 * <p>Instances can be converted back to {@link BungeeCordComponentStyle}s and Adventure's {@link Style}s using the {@link #toBungeeCordStyle()}
 * and {@link #toAdventureStyle()} methods, respectively. Conversions between the same kind of styles
 * (e.g. {@link #toBungeeCordStyle()} for {@link BungeeCordStyle} and {@link #toAdventureStyle()} for {@link AdventureStyle})
 * should be constant time and not require actual conversions.
 *
 * @see BungeeCordStyle
 * @see AdventureStyle
 * @see BungeeCordComponentStyle
 */
public interface IStyle {

    /**
     * Converts this {@code IStyle} into a {@link BungeeCordComponentStyle}.
     *
     * @return This {@code IStyle} as a {@link BungeeCordComponentStyle}.
     */
    @NotNull
    BungeeCordComponentStyle toBungeeCordStyle();

    /**
     * Converts this {@code IStyle} into an Adventure's {@link Style}.
     *
     * @return This {@code IStyle} as an Adventure's {@link Style}.
     * @apiNote This method has a default implementation so that implementing this interface does not require adding Adventure
     *     as a dependency when it's not needed. It's suggested to override this method if possible.
     * @implSpec The default implementation calls {@link #toBungeeCordStyle()} and converts the returned {@link BungeeCordComponentStyle}
     *     into an Adventure {@link Style}.
     */
    @NotNull
    default Style toAdventureStyle() {
        return BungeeCordStyle.of(toBungeeCordStyle()).toAdventureStyle();
    }

    /**
     * Returns whether this style is empty.
     *
     * @return Whether this style is empty.
     */
    boolean isEmpty();

    /**
     * Merges the provided style on top of this style.
     *
     * @param style The style to merge.
     * @return This style with the provided style merged on top.
     */
    @NotNull
    IStyle merge(@NotNull IStyle style);
}
