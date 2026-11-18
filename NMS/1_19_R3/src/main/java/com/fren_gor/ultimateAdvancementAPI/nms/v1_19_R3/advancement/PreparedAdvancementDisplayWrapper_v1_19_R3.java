package com.fren_gor.ultimateAdvancementAPI.nms.v1_19_R3.advancement;

import com.fren_gor.ultimateAdvancementAPI.chat.JsonString;
import com.fren_gor.ultimateAdvancementAPI.nms.v1_19_R3.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import com.google.common.base.Preconditions;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PreparedAdvancementDisplayWrapper_v1_19_R3 extends PreparedAdvancementDisplayWrapper {

    private final net.minecraft.world.item.ItemStack icon;
    private final Component title, description;
    private final AdvancementFrameTypeWrapper frameType;
    private final float x, y;
    private final boolean showToast, announceChat, hidden;

    public PreparedAdvancementDisplayWrapper_v1_19_R3(@NotNull ItemStack icon, @NotNull JsonString jsonTitle, @NotNull JsonString jsonDescription, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) throws JsonParseException {
        this.icon = CraftItemStack.asNMSCopy(icon);
        this.title = Util.fromJSON(jsonTitle);
        this.description = Util.fromJSON(jsonDescription);
        this.frameType = frameType;
        this.x = x;
        this.y = y;
        this.showToast = showToast;
        this.announceChat = announceChat;
        this.hidden = hidden;
    }

    protected PreparedAdvancementDisplayWrapper_v1_19_R3(@NotNull net.minecraft.world.item.ItemStack icon, @NotNull Component title, @NotNull Component description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.frameType = frameType;
        this.x = x;
        this.y = y;
        this.showToast = showToast;
        this.announceChat = announceChat;
        this.hidden = hidden;
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return CraftItemStack.asBukkitCopy(icon);
    }

    @Override
    @NotNull
    public JsonString getTitle() {
        return Util.toJSON(title);
    }

    @Override
    @NotNull
    public JsonString getDescription() {
        return Util.toJSON(description);
    }

    @Override
    @NotNull
    public AdvancementFrameTypeWrapper getAdvancementFrameType() {
        return frameType;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public boolean doesShowToast() {
        return showToast;
    }

    @Override
    public boolean doesAnnounceToChat() {
        return announceChat;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withIcon(@NotNull ItemStack icon) {
        Preconditions.checkNotNull(icon, "ItemStack is null.");
        return new PreparedAdvancementDisplayWrapper_v1_19_R3(CraftItemStack.asNMSCopy(icon), title, description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withTitle(@NotNull JsonString jsonTitle) throws JsonParseException {
        Preconditions.checkNotNull(jsonTitle, "Title is null.");
        return new PreparedAdvancementDisplayWrapper_v1_19_R3(icon, Util.fromJSON(jsonTitle), description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withDescription(@NotNull JsonString jsonDescription) throws JsonParseException {
        Preconditions.checkNotNull(jsonDescription, "Description is null.");
        return new PreparedAdvancementDisplayWrapper_v1_19_R3(icon, title, Util.fromJSON(jsonDescription), frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withAdvancementFrameType(@NotNull AdvancementFrameTypeWrapper frameType) {
        Preconditions.checkNotNull(frameType, "Frame type is null.");
        return new PreparedAdvancementDisplayWrapper_v1_19_R3(icon, title, description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withCoords(float x, float y) {
        return new PreparedAdvancementDisplayWrapper_v1_19_R3(icon, title, description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withShowToast(boolean showToast) {
        return new PreparedAdvancementDisplayWrapper_v1_19_R3(icon, title, description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withAnnounceToChat(boolean announceChat) {
        return new PreparedAdvancementDisplayWrapper_v1_19_R3(icon, title, description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withHidden(boolean hidden) {
        return new PreparedAdvancementDisplayWrapper_v1_19_R3(icon, title, description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public AdvancementDisplayWrapper toBaseAdvancementDisplay() {
        return new AdvancementDisplayWrapper_v1_19_R3(icon, title, description, frameType, x, y, showToast, announceChat, hidden, null);
    }

    @Override
    @NotNull
    public AdvancementDisplayWrapper toRootAdvancementDisplay(@NotNull String backgroundTexture) {
        Objects.requireNonNull(backgroundTexture, "The background texture is null.");
        return new AdvancementDisplayWrapper_v1_19_R3(icon, title, description, frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }
}
