package com.fren_gor.ultimateAdvancementAPI.nms.v1_18_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.util.JsonString;
import com.fren_gor.ultimateAdvancementAPI.nms.v1_18_R1.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.google.gson.JsonParseException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancementDisplayWrapper_v1_18_R1 extends AdvancementDisplayWrapper {

    private final DisplayInfo display;
    private final AdvancementFrameTypeWrapper frameType;

    public AdvancementDisplayWrapper_v1_18_R1(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull BaseComponent description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        this(CraftItemStack.asNMSCopy(icon), Util.fromComponent(title), Util.fromComponent(description), frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }

    public AdvancementDisplayWrapper_v1_18_R1(@NotNull ItemStack icon, @NotNull JsonString jsonTitle, @NotNull JsonString jsonDescription, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) throws JsonParseException {
        this(CraftItemStack.asNMSCopy(icon), Util.fromJSON(jsonTitle.jsonString()), Util.fromJSON(jsonDescription.jsonString()), frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }

    protected AdvancementDisplayWrapper_v1_18_R1(@NotNull net.minecraft.world.item.ItemStack icon, @NotNull Component title, @NotNull Component description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        ResourceLocation background = backgroundTexture == null ? null : new ResourceLocation(backgroundTexture);
        this.display = new DisplayInfo(icon, title, description, background, (FrameType) frameType.toNMS(), showToast, announceChat, hidden);
        this.display.setLocation(x, y);
        this.frameType = frameType;
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return CraftItemStack.asBukkitCopy(display.getIcon());
    }

    @Override
    @NotNull
    public BaseComponent getTitle() {
        return Util.toComponent(display.getTitle());
    }

    @Override
    @NotNull
    public BaseComponent getDescription() {
        return Util.toComponent(display.getDescription());
    }

    @Override
    @NotNull
    public AdvancementFrameTypeWrapper getAdvancementFrameType() {
        return frameType;
    }

    @Override
    public float getX() {
        return display.getX();
    }

    @Override
    public float getY() {
        return display.getY();
    }

    @Override
    public boolean doesShowToast() {
        return display.shouldShowToast();
    }

    @Override
    public boolean doesAnnounceToChat() {
        return display.shouldAnnounceChat();
    }

    @Override
    public boolean isHidden() {
        return display.isHidden();
    }

    @Override
    @Nullable
    public String getBackgroundTexture() {
        ResourceLocation r = display.getBackground();
        return r == null ? null : r.toString();
    }

    @Override
    @NotNull
    public DisplayInfo toNMS() {
        return display;
    }
}
