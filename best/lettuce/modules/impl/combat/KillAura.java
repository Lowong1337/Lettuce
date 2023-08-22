package best.lettuce.modules.impl.combat;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.game.EventWorld;
import best.lettuce.event.impl.player.EventMoveUpdate;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.gui.notification.Notification;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.MultipleBoolProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.math.MathUtils;
import best.lettuce.utils.math.RandomUtils;
import best.lettuce.utils.math.TimerUtils;
import best.lettuce.utils.packet.PacketUtils;
import best.lettuce.utils.player.RotationUtils;
import de.florianmichael.viamcp.fixes.AttackOrder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class KillAura extends Module {

    public final ModeProperty mode = new ModeProperty("Mode", "Single", "Switch", "Single", "Multi");
    public final NumberProperty range = new NumberProperty("Range", 3, 1, 10, 0.1);
    public final NumberProperty mincps = new NumberProperty("Min CPS", 3, 1, 20, 1);
    public final NumberProperty maxcps = new NumberProperty("Max CPS", 3, 1, 20, 1);
    private static final MultipleBoolProperty target = new MultipleBoolProperty("Targets", new BooleanProperty("Players", true), new BooleanProperty("Animals", true), new BooleanProperty("Mobs", true), new BooleanProperty("Invisibles", true), new BooleanProperty("Ignore teammates", true));
    public final ModeProperty sort = new ModeProperty("Targets sort", "Range", "Range", "Health", "Hurt time", "Armor");
    public final BooleanProperty autoblock = new BooleanProperty("Auto Block", true);
    public final ModeProperty abm = new ModeProperty("Block Mode", "Vanilla", autoblock::isEnabled, "Vanilla", "NCP", "Fake", "Hurttime", "Grim");
    public final BooleanProperty silentrot = new BooleanProperty("Silent rotations", true);
    public final ModeProperty rotations = new ModeProperty("Rotation", "Basic", "Basic", "NCP", "AAC", "Custom", "Smooth", "idk");
    public final BooleanProperty raytrace = new BooleanProperty("Ray Trace", true);
    public final NumberProperty turnspeed = new NumberProperty("Rotation Speed", 180, 1, 180, 1, () -> rotations.is("Custom"));
    public final BooleanProperty jitter = new BooleanProperty("Jitter", true, () -> rotations.is("Custom"));
    public final BooleanProperty keepsprint = new BooleanProperty("Keep Sprints", true);
    public static final BooleanProperty thrwall = new BooleanProperty("Through Walls", true);
    public final ModeProperty attackevent = new ModeProperty("Attack On", "Pre", "Pre", "Post");

    public final BooleanProperty movecorrection = new BooleanProperty("Movement Correction", false);

    public final MultipleBoolProperty autodisable = new MultipleBoolProperty("Disable on", new BooleanProperty("Death", true), new BooleanProperty("World Change", true));
    public TimerUtils hittimer = new TimerUtils();
    public TimerUtils rotatetimer = new TimerUtils();

    public static List<EntityLivingBase> targets = new ArrayList<>();

    public static EntityLivingBase targetmob, dog;

    public float yaw, pitch, nigga;
    public static boolean isattacking = false;
    private boolean blocking = false;
    public boolean readyToAttack = false;

    public final EventListener<EventTick> onTick = e -> this.setSuffix(mode.getMode());

    public static EntityLivingBase getEntityInRange(double range) {
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (mc.thePlayer.getDistanceToEntity(entity) <= range) {
                if (entity instanceof EntityLivingBase && canattack((EntityLivingBase) entity)) {
                    if (!thrwall.isEnabled() && !mc.thePlayer.canEntityBeSeen(entity)) {
                        return null;
                    }
                    return (EntityLivingBase) entity;
                }
            }
        }
        return null;
    }

    public static boolean canattack(EntityLivingBase elb) {
        if (target.getSetting("Players").isEnabled() && elb instanceof EntityPlayer && elb.isEntityAlive() && !Objects.equals(elb.getName(), mc.thePlayer.getName())) {
            if (!Objects.equals(elb.getName(), mc.thePlayer.getName())) {
                return (!elb.isInvisible() || target.getSetting("Invisibles").isEnabled()) && (!elb.isOnSameTeam(mc.thePlayer) || !target.getSetting("Ignore teammates").isEnabled());
            }
        }
        if (target.getSetting("Animals").isEnabled() && elb instanceof EntityAnimal && elb.isEntityAlive() || target.getSetting("Animals").isEnabled() && elb instanceof EntitySquid && elb.isEntityAlive()) {
            if (!Objects.equals(elb.getName(), mc.thePlayer.getName())) {
                if (!Objects.equals(elb.getName(), mc.thePlayer.getName())) {
                    return !elb.isInvisible() || target.getSetting("Invisibles").isEnabled();
                }
                return true;
            }
        }
        if (target.getSetting("Mobs").isEnabled() && elb instanceof EntityMob && elb.isEntityAlive() || target.getSetting("Mobs").isEnabled() && elb instanceof EntityVillager && elb.isEntityAlive()) {
            if (!Objects.equals(elb.getName(), mc.thePlayer.getName())) {
                if (!Objects.equals(elb.getName(), mc.thePlayer.getName())) {
                    return !elb.isInvisible() || target.getSetting("Invisibles").isEnabled();
                }
                return true;
            }
        }
        if (target.getSetting("Ignore teammates").isEnabled() && elb.isEntityAlive() && !elb.isOnSameTeam(mc.thePlayer)) {
            if (!Objects.equals(elb.getName(), mc.thePlayer.getName())) {
                if (!Objects.equals(elb.getName(), mc.thePlayer.getName())) {
                    return !elb.isInvisible() || target.getSetting("Ignore teammates").isEnabled();
                }
                return true;
            }
        }
        return false;
    }

    public final EventListener<EventMotion> onEvent = e -> {
        int cps = RandomUtils.nextInt(mincps.getValue().intValue(), maxcps.getValue().intValue());
        int cpss = 1000 / cps + RandomUtils.nextInt(-1, 1);
        if (targetmob != null && mc.thePlayer.getDistanceToEntity(targetmob) >= range.getValue()) {
            targetmob = null;
        }
        if (mc.thePlayer != null || targetmob != null) {
            unblock();
        }
        targets.clear();
        if (mc.thePlayer != null && mc.thePlayer.isDead) {
            toggle(ToggleType.AUTO);
            NotificationManager.post(new Notification(NotificationType.WARNING, this.getName(), "Disabled " + this.getName().toLowerCase() + " due to death."));
        }
        if (getEntityInRange(range.getValue().floatValue()) != null) {
            targets.add(getEntityInRange(range.getValue()));
        }
        if (!targets.isEmpty() && targets.get(0) != null && mode.getMode().equals("Single")) {
            switch (sort.getMode()) {
                case "Range": targets.sort(Comparator.comparingDouble(mc.thePlayer::getDistanceToEntity)); break;
                case "Health": targets.sort(Comparator.comparingDouble(EntityLivingBase::getHealth)); break;
                case "Hurt time": targets.sort(Comparator.comparingInt(EntityLivingBase::getHurtTime)); break;
                case "Armor": targets.sort(Comparator.comparingInt(EntityLivingBase::getTotalArmorValue)); break;
            }
        }
        //Lettuce.INSTANCE.text(String.valueOf(targets.get(0).getName()));
        if (targets.size() > 0) {
            switch (mode.getMode()) {
                case "Single": targetmob = targets.get(0); break;
                case "Switch": {
                    if (targets.get(0) != null) {
                        targetmob = targets.get(RandomUtils.nextInt(0, targets.size() - 1));
                    }
                    break;
                }
            }
        }
        isattacking = targetmob != null && targetmob.getHealth() > 0 && mc.thePlayer.getDistanceToEntity(targetmob) <= range.getValue().floatValue();
        if (targetmob != null && hittimer.hasTimeElapsed(cpss) && mc.thePlayer.getDistanceToEntity(targetmob) <= range.getValue().floatValue()) {
            if (e.isPre() && attackevent.is("Post") || e.isPost() && attackevent.is("Pre")) return;
            mc.thePlayer.swingItem();
            AttackOrder.sendFixedAttack(mc.thePlayer, targetmob);
            hittimer.reset();
            if (targetmob.isDead) {
                targetmob = null;
            }
            readyToAttack = false;
        }
        if (targetmob != null && e.isPre()) {
            switch (rotations.getMode()) {
                case "Basic": {
                    float[] rotations = RotationUtils.getRotations(targetmob);
                    yaw = rotations[0];
                    pitch = rotations[1];
                    if (silentrot.isEnabled()) {
                        e.setYaw(yaw);
                        e.setPitch(pitch);
                        mc.thePlayer.rotationPitchHead = pitch;
                        mc.thePlayer.rotationYawHead = yaw;
                        mc.thePlayer.renderYawOffset = yaw;
                        rotatetimer.reset();
                        if (raytrace.isEnabled()) {
                            if (RotationUtils.isMouseOver(yaw, pitch, targetmob, mc.thePlayer.getDistanceToEntity(targetmob)))
                                readyToAttack = true;
                        }
                        if (!raytrace.isEnabled()) {
                            readyToAttack = true;
                        }

                    } else {
                        mc.thePlayer.rotationYaw = yaw;
                        mc.thePlayer.rotationPitch = pitch;
                    }
                }
                break;

                case "NCP": {
                    float[] rotations = RotationUtils.getRotations(targetmob);
                    yaw = rotations[0];
                    pitch = rotations[1];
                    if (silentrot.isEnabled()) {
                        e.setYaw(yaw);
                        e.setPitch(pitch);
                        mc.thePlayer.rotationPitchHead = pitch;
                        mc.thePlayer.rotationYawHead = yaw;
                        mc.thePlayer.renderYawOffset = yaw;
                        if (raytrace.isEnabled()) {
                            if (RotationUtils.isMouseOver(yaw, pitch, targetmob, mc.thePlayer.getDistanceToEntity(targetmob)))
                                readyToAttack = true;
                        }
                        if (!raytrace.isEnabled()) {
                            readyToAttack = true;
                        }
                    } else {
                        mc.thePlayer.rotationYaw = yaw;
                        mc.thePlayer.rotationPitch = pitch;
                    }
                }
                break;

                case "AAC": {
                    float[] rots = RotationUtils.getRotations(targetmob);
                    yaw = (float) (rots[0] + MathUtils.getRandomInRange(3, -3));
                    pitch = (float) (rots[1] + MathUtils.getRandomInRange(3, -3));
                    if (silentrot.isEnabled()) {
                        e.setYaw(yaw);
                        e.setPitch(pitch);
                        mc.thePlayer.rotationPitchHead = pitch;
                        mc.thePlayer.rotationYawHead = yaw;
                        mc.thePlayer.renderYawOffset = yaw;
                        if (raytrace.isEnabled()) {
                            if (RotationUtils.isMouseOver(yaw, pitch, targetmob, mc.thePlayer.getDistanceToEntity(targetmob)))
                                readyToAttack = true;
                        }
                        if (!raytrace.isEnabled()) {
                            readyToAttack = true;
                        }
                    } else {
                        mc.thePlayer.rotationYaw = yaw;
                        mc.thePlayer.rotationPitch = pitch;
                        readyToAttack = true;
                    }
                }
                break;

                case "Smooth": {
                    float[] rots = RotationUtils.getSmoothRotations(targetmob);
                    yaw = rots[0];
                    pitch = rots[1];
                    if (silentrot.isEnabled()) {
                        e.setYaw(yaw);
                        e.setPitch(pitch);
                        mc.thePlayer.rotationPitchHead = pitch;
                        mc.thePlayer.rotationYawHead = yaw;
                        mc.thePlayer.renderYawOffset = yaw;
                        if (raytrace.isEnabled()) {
                            if (RotationUtils.isMouseOver(yaw, pitch, targetmob, mc.thePlayer.getDistanceToEntity(targetmob)))
                                readyToAttack = true;
                        }
                        if (!raytrace.isEnabled()) {
                            readyToAttack = true;
                        }
                    } else {
                        mc.thePlayer.rotationYaw = yaw;
                        mc.thePlayer.rotationPitch = pitch;
                        readyToAttack = true;
                    }
                }
                break;

                case "Custom": {
                    float[] rotations = RotationUtils.getRotations(targetmob);
                    float[] donerots = RotationUtils.changerotwithgivenspeed(e.getPrevYaw(), e.getPrevPitch(), rotations[0], rotations[1], turnspeed.getValue().floatValue());
                    float jiter = jitter.isEnabled() ? RandomUtils.nextFloat(-0.5f, 0.5f) : 0;
                    yaw = donerots[0] + jiter;
                    pitch = donerots[1] + jiter;
                    if (silentrot.isEnabled()) {
                        e.setYaw(yaw);
                        e.setPitch(pitch);
                        mc.thePlayer.rotationPitchHead = pitch;
                        mc.thePlayer.rotationYawHead = yaw;
                        mc.thePlayer.renderYawOffset = yaw;

                    } else {
                        mc.thePlayer.rotationYaw = yaw;
                        mc.thePlayer.rotationPitch = pitch;
                        readyToAttack = true;
                    }
                }
                break;
            }
            nigga = e.getYaw();
            block();
        }
        if (targetmob != null) {
            if (raytrace.isEnabled()) {
                if (RotationUtils.isMouseOver(e.getYaw(), e.getPitch(), targetmob, mc.thePlayer.getDistanceToEntity(targetmob)))
                    readyToAttack = true;
            }
            if (!raytrace.isEnabled()) {
                readyToAttack = true;
            }
        }
    };

    public final EventListener<EventMoveUpdate> onMove = event -> {
        if (movecorrection.isEnabled()) {
            event.setYaw(nigga);
        }
    };
    public final EventListener<EventMoveUpdate> onJumpfix = event -> {
        if (movecorrection.isEnabled()) {
            event.setYaw(nigga);
        }
    };

    public final EventListener<EventWorld> onWorld = event -> {
        if (autodisable.getSetting("World Change").isEnabled()) {
            toggle(ToggleType.AUTO);
            NotificationManager.post(NotificationType.WARNING, this.getName(), "Disabled kill aura due to world change.");
        }
    };

    public void block() {
        if (isattacking && autoblock.isEnabled() && mc.thePlayer.inventory.getCurrentItem() != null) {
            if (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                switch (abm.getMode()) {
                    case "Vanilla": {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                    }
                    break;

                    case "Grim": {
                        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9));
                        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    }
                    break;
                }
            }
            blocking = true;
        }
    }

    public void unblock() {
        if (blocking) {
            switch (abm.getMode()) {
                case "Grim":
                case "Vanilla":
                    PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    break;
            }
        }
        blocking = false;
    }

    @Override
    public void onDisable() {
        isattacking = false;
        targetmob = null;
        blocking = false;
    }

    public KillAura() {
        super("Kill Aura", Category.COMBAT, "Automatically hits targets in range.");
        this.addProperties(mode, thrwall);
        this.addProperties(range);
        this.addProperties(mincps, maxcps);
        this.addProperties(keepsprint);
        this.addProperties(target, sort);
        this.addProperties(autoblock);
        this.addProperties(abm);
        this.addProperties(silentrot, rotations, movecorrection, raytrace);
        this.addProperties(turnspeed);
        this.addProperties(jitter);
        this.addProperties(attackevent);
        this.addProperties(autodisable);
    }
}