package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v8_3_1;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancementArgument_v8_3_1 {

    @NotNull
    public static Argument<Advancement> getAdvancementArgument(AdvancementMain main, String nodeName) {
        return new CustomArgument<>(nodeName, input -> {
            try {
                @Nullable Advancement adv = main.getAdvancement(input.input());
                if (adv == null) {
                    throw new CustomArgumentException(new MessageBuilder("Unknown advancement: ").appendArgInput());
                } else if (!adv.isValid()) {
                    throw new CustomArgumentException(new MessageBuilder("Invalid advancement: ").appendArgInput());
                } else {
                    return adv;
                }
            } catch (IllegalArgumentException e) {
                throw new CustomArgumentException(new MessageBuilder("Illegal advancement: ").appendArgInput());
            }
        }, true).replaceSuggestions(ArgumentSuggestions.strings(sender -> main.filterNamespaces(null).toArray(new String[0])));
    }
}
