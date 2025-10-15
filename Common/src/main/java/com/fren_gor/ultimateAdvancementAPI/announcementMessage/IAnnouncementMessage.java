package com.fren_gor.ultimateAdvancementAPI.announcementMessage;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * The core interface of the Advancement Announcement Message System.
 * <p>A sub-interface of {@link IAnnouncementMessage} is suitable for the Advancement Announcement Message System if it provides
 * a default implementation to the {@link #getAnnouncementMessage(Advancement, Player)} method.
 * A suitable sub-interface can be implemented by any {@link Advancement} sub-class to change the announcement message of
 * that advancement.
 * <p>When {@link Advancement#getAnnouncementMessage(Player)} is called, it calls the default method of the first
 * suitable interface found or returns the default announcement message (see {@link DefaultAnnouncementMessage}) if no suitable interface can be found.
 * <p>See {@link Advancement#getAnnouncementMessage(Player)} for a more complete explanation of the search algorithm.
 * <p>Note that classes that override that method and does not call the {@link Advancement} one disables the
 * Advancement Announcement Message System. Thus, {@link #getAnnouncementMessage(Advancement, Player)} will not be called in that case.
 */
public interface IAnnouncementMessage {

    /**
     * Returns a function to get the per-player chat message to be displayed when an advancement is completed.
     * <p>The returned function is called for each online player and should return the announcement message
     * to send them, or {@code null} if no message should be shown to that player.
     * <p>If the same message should be displayed to every player, it is suggested to create it once and
     * return it for all the players, like in the following example:
     * <pre> {@code default Function<Player, BaseComponent> getAnnouncementMessage(Advancement advancement, Player advancementCompleter) {
     *   BaseComponent announcementMsg = ...; // Your announcement message
     *   return player -> announcementMsg; // The same message will be displayed to every player
     * }}</pre>
     *
     * @param advancement The advancement.
     * @param advancementCompleter The player who has completed the advancement.
     * @return A function which returns, for each player, the announcement message to be displayed to them.
     *         {@code null} can be returned instead if no message should be displayed to any player.
     */
    @Nullable
    Function<@NotNull Player, @Nullable BaseComponent> getAnnouncementMessage(@NotNull Advancement advancement, @NotNull Player advancementCompleter);
}
