package best.lettuce.event.impl.player;

import best.lettuce.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

@Getter @AllArgsConstructor
public class EventPlaceBlock extends Event {
    private final BlockPos blockPos;
    private final EnumFacing side;
    private final Vec3 hitVec;
}
