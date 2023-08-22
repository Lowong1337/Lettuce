package best.lettuce.event.impl.render;

import best.lettuce.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;

@Getter @Setter @AllArgsConstructor
public class EventRender2D extends Event {
    private float width, height, partialTicks;
}