package best.lettuce.event.impl.player;

import best.lettuce.event.Event;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.player.MoveUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

@Getter
@Setter
@AllArgsConstructor
public class EventMoveUpdate extends Event implements MinecraftInstance {

    private float strafe, forward, friction, yaw, pitch;

    public void applyMotion(double speed, float strafeMotion) {
        float remainder = 1 - strafeMotion;
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        strafeMotion *= 0.91;
        if (player.onGround) {
            MoveUtils.setSpeed(speed);

        } else {
            player.motionX *= strafeMotion;
            player.motionZ *= strafeMotion;
            friction = (float) speed * remainder;
        }
    }

}