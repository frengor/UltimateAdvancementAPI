package com.fren_gor.ultimateAdvancementAPI.chat;

import com.fren_gor.ultimateAdvancementAPI.chat.TextUtil.SearchResult;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Abstraction over platform-specific chat components like {@link BaseComponent} or <a href="https://docs.papermc.io/adventure/">Adventure's components</a>.
 */
public interface IComponent {

    @NotNull
    BaseComponent toBungeeCordComponent();

    @NotNull
    Component toAdventureComponent();

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

    @NotNull
    IComponent fromPlain(@NotNull String plainText);

    @NotNull
    IComponent fromLegacy(@NotNull String legacy);

    @NotNull
    IComponent empty();

    @NotNull
    IComponent space();

    @NotNull
    IComponent newLine();

    void sendTo(@NotNull Player player);
}
