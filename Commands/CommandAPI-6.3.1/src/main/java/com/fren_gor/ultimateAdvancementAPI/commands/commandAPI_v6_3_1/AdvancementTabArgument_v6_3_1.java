package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v6_3_1;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancementTabArgument_v6_3_1 {

    @NotNull
    public static Argument getAdvancementTabArgument(AdvancementMain main, String nodeName) {
        return new CustomArgument<>(nodeName, input -> {
            @Nullable AdvancementTab adv = main.getAdvancementTab(input.input());
            if (adv == null) {
                throw new CustomArgumentException(new MessageBuilder("Unknown advancement tab: ").appendArgInput());
            } else if (!adv.isActive()) {
                throw new CustomArgumentException(new MessageBuilder("Invalid advancement tab: ").appendArgInput());
            } else {
                return adv;
            }
        }).replaceSuggestions(sender -> main.getAdvancementTabNamespaces().toArray(new String[0]));
    }
}
