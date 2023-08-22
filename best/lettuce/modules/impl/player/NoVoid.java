package best.lettuce.modules.impl.player;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.utils.packet.PacketUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class NoVoid extends Module {
    public NoVoid(){
        super("No Void", Category.PLAYER, "Teleports you back when falling to the void.");
        this.addProperties(mode);
    }

    double x, y, z;

    public final ModeProperty mode = new ModeProperty("Mode", "NCP", "NCP");

    public final EventListener<EventTick> onTick = e -> {
        this.setSuffix(mode.getMode());
    };

    public final EventListener<EventMotion> onMotion = e -> {
        switch (mode.getMode()) {
            case "NCP": {
                if(intheVoid() && mc.thePlayer.fallDistance >= 3.5F && mc.thePlayer.motionY <= 0){
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                }
                break;
            }
        }
    };

    public final EventListener<EventPacketReceive> onPacket = e -> {
        switch (mode.getMode()) {
            case "NCP": {
                if(e.getPacket() instanceof C03PacketPlayer){
                    e.setCancelled(true);
                }
                if(e.getPacket() instanceof S08PacketPlayerPosLook){
                    x = ((S08PacketPlayerPosLook) e.getPacket()).getX();
                    y = ((S08PacketPlayerPosLook) e.getPacket()).getY();
                    z = ((S08PacketPlayerPosLook) e.getPacket()).getZ();
                }
                break;
            }
        }
    };

    private static boolean intheVoid(){
        double i = (-(mc.thePlayer.posY-1.4857625));
        boolean dangerous = true;
        while (i <= 0) {
            dangerous = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(mc.thePlayer.motionX * 0.5, i, mc.thePlayer.motionZ * 0.5)).isEmpty();
            i++;
            if (!dangerous) break;
        }
        return dangerous;
    }
}
