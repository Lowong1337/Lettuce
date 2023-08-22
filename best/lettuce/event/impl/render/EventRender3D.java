package best.lettuce.event.impl.render;

import best.lettuce.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class EventRender3D extends Event {
    private float ticks;
}
