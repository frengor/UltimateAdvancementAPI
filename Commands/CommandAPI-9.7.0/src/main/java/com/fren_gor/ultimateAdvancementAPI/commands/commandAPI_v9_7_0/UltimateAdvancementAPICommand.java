package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v9_7_0;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.commands.CommandsCommon;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

import static com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager.*;
import static com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v9_7_0.AdvancementArgument.getAdvancementArgument;
import static com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v9_7_0.AdvancementTabArgument.getAdvancementTabArgument;

public class UltimateAdvancementAPICommand {

    private final AdvancementMain main;
    private final CommandsCommon<WrapperCommandSyntaxException> commandsCommon;

    protected UltimateAdvancementAPICommand(@NotNull AdvancementMain main) {
        this.main = Objects.requireNonNull(main, "AdvancementMain is null.");
        this.commandsCommon = new CommandsCommon<>(main, CommandAPI::failWithString);
    }

    @SuppressWarnings("unchecked")
    public void register() {
        CommandAPICommand mainCommand = new CommandAPICommand("ultimateadvancementapi").withPermission(PERMISSION_MAIN_COMMAND).withAliases("uladvapi", "uladv", "uaapi").executes((sender, args) -> {
            sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi <progression|grant|revoke> ...");
        });

        CommandAPICommand grant = new CommandAPICommand("grant").withPermission(PERMISSION_GRANT).executes((sender, args) -> {
            sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant <all|tab|one> ...");
        }).withSubcommands(
                new CommandAPICommand("all").withPermission(PERMISSION_GRANT_ALL).executesPlayer((Player player, CommandArguments args) -> commandsCommon.grantAll(player, player, true)).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant all <player> [giveRewards]");
                }),
                new CommandAPICommand("all").withPermission(PERMISSION_GRANT_ALL).withArguments(new EntitySelectorArgument.ManyPlayers("player")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.grantAll(sender, (Collection<Player>) args.get("player"), true)),
                new CommandAPICommand("all").withPermission(PERMISSION_GRANT_ALL).withArguments(new EntitySelectorArgument.ManyPlayers("player"), new BooleanArgument("giveRewards")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.grantAll(sender, (Collection<Player>) args.get("player"), (boolean) args.get("giveRewards"))),

