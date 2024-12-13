package com.fren_gor.ultimateAdvancementAPI.nms.v1_15_R1.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.v1_15_R1.Util;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_15_R1.AdvancementDisplay;
import net.minecraft.server.v1_15_R1.AdvancementFrameType;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class AdvancementDisplayWrapper_v1_15_R1 extends AdvancementDisplayWrapper {

    private static Field iconField, xField, yField, keyField, showToastField;

    static {
        try {
            iconField = AdvancementDisplay.class.getDeclaredField("c");
            iconField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            xField = AdvancementDisplay.class.getDeclaredField("i");
            xField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            yField = AdvancementDisplay.class.getDeclaredField("j");
            yField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            keyField = AdvancementDisplay.class.getDeclaredField("d");
            keyField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        try {
            showToastField = AdvancementDisplay.class.getDeclaredField("f");
            showToastField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private final AdvancementDisplay display;
    private final AdvancementFrameTypeWrapper frameType;

    public AdvancementDisplayWrapper_v1_15_R1(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        MinecraftKey background = backgroundTexture == null ? null : new MinecraftKey(backgroundTexture);
        this.display = new AdvancementDisplay(CraftItemStack.asNMSCopy(icon), Util.fromString(title), Util.fromString(description), background, (AdvancementFrameType) frameType.toNMS(), showToast, announceChat, hidden);
        this.display.a(x, y);
        this.frameType = frameType;
    }

    public AdvancementDisplayWrapper_v1_15_R1(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull BaseComponent description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean showToast, boolean announceChat, boolean hidden, @Nullable String backgroundTexture) {
        MinecraftKey background = backgroundTexture == null ? null : new MinecraftKey(backgroundTexture);
        this.display = new AdvancementDisplay(CraftItemStack.asNMSCopy(icon), Util.fromComponent(title), Util.fromComponent(description), background, (AdvancementFrameType) frameType.toNMS(), showToast, announceChat, hidden);
        this.display.a(x, y);
        this.frameType = frameType;
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        try {
            var item = (net.minecraft.server.v1_15_R1.ItemStack) iconField.get(display);
            return CraftItemStack.asBukkitCopy(item);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @NotNull
    public String getTitle() {
        return CraftChatMessage.fromComponent(display.a());
    }

    @Override
    @NotNull
    public String getDescription() {
        return CraftChatMessage.fromComponent(display.b());
    }

    @Override
    @NotNull
    public AdvancementFrameTypeWrapper getAdvancementFrameType() {
        return frameType;
    }

    @Override
    public float getX() {
        try {
            return xField.getFloat(display);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getY() {
        try {
            return yField.getFloat(display);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean doesShowToast() {
        try {
            return showToastField.getBoolean(display);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean doesAnnounceToChat() {
        return display.i();
    }

    @Override
    public boolean isHidden() {
        return display.j();
    }

    @Override
    @Nullable
    public String getBackgroundTexture() {
        try {
            Object mckey = keyField.get(display); // Avoid cast
            return mckey == null ? null : mckey.toString();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @NotNull
    public AdvancementDisplay toNMS() {
        return display;
    }
}
