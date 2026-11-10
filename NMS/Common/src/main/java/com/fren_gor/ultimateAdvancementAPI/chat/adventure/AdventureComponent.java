package com.fren_gor.ultimateAdvancementAPI.chat.adventure;

import com.fren_gor.ultimateAdvancementAPI.chat.IComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.IDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil;
import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil.SearchResult;
import com.fren_gor.ultimateAdvancementAPI.chat.JsonString;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import com.google.common.base.Preconditions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class AdventureComponent implements IComponent {

    public static final IComponent EMPTY = of("");

    public static final IComponent SPACE = of(" ");

    public static final IComponent NEW_LINE = of("\n");

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

    @NotNull
    public static IComponent of(@NotNull Component component) {
        return new AdventureComponent(component);
    }

    @NotNull
    public static IComponent of(@NotNull JsonString jsonString) {
        Preconditions.checkNotNull(jsonString, "JsonString is null.");
        // TODO: check old versions compatibility
        var component = new AdventureComponent(GsonComponentSerializer.gson().deserialize(jsonString.jsonString()));
        component.jsonString = jsonString;
        return component;
    }

    @NotNull
    public static IComponent of(@NotNull String plainText) {
        Preconditions.checkNotNull(plainText, "String is null.");
        return new AdventureComponent(Component.text(plainText));
    }

    @NotNull
    public static IComponent ofLegacy(@NotNull String legacy) {
        Preconditions.checkNotNull(legacy, "Legacy string is null.");
        return new AdventureComponent(LegacyComponentSerializer.legacySection().deserialize(legacy));
    }

    @Override
    @NotNull
    public Component toAdventureComponent() {
        return component;
    }

    @Override
    @NotNull
    public BaseComponent toBungeeCordComponent() {
        if (baseComponent != null) {
            return baseComponent.duplicate();
        }
        // TODO: check old versions compatibility
        return (baseComponent = ComponentSerializer.deserialize(GsonComponentSerializer.gson().serializeToTree(component))).duplicate();
    }

    @NotNull
    public IComponent append(@NotNull IComponent component) {
        Preconditions.checkNotNull(component, "IComponent is null.");
        return new AdventureComponent(this.component.append(component.toAdventureComponent()));
    }

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

    @NotNull
    public IComponent addHoverEvent(@NotNull IComponent hoverMsg) {
        Preconditions.checkNotNull(hoverMsg, "IComponent is null.");
        return new AdventureComponent(this.component.hoverEvent(HoverEvent.showText(hoverMsg.toAdventureComponent())));
    }

    @Override
    @NotNull
    public IComponent applyDefaultStyle(@NotNull IDefaultStyle defaultStyle) {
        Preconditions.checkNotNull(defaultStyle, "IDefaultStyle is null.");
        if (defaultStyle.isEmpty()) {
            return this;
        }
        return new AdventureComponent(component.applyFallbackStyle(defaultStyle.toAdventureStyle()));
    }

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

    @Override
    @NotNull
    public JsonString toJson() {
        if (jsonString != null) {
            return jsonString;
        }
        return jsonString = new JsonString(GsonComponentSerializer.gson().serialize(component));
    }

    @Override
    @NotNull
    public String toLegacy() {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    @Override
    @NotNull
    public String toPlain() {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Override
    @NotNull
    public IComponent fromPlain(@NotNull String plainText) {
        return of(plainText);
    }

    @Override
    @NotNull
    public IComponent fromLegacy(@NotNull String legacy) {
        return ofLegacy(legacy);
    }

    @Override
    @NotNull
    public IComponent empty() {
        return EMPTY;
    }

    @Override
    @NotNull
    public IComponent space() {
        return SPACE;
    }

    @Override
    @NotNull
    public IComponent newLine() {
        return NEW_LINE;
    }

    @Override
    public void sendTo(@NotNull Player player) {
        Preconditions.checkNotNull(player, "Player is null.");
        ((Audience) player).sendMessage(component);
    }
}
