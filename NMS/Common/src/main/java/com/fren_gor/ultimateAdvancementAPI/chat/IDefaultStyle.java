package com.fren_gor.ultimateAdvancementAPI.chat;

import com.fren_gor.ultimateAdvancementAPI.chat.adventure.AdventureDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordStyle;
import net.kyori.adventure.text.format.Style;
import net.md_5.bungee.api.chat.ComponentStyle;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper-like abstraction over platform-specific chat component styles like {@link ComponentStyle} or Adventure's {@link Style}.
 * <p>Instances are <i>immutable</i>. Mutator methods do not modify the instance on which they are called,
 * instead they return a new instance with the necessary modifications applied.
 * <p>For provided implementations see {@link BungeeCordDefaultStyle} and {@link AdventureDefaultStyle}.
 * <p>Instances can be converted back to {@link BungeeCordStyle}s and Adventure's {@link Style}s using the {@link #toBungeeCordStyle()}
 * and {@link #toAdventureStyle()} methods, respectively. Conversions between the same kind of styles
 * (e.g. {@link #toBungeeCordStyle()} for {@link BungeeCordDefaultStyle} and {@link #toAdventureStyle()} for {@link AdventureDefaultStyle})
 * should be constant time and not require actual conversions.
 *
 * @see BungeeCordDefaultStyle
 * @see AdventureDefaultStyle
 * @see BungeeCordStyle
 */
public interface IDefaultStyle {

    /**
     * Converts this {@code IDefaultStyle} into a {@link BungeeCordStyle}.
     *
     * @return This {@code IDefaultStyle} as a {@link BungeeCordStyle}.
     */
    @NotNull
    BungeeCordStyle toBungeeCordStyle();

    /**
     * Converts this {@code IDefaultStyle} into an Adventure's {@link Style}.
     *
     * @return This {@code IDefaultStyle} as an Adventure's {@link Style}.
     * @apiNote This method has a default implementation so that implementing this interface does not require adding Adventure
     *     as a dependency when it's not needed. It's suggested to override this method if possible.
     * @implSpec The default implementation calls {@link #toBungeeCordStyle()} and converts the returned {@link BungeeCordStyle}
     *     into an Adventure {@link Style}.
     */
    @NotNull
    default Style toAdventureStyle() {
        return BungeeCordDefaultStyle.of(toBungeeCordStyle()).toAdventureStyle();
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
    IDefaultStyle merge(@NotNull IDefaultStyle style);
}
