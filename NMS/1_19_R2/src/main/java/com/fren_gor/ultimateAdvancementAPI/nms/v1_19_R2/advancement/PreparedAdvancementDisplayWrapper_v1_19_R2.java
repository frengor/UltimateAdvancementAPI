package com.fren_gor.ultimateAdvancementAPI.nms.v1_19_R2.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.JsonString;
import com.fren_gor.ultimateAdvancementAPI.nms.v1_19_R2.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import com.google.gson.JsonParseException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PreparedAdvancementDisplayWrapper_v1_19_R2 extends PreparedAdvancementDisplayWrapper {

    private final net.minecraft.world.item.ItemStack icon;
    private final Component title, description;
    private final AdvancementFrameTypeWrapper frameType;
    private final float x, y;
    private final boolean showToast, announceChat, hidden;

    public PreparedAdvancementDisplayWrapper_v1_19_R2(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull BaseComponent description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) {
        this.icon = CraftItemStack.asNMSCopy(icon);
        this.title = Util.fromComponent(title);
        this.description = Util.fromComponent(description);
        this.frameType = frameType;
        this.x = x;
        this.y = y;
        this.showToast = showToast;
        this.announceChat = announceChat;
        this.hidden = hidden;
    }

    public PreparedAdvancementDisplayWrapper_v1_19_R2(@NotNull ItemStack icon, @NotNull JsonString jsonTitle, @NotNull JsonString jsonDescription, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) throws JsonParseException {
        this.icon = CraftItemStack.asNMSCopy(icon);
        this.title = Util.fromJSON(jsonTitle.jsonString());
        this.description = Util.fromJSON(jsonDescription.jsonString());
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
    public BaseComponent getTitle() {
        return Util.toComponent(title);
    }

    @Override
    @NotNull
    public BaseComponent getDescription() {
        return Util.toComponent(description);
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
    public AdvancementDisplayWrapper toBaseAdvancementDisplay() {
        return new AdvancementDisplayWrapper_v1_19_R2(icon, title, description, frameType, x, y, showToast, announceChat, hidden, null);
    }

    @Override
    @NotNull
    public AdvancementDisplayWrapper toRootAdvancementDisplay(@NotNull String backgroundTexture) {
        Objects.requireNonNull(backgroundTexture, "The background texture is null.");
        return new AdvancementDisplayWrapper_v1_19_R2(icon, title, description, frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }
}
