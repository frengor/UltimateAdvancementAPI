package com.fren_gor.ultimateAdvancementAPI.nms.mocked1_17_R1.packets;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutSelectAdvancementTabWrapper;
import org.bukkit.entity.Player;
public class PacketPlayOutSelectAdvancementTabWrapper_mocked1_17_R1 extends PacketPlayOutSelectAdvancementTabWrapper {
 public PacketPlayOutSelectAdvancementTabWrapper_mocked1_17_R1(){}
 public PacketPlayOutSelectAdvancementTabWrapper_mocked1_17_R1(MinecraftKeyWrapper key){}
 public void sendTo(Player player){PacketPlayOutAdvancementsWrapper_mocked1_17_R1.SENT.add("select");}
}
