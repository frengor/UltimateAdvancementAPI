package com.fren_gor.ultimateAdvancementAPI.chat;

import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordStyle;
import net.kyori.adventure.text.format.Style;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentStyle;
import org.jetbrains.annotations.NotNull;

public interface IDefaultStyle {
    // TODO: Make a (static) method
    @NotNull
    static IDefaultStyle empty() {
        return of(new BungeeCordStyle());
    }

    @NotNull
    static IDefaultStyle of(@NotNull ChatColor color) {
        return of(new BungeeCordStyle(color));
    }

    @NotNull
    static IDefaultStyle of(@NotNull ComponentStyle style) {
        return new BungeeCordDefaultStyle(style);
    }

    @NotNull
    static IDefaultStyle of(@NotNull BungeeCordStyle style) {
        return new BungeeCordDefaultStyle(style);
    }

    /*@NotNull
    static IDefaultStyle of(@NotNull Style component) {
        return new AdventureDefaultStyle(component);
    }*/

    @NotNull
    default BungeeCordStyle toBungeeCordStyle() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    default Style toAdventureStyle() {
        throw new UnsupportedOperationException();
    }

    boolean isEmpty();

    @NotNull
    IDefaultStyle merge(@NotNull IDefaultStyle style);
}
