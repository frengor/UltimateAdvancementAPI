package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalOperationException;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAdvancementDisplay {
    AbstractAdvancementDisplay() {
        if (!(this instanceof AbstractUnchangingAdvancementDisplay || this instanceof AbstractPerPlayerAdvancementDisplay || this instanceof AbstractPerTeamAdvancementDisplay)) {
            throw new IllegalOperationException(getClass().getName() + " is neither an instance of AbstractUnchangingAdvancementDisplay, AbstractPerPlayerAdvancementDisplay nor AbstractPerTeamAdvancementDisplay.");
        }
    }

    /**
     * Gets the chat message to be sent when an advancement is completed.
     * <p>The message is sent to everybody online on the server.
     *
     * @param advancementCompleter The player who has completed the advancement.
     * @param advancement The advancement being completed.
     * @return The message to be displayed.
     */
    @NotNull
    public abstract BaseComponent[] getAnnounceMessage(@NotNull Player advancementCompleter, @NotNull Advancement advancement);
}
