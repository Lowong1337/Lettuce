package best.lettuce.event.impl.player;

import best.lettuce.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

@Getter @Setter @AllArgsConstructor
public class EventCollide extends Event {
    private Entity collidingEntity;
    private int x, y, z;
    private AxisAlignedBB axisAlignedBB;
    private Block block;
}
