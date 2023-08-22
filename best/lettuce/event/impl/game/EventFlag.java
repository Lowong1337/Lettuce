package best.lettuce.event.impl.game;

import best.lettuce.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.play.client.C03PacketPlayer;

@Getter @Setter @AllArgsConstructor
public class EventFlag extends Event {
    private C03PacketPlayer.C06PacketPlayerPosLook response;
    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;
}