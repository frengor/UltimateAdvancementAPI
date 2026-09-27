package com.fren_gor.ultimateAdvancementAPI.nms.v26_3_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.JsonString;
import com.fren_gor.ultimateAdvancementAPI.nms.v26_3_R1.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.google.gson.JsonParseException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AdvancementDisplayWrapper_v26_3_R1 extends AdvancementDisplayWrapper {

    private final DisplayInfo display;
    private final float x, y;
    private final AdvancementFrameTypeWrapper frameType;

    public AdvancementDisplayWrapper_v26_3_R1(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull BaseComponent description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        this(ItemStackTemplate.fromNonEmptyStack(CraftItemStack.asNMSCopy(icon)), Util.fromComponent(title), Util.fromComponent(description), frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }

    public AdvancementDisplayWrapper_v26_3_R1(@NotNull ItemStack icon, @NotNull JsonString jsonTitle, @NotNull JsonString jsonDescription, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) throws JsonParseException {
        this(ItemStackTemplate.fromNonEmptyStack(CraftItemStack.asNMSCopy(icon)), Util.fromJSON(jsonTitle.jsonString()), Util.fromJSON(jsonDescription.jsonString()), frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }

    protected AdvancementDisplayWrapper_v26_3_R1(@NotNull ItemStackTemplate icon, @NotNull Component title, @NotNull Component description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        ClientAsset.ResourceTexture clientAsset = Util.parseBackgroundTexture(backgroundTexture);
        this.display = new DisplayInfo(icon, title, description, Optional.ofNullable(clientAsset), (AdvancementType) frameType.toNMS(), showToast, announceChat, hidden);
        this.x = x;
        this.y = y;
        this.frameType = frameType;
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return Util.asCraftMirror(display.icon().create());
    }

    @Override
    @NotNull
    public BaseComponent getTitle() {
        return Util.toComponent(display.title());
    }

    @Override
    @NotNull
    public BaseComponent getDescription() {
        return Util.toComponent(display.description());
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
        return display.showToast();
    }

    @Override
    public boolean doesAnnounceToChat() {
        return display.announceToChat();
    }

    @Override
    public boolean isHidden() {
        return display.hidden();
    }

    @Override
    @Nullable
    public String getBackgroundTexture() {
        Optional<ClientAsset.ResourceTexture> r = display.background();
        return r.isEmpty() ? null : r.toString();
    }

    @Override
    @NotNull
    public DisplayInfo toNMS() {
        return display;
    }
}
