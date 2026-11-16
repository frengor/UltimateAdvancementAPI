package com.fren_gor.ultimateAdvancementAPI.chat.bungeecord;

import com.fren_gor.ultimateAdvancementAPI.chat.IComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.IStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil;
import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil.SearchResult;
import com.fren_gor.ultimateAdvancementAPI.chat.JsonString;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * Implementation of {@link IComponent} for BungeeCord chat components.
 *
 * @see BaseComponent
 * @see BungeeCordStyle
 */
public class BungeeCordComponent implements IComponent {

    /**
     * An empty component.
     */
    public static final IComponent EMPTY = of("");

    /**
     * A component containing a single space character.
     */
    public static final IComponent SPACE = of(" ");

    /**
     * A component containing a single new line character (\n).
     */
    public static final IComponent NEW_LINE = of("\n");

    private static final boolean COMPONENT_SERIALIZER_HAS_DESERIALIZE = ReflectionUtil.hasMethod(ComponentSerializer.class, "deserialize", String.class);
    private static final boolean TEXT_COMPONENT_HAS_FROM_LEGACY = ReflectionUtil.hasMethod(TextComponent.class, "fromLegacy");

    private final BaseComponent component;

    @LazyValue
    private Object adventureComponent; // Use Object to avoid loading issues on Spigot
    @LazyValue
    private JsonString jsonString;
    @LazyValue
    private Boolean emptyOrStartsWithNewLine;

    private BungeeCordComponent(@NotNull BaseComponent component) {
        // No need to duplicate here, the constructor is private and the provided component is never modified
        this.component = Objects.requireNonNull(component, "BaseComponent is null.");
    }

    /**
     * Creates a new {@code BungeeCordComponent} with the provided {@link BaseComponent}.
     *
     * @param component The component.
     * @return A new {@code BungeeCordComponent} with the provided {@link BaseComponent}.
     */
    @NotNull
    public static IComponent of(@NotNull BaseComponent component) {
        Preconditions.checkNotNull(component, "BaseComponent is null.");
        return new BungeeCordComponent(component.duplicate());
    }

    /**
     * Creates a new {@code BungeeCordComponent} from the provided {@link JsonString}.
     *
     * @param jsonString The JSON string.
     * @return A new {@code BungeeCordComponent} from the provided {@link JsonString}.
     */
    @NotNull
    public static IComponent of(@NotNull JsonString jsonString) {
        Preconditions.checkNotNull(jsonString, "JsonString is null.");
        BaseComponent deserialized;
        if (COMPONENT_SERIALIZER_HAS_DESERIALIZE) {
            deserialized = ComponentSerializer.deserialize(jsonString.jsonString());
        } else {
            BaseComponent[] parsed = ComponentSerializer.parse(jsonString.jsonString());
            deserialized = parsed.length == 1 ? parsed[0] : new TextComponent(parsed);
        }
        var component = new BungeeCordComponent(deserialized);
        component.jsonString = jsonString;
        return component;
    }

    /**
     * Creates a new {@code BungeeCordComponent} with the provided plain text.
     *
     * @param plainText The plain text.
     * @return A new {@code BungeeCordComponent} with the provided plain text.
     */
    @NotNull
    public static IComponent of(@NotNull String plainText) {
        Preconditions.checkNotNull(plainText, "String is null.");
        return new BungeeCordComponent(new TextComponent(plainText));
    }

