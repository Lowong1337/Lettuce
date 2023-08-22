package best.lettuce.utils.player;

import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.math.RandomUtils;
import com.google.common.base.Predicates;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;

import java.util.List;

@UtilityClass
public class RotationUtils implements MinecraftInstance {
    private final double DEG_TO_RAD = Math.PI / 180.0, RAD_TO_DEG = 180.0 / Math.PI;

    public void setVisualRotations(float yaw, float pitch, boolean rotateBody) {
        mc.thePlayer.rotationPitchHead = pitch;
        mc.thePlayer.rotationYawHead = yaw;
        if (rotateBody) mc.thePlayer.renderYawOffset = yaw;
    }

    public float[] getRotations(double posX, double posY, double posZ) {
        double x = posX - mc.thePlayer.posX, z = posZ - mc.thePlayer.posZ, y = posY - (mc.thePlayer.getEyeHeight() + mc.thePlayer.posY);
        double d3 = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (MathHelper.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(y, d3) * 180.0D / Math.PI));
        return new float[]{yaw, pitch};
    }

    public boolean isMouseOver(final float yaw, final float pitch, final Entity target, final float range) {
        final float partialTicks = mc.timer.renderPartialTicks;
        final Entity entity = mc.getRenderViewEntity();
        MovingObjectPosition objectMouseOver;
        Entity mcPointedEntity = null;

        if (entity != null && mc.theWorld != null) {

            mc.mcProfiler.startSection("pick");
            final double d0 = mc.playerController.getBlockReachDistance();
            objectMouseOver = entity.rayTrace(d0, partialTicks);
            double d1 = d0;
            final Vec3 vec3 = entity.getPositionEyes(partialTicks);
            final boolean flag = d0 > (double) range;

            if (objectMouseOver != null) {
                d1 = objectMouseOver.hitVec.distanceTo(vec3);
            }

            final Vec3 vec31 = mc.thePlayer.getVectorForRotation(pitch, yaw);
            final Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            final float f = 1.0F;
            final List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = d1;

            for (final Entity entity1 : list) {
                final float f1 = entity1.getCollisionBorderSize();
                final AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                final MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    final double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition.hitVec;
                        d2 = d3;
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > (double) range) {
                pointedEntity = null;
                objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    mcPointedEntity = pointedEntity;
                }
            }

            mc.mcProfiler.endSection();

            return mcPointedEntity == target;
        }

        return false;
    }

    public Vec3 getVecRotations(float yaw, float pitch) {
        double d = Math.cos(Math.toRadians(-yaw) - Math.PI);
        double d1 = Math.sin(Math.toRadians(-yaw) - Math.PI);
        double d2 = -Math.cos(Math.toRadians(-pitch));
        double d3 = Math.sin(Math.toRadians(-pitch));
        return new Vec3(d1 * d2, d3, d * d2);
    }

    public float[] changerotwithgivenspeed(float currentYaw, float currentPitch, float targetYaw, float targetPitch, float turnspeed) {
        float yaw = targetYaw - currentYaw == 0 ? currentYaw : currentYaw + MathHelper.clamp_float(getAngleDifference(targetYaw, currentYaw), -1 * turnspeed, turnspeed);
        float pitch = targetPitch - currentPitch == 0 ? currentPitch : currentPitch + MathHelper.clamp_float(getAngleDifference(targetPitch, currentPitch), -1 * turnspeed, turnspeed);
        return new float[]{yaw, pitch};
    }

    public float[] getRotations(EntityLivingBase ent) {
        double x = ent.posX;
        double z = ent.posZ;
        double y = ent.posY + ent.getEyeHeight() / 2.0F;
        return getRotationFromPosition(x, z, y);
    }

    public float[] getSmoothRotations(EntityLivingBase entity) {
        float f1 = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float fac = f1 * f1 * f1 * 256.0F;

        double x = entity.posX - mc.thePlayer.posX;
        double z = entity.posZ - mc.thePlayer.posZ;
        double y = entity.posY + entity.getEyeHeight() - (mc.thePlayer.getEntityBoundingBox().minY + (mc.thePlayer.getEntityBoundingBox().maxY - mc.thePlayer.getEntityBoundingBox().minY));

        double d3 = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (MathHelper.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(y, d3) * 180.0 / Math.PI));
        yaw = smoothRotation(mc.thePlayer.prevRotationYawHead, yaw, fac * RandomUtils.nextFloat(0.9F, 1));
        pitch = smoothRotation(mc.thePlayer.prevRotationPitchHead, pitch, fac * RandomUtils.nextFloat(0.7F, 1));

        return new float[]{yaw, pitch};
    }

    public float getYaw(Vec3 to) {
        float x = (float) (to.xCoord - mc.thePlayer.posX);
        float z = (float) (to.zCoord - mc.thePlayer.posZ);
        float var1 = (float) (StrictMath.atan2(z, x) * 180.0D / StrictMath.PI) - 90.0F;
        float rotationYaw = mc.thePlayer.rotationYaw;
        return rotationYaw + MathHelper.wrapAngleTo180_float(var1 - rotationYaw);
    }

    public float smoothRotation(float from, float to, float speed) {
        float f = MathHelper.wrapAngleTo180_float(to - from);

        if (f > speed) {
            f = speed;
        }

        if (f < -speed) {
            f = -speed;
        }

        return from + f;
    }

    public float[] getRotationFromPosition(double x, double z, double y) {
        double xDiff = x - mc.thePlayer.posX;
        double zDiff = z - mc.thePlayer.posZ;
        double yDiff = y - mc.thePlayer.posY - 1.2;
        double dist = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        float yaw = MathHelper.wrapAngleTo180_float((float) (Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90f));
        float pitch = MathHelper.wrapAngleTo180_float((float) -Math.toDegrees(Math.atan2(yDiff, dist)));
        return new float[]{yaw, pitch};
    }

    public float getAngleDifference(float a, float b) {
        return ((a - b) % 360f + 540f) % 360f - 180f;
    }

    public void rotate(final EventMotion event, final float[] rotations, final float aimSpeed, boolean lockview) {
        final float[] prevRotations = {event.getPrevYaw(), event.getPrevPitch()};

        final float[] cappedRotations = {maxAngleChange(prevRotations[0], rotations[0], aimSpeed), maxAngleChange(prevRotations[1], rotations[1], aimSpeed)};

        final float[] appliedRotations = RotationUtils.applyGCD(cappedRotations, prevRotations);

        event.setYaw(appliedRotations[0]);
        event.setPitch(appliedRotations[1]);

        if (lockview) {
            mc.thePlayer.rotationYaw = appliedRotations[0];
            mc.thePlayer.rotationPitch = appliedRotations[1];
        }
    }

    private float maxAngleChange(final float prev, final float now, final float maxTurn) {
        float dif = MathHelper.wrapAngleTo180_float(now - prev);
        if (dif > maxTurn) dif = maxTurn;
        if (dif < -maxTurn) dif = -maxTurn;
        return prev + dif;
    }

    public double getMouseGCD() {
        final float sens = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        final float pow = sens * sens * sens * 8.0F;
        return pow * 0.15D;
    }

    public float[] applyGCD(final float[] rotations, final float[] prevRots) {
        final float yawDif = rotations[0] - prevRots[0];
        final float pitchDif = rotations[1] - prevRots[1];
        final double gcd = getMouseGCD();

        rotations[0] -= yawDif % gcd;
        rotations[1] -= pitchDif % gcd;
        return rotations;
    }

    public Vec3 getHitOrigin(final Entity entity) {
        return new Vec3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
    }

    public MovingObjectPosition rayTraceBlocks(final Minecraft mc, final Vec3 src, final double reach, final float yaw, final float pitch) {
        return mc.theWorld.rayTraceBlocks(src, getDstVec(src, yaw, pitch, reach), false, false, true);
    }

    public MovingObjectPosition rayTraceBlocks(final Minecraft mc, final float yaw, final float pitch) {
        return rayTraceBlocks(mc, getHitOrigin(mc.thePlayer), mc.playerController.getBlockReachDistance(), yaw, pitch);
    }

    public Vec3 getDstVec(final Vec3 src, final float yaw, final float pitch, final double reach) {
        final Vec3 rotationVec = getPointedVec(yaw, pitch);
        return src.addVector(rotationVec.xCoord * reach, rotationVec.yCoord * reach, rotationVec.zCoord * reach);
    }

    public Vec3 getPointedVec(final float yaw, final float pitch) {
        final double theta = -Math.cos(-pitch * DEG_TO_RAD);

        return new Vec3(Math.sin(-yaw * DEG_TO_RAD - Math.PI) * theta, Math.sin(-pitch * DEG_TO_RAD), Math.cos(-yaw * DEG_TO_RAD - Math.PI) * theta);
    }

    public void applySmoothing(final float[] lastRotations, final float smoothing, final float[] dstRotation) {
        if (smoothing > 0.0F) {
            final float yawChange = MathHelper.wrapAngleTo180_float(dstRotation[0] - lastRotations[0]);
            final float pitchChange = MathHelper.wrapAngleTo180_float(dstRotation[1] - lastRotations[1]);

            final float smoothingFactor = Math.max(1.0F, smoothing / 10.0F);

            dstRotation[0] = lastRotations[0] + yawChange / smoothingFactor;
            dstRotation[1] = Math.max(Math.min(90.0F, lastRotations[1] + pitchChange / smoothingFactor), -90.0F);
        }
    }

    public float[] getRotations(final Vec3 start, final Vec3 dst) {
        final double xDif = dst.xCoord - start.xCoord;
        final double yDif = dst.yCoord - start.yCoord;
        final double zDif = dst.zCoord - start.zCoord;

        final double distXZ = Math.sqrt(xDif * xDif + zDif * zDif);

        return new float[]{(float) (Math.atan2(zDif, xDif) * RAD_TO_DEG) - 90.0F, (float) (-(Math.atan2(yDif, distXZ) * RAD_TO_DEG))};
    }

    public float[] getRotations(final float[] lastRotations, final float smoothing, final Vec3 start, final Vec3 dst) {
        // Get rotations from start - dst
        final float[] rotations = getRotations(start, dst);
        // Apply smoothing to them
        applySmoothing(lastRotations, smoothing, rotations);
        return rotations;
    }
}