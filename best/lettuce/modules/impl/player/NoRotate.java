package best.lettuce.modules.impl.player;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.utils.packet.PacketUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S14PacketEntity;

public class NoRotate extends Module {
    public NoRotate(){
        super("No Rotate", Category.PLAYER, "idk");
        addProperties(mode);
    }

    public final ModeProperty mode = new ModeProperty("Mode", "Packet", "Packet");

    public final EventListener<EventTick> onTick = event -> {
        this.setSuffix(mode.getMode());
    };

    public final EventListener<EventPacketReceive> onPacketReceive = event -> {
        switch (mode.getMode()){
            case "Packet": {
                if(event.getPacket() instanceof S14PacketEntity.S17PacketEntityLookMove){
                    event.setCancelled(true);
                    //PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, s17.yaw, s17.pitch, s17.getOnGround()));
                }
                break;
            }
        }
    };
}
