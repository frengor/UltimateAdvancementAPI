package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v8_8_0;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import dev.jorel.commandapi.arguments.TextArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancementTabArgument {

    @NotNull
    public static Argument<AdvancementTab> getAdvancementTabArgument(AdvancementMain main, String nodeName) {
        return new CustomArgument<>(new TextArgument(nodeName), input -> {
            @Nullable AdvancementTab adv = main.getAdvancementTab(input.input());
            if (adv == null) {
                throw new CustomArgumentException(new MessageBuilder("Unknown advancement tab: ").appendArgInput().appendHere());
            } else if (!adv.isActive()) {
                throw new CustomArgumentException(new MessageBuilder("Invalid advancement tab: ").appendArgInput().appendHere());
            } else {
                return adv;
            }
        }).replaceSuggestions(ArgumentSuggestions.strings(sender -> main.getAdvancementTabNamespaces().toArray(new String[0])));
    }
}