                new CommandAPICommand("tab").withPermission(PERMISSION_GRANT_TAB).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant tab <advancementTab> [player] [giveRewards]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant tab <advancementTab> <player> [giveRewards]");
                }),
                new CommandAPICommand("tab").withPermission(PERMISSION_GRANT_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab")).executesPlayer((Player player, CommandArguments args) -> commandsCommon.grantTab(player, (AdvancementTab) args.get("advancementTab"), player, true)),
                new CommandAPICommand("tab").withPermission(PERMISSION_GRANT_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab"), new EntitySelectorArgument.ManyPlayers("player")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.grantTab(sender, (AdvancementTab) args.get("advancementTab"), (Collection<Player>) args.get("player"), true)),
                new CommandAPICommand("tab").withPermission(PERMISSION_GRANT_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab"), new EntitySelectorArgument.ManyPlayers("player"), new BooleanArgument("giveRewards")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.grantTab(sender, (AdvancementTab) args.get("advancementTab"), (Collection<Player>) args.get("player"), (boolean) args.get("giveRewards"))),

                new CommandAPICommand("one").withPermission(PERMISSION_GRANT_ONE).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant one <advancement> [player] [giveRewards]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant one <advancement> <player> [giveRewards]");
                }),
                new CommandAPICommand("one").withPermission(PERMISSION_GRANT_ONE).withArguments(getAdvancementArgument(main, "advancement")).executesPlayer((Player player, CommandArguments args) -> commandsCommon.grantOne(player, (Advancement) args.get("advancement"), player, true)),
                new CommandAPICommand("one").withPermission(PERMISSION_GRANT_ONE).withArguments(getAdvancementArgument(main, "advancement"), new EntitySelectorArgument.ManyPlayers("player")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.grantOne(sender, (Advancement) args.get("advancement"), (Collection<Player>) args.get("player"), true)),
                new CommandAPICommand("one").withPermission(PERMISSION_GRANT_ONE).withArguments(getAdvancementArgument(main, "advancement"), new EntitySelectorArgument.ManyPlayers("player"), new BooleanArgument("giveRewards")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.grantOne(sender, (Advancement) args.get("advancement"), (Collection<Player>) args.get("player"), (boolean) args.get("giveRewards")))
        );

        CommandAPICommand revoke = new CommandAPICommand("revoke").withPermission(PERMISSION_REVOKE).executes((sender, args) -> {
            sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke <all|tab|one> ...");
        }).withSubcommands(
                new CommandAPICommand("all").withPermission(PERMISSION_REVOKE_ALL).executesPlayer((Player player, CommandArguments args) -> commandsCommon.revokeAll(player, player, false)).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke all <player> [hideTab]");
                }),
                new CommandAPICommand("all").withPermission(PERMISSION_REVOKE_ALL).withArguments(new EntitySelectorArgument.ManyPlayers("player")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.revokeAll(sender, (Collection<Player>) args.get("player"), false)),
                new CommandAPICommand("all").withPermission(PERMISSION_REVOKE_ALL).withArguments(new EntitySelectorArgument.ManyPlayers("player"), new BooleanArgument("hideTabs")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.revokeAll(sender, (Collection<Player>) args.get("player"), (boolean) args.get("hideTabs"))),

                new CommandAPICommand("tab").withPermission(PERMISSION_REVOKE_TAB).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke tab <advancementTab> [player] [hideTab]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke tab <advancementTab> <player> [hideTab]");
                }),
                new CommandAPICommand("tab").withPermission(PERMISSION_REVOKE_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab")).executesPlayer((Player player, CommandArguments args) -> commandsCommon.revokeTab(player, (AdvancementTab) args.get("advancementTab"), player, false)),
                new CommandAPICommand("tab").withPermission(PERMISSION_REVOKE_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab"), new EntitySelectorArgument.ManyPlayers("player")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.revokeTab(sender, (AdvancementTab) args.get("advancementTab"), (Collection<Player>) args.get("player"), false)),
                new CommandAPICommand("tab").withPermission(PERMISSION_REVOKE_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab"), new EntitySelectorArgument.ManyPlayers("player"), new BooleanArgument("hideTab")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.revokeTab(sender, (AdvancementTab) args.get("advancementTab"), (Collection<Player>) args.get("player"), (boolean) args.get("hideTabs"))),

                new CommandAPICommand("one").withPermission(PERMISSION_REVOKE_ONE).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke one <advancement> [player]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke one <advancement> <player>");
                }),
                new CommandAPICommand("one").withPermission(PERMISSION_REVOKE_ONE).withArguments(getAdvancementArgument(main, "advancement")).executesPlayer((Player player, CommandArguments args) -> commandsCommon.revokeOne(player, (Advancement) args.get("advancement"), player)),
                new CommandAPICommand("one").withPermission(PERMISSION_REVOKE_ONE).withArguments(getAdvancementArgument(main, "advancement"), new EntitySelectorArgument.ManyPlayers("player")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.revokeOne(sender, (Advancement) args.get("advancement"), (Collection<Player>) args.get("player")))
        );

        CommandAPICommand progression = new CommandAPICommand("progression").withPermission(PERMISSION_PROGRESSION).executes((sender, args) -> {
            sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi progression <get|set> ...");
        }).withSubcommands(new CommandAPICommand("get").withPermission(PERMISSION_PROGRESSION_GET).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi progression get <advancement> [player]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi progression get <advancement> <player>");
                }),
                new CommandAPICommand("get").withPermission(PERMISSION_PROGRESSION_GET).withArguments(getAdvancementArgument(main, "advancement")).executesPlayer((Player player, CommandArguments args) -> commandsCommon.getProgression(player, (Advancement) args.get("advancement"), player)),
                new CommandAPICommand("get").withPermission(PERMISSION_PROGRESSION_GET).withArguments(getAdvancementArgument(main, "advancement"), new EntitySelectorArgument.ManyPlayers("player")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.getProgression(sender, (Advancement) args.get("advancement"), (Collection<Player>) args.get("player"))),

                new CommandAPICommand("set").withPermission(PERMISSION_PROGRESSION_SET).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi progression set <advancement> <progression> [player] [giveRewards]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi progression set <advancement> <progression> <player> [giveRewards]");
                }),
                new CommandAPICommand("set").withPermission(PERMISSION_PROGRESSION_SET).withArguments(getAdvancementArgument(main, "advancement"), new IntegerArgument("progression", 0)).executesPlayer((Player player, CommandArguments args) -> commandsCommon.setProgression(player, (Advancement) args.get("advancement"), (int) args.get("progression"), player, true)),
                new CommandAPICommand("set").withPermission(PERMISSION_PROGRESSION_SET).withArguments(getAdvancementArgument(main, "advancement"), new IntegerArgument("progression", 0), new EntitySelectorArgument.ManyPlayers("player")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.setProgression(sender, (Advancement) args.get("advancement"), (int) args.get("progression"), (Collection<Player>) args.get("player"), true)),
                new CommandAPICommand("set").withPermission(PERMISSION_PROGRESSION_SET).withArguments(getAdvancementArgument(main, "advancement"), new IntegerArgument("progression", 0), new EntitySelectorArgument.ManyPlayers("player"), new BooleanArgument("giveRewards")).executes((CommandSender sender, CommandArguments args) -> commandsCommon.setProgression(sender, (Advancement) args.get("advancement"), (int) args.get("progression"), (Collection<Player>) args.get("player"), (boolean) args.get("giveRewards")))
        );

        mainCommand.withSubcommands(
                progression,
                grant,
                revoke
        );

        mainCommand.withHelp("Command to handle advancements.", "Command to grant/revoke/update player's advancements.");
        mainCommand.register();
    }
}
