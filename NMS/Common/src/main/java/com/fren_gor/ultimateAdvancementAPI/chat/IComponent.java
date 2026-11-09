package com.fren_gor.ultimateAdvancementAPI.chat;

import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil.SearchResult;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordComponent;
import com.fren_gor.ultimateAdvancementAPI.nms.util.JsonString;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Abstraction over platform-specific chat components like {@link BaseComponent} or <a href="https://docs.papermc.io/adventure/">Adventure's components</a>.
 */
public interface IComponent {

    // TODO: Make a (static) method
    @NotNull
    static IComponent empty() {
        return of(new TextComponent(""));
    }

    @NotNull
    static IComponent newLine() {
        return of(new TextComponent("\n"));
    }

    @NotNull
    static IComponent of(@NotNull BaseComponent component) {
        return new BungeeCordComponent(component);
    }

    /*@NotNull
    static IComponent of(@NotNull Component component) {
        return new AdventureComponent(component);
    }*/

    @NotNull
    static IComponent fromLegacy(@NotNull String legacy) {
        return of(BungeeCordComponent.fromLegacy(legacy));
    }

    @NotNull
    default BaseComponent toBungeeCordComponent() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    default Component toAdventureComponent() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    IComponent append(@NotNull IComponent component);

    @NotNull
    IComponent setChildren(@NotNull Collection<? extends IComponent> children);

    @NotNull
    IComponent addHoverEvent(@NotNull IComponent hoverMsg);

    @NotNull
    IComponent applyDefaultStyle(@NotNull IDefaultStyle defaultStyle);

    default boolean isEmptyOrStartsWithNewLine() {
        var res = TextUtil.startsWithNewLine(toPlain());
        return res == SearchResult.FOUND || res == SearchResult.EMPTY;
    }

    @NotNull
    JsonString toJson();

    @NotNull
    String toLegacy();

    @NotNull
    String toPlain();

    void sendTo(@NotNull Player player);
}
