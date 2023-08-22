package best.lettuce.modules.impl.movement;

import best.lettuce.Lettuce;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.player.EventMove;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.utils.player.MoveUtils;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

public class Strafe extends Module {
    public Strafe() {
        super("Strafe", Category.MOVEMENT, "Allows you to strafe in air or on ground.");
        addProperties(mode, groundOnly);
    }

    public static double oldSpeed, contextFriction;
    public static boolean needSwap, needSprintState;
    public static int noSlowTicks;

    public final ModeProperty mode = new ModeProperty("Mode", "Vanilla", "Vanilla", "Matrix");
    public final BooleanProperty groundOnly = new BooleanProperty("Ground Only", false, () -> mode.is("Vanilla"));

    public final EventListener<EventTick> onTick = e -> this.setSuffix(mode.getMode());

    public final EventListener<EventMove> onMove = e -> {
        switch (mode.getMode()) {
            case "Vanilla" -> {
                if (groundOnly.isEnabled() && !mc.thePlayer.onGround) return;
                MoveUtils.strafe(MoveUtils.getSpeed());
            }
            case "Matrix" -> {
                if (canStrafe()) {
                    if (MoveUtils.isMoving()) {
                        double[] motions = MoveUtils.forward(calculateSpeed(e));
                        e.setX(motions[0]);
                        e.setZ(motions[1]);
                    } else {
                        oldSpeed = 0;
                        e.setX(0);
                        e.setZ(0);
                    }
                } else {
                    oldSpeed = 0;
                }
            }
        }
    };

    public boolean canStrafe() {
        if (mc.thePlayer.isSneaking()) {
            return false;
        }
        if (mc.thePlayer.isInLava() || mc.thePlayer.isInWater()) {
            return false;
        }
        if (Lettuce.INSTANCE.getModuleManager().getModule(Speed.class).isEnabled()) {
            return false;
        }

        return !mc.thePlayer.capabilities.isFlying;
    }

    public double calculateSpeed(EventMove move) {
        float speedAttributes = getAIMoveSpeed();
        final float frictionFactor = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY - 0.1f, mc.thePlayer.posZ)).getBlock().slipperiness * 0.91F;
        float n6 = mc.thePlayer.isPotionActive(Potion.jump) && mc.thePlayer.isUsingItem() ? 0.88f : (float) (oldSpeed > 0.32 && mc.thePlayer.isUsingItem() ? 0.88 : 0.91F);
        if (mc.thePlayer.onGround) {
            n6 = frictionFactor;
        }
        float n7 = (float) ((1646f / 10000f) / Math.pow(n6, 3.0));
        float n8;
        if (mc.thePlayer.onGround) {
            n8 = speedAttributes * n7;
            if (move.getY() > 0) {
                n8 += 0.2f;
            }
        } else {
            n8 = 2.55f / 100f;
        }
        boolean noslow = false;
        double max2 = oldSpeed + n8;
        double max = 0.0;
        if (mc.thePlayer.isUsingItem() && move.getY() <= 0) {
            double n10 = oldSpeed + n8 * 0.25;
            double motionY2 = move.getY();
            if (motionY2 != 0.0 && Math.abs(motionY2) < 0.08) {
                n10 += 0.055;
            }
            if (max2 > (max = Math.max(0.043, n10))) {
                noslow = true;
                ++noSlowTicks;
            } else {
                noSlowTicks = Math.max(noSlowTicks - 1, 0);
            }
        } else {
            noSlowTicks = 0;
        }
        if (noSlowTicks > 3) {
            max2 = max - 0.019;
        } else {
            max2 = Math.max(noslow ? 0 : 0.25, max2) - (mc.thePlayer.ticksExisted % 2 == 0 ? 0.001 : 0.002);
        }

        contextFriction = n6;

        if (!mc.thePlayer.onGround) {
            needSprintState = !mc.thePlayer.serverSprintState;
            needSwap = true;
        } else {
            needSprintState = false;
        }
        return max2;
    }

    public float getAIMoveSpeed() {
        boolean prevSprinting = mc.thePlayer.isSprinting();
        mc.thePlayer.setSprinting(false);
        float speed = mc.thePlayer.getAIMoveSpeed() * 1.3f;
        mc.thePlayer.setSprinting(prevSprinting);
        return speed;
    }
}
