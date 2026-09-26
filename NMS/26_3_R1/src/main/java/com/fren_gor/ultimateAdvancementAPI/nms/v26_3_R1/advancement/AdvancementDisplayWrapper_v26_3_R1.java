package com.fren_gor.ultimateAdvancementAPI.nms.v26_3_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.v26_3_R1.Util;
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

public class AdvancementDisplayWrapper_v26_3_R1 extends AdvancementDisplayWrapper {

    private final DisplayInfo display;
    private final float x, y;
    private final AdvancementFrameTypeWrapper frameType;

    public AdvancementDisplayWrapper_v26_3_R1(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        ClientAsset.ResourceTexture clientAsset = Util.parseBackgroundTexture(backgroundTexture);
        this.display = new DisplayInfo(ItemStackTemplate.fromNonEmptyStack(CraftItemStack.asNMSCopy(icon)), Util.fromString(title), Util.fromString(description), Optional.ofNullable(clientAsset), (AdvancementType) frameType.toNMS(), showToast, announceChat, hidden);
        this.x = x;
        this.y = y;
        this.frameType = frameType;
    }

    public AdvancementDisplayWrapper_v26_3_R1(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull BaseComponent description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        ClientAsset.ResourceTexture clientAsset = Util.parseBackgroundTexture(backgroundTexture);
        this.display = new DisplayInfo(ItemStackTemplate.fromNonEmptyStack(CraftItemStack.asNMSCopy(icon)), Util.fromComponent(title), Util.fromComponent(description), Optional.ofNullable(clientAsset), (AdvancementType) frameType.toNMS(), showToast, announceChat, hidden);
        this.x = x;
        this.y = y;
        this.frameType = frameType;
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return CraftItemStack.asCraftMirror(display.icon().create());
    }

    @Override
    @NotNull
    public String getTitle() {
        return CraftChatMessage.fromComponent(display.title());
    }

    @Override
    @NotNull
    public String getDescription() {
        return CraftChatMessage.fromComponent(display.description());
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
