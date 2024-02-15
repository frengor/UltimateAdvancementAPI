package com.fren_gor.ultimateAdvancementAPI.nms.v1_20_R2.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancementDisplayWrapper_v1_20_R2 extends AdvancementDisplayWrapper {

    private final DisplayInfo display;
    private final AdvancementFrameTypeWrapper frameType;

    public AdvancementDisplayWrapper_v1_20_R2(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        ResourceLocation background = backgroundTexture == null ? null : new ResourceLocation(backgroundTexture);
        this.display = new DisplayInfo(CraftItemStack.asNMSCopy(icon), Component.literal(title), Component.literal(description), background, (FrameType) frameType.toNMS(), showToast, announceChat, hidden);
        this.display.setLocation(x, y);
        this.frameType = frameType;
    }

    protected AdvancementDisplayWrapper_v1_20_R2(@NotNull net.minecraft.world.item.ItemStack icon, @NotNull Component title, @NotNull Component description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
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
    public String getTitle() {
        return ((LiteralContents) display.getTitle().getContents()).text();
    }

    @Override
    @NotNull
    public String getDescription() {
        return ((LiteralContents) display.getDescription().getContents()).text();
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
