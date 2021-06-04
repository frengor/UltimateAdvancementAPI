package com.fren_gor.ultimateAdvancementAPI.commands;

import dev.jorel.commandapi.arguments.CustomArgument;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import org.jetbrains.annotations.Nullable;

public class AdvancementTabArgument extends CustomArgument<AdvancementTab> {

    public AdvancementTabArgument(String nodeName) {
        super(nodeName, input -> {
            @Nullable AdvancementTab adv = AdvancementMain.getInstance().getAdvancementTab(input);
            if (adv == null) {
                throw new CustomArgumentException(new MessageBuilder("Unknown advancement tab: ").appendArgInput());
            } else {
                return adv;
            }
        });
        overrideSuggestions(sender -> AdvancementMain.getInstance().getAdvancementTabNamespaces().toArray(new String[0]));
    }

}
