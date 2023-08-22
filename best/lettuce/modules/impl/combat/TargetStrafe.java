package best.lettuce.modules.impl.combat;

import best.lettuce.Lecture;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.event.impl.player.EventMove;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.impl.movement.Fly;
import best.lettuce.modules.impl.movement.Speed;
import best.lettuce.modules.property.impl.*;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.player.MoveUtils;
import best.lettuce.utils.player.RotationUtils;
import best.lettuce.utils.server.ServerUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TargetStrafe extends Module {

    public static final ModeProperty mode = new ModeProperty("Mode", "Normal", "Normal", "Adaptive");

    public static final NumberProperty radius = new NumberProperty("Radius", 2, 0.5, 8, 0.5);
    private static final NumberProperty points = new NumberProperty("Points", 12, 3, 16, 1);
    public static final BooleanProperty controllabe = new BooleanProperty("Controllable", true);
    public static final BooleanProperty space = new BooleanProperty("Space Down", true);
    public static final BooleanProperty auto3rdPerson = new BooleanProperty("Third Person", false);
    private final BooleanProperty render = new BooleanProperty("Render", true);
    private final ColorProperty color = new ColorProperty("Color", new Color(-16711712));

    private static int strafe = 1;
    private static int position;

    private final DecelerateAnimation animation = new DecelerateAnimation(250, radius.getValue(), Direction.FORWARDS);
    private boolean returnState;

    public TargetStrafe() {
        super("Target Strafe", Category.COMBAT, "strafe around targets");
        addProperties(mode, radius, points, space, controllabe,auto3rdPerson);
    }

    public final EventListener<EventTick> onTick = e -> {
        setSuffix(mode.getMode());
    };
    public final EventListener<EventMotion> onMotion = e -> {
        if (canStrafe()) {
            if (auto3rdPerson.isEnabled() && mc.gameSettings.thirdPersonView == 0) {
                mc.gameSettings.thirdPersonView = 1;
                returnState = true;
            }
            boolean updatePosition = false, positive = true;
            if (mc.thePlayer.isCollidedHorizontally) {
                strafe = -strafe;
                updatePosition = true;
                positive = strafe == 1;
            } else {
                if (controllabe.isEnabled()) {
                    if (mc.gameSettings.keyBindLeft.isPressed()) {
                        strafe = 1;
                        updatePosition = true;
                    }
                    if (mc.gameSettings.keyBindRight.isPressed()) {
                        strafe = -1;
                        updatePosition = true;
                        positive = false;
                    }
                }
                if (mode.is("Adaptive") && isInVoid()) {
                    strafe = -strafe;
                    updatePosition = true;
                    positive = false;
                }
                if (mode.is("Adaptive") && isInLiquid()) {
                    strafe = -strafe;
                    updatePosition = true;
                    positive = false;
                }
            }
            if (updatePosition) {
                position = (position + (positive ? 1 : -1)) % points.getValue().intValue();
            }
        } else if (auto3rdPerson.isEnabled() && mc.gameSettings.thirdPersonView != 0 && returnState) {
            mc.gameSettings.thirdPersonView = 0;
            returnState = false;
        }
    };

    /*@Override
    public void onRender3DEvent(Render3DEvent event) {
        if (render.isEnabled()) {
            if (animation.getEndPoint() != radius.getValue()) animation.setEndPoint(radius.getValue());
            boolean canStrafe = canStrafe();
            animation.setDirection(canStrafe ? Direction.FORWARDS : Direction.BACKWARDS);
            if (canStrafe || !animation.isDone()) {
                drawCircle(5, 0xFF000000);
                drawCircle(3, color.getColor().getRGB());
            }
        }
    }*/

    public static boolean strafe(EventMove e) {
        return strafe(e, MoveUtils.getSpeed());
    }

    public static boolean strafe(EventMove e, double moveSpeed) {
        if (canStrafe()) {
            setSpeed(e, moveSpeed, RotationUtils.getYaw(KillAura.targetmob.getPositionVector()), strafe,
                    mc.thePlayer.getDistanceToEntity(KillAura.targetmob) <= radius.getValue() ? 0 : 1);
            return true;
        }
        return false;
    }

    public static boolean canStrafe() {
        KillAura killAura = Lecture.INSTANCE.getModuleManager().getModule(KillAura.class);
        if (!Lecture.INSTANCE.getModuleManager().getModule(TargetStrafe.class).isEnabled() || !killAura.isEnabled()
                || !MoveUtils.isMoving() || (space.isEnabled() && !Keyboard.isKeyDown(Keyboard.KEY_SPACE))) {
            return false;
        }
        if (!(!Lecture.INSTANCE.getModuleManager().getModule(Speed.class).isEnabled() || !Lecture.INSTANCE.getModuleManager().getModule(Fly.class).isEnabled())) {
            return false;
        }
        return KillAura.targetmob != null;
    }

    public static void setSpeed(EventMove moveEvent, double speed, float yaw, double strafe, double forward) {
        EntityLivingBase target = KillAura.targetmob;
        double rad = radius.getValue();
        int count = points.getValue().intValue();

        double a = (Math.PI * 2.0) / (double) count;
        double posX = StrictMath.sin(a * position) * rad * strafe, posY = StrictMath.cos(a * position) * rad;

        if (forward == 0 && strafe == 0) {
            moveEvent.setX(0);
            moveEvent.setZ(0);
        } else {
            if (ServerUtils.isGeniuneHypixel()) speed = Math.min(speed, 0.3375);

            boolean skip = false;
            if (mode.is("Adaptive")) {
                Vec3 pos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
                Vec3 vec = RotationUtils.getVecRotations(0, 90);
                if (mc.theWorld.rayTraceBlocks(pos, pos.addVector(vec.xCoord * 5, vec.yCoord * 5, vec.zCoord * 5), false, false, false) == null) {
                    moveEvent.setX(0);
                    moveEvent.setZ(0);
                    skip = true;
                }
            }

            if (!skip) {
                double d;
                d = StrictMath.toRadians(RotationUtils.getRotations(target.posX + posX, target.posY, target.posZ + posY)[0]);
                moveEvent.setX(speed * -StrictMath.sin(d));
                moveEvent.setZ(speed * StrictMath.cos(d));
            }
        }

        double x = Math.abs(target.posX + posX - mc.thePlayer.posX), z = Math.abs(target.posZ + posY - mc.thePlayer.posZ);
        double dist = StrictMath.sqrt(x * x + z * z);
        if (dist <= 0.7) {
            position = (position + TargetStrafe.strafe) % count;
        } else if (dist > 3) {
            position = getClosestPoint(target);
        }
    }

    /*private void drawCircle(float lineWidth, int color) {
        EntityLivingBase entity = KillAura.target;
        if (entity == null) return;

        glPushMatrix();
        RenderUtil.color(color, (float) ((animation.getOutput().floatValue() / radius.getValue()) / 2F));
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glLineWidth(lineWidth);
        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);

        glBegin(GL_LINE_STRIP);
        EntityLivingBase target = KillAura.target;
        float partialTicks = mc.timer.elapsedPartialTicks;
        double rad = radius.getValue();
        double d = (Math.PI * 2.0) / points.getValue();

        double posX = target.posX, posY = target.posY, posZ = target.posZ;
        double lastTickX = target.lastTickPosX, lastTickY = target.lastTickPosY, lastTickZ = target.lastTickPosZ;
        double renderPosX = mc.getRenderManager().renderPosX, renderPosY = mc.getRenderManager().renderPosY, renderPosZ = mc.getRenderManager().renderPosZ;

        double y = lastTickY + (posY - lastTickY) * partialTicks - renderPosY;
        for (double i = 0; i < Math.PI * 2.0; i += d) {
            double x = lastTickX + (posX - lastTickX) * partialTicks + StrictMath.sin(i) * rad - renderPosX,
                    z = lastTickZ + (posZ - lastTickZ) * partialTicks + StrictMath.cos(i) * rad - renderPosZ;
            glVertex3d(x, y, z);
        }
        double x = lastTickX + (posX - lastTickX) * partialTicks - renderPosX,
                z = lastTickZ + (posZ - lastTickZ) * partialTicks + rad - renderPosZ;
        glVertex3d(x, y, z);
        glEnd();

        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glColor4f(1, 1, 1, 1);
        glPopMatrix();
    }*/

    private boolean isInVoid() {
        double yaw = Math.toRadians(RotationUtils.getYaw(KillAura.targetmob.getPositionVector()));
        double xValue = -Math.sin(yaw) * 2;
        double zValue = Math.cos(yaw) * 2;
        for (int i = 0; i <= 256; i++) {
            BlockPos b = new BlockPos(mc.thePlayer.posX + xValue, mc.thePlayer.posY - i, mc.thePlayer.posZ + zValue);
            if (mc.theWorld.getBlockState(b).getBlock() instanceof BlockAir) {
                if (b.getY() == 0) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return !mc.thePlayer.isCollidedVertically && !mc.thePlayer.onGround && mc.thePlayer.fallDistance != 0 && mc.thePlayer.motionY != 0 && mc.thePlayer.isAirBorne && !mc.thePlayer.capabilities.isFlying && !mc.thePlayer.isInWater() && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isPotionActive(Potion.invisibility.id);
    }

    private boolean isInLiquid() {
        double yaw = Math.toRadians(RotationUtils.getYaw(KillAura.targetmob.getPositionVector()));
        double xValue = -Math.sin(yaw) * 2;
        double zValue = Math.cos(yaw) * 2;
        BlockPos b = new BlockPos(mc.thePlayer.posX + xValue, mc.thePlayer.posY, mc.thePlayer.posZ + zValue);
        return mc.theWorld.getBlockState(b).getBlock() instanceof BlockLiquid;
    }

    private static int getClosestPoint(Entity target) {
        double playerX = mc.thePlayer.posX, playerZ = mc.thePlayer.posZ;
        return getPoints(target).stream().min(Comparator.comparingDouble(p -> p.getDistance(playerX, playerZ))).get().iteration;
    }

    private static List<Point> getPoints(Entity target) {
        double radius = TargetStrafe.radius.getValue();
        List<Point> pointList = new ArrayList<>();
        int count = points.getValue().intValue();
        double posX = target.posX, posZ = target.posZ;
        double d = (Math.PI * 2.0) / count;
        for (int i = 0; i <= count; i++) {
            double x = radius * StrictMath.cos(i * d);
            double z = radius * StrictMath.sin(i * d);
            pointList.add(new Point(posX + x, posZ + z, i));
        }
        return pointList;
    }

    @Getter
    @AllArgsConstructor
    private static class Point {
        private final double x, z;
        private final int iteration;

        private double getDistance(double posX, double posZ) {
            double x2 = Math.abs(posX - x), z2 = Math.abs(posZ - z);
            return StrictMath.sqrt(x2 * x2 + z2 * z2);
        }
    }

}
