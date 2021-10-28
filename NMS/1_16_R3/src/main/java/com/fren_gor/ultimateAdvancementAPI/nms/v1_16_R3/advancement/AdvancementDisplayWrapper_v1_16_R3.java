package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R3.advancement;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import net.minecraft.server.v1_16_R3.AdvancementDisplay;
import net.minecraft.server.v1_16_R3.AdvancementFrameType;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class AdvancementDisplayWrapper_v1_16_R3 extends AdvancementDisplayWrapper {

    private static Field iconField, xField, yField, keyField;

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
    }

    private final AdvancementDisplay display;
    private final AdvancementFrameTypeWrapper frameType;

    public AdvancementDisplayWrapper_v1_16_R3(@NotNull ItemStack icon, @NotNull String title, @NotNull String description, @NotNull AdvancementFrameTypeWrapper frameType, float x, float y, boolean hidden, @Nullable String backgroundTexture) {
        MinecraftKey background = backgroundTexture == null ? null : new MinecraftKey(backgroundTexture);
        this.display = new AdvancementDisplay(CraftItemStack.asNMSCopy(icon), new ChatComponentText(title), new ChatComponentText(description), background, (AdvancementFrameType) frameType.getNMSFrameType(), false, false, hidden);
        this.display.a(x, y);
        this.frameType = frameType;
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        try {
            var item = (net.minecraft.server.v1_16_R3.ItemStack) iconField.get(display);
            return CraftItemStack.asBukkitCopy(item);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @NotNull
    public String getTitle() {
        return ((ChatComponentText) display.a()).h();
    }

    @Override
    @NotNull
    public String getDescription() {
        return ((ChatComponentText) display.b()).h();
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
    public AdvancementDisplay getNMSDisplay() {
        return display;
    }
}
