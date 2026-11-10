package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.chat.JsonString;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.google.gson.JsonParseException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AdvancementDisplayWrapper_mocked0_0_R1 extends AdvancementDisplayWrapper {

    private final AdvancementFrameTypeWrapper frameType;
    private final float x, y;
    private final String background;
    private final ItemStack icon;
    private final JsonString title, description;
    private final boolean showToast, announceChat, hidden;

    public AdvancementDisplayWrapper_mocked0_0_R1(@NotNull ItemStack icon, @NotNull JsonString jsonTitle, @NotNull JsonString jsonDescription, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) throws JsonParseException {
        this.frameType = Objects.requireNonNull(frameType);
        this.x = x;
        this.y = y;
        this.icon = Objects.requireNonNull(icon).clone();
        this.title = Objects.requireNonNull(jsonTitle);
        this.description = Objects.requireNonNull(jsonDescription);
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
    public JsonString getTitle() {
        return title;
    }

    @Override
    @NotNull
    public JsonString getDescription() {
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
    public Object toNMS() {
        return this;
    }
}
