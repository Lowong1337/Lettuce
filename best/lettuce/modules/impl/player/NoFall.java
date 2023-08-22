package best.lettuce.modules.impl.player;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.utils.math.TimerUtils;
import best.lettuce.utils.packet.PacketUtils;
import net.minecraft.network.play.client.C03PacketPlayer;

public class NoFall extends Module {
    public NoFall() {
        super("No Fall", Category.PLAYER, "Reduces your fall damage.");
        addProperties(mode);
    }

    public final ModeProperty mode = new ModeProperty("Mode", "Ground", "Ground", "Collision");

    double x,y,z;

    public TimerUtils timer = new TimerUtils();

    public final EventListener<EventTick> onTick = e -> {
        this.setSuffix(mode.getMode());
    };
    public final EventListener<EventMotion> onMotion = e -> {
        x= mc.thePlayer.posX;
        y= mc.thePlayer.posY;
        z= mc.thePlayer.posZ;
        switch (mode.getMode()) {
            case "Ground": {
                if (mc.thePlayer.fallDistance > 3) {
                    PacketUtils.sendPacket(new C03PacketPlayer(true));
                }
                break;
            }
            case "Collision": {
                if (mc.thePlayer.fallDistance > 3F) {
                    //if (timer.hasTimeElapsed(0)) {
                    mc.thePlayer.motionY = 0;
                    mc.thePlayer.fallDistance = 0;
                    mc.thePlayer.setPosition(x, y, z);
                    mc.thePlayer.motionX *= 0.6;
                    mc.thePlayer.motionZ *= 0.6;
                    timer.reset();
                    PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));
                    //}
                    PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                }
            }
            break;
        }
    };

    public EventListener<EventPacketReceive> onPacket = e -> {
        if(mc.thePlayer != null) {
            switch (mode.getMode()) {
                case "Collision": {
                    if (mc.thePlayer.fallDistance > 3 && e.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition) {
                        e.setCancelled(true);
                        mc.thePlayer.setPosition(x, y, z);
                    }
                    break;
                }
            }
        }
    };
}