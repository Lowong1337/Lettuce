package best.lettuce.modules.impl.movement;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.player.EventStep;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.utils.math.TimerUtils;

public class Step extends Module {
    public Step() {
        super("Step", Category.MOVEMENT, "Allows you to step on a higher block without jumping.");
    }

    public TimerUtils timer = new TimerUtils();
    private boolean hasStepped;
    public static boolean isStepping;

    public EventListener<EventMotion> onUpdate = event -> {
        if (mc.thePlayer.onGround) {
            if (mc.thePlayer.stepHeight != 1) mc.thePlayer.stepHeight = 1;
        } else {
            if (mc.thePlayer.stepHeight != 0.625f) mc.thePlayer.stepHeight = 0.625f;
        }
        if (timer.hasTimeElapsed(20) && hasStepped) {
            mc.timer.timerSpeed = 1;
            hasStepped = false;
            isStepping = false;
            for (double offset : new double[]{0.41999998688698, 0.7531999805212})
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + offset, mc.thePlayer.posZ);
            event.setOnGround(true);

        }
    };

    public final EventListener<EventStep> onStep = event -> {
        double diffY = mc.thePlayer.getEntityBoundingBox().minY - mc.thePlayer.posY;
        if (diffY > 0.625f && diffY <= 1.5f && mc.thePlayer.onGround) {
            mc.timer.timerSpeed = 1f;
            timer.reset();
            hasStepped = true;
            isStepping = true;
        }
    };

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.625f;
    }
}
