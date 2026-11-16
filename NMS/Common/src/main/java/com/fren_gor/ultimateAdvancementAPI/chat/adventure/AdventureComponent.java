package com.fren_gor.ultimateAdvancementAPI.chat.adventure;

import com.fren_gor.ultimateAdvancementAPI.chat.IComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.IStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil;
import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil.SearchResult;
import com.fren_gor.ultimateAdvancementAPI.chat.JsonString;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordComponent;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * Implementation of {@link IComponent} for Adventure chat components.
 *
 * @see Component
 * @see AdventureStyle
 */
public class AdventureComponent implements IComponent {

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

    private static final String OLD_PLAIN_SERIALIZER_CLASS = "net.kyori.adventure.text.serializer.plain.PlainComponentSerializer";
    private static final boolean IS_OLD_PLAIN_SERIALIZER = ReflectionUtil.classExists(OLD_PLAIN_SERIALIZER_CLASS);
    private static final boolean COMPONENT_SERIALIZER_HAS_DESERIALIZE = ReflectionUtil.hasMethod(ComponentSerializer.class, "deserialize", JsonElement.class);

    private final Component component;

    @LazyValue
    private BaseComponent baseComponent;
    @LazyValue
    private JsonString jsonString;
    @LazyValue
    private Boolean emptyOrStartsWithNewLine;

    private AdventureComponent(@NotNull Component component) {
        this.component = Objects.requireNonNull(component, "Component is null.");
    }

    /**
     * Creates a new {@code AdventureComponent} with the provided {@link Component}.
     *
     * @param component The component.
     * @return A new {@code AdventureComponent} with the provided {@link Component}.
     */
    @NotNull
    public static IComponent of(@NotNull Component component) {
        return new AdventureComponent(component);
    }

    /**
     * Creates a new {@code AdventureComponent} from the provided {@link JsonString}.
     *
     * @param jsonString The JSON string.
     * @return A new {@code AdventureComponent} from the provided {@link JsonString}.
     */
    @NotNull
    public static IComponent of(@NotNull JsonString jsonString) {
        Preconditions.checkNotNull(jsonString, "JsonString is null.");
        var component = new AdventureComponent(GsonComponentSerializer.gson().deserialize(jsonString.jsonString()));
        component.jsonString = jsonString;
        return component;
    }

    /**
     * Creates a new {@code AdventureComponent} with the provided plain text.
     *
     * @param plainText The plain text.
     * @return A new {@code AdventureComponent} with the provided plain text.
     */
    @NotNull
    public static IComponent of(@NotNull String plainText) {
        Preconditions.checkNotNull(plainText, "String is null.");
        return new AdventureComponent(Component.text(plainText));
    }

    /**
     * Creates a new {@code AdventureComponent} from the provided legacy string.
     *
     * @param legacy The legacy string. May contain legacy color codes.
     * @return A new {@code AdventureComponent} from the provided legacy string.
     */
    @NotNull
    public static IComponent ofLegacy(@NotNull String legacy) {
        Preconditions.checkNotNull(legacy, "Legacy string is null.");
        return new AdventureComponent(LegacyComponentSerializer.legacySection().deserialize(legacy));
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The returned value is <a href="https://en.wikipedia.org/wiki/Memoization">memoized</a>,
     *     i.e. it is computed on the first call and cached for subsequent ones.
     */
    @Override
    @NotNull
    public BaseComponent toBungeeCordComponent() {
        if (baseComponent != null) {
            return baseComponent.duplicate();
        }
        if (COMPONENT_SERIALIZER_HAS_DESERIALIZE) {
            baseComponent = ComponentSerializer.deserialize(GsonComponentSerializer.gson().serializeToTree(component));
        } else {
            baseComponent = BungeeCordComponent.of(toJson()).toBungeeCordComponent();
        }
        return baseComponent.duplicate();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This method requires no conversion and simply returns the wrapped value.
     */
    @Override
    @NotNull
    public Component toAdventureComponent() {
        return component;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent append(@NotNull IComponent component) {
        Preconditions.checkNotNull(component, "IComponent is null.");
        return new AdventureComponent(this.component.append(component.toAdventureComponent()));
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
        return new AdventureComponent(component.children(children
            .stream()
            .map(c -> Objects.requireNonNull(c, "A child component is null.").toAdventureComponent())
            .toList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IComponent addHoverEvent(@NotNull IComponent hoverMsg) {
        Preconditions.checkNotNull(hoverMsg, "IComponent is null.");
        return new AdventureComponent(this.component.hoverEvent(HoverEvent.showText(hoverMsg.toAdventureComponent())));
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
        // applyFallbackStyle(...) is present only on 1.18.2+
        return new AdventureComponent(component.style(component.style().merge(defaultStyle.toAdventureStyle(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET)));
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

        // Workaround for "Variable is accessed from within inner class, needs to be final or effectively final"
        final SearchResult[] result = {SearchResult.EMPTY};

        ComponentFlattener.basic().flatten(component, new FlattenerListener() {
            private boolean cont = true;

            @Override
            public void component(@NotNull String text) {
                if (!cont) {
                    // shouldContinue() was added after ComponentFlattener,
                    // make sure the search is not continued on old versions
                    return;
                }
                var res = TextUtil.startsWithNewLine(text);
                if (res != SearchResult.EMPTY) {
                    result[0] = res;
                    cont = false;
                }
            }

            @Override
            public boolean shouldContinue() {
                return cont;
            }
        });

        return emptyOrStartsWithNewLine = (result[0] == SearchResult.FOUND || result[0] == SearchResult.EMPTY);
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
        return jsonString = new JsonString(GsonComponentSerializer.gson().serialize(component));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String toLegacy() {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public String toPlain() {
        net.kyori.adventure.text.serializer.ComponentSerializer<Component, ?, String> serializer;
        if (IS_OLD_PLAIN_SERIALIZER) {
            try {
                serializer = (net.kyori.adventure.text.serializer.ComponentSerializer<Component, ?, String>)
                    Class.forName(OLD_PLAIN_SERIALIZER_CLASS).getDeclaredMethod("plain").invoke(null);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } else {
            serializer = PlainTextComponentSerializer.plainText();
        }
        return serializer.serialize(component);
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
        ((Audience) player).sendMessage(component);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AdventureComponent that = (AdventureComponent) o;
        return Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {
        return component.hashCode();
    }

    @Override
    public String toString() {
        return "AdventureComponent{" +
            "component=" + component +
            '}';
    }
}
