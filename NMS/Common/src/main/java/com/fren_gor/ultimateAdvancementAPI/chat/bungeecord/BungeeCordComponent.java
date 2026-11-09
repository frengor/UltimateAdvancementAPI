package com.fren_gor.ultimateAdvancementAPI.chat.bungeecord;

import com.fren_gor.ultimateAdvancementAPI.chat.IComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.IDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil;
import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil.SearchResult;
import com.fren_gor.ultimateAdvancementAPI.nms.util.JsonString;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class BungeeCordComponent implements IComponent {

    private static final boolean TEXT_COMPONENT_HAS_FROM_LEGACY = ReflectionUtil.hasMethod(TextComponent.class, "fromLegacy");

    private final BaseComponent component;

    public BungeeCordComponent(@NotNull BaseComponent component) {
        this.component = Objects.requireNonNull(component, "BaseComponent is null.").duplicate();
    }

    public BungeeCordComponent(@NotNull JsonString jsonString) {
        Preconditions.checkNotNull(jsonString, "JsonString is null.");

        // TODO: check old versions compatibility
        this.component = ComponentSerializer.deserialize(jsonString.jsonString());
    }

    @Override
    @NotNull
    public Component toAdventureComponent() {
        // TODO: check old versions compatibility
        return GsonComponentSerializer.gson().deserializeFromTree(ComponentSerializer.toJson(component));
    }

    @Override
    @NotNull
    public BaseComponent toBungeeCordComponent() {
        return component.duplicate();
    }

    @NotNull
    public IComponent append(@NotNull IComponent component) {
        Preconditions.checkNotNull(component, "IComponent is null.");
        var toReturn = this.component.duplicate();
        toReturn.addExtra(component.toBungeeCordComponent());
        return new BungeeCordComponent(toReturn);
    }

    @Override
    @NotNull
    public IComponent setChildren(@NotNull Collection<? extends IComponent> children) {
        Preconditions.checkNotNull(children, "Children components are null.");
        var toReturn = component.duplicate();
        toReturn.setExtra(children
            .stream()
            .map(c -> Objects.requireNonNull(c, "A child component is null.").toBungeeCordComponent())
            .toList());
        return new BungeeCordComponent(toReturn);
    }

    @NotNull
    public IComponent addHoverEvent(@NotNull IComponent hoverMsg) {
        Preconditions.checkNotNull(hoverMsg, "IComponent is null.");
        var toReturn = component.duplicate();
        toReturn.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new BaseComponent[]{hoverMsg.toBungeeCordComponent()}));
        return new BungeeCordComponent(toReturn);
    }

    @Override
    @NotNull
    public IComponent applyDefaultStyle(@NotNull IDefaultStyle defaultStyle) {
        Preconditions.checkNotNull(defaultStyle, "IDefaultStyle is null.");
        if (defaultStyle.isEmpty()) {
            return this;
        }
        BaseComponent component = this.component.duplicate();
        defaultStyle.toBungeeCordStyle().applyTo(component);
        return new BungeeCordComponent(component);
    }

    @Override
    public boolean isEmptyOrStartsWithNewLine() {
        var res = TextUtil.startsWithNewLine(component, TextUtil.MAX_DEPTH);
        return res == SearchResult.FOUND || res == SearchResult.EMPTY;
    }

    @Override
    @NotNull
    public JsonString toJson() {
        return new JsonString(ComponentSerializer.toString(component));
    }

    @Override
    @NotNull
    public String toLegacy() {
        return component.toLegacyText();
    }

    @Override
    @NotNull
    public String toPlain() {
        return component.toPlainText();
    }

    @Override
    public void sendTo(@NotNull Player player) {
        Preconditions.checkNotNull(player, "Player is null.");
        player.spigot().sendMessage(component);
    }

    /**
     * Equivalent of {@link TextComponent#fromLegacy(String)}. Present for compatibility with old versions.
     *
     * @param legacy The legacy string to convert.
     * @return The resulting {@link BaseComponent}.
     * @see TextComponent#fromLegacy(String)
     * @see TextComponent#fromLegacyText(String)
     */
    @NotNull
    public static BaseComponent fromLegacy(@NotNull String legacy) {
        Preconditions.checkNotNull(legacy, "Legacy string is null.");
        if (TEXT_COMPONENT_HAS_FROM_LEGACY) {
            return TextComponent.fromLegacy(legacy);
        }
        return new TextComponent(TextComponent.fromLegacyText(legacy));
    }
}
