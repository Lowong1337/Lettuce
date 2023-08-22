package best.lettuce.utils.packet;

import best.lettuce.utils.MinecraftInstance;
import lombok.experimental.UtilityClass;
import net.minecraft.network.Packet;

@UtilityClass
public class PacketUtils implements MinecraftInstance {
    public void sendPacket(Packet<?> packet) {
        mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    public void sendPacketNoEvent(Packet<?> packet) {
        mc.getNetHandler().getNetworkManager().sendPacket(packet, true);
    }
}
