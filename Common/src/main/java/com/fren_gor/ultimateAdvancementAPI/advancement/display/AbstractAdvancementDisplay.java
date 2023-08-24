package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public abstract class AbstractAdvancementDisplay {

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public abstract boolean doesShowToast();

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat();

    /**
     * Gets a clone of the icon.
     *
     * @return A clone of the icon.
     */
    @NotNull
    public abstract ItemStack getIcon();

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public String getTitle() {
        return TextComponent.toLegacyText(getTitleBaseComponent());
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent[] getTitleBaseComponent();

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public List<String> getDescription() {
        return getDescriptionBaseComponent().stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public abstract List<BaseComponent[]> getDescriptionBaseComponent();

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public abstract AdvancementFrameType getFrame();

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public abstract float getX();

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public abstract float getY();

    /**
     * Gets the chat message to be sent when an advancement is completed.
     * <p>The message is sent to everybody online on the server.
     *
     * @param advancementCompleter The player who has completed the advancement.
     * @param advancement The advancement being completed.
     * @return The message to be displayed.
     */
    @NotNull
    public BaseComponent[] getAnnounceMessage(@NotNull Player advancementCompleter, @NotNull Advancement advancement) {
        //TODO Add title in hover event
        Preconditions.checkNotNull(advancementCompleter, "Player is null.");
        ChatColor color = getFrame().getColor();

        String title = getTitle();
        String description = String.join("\n", getDescription());

        return new ComponentBuilder(advancementCompleter.getName() + ' ' + getFrame().getChatText() + ' ')
                .color(ChatColor.WHITE)
                .append(new ComponentBuilder("[")
                                .color(color)
                                .event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("")
                                        .append(title, FormatRetention.NONE)
                                        .append("\n")
                                        .append(description, FormatRetention.NONE)
                                        .create()))
                                .create()
                        , FormatRetention.NONE)
                .append(title, FormatRetention.EVENTS)
                .append(new ComponentBuilder("]")
                                .color(color)
                                .create()
                        , FormatRetention.EVENTS)
                .create();
    }

    /**
     * Returns the {@code AdvancementDisplay} NMS wrapper, using the provided advancement for construction (when necessary).
     *
     * @param advancement The advancement used, when necessary, to create the NMS wrapper. Must be not {@code null}.
     * @return The {@code AdvancementDisplay} NMS wrapper.
     */
    @NotNull
    public abstract AdvancementDisplayWrapper getNMSWrapper(@NotNull Advancement advancement);

    public static boolean dispatchDoesToast(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchDoesToast(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public static boolean dispatchDoesToast(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return perPlayer.doesShowToast(player);
        }
        if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            try {
                return perTeam.doesShowToast(databaseManager.getTeamProgression(player));
            } catch (UserNotLoadedException ignored) {
            }
        }
        return display.doesShowToast();
    }

    public static boolean dispatchDoesAnnounceToChat(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchDoesAnnounceToChat(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public static boolean dispatchDoesAnnounceToChat(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return perPlayer.doesAnnounceToChat(player);
        }
        if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            try {
                return perTeam.doesAnnounceToChat(databaseManager.getTeamProgression(player));
            } catch (UserNotLoadedException ignored) {
            }
        }
        return display.doesAnnounceToChat();
    }

    public static ItemStack dispatchIcon(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchIcon(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public static ItemStack dispatchIcon(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return perPlayer.getIcon(player);
        }
        if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            try {
                return perTeam.getIcon(databaseManager.getTeamProgression(player));
            } catch (UserNotLoadedException ignored) {
            }
        }
        return display.getIcon();
    }

    public static String dispatchTitle(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchTitle(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public static String dispatchTitle(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return perPlayer.getTitle(player);
        }
        if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            try {
                return perTeam.getTitle(databaseManager.getTeamProgression(player));
            } catch (UserNotLoadedException ignored) {
            }
        }
        return display.getTitle();
    }

    public static BaseComponent[] dispatchTitleBaseComponent(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchTitleBaseComponent(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public static BaseComponent[] dispatchTitleBaseComponent(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return perPlayer.getTitleBaseComponent(player);
        }
        if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            try {
                return perTeam.getTitleBaseComponent(databaseManager.getTeamProgression(player));
            } catch (UserNotLoadedException ignored) {
            }
        }
        return display.getTitleBaseComponent();
    }

    public static List<String> dispatchDescription(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchDescription(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public static List<String> dispatchDescription(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return perPlayer.getDescription(player);
        }
        if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            try {
                return perTeam.getDescription(databaseManager.getTeamProgression(player));
            } catch (UserNotLoadedException ignored) {
            }
        }
        return display.getDescription();
    }

    public static List<BaseComponent[]> dispatchDescriptionBaseComponent(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchDescriptionBaseComponent(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public static List<BaseComponent[]> dispatchDescriptionBaseComponent(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return perPlayer.getDescriptionBaseComponent(player);
        }
        if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            try {
                return perTeam.getDescriptionBaseComponent(databaseManager.getTeamProgression(player));
            } catch (UserNotLoadedException ignored) {
            }
        }
        return display.getDescriptionBaseComponent();
    }

    public static AdvancementFrameType dispatchFrame(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchFrame(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public static AdvancementFrameType dispatchFrame(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return perPlayer.getFrame(player);
        }
        if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            try {
                return perTeam.getFrame(databaseManager.getTeamProgression(player));
            } catch (UserNotLoadedException ignored) {
            }
        }
        return display.getFrame();
    }

    public static float dispatchX(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchX(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public static float dispatchX(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return perPlayer.getX(player);
        }
        if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            try {
                return perTeam.getX(databaseManager.getTeamProgression(player));
            } catch (UserNotLoadedException ignored) {
            }
        }
        return display.getX();
    }

    public static float dispatchY(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchY(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public static float dispatchY(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return perPlayer.getY(player);
        }
        if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            try {
                return perTeam.getY(databaseManager.getTeamProgression(player));
            } catch (UserNotLoadedException ignored) {
            }
        }
        return display.getY();
    }

}
