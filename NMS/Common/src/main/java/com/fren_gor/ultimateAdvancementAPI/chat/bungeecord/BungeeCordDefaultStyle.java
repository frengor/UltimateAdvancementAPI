package com.fren_gor.ultimateAdvancementAPI.chat.bungeecord;

import com.fren_gor.ultimateAdvancementAPI.chat.IDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import net.kyori.adventure.text.format.Style;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentStyle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BungeeCordDefaultStyle implements IDefaultStyle {

    public static IDefaultStyle EMPTY = of(new BungeeCordStyle());

    private final BungeeCordStyle style;

    @LazyValue
    private Object adventureStyle; // Use Object to avoid loading issues on Spigot

    private BungeeCordDefaultStyle(@NotNull BungeeCordStyle style) {
        this.style = Objects.requireNonNull(style, "BungeeCordStyle is null.");
    }

    @NotNull
    public static IDefaultStyle of(@NotNull ChatColor color) {
        return of(new BungeeCordStyle(color));
    }

    @NotNull
    public static IDefaultStyle of(@NotNull ComponentStyle style) {
        return of(new BungeeCordStyle(style));
    }

    @NotNull
    public static IDefaultStyle of(@NotNull BungeeCordStyle style) {
        return new BungeeCordDefaultStyle(style);
    }

    @Override
    @NotNull
    public Style toAdventureStyle() {
        if (adventureStyle != null) {
            return (Style) adventureStyle;
        }
        return (Style) (adventureStyle = AdventureConverter.toAdventureStyle(style));
    }

    @Override
    @NotNull
    public BungeeCordStyle toBungeeCordStyle() {
        return this.style;
    }

    @Override
    public boolean isEmpty() {
        return style.isEmpty();
    }

    @Override
    @NotNull
    public IDefaultStyle merge(@NotNull IDefaultStyle style) {
        return new BungeeCordDefaultStyle(this.style.merge(style.toBungeeCordStyle()));
    }
}
