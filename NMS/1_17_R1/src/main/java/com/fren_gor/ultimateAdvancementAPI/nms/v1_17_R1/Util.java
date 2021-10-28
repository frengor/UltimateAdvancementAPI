package com.fren_gor.ultimateAdvancementAPI.nms.v1_17_R1;

import com.google.common.collect.Maps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
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
            c = AdvancementList.class.getDeclaredField("c");
            c.setAccessible(true);
            d = AdvancementList.class.getDeclaredField("d");
            d.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        advancementRoots = c;
        advancementTasks = d;
    }

    public static final ResourceLocation IMPOSSIBLE = new ResourceLocation("minecraft", "impossible");
    public static final ResourceLocation NOTIFICATION_KEY = new ResourceLocation("com.fren_gor", "notification"), ROOT_KEY = new ResourceLocation("com.fren_gor", "root");
    public static final AdvancementRewards ADV_REWARDS = new AdvancementRewards(0, new ResourceLocation[0], new ResourceLocation[0], null);
    private static final TextComponent ADV_DESCRIPTION = new TextComponent("\n§7A notification.");
    private static final Map<String, Criterion> ADV_CRITERIA_MAP = Collections.singletonMap("criterion", new Criterion(new ImpossibleTrigger.TriggerInstance()));
    private static final String[][] ADV_REQUIREMENTS = {{"criterion"}};
    private static final AdvancementProgress ADV_PROGRESS = new AdvancementProgress();
    private static final Advancement ROOT;
    private static final Map<ResourceLocation, AdvancementProgress> PROGRESSIONS;

    static {
        ADV_PROGRESS.update(ADV_CRITERIA_MAP, ADV_REQUIREMENTS);
        CriterionProgress criterion = ADV_PROGRESS.getCriterion("criterion");
        assert criterion != null;
        criterion.grant();
        DisplayInfo display = new DisplayInfo(new ItemStack(Blocks.GRASS_BLOCK.asItem()), new TextComponent("§f§lNotifications§1§2§3§4§5§6§7§8§9§0"), new TextComponent("§7Notification page.\n§7Close and reopen advancements to hide."), new ResourceLocation("textures/block/stone.png"), FrameType.TASK, false, false, false);
        ROOT = new Advancement(ROOT_KEY, null, display, ADV_REWARDS, ADV_CRITERIA_MAP, ADV_REQUIREMENTS);

        PROGRESSIONS = new HashMap<>();
        PROGRESSIONS.put(ROOT_KEY, ADV_PROGRESS);
        PROGRESSIONS.put(NOTIFICATION_KEY, ADV_PROGRESS);
    }

    @NotNull
    public static Map<String, Criterion> getAdvancementCriteria(@Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        Validate.isTrue(maxCriteria >= 1, "Max criteria must be >= 1.");

        Map<String, Criterion> advCriteria = Maps.newHashMapWithExpectedSize(maxCriteria);
        for (int i = 0; i < maxCriteria; i++) {
            advCriteria.put(String.valueOf(i), new Criterion(new ImpossibleTrigger.TriggerInstance()));
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
    public static AdvancementProgress getAdvancementProgress(@NotNull Advancement mcAdv, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        Validate.notNull(mcAdv, "NMS Advancement is null.");
        Validate.isTrue(criteria >= 0, "Criteria must be >= 0.");

        AdvancementProgress advPrg = new AdvancementProgress();
        advPrg.update(mcAdv.getCriteria(), mcAdv.getRequirements());

        for (int i = 0; i < criteria; i++) {
            CriterionProgress criteriaPrg = advPrg.getCriterion(String.valueOf(i));
            if (criteriaPrg != null) {
                criteriaPrg.grant();
            }
        }

        return advPrg;
    }

    private Util() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
