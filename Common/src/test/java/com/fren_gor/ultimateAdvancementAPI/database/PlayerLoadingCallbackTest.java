package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.events.team.TeamUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlayerLoadingCallbackTest {
    private final UUID uuid = UUID.randomUUID();
    private final Map<UUID, TeamProgression> cache = new HashMap<>();
    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<CompletableFuture> futures;
    private MockedConstruction<PlayerLoadingCompletedEvent> events;
    private MockedConstruction<AsyncTeamUpdateEvent> asyncTeamEvents;
    private MockedConstruction<TeamUpdateEvent> teamEvents;
    private DatabaseManager manager;
    private AdvancementMain main;
    private Player player;
    private TeamProgression progression;
    private PluginManager plugins;
    private Method callback;

    @Before
    public void setUp() throws Exception {
        bukkit = Utils.mockServer();
        futures = mockStatic(CompletableFuture.class);
        futures.when(() -> CompletableFuture.runAsync(any(Runnable.class)))
                .thenReturn(new CompletableFuture<Void>());
        // Keep real dispatch/listener ordering without requiring NMS display wrappers
        // initialized by the event DTO's progression validation utility.
        events = mockConstruction(PlayerLoadingCompletedEvent.class);
        asyncTeamEvents = mockConstruction(AsyncTeamUpdateEvent.class);
        teamEvents = mockConstruction(TeamUpdateEvent.class);
        main = mock(AdvancementMain.class);
        manager = mock(DatabaseManager.class, CALLS_REAL_METHODS);
        player = mock(Player.class);
        progression = mock(TeamProgression.class);
        plugins = mock(PluginManager.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.isOnline()).thenReturn(true);
        when(progression.isValid()).thenReturn(true);
        bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(player);
        bukkit.when(Bukkit::getPluginManager).thenReturn(plugins);
        cache.put(uuid, progression);
        setField("main", main);
        setField("progressionCache", cache);
        for (Method method : DatabaseManager.class.getDeclaredMethods()) {
            if (method.getName().startsWith("lambda$loadPlayerMainFunction$")
                    && method.getParameterCount() == 4) {
                callback = method;
                callback.setAccessible(true);
                break;
            }
        }
        if (callback == null) throw new AssertionError("Delayed loading callback not found");
    }

    private void setField(String name, Object value) throws Exception {
        Field field = DatabaseManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(manager, value);
    }

    private void complete() throws Exception {
        complete(false);
    }

    private void complete(boolean newlyRegistered) throws Exception {
        futures.clearInvocations();
        callback.invoke(manager, progression, new SimpleEntry<>(progression, newlyRegistered), uuid, player);
    }

    private void verifyNoCompletion() {
        verifyNoInteractions(plugins);
        verifyNoInteractions(main);
        futures.verify(() -> CompletableFuture.runAsync(any(Runnable.class)), never());
    }

    @After
    public void tearDown() {
        if (teamEvents != null) teamEvents.close();
        if (asyncTeamEvents != null) asyncTeamEvents.close();
        if (events != null) events.close();
        if (futures != null) futures.close();
        if (bukkit != null) bukkit.close();
    }

    @Test
    public void currentPlayerReceivesCompletionAndUpdate() throws Exception {
        complete();
        verify(plugins).callEvent(any(PlayerLoadingCompletedEvent.class));
        verify(main).updatePlayer(player);
        futures.verify(() -> CompletableFuture.runAsync(any(Runnable.class)));
    }

    @Test
    public void newlyRegisteredPlayerStillReceivesTeamNotifications() throws Exception {
        complete(true);
        verify(plugins).callEvent(any(PlayerLoadingCompletedEvent.class));
        verify(plugins).callEvent(any(AsyncTeamUpdateEvent.class));
        verify(plugins).callEvent(any(TeamUpdateEvent.class));
        verify(main).updatePlayer(player);
        futures.verify(() -> CompletableFuture.runAsync(any(Runnable.class)));
    }

    @Test
    public void disconnectedPlayerReceivesNothing() throws Exception {
        when(player.isOnline()).thenReturn(false);
        complete();
        verifyNoCompletion();
    }

    @Test
    public void reconnectWithSameUuidRejectsOldPlayerInstance() throws Exception {
        bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(mock(Player.class));
        complete();
        verifyNoCompletion();
    }

    @Test
    public void absentBukkitPlayerReceivesNothing() throws Exception {
        bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(null);
        complete();
        verifyNoCompletion();
    }

    @Test
    public void removedCacheReceivesNothing() throws Exception {
        cache.clear();
        complete();
        verifyNoCompletion();
    }

    @Test
    public void replacementProgressionRejectsOldCallback() throws Exception {
        cache.put(uuid, mock(TeamProgression.class));
        complete();
        verifyNoCompletion();
    }

    @Test
    public void invalidProgressionReceivesNothing() throws Exception {
        when(progression.isValid()).thenReturn(false);
        complete();
        verifyNoCompletion();
    }

    @Test
    public void listenerUnloadingPlayerPreventsUpdateAndRewardDispatch() throws Exception {
        doAnswer(invocation -> { cache.clear(); return null; })
                .when(plugins).callEvent(any(PlayerLoadingCompletedEvent.class));
        complete();
        verify(plugins).callEvent(any(PlayerLoadingCompletedEvent.class));
        verifyNoInteractions(main);
        futures.verify(() -> CompletableFuture.runAsync(any(Runnable.class)), never());
    }

    @Test
    public void listenerDisconnectPreventsUpdateAndRewardDispatch() throws Exception {
        doAnswer(invocation -> { when(player.isOnline()).thenReturn(false); return null; })
                .when(plugins).callEvent(any(PlayerLoadingCompletedEvent.class));
        complete();
        verify(plugins).callEvent(any(PlayerLoadingCompletedEvent.class));
        verifyNoInteractions(main);
        futures.verify(() -> CompletableFuture.runAsync(any(Runnable.class)), never());
    }

    @Test
    public void listenerUnloadingNewPlayerPreventsTeamNotifications() throws Exception {
        doAnswer(invocation -> { cache.clear(); return null; })
                .when(plugins).callEvent(any(PlayerLoadingCompletedEvent.class));
        complete(true);
        verify(plugins).callEvent(any(PlayerLoadingCompletedEvent.class));
        verify(plugins, never()).callEvent(any(AsyncTeamUpdateEvent.class));
        verify(plugins, never()).callEvent(any(TeamUpdateEvent.class));
        verifyNoInteractions(main);
        futures.verify(() -> CompletableFuture.runAsync(any(Runnable.class)), never());
    }
}
