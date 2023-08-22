package best.lettuce.event.impl.player;

import best.lettuce.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class EventMove extends Event {
    public double x, y, z;
}