    /**
     * Creates a new {@code BungeeCordComponent} from the provided legacy string.
     *
     * @param legacy The legacy string. May contain legacy color codes.
     * @return A new {@code BungeeCordComponent} from the provided legacy string.
     */
    @NotNull
    public static IComponent ofLegacy(@NotNull String legacy) {
        Preconditions.checkNotNull(legacy, "Legacy string is null.");
        if (TEXT_COMPONENT_HAS_FROM_LEGACY) {
            return new BungeeCordComponent(TextComponent.fromLegacy(legacy));
        }
        return new BungeeCordComponent(new TextComponent(TextComponent.fromLegacyText(legacy)));
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This method requires no conversion and simply returns the wrapped value.
     */
    @Override
    @NotNull
    public BaseComponent toBungeeCordComponent() {
        return component.duplicate();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The returned value is <a href="https://en.wikipedia.org/wiki/Memoization">memoized</a>,
     *     i.e. it is computed on the first call and cached for subsequent ones.
     */
    @Override
    @NotNull
    public Component toAdventureComponent() {
        if (adventureComponent != null) {
            return (Component) adventureComponent;
        }
        return (Component) (adventureComponent = AdventureConverter.toAdventureComponent(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent append(@NotNull IComponent component) {
        Preconditions.checkNotNull(component, "IComponent is null.");
        var toReturn = this.component.duplicate();
        toReturn.addExtra(component.toBungeeCordComponent());
        return new BungeeCordComponent(toReturn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent setChildren(@NotNull Collection<? extends IComponent> children) {
        Preconditions.checkNotNull(children, "Children components are null.");
        if (children.isEmpty()) {
            return this;
        }
        var toReturn = component.duplicate();
        toReturn.setExtra(children
            .stream()
            .map(c -> Objects.requireNonNull(c, "A child component is null.").toBungeeCordComponent())
            .toList());
        return new BungeeCordComponent(toReturn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent addHoverEvent(@NotNull IComponent hoverMsg) {
        Preconditions.checkNotNull(hoverMsg, "IComponent is null.");
        var toReturn = component.duplicate();
        toReturn.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new BaseComponent[]{hoverMsg.toBungeeCordComponent()}));
        return new BungeeCordComponent(toReturn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent applyStyle(@NotNull IStyle style) {
        Preconditions.checkNotNull(style, "IStyle is null.");
        BaseComponent component = this.component.duplicate();
        style.toBungeeCordStyle().applyDefaultStyleTo(component);
        return new BungeeCordComponent(component);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent applyDefaultStyle(@NotNull IStyle defaultStyle) {
        Preconditions.checkNotNull(defaultStyle, "IStyle is null.");
        if (defaultStyle.isEmpty()) {
            return this;
        }
        BaseComponent component = this.component.duplicate();
        defaultStyle.toBungeeCordStyle().applyDefaultStyleTo(component);
        return new BungeeCordComponent(component);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The returned value is <a href="https://en.wikipedia.org/wiki/Memoization">memoized</a>,
     *     i.e. it is computed on the first call and cached for subsequent ones.
     */
    @Override
    public boolean isEmptyOrStartsWithNewLine() {
        if (emptyOrStartsWithNewLine != null) {
            return emptyOrStartsWithNewLine;
        }

        var res = TextUtil.startsWithNewLine(component, TextUtil.MAX_DEPTH);
        return emptyOrStartsWithNewLine = (res == SearchResult.FOUND || res == SearchResult.EMPTY);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The returned value is <a href="https://en.wikipedia.org/wiki/Memoization">memoized</a>,
     *     i.e. it is computed on the first call and cached for subsequent ones.
     */
    @Override
    @NotNull
    public JsonString toJson() {
        if (jsonString != null) {
            return jsonString;
        }
        return jsonString = new JsonString(ComponentSerializer.toString(component));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String toLegacy() {
        return component.toLegacyText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String toPlain() {
        return component.toPlainText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent fromPlain(@NotNull String plainText) {
        return of(plainText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent fromLegacy(@NotNull String legacy) {
        return ofLegacy(legacy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent empty() {
        return EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent space() {
        return SPACE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent newLine() {
        return NEW_LINE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendTo(@NotNull Player player) {
        Preconditions.checkNotNull(player, "Player is null.");
        player.spigot().sendMessage(component);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        BungeeCordComponent that = (BungeeCordComponent) o;
        return Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {
        return component.hashCode();
    }

    @Override
    public String toString() {
        return "BungeeCordComponent{" +
            "component=" + component +
            '}';
    }
}
