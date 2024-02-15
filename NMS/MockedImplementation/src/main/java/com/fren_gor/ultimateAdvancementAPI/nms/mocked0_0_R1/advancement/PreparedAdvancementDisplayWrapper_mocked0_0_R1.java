package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PreparedAdvancementDisplayWrapper_mocked0_0_R1 extends PreparedAdvancementDisplayWrapper {

    private final ItemStack icon;
    private final String title, description;
    private final AdvancementFrameTypeWrapper frameType;
    private final float x, y;
    private final boolean showToast, announceChat, hidden;

    public PreparedAdvancementDisplayWrapper_mocked0_0_R1(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) {
        this.icon = icon.clone();
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
        return icon;
    }

    @Override
    @NotNull
    public String getTitle() {
        return title;
    }

    @Override
    @NotNull
    public String getDescription() {
        return description;
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
        return new AdvancementDisplayWrapper_mocked0_0_R1(icon, title, description, frameType, x, y, showToast, announceChat, hidden, null);
    }

    @Override
    @NotNull
    public AdvancementDisplayWrapper toRootAdvancementDisplay(@NotNull String backgroundTexture) {
        Objects.requireNonNull(backgroundTexture, "The background texture is null.");
        return new AdvancementDisplayWrapper_mocked0_0_R1(icon, title, description, frameType, x, y, showToast, announceChat, hidden, backgroundTexture);
    }
}
