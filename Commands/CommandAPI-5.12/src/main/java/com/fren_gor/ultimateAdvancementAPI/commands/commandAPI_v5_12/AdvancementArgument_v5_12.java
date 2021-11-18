package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v5_12;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import dev.jorel.commandapi.arguments.CustomArgument;
import org.jetbrains.annotations.Nullable;

public class AdvancementArgument_v5_12 extends CustomArgument<Advancement> {

    public AdvancementArgument_v5_12(AdvancementMain main, String nodeName) {
        super(nodeName, input -> {
            try {
                @Nullable Advancement adv = main.getAdvancement(input);
                if (adv == null) {
                    throw new CustomArgumentException(new MessageBuilder("Unknown advancement: ").appendArgInput());
                } else if (!adv.isValid()) {
                    throw new CustomArgumentException(new MessageBuilder("Invalid advancement: ").appendArgInput());
                } else {
                    return adv;
                }
            } catch (IllegalArgumentException e) {
                throw new CustomArgumentException(new MessageBuilder("Invalid advancement: ").appendArgInput().appendHere());
            }
        }, true);
        overrideSuggestions(sender -> main.filterNamespaces(null).toArray(new String[0]));
    }
}
