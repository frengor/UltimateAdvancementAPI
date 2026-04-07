package com.fren_gor.ultimateAdvancementAPI.advancement.tasks;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementGrantEvent;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.tests.AutoInject;
import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.ServerMock;

import static com.fren_gor.ultimateAdvancementAPI.tests.Utils.waitCompletion;

@ExtendWith(UAAPIExtension.class)
public class MultiTasksAdvancementTest {

    @AutoInject
    private ServerMock server;
    @AutoInject
    private AdvancementMain advancementMain;
    @AutoInject
    private DatabaseManager dbManager;

    // Initialized by init()
    private MultiTasksAdvancement multiTask;
    private TaskAdvancement task1, task2, task3;
    private Player player;
    private TeamProgression progression;

    @AfterEach
    void tearDown() {
        multiTask = null;
        task1 = null;
        task2 = null;
        task3 = null;
        player = null;
        progression = null;
    }

    @Test
    void taskSetProgression() {
        init();

        waitCompletion(task1.setProgression(player, 1, true));
        assertProgressionEventFired(task1, progression, 0, 1);
        assertProgressionEventFired(multiTask, progression, 0, 1);
        server.getPluginManager().assertEventNotFired(AdvancementGrantEvent.class);
        server.getPluginManager().clearEvents();

        waitCompletion(task2.incrementProgression(player, 2, true));
        assertProgressionEventFired(task2, progression, 0, 2);
        assertProgressionEventFired(multiTask, progression, 1, 3);
        server.getPluginManager().assertEventNotFired(AdvancementGrantEvent.class);
        server.getPluginManager().clearEvents();

        waitCompletion(task2.incrementProgression(player, 1, false));
        assertProgressionEventFired(task2, progression, 2, 3);
        assertProgressionEventFired(multiTask, progression, 3, 4);
        assertGrantEventFired(task2, progression, player, false);
        server.getPluginManager().clearEvents();

        waitCompletion(task3.setProgression(player, 5, true));
        assertProgressionEventFired(task3, progression, 0, 5);
        assertProgressionEventFired(multiTask, progression, 4, 9);
        assertGrantEventFired(task3, progression, player, true);
        server.getPluginManager().clearEvents();

        waitCompletion(task1.setProgression(player, 2, true));
        assertProgressionEventFired(task1, progression, 1, 2);
        assertProgressionEventFired(multiTask, progression, 9, 10);
        assertGrantEventFired(task1, progression, player, true);
        assertGrantEventFired(multiTask, progression, player, true);
        server.getPluginManager().clearEvents();

        waitCompletion(task1.setProgression(player, 0, true));
        assertProgressionEventFired(task1, progression, 2, 0);
        assertProgressionEventFired(multiTask, progression, 10, 8);
        server.getPluginManager().assertEventNotFired(AdvancementGrantEvent.class);
        server.getPluginManager().clearEvents();

        waitCompletion(task1.setProgression(player, 2, false));
        assertProgressionEventFired(task1, progression, 0, 2);
        assertProgressionEventFired(multiTask, progression, 8, 10);
        assertGrantEventFired(task1, progression, player, false);
        assertGrantEventFired(multiTask, progression, player, false);
        server.getPluginManager().clearEvents();

        advancementMain.unregisterAdvancementTabs(advancementMain.getOwningPlugin());
    }

    @Test
    void taskIncrementMoreThanMaxProgression() {
        init();

        waitCompletion(task1.incrementProgression(player, 10, true));
        assertProgressionEventFired(task1, progression, 0, 2);
        assertProgressionEventFired(multiTask, progression, 0, 2);
        assertGrantEventFired(task1, progression, player, true);
        server.getPluginManager().clearEvents();

        advancementMain.unregisterAdvancementTabs(advancementMain.getOwningPlugin());
    }

