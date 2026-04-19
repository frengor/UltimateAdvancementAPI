package com.fren_gor.ultimateAdvancementAPI.nms.v26_1_R2.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.v26_1_R2.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.item.ItemStackTemplate;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AdvancementDisplayWrapper_v26_1_R2 extends AdvancementDisplayWrapper {

    private final DisplayInfo display;
    private final AdvancementFrameTypeWrapper frameType;

    public AdvancementDisplayWrapper_v26_1_R2(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        ClientAsset.ResourceTexture clientAsset = Util.parseBackgroundTexture(backgroundTexture);
        this.display = new DisplayInfo(ItemStackTemplate.fromNonEmptyStack(CraftItemStack.asNMSCopy(icon)), Util.fromString(title), Util.fromString(description), Optional.ofNullable(clientAsset), (AdvancementType) frameType.toNMS(), showToast, announceChat, hidden);
        this.display.setLocation(x, y);
        this.frameType = frameType;
    }

    public AdvancementDisplayWrapper_v26_1_R2(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull BaseComponent description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        ClientAsset.ResourceTexture clientAsset = Util.parseBackgroundTexture(backgroundTexture);
        this.display = new DisplayInfo(ItemStackTemplate.fromNonEmptyStack(CraftItemStack.asNMSCopy(icon)), Util.fromComponent(title), Util.fromComponent(description), Optional.ofNullable(clientAsset), (AdvancementType) frameType.toNMS(), showToast, announceChat, hidden);
        this.display.setLocation(x, y);
        this.frameType = frameType;
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return CraftItemStack.asBukkitCopy(display.getIcon().create());
    }

    @Override
    @NotNull
    public String getTitle() {
        return CraftChatMessage.fromComponent(display.getTitle());
    }

    @Override
    @NotNull
    public String getDescription() {
        return CraftChatMessage.fromComponent(display.getDescription());
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
        Optional<ClientAsset.ResourceTexture> r = display.getBackground();
        return r.isEmpty() ? null : r.toString();
    }

    @Override
    @NotNull
    public DisplayInfo toNMS() {
        return display;
    }
}
