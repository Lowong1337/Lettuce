package best.lettuce.event.impl.render;

import best.lettuce.event.Event;
import best.lettuce.modules.property.impl.MultipleBoolProperty;
import lombok.*;

@AllArgsConstructor @Getter @Setter
public class EventShader extends Event {
    private final boolean bloom;
    private final MultipleBoolProperty bloomOptions;
}