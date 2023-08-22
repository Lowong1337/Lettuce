package best.lettuce.event.impl.player;

import best.lettuce.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;

@Getter @AllArgsConstructor
public class EventAttack extends Event {
    public EntityLivingBase target;
}
