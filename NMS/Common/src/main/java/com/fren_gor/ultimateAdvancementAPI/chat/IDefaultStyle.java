package com.fren_gor.ultimateAdvancementAPI.chat;

import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordStyle;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

public interface IDefaultStyle {

    @NotNull
    BungeeCordStyle toBungeeCordStyle();

    @NotNull
    Style toAdventureStyle();

    boolean isEmpty();

    @NotNull
    IDefaultStyle merge(@NotNull IDefaultStyle style);
}
