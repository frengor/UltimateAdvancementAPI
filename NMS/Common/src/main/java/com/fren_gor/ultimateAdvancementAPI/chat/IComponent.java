package com.fren_gor.ultimateAdvancementAPI.chat;

import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil.SearchResult;
import com.fren_gor.ultimateAdvancementAPI.chat.adventure.AdventureComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordComponent;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Wrapper-like abstraction over platform-specific chat components like {@link BaseComponent} or Adventure's {@link Component}.
 * <p>Instances are <i>immutable</i>. Mutator methods do not modify the instance on which they are called,
 * instead they return a new instance with the necessary modifications applied.
 * <p>For provided implementations see {@link BungeeCordComponent} and {@link AdventureComponent}.
 * <p>Instances can be converted back to {@link BaseComponent}s and Adventure's {@link Component}s using the {@link #toBungeeCordComponent()}
 * and {@link #toAdventureComponent()} methods, respectively. Conversions between the same kind of chat components
 * (e.g. {@link #toBungeeCordComponent()} for {@link BungeeCordComponent} and {@link #toAdventureComponent()} for {@link AdventureComponent})
 * should be constant time (except for cloning in case of mutable components like {@link BaseComponent}) and not require actual conversions.
 * <p>The methods {@link #empty()}, {@link #space()}, {@link #newLine()}, {@link #fromPlain(String)} and {@link #fromLegacy(String)} can be used
 * to create chat components of the same kind as the one they are called on. This is especially important when using different types of components
 * together, since they can reduce (or completely remove) the conversions needed between them.
 *
 * @see BungeeCordComponent
 * @see AdventureComponent
 */
public interface IComponent {

    /**
     * Converts this {@code IComponent} into a {@link BaseComponent}.
     *
     * @return This {@code IComponent} as a {@link BaseComponent}.
     */
    @NotNull
    BaseComponent toBungeeCordComponent();

    /**
     * Converts this {@code IComponent} into an Adventure's {@link Component}.
     *
     * @return This {@code IComponent} as an Adventure's {@link Component}.
     * @apiNote This method has a default implementation so that implementing this interface does not require adding Adventure
     *     as a dependency when it's not needed. It's suggested to override this method if possible.
     * @implSpec The default implementation calls {@link #toBungeeCordComponent()} and converts the returned {@link BaseComponent}
     *     into an Adventure {@link Component}.
     */
    @NotNull
    default Component toAdventureComponent() {
        return BungeeCordComponent.of(toBungeeCordComponent()).toAdventureComponent();
    }

    /**
     * Appends the provided components to this component.
     *
     * @param component The component to append.
     * @return This component with the provided one appended.
     */
    @NotNull
    IComponent append(@NotNull IComponent component);

    /**
     * Sets the children of this component.
     *
     * @param children The children components.
     * @return This component with the provided children ones.
     */
    @NotNull
    IComponent setChildren(@NotNull Collection<? extends IComponent> children);

    /**
     * Adds the hover event to this component.
     *
     * @param hoverMsg The component to be displayed on hover.
     * @return This component with the provided hover event.
     */
    @NotNull
    IComponent addHoverEvent(@NotNull IComponent hoverMsg);

    /**
     * Applies the provided style to this component.
     *
     * @param style The style to apply.
     * @return This component with the provided style applied.
     */
    @NotNull
    IComponent applyStyle(@NotNull IStyle style);

    /**
     * Applies the provided default style to this component.
     *
     * @param defaultStyle The default style to apply.
     * @return This component with the provided default style applied.
     */
    @NotNull
    IComponent applyDefaultStyle(@NotNull IStyle defaultStyle);

    /**
     * Returns whether this chat component, once rendered, would be empty or start with a new line character ({@code \n}).
     * <p>Whitespaces at the start of the message should be ignored.
     *
     * @return Whether this chat component, once rendered, would be empty or start with a new line character ({@code \n}).
     * @implSpec The default implementation performs the search on the string returned by {@link #toPlain()}.
     */
    default boolean isEmptyOrStartsWithNewLine() {
        var res = TextUtil.startsWithNewLine(toPlain());
        return res == SearchResult.FOUND || res == SearchResult.EMPTY;
    }

    /**
     * Returns this chat component as a JSON string.
     *
     * @return This chat component as a JSON string.
     */
    @NotNull
    JsonString toJson();

    /**
     * Returns this chat component as a string that uses legacy color codes.
     *
     * @return This chat component as a string that uses legacy color codes.
     */
    @NotNull
    String toLegacy();

    /**
     * Returns this chat component as a plain string with no formatting or coloring.
     *
     * @return This chat component as a plain string with no formatting or coloring.
     */
    @NotNull
    String toPlain();

    /**
     * Returns a component with the provided plain string as content.
     *
     * @param plainText The content of the returned component.
     * @return A component with the provided plain string as content.
     */
    @NotNull
    IComponent fromPlain(@NotNull String plainText);

    /**
     * Returns a component with the provided legacy string as content.
     *
     * @param legacy The content of the returned component. May contain legacy color codes.
     * @return A component with the provided legacy string as content.
     */
    @NotNull
    IComponent fromLegacy(@NotNull String legacy);

    /**
     * Returns an empty component.
     *
     * @return An empty component.
     */
    @NotNull
    IComponent empty();

    /**
     * Returns a component containing a single space character.
     *
     * @return A component containing a single space character.
     */
    @NotNull
    IComponent space();

    /**
     * Returns a component containing a single new line character (\n).
     *
     * @return A component containing a single new line character (\n).
     */
    @NotNull
    IComponent newLine();

    /**
     * Displays this component on the chat of the provided player.
     *
     * @param player The player.
     */
    void sendTo(@NotNull Player player);
}
