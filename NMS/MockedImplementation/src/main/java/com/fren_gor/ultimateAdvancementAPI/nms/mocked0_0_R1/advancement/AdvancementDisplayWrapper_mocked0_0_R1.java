package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancementDisplayWrapper_mocked0_0_R1 extends AdvancementDisplayWrapper {

    private final AdvancementFrameTypeWrapper frameType;
    private final float x, y;
    private final String background;
    private final ItemStack icon;
    private final String title, description;
    private final boolean showToast, announceChat, hidden;

    public AdvancementDisplayWrapper_mocked0_0_R1(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        this.frameType = frameType;
        this.x = x;
        this.y = y;
        this.icon = icon.clone();
        this.title = title;
        this.description = description;
        this.showToast = showToast;
        this.announceChat = announceChat;
        this.hidden = hidden;
        this.background = backgroundTexture;
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return icon.clone();
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
    @Nullable
    public String getBackgroundTexture() {
        return background;
    }

    @Override
    @NotNull
    public AdvancementDisplay toNMS() {
        throw new UnsupportedOperationException();
    }
}
