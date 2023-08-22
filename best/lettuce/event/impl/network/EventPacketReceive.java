package best.lettuce.event.impl.network;

import net.minecraft.network.Packet;

public class EventPacketReceive extends EventPacket {
    public EventPacketReceive(Packet<?> packet) {
        super(packet);
    }
}