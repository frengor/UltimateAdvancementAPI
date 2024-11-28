package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.announceMessage.IAnnounceMessage;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A display which is immutable, that is every method call should always return the same value.
 * <p>An implementation of an immutable display is {@link AdvancementDisplay}.
 * <br>
 * <h2>Optimizing immutable displays</h2>
 * <p>It's suggested to create and store the values to be returned by the various methods once and simply return them afterward
 * (this technique is also known as <a href="https://en.wikipedia.org/wiki/Memoization">memoization</a>).
 * The most important method where this applies is {@link #getNMSWrapper()}, since reducing the creation of NMS wrappers
 * usually leads to better performance.
 */
public abstract class AbstractImmutableAdvancementDisplay extends AbstractAdvancementDisplay {

    @LazyValue
    private PreparedAdvancementDisplayWrapper wrapper;

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
     * Returns the icon of the advancement.
     *
     * @return The icon of the advancement.
     */
    @NotNull
    public abstract ItemStack getIcon();

    /**
     * Returns the title of the advancement as a legacy string.
     *
     * @return The title of the advancement as a legacy string.
     * @implSpec The default implementation returns the title returned by {@link #getTitle()} converted into a legacy string.
     */
    @NotNull
    public String getLegacyTitle() {
        return TextComponent.toLegacyText(getTitle());
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent getTitle();

    /**
     * Returns the default color of the title when displayed in the advancement GUI.
     *
     * @return The default color of the title when displayed in the advancement GUI
     *         or {@code null} to use Minecraft's default color.
     * @implSpec The default implementation returns {@code null}.
     */
    @Nullable
    public ChatColor getDefaultTitleColor() {
        return null;
    }

    /**
     * Returns the default color of the title in the advancement's announcement message.
     *
     * @return The default color of the title in the advancement's announcement message
     *         or {@code null} to use the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the frame returned by {@link #getFrame() getFrame}).
     * @implSpec The default implementation returns {@code null}.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Nullable
    public ChatColor getAnnouncementMessageDefaultTitleColor() {
        return null;
    }

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @return The description of the advancement as a list of legacy strings.
     * @implSpec The default implementation returns the description returned by {@link #getDescription()} converted into a list of legacy strings.
     */
    @NotNull
    @Unmodifiable
    public List<String> getLegacyDescription() {
        return getDescription().stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @NotNull
    @Unmodifiable
    public abstract List<BaseComponent> getDescription();

    /**
     * Returns the default color of the description when displayed in the advancement GUI.
     *
     * @return The default color of the description when displayed in the advancement GUI
     *         or {@code null} to use Minecraft's default color.
     * @implSpec The default implementation returns {@code null}.
     */
    @Nullable
    public ChatColor getDefaultDescriptionColor() {
        return null;
    }

    /**
     * Returns the default color of the description in the advancement's announcement message.
     *
     * @return The default color of the description in the advancement's announcement message
     *         or {@code null} to use the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the frame returned by {@link #getFrame() getFrame}).
     * @implSpec The default implementation returns {@code null}.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Nullable
    public ChatColor getAnnouncementMessageDefaultDescriptionColor() {
        return null;
    }

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
     * Returns the NMS wrapper of the display.
     *
     * @return The NMS wrapper of the display.
     */
    @NotNull
    public PreparedAdvancementDisplayWrapper getNMSWrapper() throws ReflectiveOperationException {
        if (wrapper != null) {
            return wrapper;
        }

        BaseComponent title = AdvancementUtils.applyDefaultColor(getTitle(), getDefaultTitleColor());
        BaseComponent description = AdvancementUtils.joinBaseComponents(new TextComponent("\n"), getDefaultDescriptionColor(), getDescription());
        return wrapper = PreparedAdvancementDisplayWrapper.craft(getIcon(), title, description, getFrame().getNMSWrapper(), getX(), getY());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesShowToast(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return doesShowToast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesShowToast(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return doesShowToast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesAnnounceToChat(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return doesAnnounceToChat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesAnnounceToChat(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return doesAnnounceToChat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ItemStack dispatchGetIcon(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ItemStack dispatchGetIcon(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String dispatchGetLegacyTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String dispatchGetLegacyTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BaseComponent dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BaseComponent dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultTitleColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultTitleColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultTitleColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultTitleColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unmodifiable
    public final List<String> dispatchGetLegacyDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unmodifiable
    public final List<String> dispatchGetLegacyDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unmodifiable
    public final List<BaseComponent> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unmodifiable
    public final List<BaseComponent> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultDescriptionColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultDescriptionColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultDescriptionColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultDescriptionColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AdvancementFrameType dispatchGetFrame(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getFrame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AdvancementFrameType dispatchGetFrame(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getFrame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetX(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getX();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetX(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getX();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetY(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetY(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) throws ReflectiveOperationException {
        return getNMSWrapper();
    }
}
