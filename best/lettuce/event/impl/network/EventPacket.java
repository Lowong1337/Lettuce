package best.lettuce.event.impl.network;

import best.lettuce.event.Event;
import lombok.*;
import net.minecraft.network.Packet;

@AllArgsConstructor @Getter @Setter
public class EventPacket extends Event {
    private Packet<?> packet;
}