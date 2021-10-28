package com.fren_gor.ultimateAdvancementAPI.nms.v1_16_R1;

import com.google.common.collect.Maps;
import net.minecraft.server.v1_16_R1.AdvancementProgress;
import net.minecraft.server.v1_16_R1.AdvancementRewards;
import net.minecraft.server.v1_16_R1.Advancements;
import net.minecraft.server.v1_16_R1.Blocks;
import net.minecraft.server.v1_16_R1.ChatComponentText;
import net.minecraft.server.v1_16_R1.Criterion;
import net.minecraft.server.v1_16_R1.CriterionProgress;
import net.minecraft.server.v1_16_R1.CriterionTriggerImpossible;
import net.minecraft.server.v1_16_R1.MinecraftKey;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Util {

    private static final Field advancementRoots, advancementTasks;

    static {
        Field c = null, d = null;
        try {
            c = Advancements.class.getDeclaredField("c");
            c.setAccessible(true);
            d = Advancements.class.getDeclaredField("d");
            d.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        advancementRoots = c;
        advancementTasks = d;
    }

    public static final MinecraftKey IMPOSSIBLE = new MinecraftKey("minecraft", "impossible");
    public static final MinecraftKey NOTIFICATION_KEY = new MinecraftKey("com.fren_gor", "notification"), ROOT_KEY = new MinecraftKey("com.fren_gor", "root");
    public static final AdvancementRewards ADV_REWARDS = AdvancementRewards.a;
    private static final ChatComponentText ADV_DESCRIPTION = new ChatComponentText("\n§7A notification.");
    private static final Map<String, Criterion> ADV_CRITERIA_MAP = Collections.singletonMap("criterion", new Criterion(new CriterionTriggerImpossible.a()));
    private static final String[][] ADV_REQUIREMENTS = {{"criterion"}};
    private static final AdvancementProgress ADV_PROGRESS = new AdvancementProgress();
    private static final net.minecraft.server.v1_16_R1.Advancement ROOT;
    private static final Map<MinecraftKey, AdvancementProgress> PROGRESSIONS;

    static {
        ADV_PROGRESS.a(ADV_CRITERIA_MAP, ADV_REQUIREMENTS);
        CriterionProgress criterion = ADV_PROGRESS.getCriterionProgress("criterion");
        assert criterion != null;
        criterion.b();
        net.minecraft.server.v1_16_R1.AdvancementDisplay display = new net.minecraft.server.v1_16_R1.AdvancementDisplay(new net.minecraft.server.v1_16_R1.ItemStack(Blocks.GRASS_BLOCK.getItem()), new ChatComponentText("§f§lNotifications§1§2§3§4§5§6§7§8§9§0"), new ChatComponentText("§7Notification page.\n§7Close and reopen advancements to hide."), new MinecraftKey("textures/block/stone.png"), net.minecraft.server.v1_16_R1.AdvancementFrameType.TASK, false, false, false);
        ROOT = new net.minecraft.server.v1_16_R1.Advancement(ROOT_KEY, null, display, ADV_REWARDS, ADV_CRITERIA_MAP, ADV_REQUIREMENTS);

        PROGRESSIONS = new HashMap<>();
        PROGRESSIONS.put(ROOT_KEY, ADV_PROGRESS);
        PROGRESSIONS.put(NOTIFICATION_KEY, ADV_PROGRESS);
    }

    @NotNull
    public static Map<String, Criterion> getAdvancementCriteria(@Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        Validate.isTrue(maxCriteria >= 1, "Max criteria must be >= 1.");

        Map<String, Criterion> advCriteria = Maps.newHashMapWithExpectedSize(maxCriteria);
        for (int i = 0; i < maxCriteria; i++) {
            advCriteria.put(String.valueOf(i), new Criterion(new CriterionTriggerImpossible.a()));
        }

        return advCriteria;
    }

    @NotNull
    public static String[][] getAdvancementRequirements(@NotNull Map<String, Criterion> advCriteria) {
        Validate.notNull(advCriteria, "Advancement criteria map is null.");

        String[][] array = new String[advCriteria.size()][1];
        int index = 0;
        for (String name : advCriteria.keySet()) {
            array[index++][0] = name;
        }

        return array;
    }

    @NotNull
    public static net.minecraft.server.v1_16_R1.AdvancementProgress getAdvancementProgress(@NotNull net.minecraft.server.v1_16_R1.Advancement mcAdv, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        Validate.notNull(mcAdv, "NMS Advancement is null.");
        Validate.isTrue(criteria >= 0, "Criteria must be >= 0.");

        AdvancementProgress advPrg = new AdvancementProgress();
        advPrg.a(mcAdv.getCriteria(), mcAdv.i());

        for (int i = 0; i < criteria; i++) {
            CriterionProgress criteriaPrg = advPrg.getCriterionProgress(String.valueOf(i));
            if (criteriaPrg != null) {
                criteriaPrg.b();
            }
        }

        return advPrg;
    }

    private Util() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
