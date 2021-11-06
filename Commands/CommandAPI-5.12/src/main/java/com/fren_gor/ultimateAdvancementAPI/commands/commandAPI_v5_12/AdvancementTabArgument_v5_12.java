package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v5_12;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import dev.jorel.commandapi.arguments.CustomArgument;
import org.jetbrains.annotations.Nullable;

public class AdvancementTabArgument_v5_12 extends CustomArgument<AdvancementTab> {

    public AdvancementTabArgument_v5_12(AdvancementMain main, String nodeName) {
        super(nodeName, input -> {
            @Nullable AdvancementTab adv = main.getAdvancementTab(input);
            if (adv == null) {
                throw new CustomArgumentException(new MessageBuilder("Unknown advancement tab: ").appendArgInput());
            } else {
                return adv;
            }
        });
        overrideSuggestions(sender -> main.getAdvancementTabNamespaces().toArray(new String[0]));
    }
}
