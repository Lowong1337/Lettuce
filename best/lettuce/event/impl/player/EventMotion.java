package best.lettuce.event.impl.player;

import best.lettuce.event.Event;
import lombok.*;

@AllArgsConstructor @Getter @Setter
public class EventMotion extends Event {
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;
    private float prevYaw, prevPitch;

    public void setRotations(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
