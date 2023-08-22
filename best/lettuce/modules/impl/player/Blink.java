package best.lettuce.modules.impl.player;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.event.impl.network.EventPacketSend;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.utils.math.TimerUtils;
import best.lettuce.utils.packet.PacketUtils;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {
    public Blink(){
        super("Blink", Category.PLAYER, "Blink");
    }

    public List<Packet> packets = new ArrayList<Packet>();

    public TimerUtils timerUtils = new TimerUtils();

    public final EventListener<EventTick> onTick = event -> {
      this.setSuffix(String.valueOf(timerUtils.getTime()));
    };

    public final EventListener<EventPacketSend> onPacketSend = event -> {
        if(mc.theWorld == null) return;
        event.setCancelled(true);
        packets.add(event.getPacket());
    };

    public final EventListener<EventPacketReceive> onPacketReceive = event -> {
        if(mc.theWorld == null) return;
        event.setCancelled(true);
    };

    @Override
    public void onDisable(){
        for (Packet packet : packets){
            PacketUtils.sendPacket(packet);
        }
    }

    @Override
    public void onEnable(){
        packets.clear();
        timerUtils.reset();
    }
}
