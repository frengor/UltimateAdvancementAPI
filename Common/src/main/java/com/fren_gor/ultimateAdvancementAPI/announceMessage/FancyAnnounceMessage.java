package com.fren_gor.ultimateAdvancementAPI.announceMessage;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * If an advancement implements this interface, its announce message will be vanilla like with an empty line always
 * present between the title and description (in the hover event).
 * <p>This class is part of the Advancement Announce Message System. See {@link IAnnounceMessage} for more information.
 */
public interface FancyAnnounceMessage extends IAnnounceMessage {

    /**
     * Returns the fancy announce message.
     *
     * @param advancement The advancement.
     * @param advancementCompleter The player who has completed the advancement.
     * @return The fancy announce message.
     */
    @Override
    @NotNull
    default BaseComponent getAnnounceMessage(@NotNull Advancement advancement, @NotNull Player advancementCompleter) {
        return AdvancementUtils.getAnnounceMessage(advancement, advancementCompleter, true);
    }
}
