package com.fren_gor.ultimateAdvancementAPI.nms.v1_20_R3.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PreparedAdvancementDisplayWrapper_v1_20_R3 extends PreparedAdvancementDisplayWrapper {

    private final net.minecraft.world.item.ItemStack icon;
    private final Component title, description;
    private final AdvancementFrameTypeWrapper frameType;
    private final float x, y;
    private final boolean showToast, announceChat, hidden;

    public PreparedAdvancementDisplayWrapper_v1_20_R3(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) {
        this.icon = CraftItemStack.asNMSCopy(icon);
        this.title = Component.literal(title);
        this.description = Component.literal(description);
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
    public String getTitle() {
        return ((LiteralContents) title.getContents()).text();
    }

    @Override
    @NotNull
    public String getDescription() {
        return ((LiteralContents) description.getContents()).text();
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
        return new AdvancementDisplayWrapper_v1_20_R3(icon, title, description, frameType, x, y, showToast, announceChat, hidden, null);
    }

    @Override
    @NotNull
    public AdvancementDisplayWrapper toRootAdvancementDisplay(@NotNull String backgroundTexture) {
        Objects.requireNonNull(backgroundTexture, "The background texture is null.");
        return new AdvancementDisplayWrapper_v1_20_R3(icon, title, description, frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }
}
