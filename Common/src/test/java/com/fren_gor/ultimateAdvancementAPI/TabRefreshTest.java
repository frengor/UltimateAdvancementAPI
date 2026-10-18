package com.fren_gor.ultimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.*;
import com.fren_gor.ultimateAdvancementAPI.database.*;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.*;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.*;
import com.fren_gor.ultimateAdvancementAPI.tests.*;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.*;
import org.junit.*;
import org.mockito.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static com.fren_gor.ultimateAdvancementAPI.nms.mocked1_17_R1.packets.PacketPlayOutAdvancementsWrapper_mocked1_17_R1.SENT;
public class TabRefreshTest {
 private MockedStatic<Bukkit> bukkit;
 private AdvancementTab tab;
 private TeamProgression team;
 private AdvancementWrapper definition;
 private final List<Runnable> scheduled=new ArrayList<>();
 private int progress;
 private Player player;
 @Before public void setup() throws Exception {
  bukkit=Utils.mockServer();
  Plugin plugin=InterfaceImplementer.newFakePlugin("inspection");
  DatabaseManager database=mock(DatabaseManager.class);
  tab=new AdvancementTab(plugin,database,"inspection");
  player=mock(Player.class);UUID id=UUID.randomUUID();
  bukkit.when(()->Bukkit.getPlayer(id)).thenReturn(player);
  BukkitScheduler scheduler=mock(BukkitScheduler.class);
  bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
  when(scheduler.runTaskLater(eq(plugin),any(Runnable.class),eq(1L))).thenAnswer(i->{scheduled.add(i.getArgument(1));return mock(BukkitTask.class);});
  team=mock(TeamProgression.class);when(team.isValid()).thenReturn(true);
  doAnswer(i->{((Consumer<UUID>)i.getArgument(0)).accept(id);return null;}).when(team).forEachMember(any());
  RootAdvancement root=mock(RootAdvancement.class);
  when(root.getKey()).thenReturn(new AdvancementKey("inspection","root"));
  definition=mock(AdvancementWrapper.class);
  when(definition.getKey()).thenReturn(MinecraftKeyWrapper.craft("inspection","root"));
  when(definition.getParent()).thenReturn(null);
  Advancement advancement=mock(Advancement.class);
  doAnswer(i->{((Map<AdvancementWrapper,Integer>)i.getArgument(1)).put(definition,progress);return null;}).when(advancement).onUpdate(eq(team),anyMap());
  field("rootAdvancement").set(tab,root);field("initialised").set(tab,true);
  ((Map<AdvancementKey,Advancement>)field("advancements").get(tab)).put(root.getKey(),advancement);
  SENT.clear();com.fren_gor.ultimateAdvancementAPI.nms.mocked1_17_R1.packets.PacketPlayOutAdvancementsWrapper_mocked1_17_R1.FAIL_PROGRESS=false;
 }
 private Field field(String name)throws ReflectiveOperationException{Field f=AdvancementTab.class.getDeclaredField(name);f.setAccessible(true);return f;}
 private void refresh(){try{
 Object manager=field("updateManager").get(tab);
 var schedule=manager.getClass().getDeclaredMethod("schedule",TeamProgression.class);schedule.setAccessible(true);schedule.invoke(manager,team);
 scheduled.remove(0).run();
 }catch(ReflectiveOperationException e){throw new AssertionError(e);}}
 @After public void close(){if(bukkit!=null)bukkit.close();}
 @Test public void ordinaryProgressNeverRemovesOrSelectsAnotherTab(){
  refresh();SENT.clear();progress=1;refresh();assertEquals(List.of("progress"),SENT);
 }
 @Test public void changedDefinitionStillUsesFullRefresh(){
  refresh();SENT.clear();definition=mock(AdvancementWrapper.class);when(definition.getKey()).thenReturn(uncheckedKey());
  refresh();assertTrue(SENT.contains("definitions"));assertTrue(SENT.contains("remove"));
 }
 @Test public void unchangedProgressAlsoLeavesSelectionAlone(){
  refresh();SENT.clear();refresh();assertEquals(List.of("progress"),SENT);
 }
 @Test public void hiddenThenShownTabResendsDefinitions(){
  refresh();tab.hideTab(player);SENT.clear();refresh();assertTrue(SENT.contains("definitions"));assertFalse(SENT.contains("progress"));
 }
 @Test public void failedPacketConstructionRetainsSnapshotForRetry(){
  refresh();SENT.clear();
  com.fren_gor.ultimateAdvancementAPI.nms.mocked1_17_R1.packets.PacketPlayOutAdvancementsWrapper_mocked1_17_R1.FAIL_PROGRESS=true;
  refresh();assertTrue(SENT.isEmpty());
  com.fren_gor.ultimateAdvancementAPI.nms.mocked1_17_R1.packets.PacketPlayOutAdvancementsWrapper_mocked1_17_R1.FAIL_PROGRESS=false;
  refresh();assertEquals(List.of("progress"),SENT);
 }
 private MinecraftKeyWrapper uncheckedKey(){try{return MinecraftKeyWrapper.craft("inspection","root");}catch(Exception e){throw new AssertionError(e);}}
}

