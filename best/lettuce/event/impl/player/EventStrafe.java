package best.lettuce.event.impl.player;

import best.lettuce.event.Event;
import lombok.*;

@Getter @Setter @AllArgsConstructor
public class EventStrafe extends Event {
    public float forward, strafe, yaw, friction;
}