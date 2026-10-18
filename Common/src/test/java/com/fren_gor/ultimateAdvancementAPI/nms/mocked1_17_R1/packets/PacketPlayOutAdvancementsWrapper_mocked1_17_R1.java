package com.fren_gor.ultimateAdvancementAPI.nms.mocked1_17_R1.packets;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import org.bukkit.entity.Player;
import java.util.*;
public class PacketPlayOutAdvancementsWrapper_mocked1_17_R1 extends PacketPlayOutAdvancementsWrapper {
 public static final List<String> SENT=new ArrayList<>();
 public static boolean FAIL_PROGRESS;
 private final String kind;
 public PacketPlayOutAdvancementsWrapper_mocked1_17_R1(){kind="reset";}
 public PacketPlayOutAdvancementsWrapper_mocked1_17_R1(Map<AdvancementWrapper,Integer> values){this(values,true);}
 public PacketPlayOutAdvancementsWrapper_mocked1_17_R1(Map<AdvancementWrapper,Integer> values,boolean definitions){if(!definitions && FAIL_PROGRESS)throw new IllegalStateException("test packet failure");kind=definitions?"definitions":"progress";}
 public PacketPlayOutAdvancementsWrapper_mocked1_17_R1(Set<MinecraftKeyWrapper> keys){kind="remove";}
 public void sendTo(Player player){SENT.add(kind);}
}
