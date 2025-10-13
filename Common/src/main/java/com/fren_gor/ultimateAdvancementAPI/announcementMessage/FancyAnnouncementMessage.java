package com.fren_gor.ultimateAdvancementAPI.announcementMessage;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * If an advancement implements this interface, its announcement message will be vanilla like with an empty line always
 * present between the title and description (in the hover event).
 * <p>This class is part of the Advancement Announcement Message System. See {@link IAnnouncementMessage} for more information.
 */
public interface FancyAnnouncementMessage extends IAnnouncementMessage {

    /**
     * Returns the fancy announcement message.
     *
     * @param advancement The advancement.
     * @param advancementCompleter The player who has completed the advancement.
     * @return The fancy announcement message.
     */
    @Override
    default BaseComponent getAnnouncementMessage(@NotNull Advancement advancement, @NotNull Player advancementCompleter) {
        return AdvancementUtils.getAnnouncementMessage(advancement, advancementCompleter, true);
    }
}
