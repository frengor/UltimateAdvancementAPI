package com.fren_gor.ultimateAdvancementAPI.announceMessage;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The core interface of the Advancement Announce Message System.
 * <p>A sub-interface of {@link IAnnounceMessage} is suitable for the Advancement Announce Message System if it provides
 * a default implementation to the {@link #getAnnounceMessage(Advancement, Player)} method.
 * A suitable sub-interface can be implemented by any {@link Advancement} sub-class to change the announce message of
 * that advancement.
 * <p>When {@link Advancement#getAnnounceMessage(Player)} is called, it calls the default method of the first
 * suitable interface found or returns the default announce message (see {@link DefaultAnnounceMessage}) if no suitable interface can be found.
 * <p>See {@link Advancement#getAnnounceMessage(Player)} for a more complete explanation of the search algorithm.
 * <p>Note that classes that override that method and does not call the {@link Advancement} one disables the
 * Advancement Announce Message System. Thus, {@link #getAnnounceMessage(Advancement, Player)} will not be called in that case.
 */
public interface IAnnounceMessage {

    /**
     * Gets the chat message to be sent when an advancement is completed.
     * <p>The message is sent to everybody online on the server.
     *
     * @param advancement The advancement.
     * @param advancementCompleter The player who has completed the advancement.
     * @return The message to be displayed, or {@code null} if no message should be displayed.
     */
    @Nullable
    BaseComponent getAnnounceMessage(@NotNull Advancement advancement, @NotNull Player advancementCompleter);
}
