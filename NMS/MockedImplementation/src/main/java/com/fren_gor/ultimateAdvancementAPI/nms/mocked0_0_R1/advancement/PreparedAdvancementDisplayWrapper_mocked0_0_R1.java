package com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.chat.JsonString;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import com.google.gson.JsonParseException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PreparedAdvancementDisplayWrapper_mocked0_0_R1 extends PreparedAdvancementDisplayWrapper {

    private final ItemStack icon;
    private final JsonString title, description;
    private final AdvancementFrameTypeWrapper frameType;
    private final float x, y;
    private final boolean showToast, announceChat, hidden;

    public PreparedAdvancementDisplayWrapper_mocked0_0_R1(@NotNull ItemStack icon, @NotNull JsonString jsonTitle, @NotNull JsonString jsonDescription, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden) {
        this.icon = Objects.requireNonNull(icon).clone();
        this.title = Objects.requireNonNull(jsonTitle);
        this.description = Objects.requireNonNull(jsonDescription);
        this.frameType = Objects.requireNonNull(frameType);
        this.x = x;
        this.y = y;
        this.showToast = showToast;
        this.announceChat = announceChat;
        this.hidden = hidden;
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
    @NotNull
    public PreparedAdvancementDisplayWrapper withIcon(@NotNull ItemStack icon) {
        return new PreparedAdvancementDisplayWrapper_mocked0_0_R1(Objects.requireNonNull(icon).clone(), title, description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withTitle(@NotNull JsonString jsonTitle) throws JsonParseException {
        return new PreparedAdvancementDisplayWrapper_mocked0_0_R1(icon, Objects.requireNonNull(jsonTitle), description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withDescription(@NotNull JsonString jsonDescription) throws JsonParseException {
        return new PreparedAdvancementDisplayWrapper_mocked0_0_R1(icon, title, Objects.requireNonNull(jsonDescription), frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withAdvancementFrameType(@NotNull AdvancementFrameTypeWrapper frameType) {
        return new PreparedAdvancementDisplayWrapper_mocked0_0_R1(icon, title, description, Objects.requireNonNull(frameType), x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withCoords(float x, float y) {
        return new PreparedAdvancementDisplayWrapper_mocked0_0_R1(icon, title, description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withShowToast(boolean showToast) {
        return new PreparedAdvancementDisplayWrapper_mocked0_0_R1(icon, title, description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withAnnounceToChat(boolean announceChat) {
        return new PreparedAdvancementDisplayWrapper_mocked0_0_R1(icon, title, description, frameType, x, y, showToast, announceChat, hidden);
    }

    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper withHidden(boolean hidden) {
        return new PreparedAdvancementDisplayWrapper_mocked0_0_R1(icon, title, description, frameType, x, y, showToast, announceChat, hidden);
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