    @Test
    void multiTaskSetProgression() {
        init();

        waitCompletion(multiTask.setProgression(player, multiTask.getMaxProgression(), true));
        assertProgressionEventFired(task1, progression, 0, task1.getMaxProgression());
        assertProgressionEventFired(task2, progression, 0, task2.getMaxProgression());
        assertProgressionEventFired(task3, progression, 0, task3.getMaxProgression());
        assertProgressionEventFired(multiTask, progression, 0, null);
        assertProgressionEventFired(multiTask, progression, null, multiTask.getMaxProgression());
        assertGrantEventFired(task1, progression, player, true);
        assertGrantEventFired(task2, progression, player, true);
        assertGrantEventFired(task3, progression, player, true);
        assertGrantEventFired(multiTask, progression, player, true);
        server.getPluginManager().clearEvents();

        waitCompletion(multiTask.setProgression(player, 0, true));
        assertProgressionEventFired(task1, progression, task1.getMaxProgression(), 0);
        assertProgressionEventFired(task2, progression, task2.getMaxProgression(), 0);
        assertProgressionEventFired(task3, progression, task3.getMaxProgression(), 0);
        assertProgressionEventFired(multiTask, progression, multiTask.getMaxProgression(), null);
        assertProgressionEventFired(multiTask, progression, null, 0);
        server.getPluginManager().assertEventNotFired(AdvancementGrantEvent.class);
        server.getPluginManager().clearEvents();

        waitCompletion(task1.setProgression(player, 1));
        waitCompletion(task2.setProgression(player, 1));
        waitCompletion(task3.setProgression(player, 1));
        assertProgressionEventFired(multiTask, progression, null, 3);
        server.getPluginManager().assertEventNotFired(AdvancementGrantEvent.class);
        server.getPluginManager().clearEvents();

        waitCompletion(multiTask.incrementProgression(player, 0, true));
        assertProgressionEventFired(multiTask, progression, 3, 3);
        server.getPluginManager().assertEventNotFired(AdvancementGrantEvent.class);
        server.getPluginManager().clearEvents();

        advancementMain.unregisterAdvancementTabs(advancementMain.getOwningPlugin());
    }

    private void init() {
        var tab = advancementMain.createAdvancementTab(advancementMain.getOwningPlugin(), "test-tab", "background/texture");
        var root = new RootAdvancement(tab, "root", new AdvancementDisplayBuilder(Material.GRASS_BLOCK, "root").build());

        multiTask = new MultiTasksAdvancement(root, "multi-task", 10, new AdvancementDisplayBuilder(Material.GRASS_BLOCK, "root").build());
        task1 = new TaskAdvancement(multiTask, "task1", 2);
        task2 = new TaskAdvancement(multiTask, "task2", 3);
        task3 = new TaskAdvancement(multiTask, "task3", 5);
        multiTask.registerTasks(task1, task2, task3);

        tab.registerAdvancements(root, multiTask);

        this.player = server.addPlayer();

        // TODO Replace with a proper loadPlayer() call like in DatabaseManagerTest
        long startTime = System.currentTimeMillis();
        do {
            if (System.currentTimeMillis() - startTime >= 5000) {
                throw new RuntimeException("Timeout loading player");
            }
            server.getScheduler().performTicks(5);
            try {
                this.progression = dbManager.getTeamProgression(this.player);
            } catch (UserNotLoadedException e) {
                this.progression = null;
            }
        } while (this.progression == null);
    }

    private void assertProgressionEventFired(Advancement adv, TeamProgression progression, Integer oldProgr, Integer newProgr) {
        server.getPluginManager().assertEventFired(AdvancementProgressionUpdateEvent.class, e -> {
            return e.getAdvancement().equals(adv) &&
                    progression.equals(e.getTeamProgression()) &&
                    (oldProgr == null || e.getOldProgression() == oldProgr) &&
                    (newProgr == null || e.getNewProgression() == newProgr);
        });
    }

    private void assertGrantEventFired(Advancement adv, TeamProgression progression, Player player, boolean giveRewards) {
        server.getPluginManager().assertEventFired(AdvancementGrantEvent.class, e -> {
            return e.getAdvancement().equals(adv) &&
                    progression.equals(e.getTeamProgression()) &&
                    e.getAdvancementCompleter().equals(player) &&
                    e.doesGiveRewards() == giveRewards;
        });
    }
}
