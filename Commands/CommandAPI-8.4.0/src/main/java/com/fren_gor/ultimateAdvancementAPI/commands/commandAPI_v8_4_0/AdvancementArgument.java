package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v8_4_0;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancementArgument {

    @NotNull
    public static Argument<Advancement> getAdvancementArgument(AdvancementMain main, String nodeName) {
        return new CustomArgument<>(new NamespacedKeyArgument(nodeName), input -> {
            try {
                @Nullable Advancement adv = main.getAdvancement(input.input());
                if (adv == null) {
                    throw new CustomArgumentException(new MessageBuilder("Unknown advancement: ").appendArgInput().appendHere());
                } else if (!adv.isValid()) {
                    throw new CustomArgumentException(new MessageBuilder("Invalid advancement: ").appendArgInput().appendHere());
                } else {
                    return adv;
                }
            } catch (IllegalArgumentException e) {
                throw new CustomArgumentException(new MessageBuilder("Illegal advancement: ").appendArgInput().appendHere());
            }
        }).replaceSuggestions(ArgumentSuggestions.strings(sender -> main.filterNamespaces(null).toArray(new String[0])));
    }
}
