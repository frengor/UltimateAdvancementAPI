package com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v8_3_1;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.fren_gor.ultimateAdvancementAPI.commands.CommandAPIManager.*;
import static com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v8_3_1.AdvancementArgument_v8_3_1.getAdvancementArgument;
import static com.fren_gor.ultimateAdvancementAPI.commands.commandAPI_v8_3_1.AdvancementTabArgument_v8_3_1.getAdvancementTabArgument;

public class UltimateAdvancementAPICommand_v8_3_1 {

    private final AdvancementMain main;

    protected UltimateAdvancementAPICommand_v8_3_1(@NotNull AdvancementMain main) {
        this.main = Objects.requireNonNull(main, "AdvancementMain is null.");
    }

    @SuppressWarnings("unchecked")
    public void register() {
        CommandAPICommand mainCommand = new CommandAPICommand("ultimateadvancementapi").withPermission(PERMISSION_MAIN_COMMAND).withAliases("uladvapi", "uladv", "uaapi").executes((sender, args) -> {
            sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi <progression|grant|revoke> ...");
        });

        CommandAPICommand grant = new CommandAPICommand("grant").withPermission(PERMISSION_GRANT).executes((sender, args) -> {
            sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant <all|tab|one> ...");
        }).withSubcommands(
                new CommandAPICommand("all").withPermission(PERMISSION_GRANT_ALL).executesPlayer((Player player, Object[] args) -> grantAll(player, player, true)).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant all <player> [giveRewards]");
                }),
                new CommandAPICommand("all").withPermission(PERMISSION_GRANT_ALL).withArguments(new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS)).executes((CommandSender sender, Object[] args) -> grantAll(sender, (Collection<Player>) args[0], true)),
                new CommandAPICommand("all").withPermission(PERMISSION_GRANT_ALL).withArguments(new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS), new BooleanArgument("giveRewards")).executes((CommandSender sender, Object[] args) -> grantAll(sender, (Collection<Player>) args[0], (boolean) args[1])),

                new CommandAPICommand("tab").withPermission(PERMISSION_GRANT_TAB).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant tab <advancementTab> [player] [giveRewards]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant tab <advancementTab> <player> [giveRewards]");
                }),
                new CommandAPICommand("tab").withPermission(PERMISSION_GRANT_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab")).executesPlayer((Player player, Object[] args) -> grantTab(player, (AdvancementTab) args[0], player, true)),
                new CommandAPICommand("tab").withPermission(PERMISSION_GRANT_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab"), new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS)).executes((CommandSender sender, Object[] args) -> grantTab(sender, (AdvancementTab) args[0], (Collection<Player>) args[1], true)),
                new CommandAPICommand("tab").withPermission(PERMISSION_GRANT_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab"), new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS), new BooleanArgument("giveRewards")).executes((CommandSender sender, Object[] args) -> grantTab(sender, (AdvancementTab) args[0], (Collection<Player>) args[1], (boolean) args[2])),

                new CommandAPICommand("one").withPermission(PERMISSION_GRANT_ONE).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant one <advancement> [player] [giveRewards]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi grant one <advancement> <player> [giveRewards]");
                }),
                new CommandAPICommand("one").withPermission(PERMISSION_GRANT_ONE).withArguments(getAdvancementArgument(main, "advancement")).executesPlayer((Player player, Object[] args) -> grantOne(player, (Advancement) args[0], player, true)),
                new CommandAPICommand("one").withPermission(PERMISSION_GRANT_ONE).withArguments(getAdvancementArgument(main, "advancement"), new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS)).executes((CommandSender sender, Object[] args) -> grantOne(sender, (Advancement) args[0], (Collection<Player>) args[1], true)),
                new CommandAPICommand("one").withPermission(PERMISSION_GRANT_ONE).withArguments(getAdvancementArgument(main, "advancement"), new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS), new BooleanArgument("giveRewards")).executes((CommandSender sender, Object[] args) -> grantOne(sender, (Advancement) args[0], (Collection<Player>) args[1], (boolean) args[2]))
        );

        CommandAPICommand revoke = new CommandAPICommand("revoke").withPermission(PERMISSION_REVOKE).executes((sender, args) -> {
            sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke <all|tab|one> ...");
        }).withSubcommands(
                new CommandAPICommand("all").withPermission(PERMISSION_REVOKE_ALL).executesPlayer((Player player, Object[] args) -> revokeAll(player, player, false)).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke all <player> [hideTab]");
                }),
                new CommandAPICommand("all").withPermission(PERMISSION_REVOKE_ALL).withArguments(new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS)).executes((CommandSender sender, Object[] args) -> revokeAll(sender, (Collection<Player>) args[0], false)),
                new CommandAPICommand("all").withPermission(PERMISSION_REVOKE_ALL).withArguments(new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS), new BooleanArgument("hideTabs")).executes((CommandSender sender, Object[] args) -> revokeAll(sender, (Collection<Player>) args[0], (boolean) args[1])),

                new CommandAPICommand("tab").withPermission(PERMISSION_REVOKE_TAB).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke tab <advancementTab> [player] [hideTab]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke tab <advancementTab> <player> [hideTab]");
                }),
                new CommandAPICommand("tab").withPermission(PERMISSION_REVOKE_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab")).executesPlayer((Player player, Object[] args) -> revokeTab(player, (AdvancementTab) args[0], player, false)),
                new CommandAPICommand("tab").withPermission(PERMISSION_REVOKE_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab"), new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS)).executes((CommandSender sender, Object[] args) -> revokeTab(sender, (AdvancementTab) args[0], (Collection<Player>) args[1], false)),
                new CommandAPICommand("tab").withPermission(PERMISSION_REVOKE_TAB).withArguments(getAdvancementTabArgument(main, "advancementTab"), new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS), new BooleanArgument("hideTab")).executes((CommandSender sender, Object[] args) -> revokeTab(sender, (AdvancementTab) args[0], (Collection<Player>) args[1], (boolean) args[2])),

                new CommandAPICommand("one").withPermission(PERMISSION_REVOKE_ONE).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke one <advancement> [player]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi revoke one <advancement> <player>");
                }),
                new CommandAPICommand("one").withPermission(PERMISSION_REVOKE_ONE).withArguments(getAdvancementArgument(main, "advancement")).executesPlayer((Player player, Object[] args) -> revokeOne(player, (Advancement) args[0], player)),
                new CommandAPICommand("one").withPermission(PERMISSION_REVOKE_ONE).withArguments(getAdvancementArgument(main, "advancement"), new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS)).executes((CommandSender sender, Object[] args) -> revokeOne(sender, (Advancement) args[0], (Collection<Player>) args[1]))
        );

        CommandAPICommand progression = new CommandAPICommand("progression").withPermission(PERMISSION_PROGRESSION).executes((sender, args) -> {
            sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi progression <get|set> ...");
        }).withSubcommands(new CommandAPICommand("get").withPermission(PERMISSION_PROGRESSION_GET).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi progression get <advancement> [player]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi progression get <advancement> <player>");
                }),
                new CommandAPICommand("get").withPermission(PERMISSION_PROGRESSION_GET).withArguments(getAdvancementArgument(main, "advancement")).executesPlayer((Player player, Object[] args) -> getProgression(player, (Advancement) args[0], player)),
                new CommandAPICommand("get").withPermission(PERMISSION_PROGRESSION_GET).withArguments(getAdvancementArgument(main, "advancement"), new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS)).executes((CommandSender sender, Object[] args) -> getProgression(sender, (Advancement) args[0], (Collection<Player>) args[1])),

                new CommandAPICommand("set").withPermission(PERMISSION_PROGRESSION_SET).executesPlayer((player, args) -> {
                    player.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi progression set <advancement> <progression> [player] [giveRewards]");
                }).executes((sender, args) -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /ultimateadvancementapi progression set <advancement> <progression> <player> [giveRewards]");
                }),
                new CommandAPICommand("set").withPermission(PERMISSION_PROGRESSION_SET).withArguments(getAdvancementArgument(main, "advancement"), new IntegerArgument("progression", 0)).executesPlayer((Player player, Object[] args) -> setProgression(player, (Advancement) args[0], (int) args[1], player, true)),
                new CommandAPICommand("set").withPermission(PERMISSION_PROGRESSION_SET).withArguments(getAdvancementArgument(main, "advancement"), new IntegerArgument("progression", 0), new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS)).executes((CommandSender sender, Object[] args) -> setProgression(sender, (Advancement) args[0], (int) args[1], (Collection<Player>) args[2], true)),
                new CommandAPICommand("set").withPermission(PERMISSION_PROGRESSION_SET).withArguments(getAdvancementArgument(main, "advancement"), new IntegerArgument("progression", 0), new EntitySelectorArgument<Collection<Player>>("player", EntitySelector.MANY_PLAYERS), new BooleanArgument("giveRewards")).executes((CommandSender sender, Object[] args) -> setProgression(sender, (Advancement) args[0], (int) args[1], (Collection<Player>) args[2], (boolean) args[3]))
        );

        mainCommand.withSubcommands(
                progression,
                grant,
                revoke
        );

        mainCommand.withHelp("Command to handle advancements.", "Command to grant/revoke/update player's advancements.");
        mainCommand.register();
    }

    private void grantAll(CommandSender sender, Player player, boolean giveRewards) throws WrapperCommandSyntaxException {
        grantAll(sender, List.of(player), giveRewards);
    }

    private void grantAll(CommandSender sender, Collection<Player> players, boolean giveRewards) throws WrapperCommandSyntaxException {
        validatePlayerArgument(players);
        for (AdvancementTab m : main.getTabs()) {
            if (m.isActive()) {
                for (Advancement a : m.getAdvancements()) {
                    for (Player p : players)
                        a.grant(p, giveRewards);
                }
            }
        }
        for (Player p : players)
            sender.sendMessage(ChatColor.GREEN + "All advancement has been unlocked to " + ChatColor.YELLOW + p.getName());
    }

    private void grantTab(CommandSender sender, AdvancementTab tab, Player player, boolean giveRewards) throws WrapperCommandSyntaxException {
        grantTab(sender, tab, List.of(player), giveRewards);
    }

    private void grantTab(CommandSender sender, AdvancementTab tab, Collection<Player> players, boolean giveRewards) throws WrapperCommandSyntaxException {
        validatePlayerArgument(players);
        if (!tab.isActive()) {
            throw CommandAPI.fail("Advancement tab is not active.");
        }
        for (Advancement a : tab.getAdvancements()) {
            for (Player p : players)
                a.grant(p, giveRewards);
        }
        for (Player p : players)
            sender.sendMessage(ChatColor.GREEN + "All advancement of tab " + ChatColor.YELLOW + tab + ChatColor.GREEN + " has been unlocked to " + ChatColor.YELLOW + p.getName());
    }

    private void grantOne(CommandSender sender, Advancement advancement, Player player, boolean giveRewards) throws WrapperCommandSyntaxException {
        grantOne(sender, advancement, List.of(player), giveRewards);
    }

    private void grantOne(CommandSender sender, Advancement advancement, Collection<Player> players, boolean giveRewards) throws WrapperCommandSyntaxException {
        validatePlayerArgument(players);
        for (Player p : players) {
            advancement.getAdvancementTab().showTab(p);
            advancement.grant(p, giveRewards);
            sender.sendMessage(ChatColor.GREEN + "Advancement " + ChatColor.YELLOW + advancement.getKey() + ChatColor.GREEN + " has been unlocked to " + ChatColor.YELLOW + p.getName());
        }
    }

    private void revokeAll(CommandSender sender, Player player, boolean hideTabs) throws WrapperCommandSyntaxException {
        revokeAll(sender, List.of(player), hideTabs);
    }

    private void revokeAll(CommandSender sender, Collection<Player> players, boolean hideTabs) throws WrapperCommandSyntaxException {
        validatePlayerArgument(players);
        for (AdvancementTab m : main.getTabs()) {
            var advancements = m.getAdvancements();
            for (Player p : players) {
                for (Advancement a : advancements) {
                    a.revoke(p);
                }
                if (hideTabs)
                    m.hideTab(p);
            }
        }
        for (Player p : players)
            sender.sendMessage(ChatColor.GREEN + "All advancement has been revoked to " + ChatColor.YELLOW + p.getName());
    }

    private void revokeTab(CommandSender sender, AdvancementTab tab, Player player, boolean hideTab) throws WrapperCommandSyntaxException {
        revokeTab(sender, tab, List.of(player), hideTab);
    }

    private void revokeTab(CommandSender sender, AdvancementTab tab, Collection<Player> players, boolean hideTab) throws WrapperCommandSyntaxException {
        validatePlayerArgument(players);
        if (!tab.isActive()) {
            throw CommandAPI.fail("Advancement tab is not active.");
        }
        var advancements = tab.getAdvancements();
        for (Player p : players) {
            for (Advancement a : advancements) {
                a.revoke(p);
            }
            if (hideTab)
                tab.hideTab(p);
            sender.sendMessage(ChatColor.GREEN + "All advancement of tab " + ChatColor.YELLOW + tab + ChatColor.GREEN + " has been revoked to " + ChatColor.YELLOW + p.getName());
        }
    }

    private void revokeOne(CommandSender sender, Advancement advancement, Player player) throws WrapperCommandSyntaxException {
        revokeOne(sender, advancement, List.of(player));
    }

    private void revokeOne(CommandSender sender, Advancement advancement, Collection<Player> players) throws WrapperCommandSyntaxException {
        validatePlayerArgument(players);
        for (Player p : players) {
            advancement.revoke(p);
            sender.sendMessage(ChatColor.GREEN + "Advancement " + ChatColor.YELLOW + advancement + ChatColor.GREEN + " has been revoked to " + ChatColor.YELLOW + p.getName());
        }
    }

    private int getProgression(CommandSender sender, Advancement advancement, Player player) throws WrapperCommandSyntaxException {
        return getProgression(sender, advancement, List.of(player));
    }

    private int getProgression(CommandSender sender, Advancement advancement, Collection<Player> players) throws WrapperCommandSyntaxException {
        validatePlayerArgument(players);
        int progression = 0;
        for (Player p : players) {
            progression = advancement.getProgression(p);
            sender.sendMessage(ChatColor.YELLOW + p.getName() + ChatColor.GREEN + " progression is " + ChatColor.YELLOW + progression + '/' + advancement.getMaxProgression());
        }
        return progression;
    }

    private void setProgression(CommandSender sender, Advancement advancement, int progression, Player player, boolean giveRewards) throws WrapperCommandSyntaxException {
        setProgression(sender, advancement, progression, List.of(player), giveRewards);
    }

    private void setProgression(CommandSender sender, Advancement advancement, int progression, Collection<Player> players, boolean giveRewards) throws WrapperCommandSyntaxException {
        validatePlayerArgument(players);
        for (Player p : players) {
            progression = Math.min(advancement.getMaxProgression(), progression);
            advancement.setProgression(p, progression, giveRewards);
            sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.YELLOW + p.getName() + ChatColor.GREEN + " progression to " + ChatColor.YELLOW + progression + '/' + advancement.getMaxProgression());
        }
    }

    private static void validatePlayerArgument(@NotNull Collection<Player> players) throws WrapperCommandSyntaxException {
        if (players.size() == 0) {
            throw CommandAPI.fail("No player has been provided.");
        }
    }
}
