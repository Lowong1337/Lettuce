package best.lettuce.modules.impl.movement;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventFlag;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.game.EventWorld;
import best.lettuce.event.impl.network.EventPacketSend;
import best.lettuce.event.impl.player.EventMove;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.event.impl.player.EventStrafe;
import best.lettuce.gui.notification.Notification;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.impl.combat.KillAura;
import best.lettuce.modules.impl.combat.TargetStrafe;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.MultipleBoolProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.math.TimerUtils;
import best.lettuce.utils.packet.PacketUtils;
import best.lettuce.utils.player.InventoryUtils;
import best.lettuce.utils.player.MoveUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class Speed extends Module {
    public Speed() {
        super("Speed", Category.MOVEMENT, "Allows you to move faster.");
        this.addProperties(mode, speed, autojump,timerboost, downmotion, smoothCamera, autodisable, experimentalStrafe);
    }

    public final ModeProperty mode = new ModeProperty("Mode", "Vanilla", "Vanilla", "Legit", "Headhitter", "AAC", "NCP", "Verus", "Vulcan", "Vulcan Exploit", "Watchdog", "Matrix Jump Boost", "Matrix Timer", "Test");
    public final NumberProperty speed = new NumberProperty("Speed", 1f, 0.1f, 10f, 0.1f, () -> mode.is("Vanilla"));
    public final BooleanProperty autojump = new BooleanProperty("Auto Jump", true, () -> mode.is("Vanilla"));
    public final NumberProperty timerboost = new NumberProperty("Timer Boost", 1.0, 1.0, 1.5, 0.01, () -> mode.getMode().equals("NCP"));
    public final BooleanProperty downmotion = new BooleanProperty("Down Motion", false, () -> mode.getMode().equals("NCP"));
    public final BooleanProperty smoothCamera = new BooleanProperty("Smooth Camera", false);
    public final MultipleBoolProperty autodisable = new MultipleBoolProperty("Disable on", new BooleanProperty("Death", true), new BooleanProperty("Flag", true), new BooleanProperty("World change", true));

    public final BooleanProperty experimentalStrafe = new BooleanProperty("Strafe Check Test", true);

    public final EventListener<EventTick> onTick = e -> {
        this.setSuffix(mode.getMode());
    };

    State state;
    private boolean mpj_flip = false;
    int currentSlot = -1;
    int blockSlot = -1;
    int lastPacket = 0;
    double startY = 0.0;

    public final TimerUtils timer = new TimerUtils();

    @Override
    public void onEnable() {
        startY = mc.thePlayer.posY;
        currentSlot = mc.thePlayer.inventory.currentItem;
        if (mode.is("Vulcan Exploit")) {
            blockSlot = InventoryUtils.getBlockSlot();

            if (blockSlot == -1) {
                toggle(ToggleType.AUTO);
                NotificationManager.post(NotificationType.INFO, "Error", "You need a block in your hot bar in order to use this speed mode.", 1);
            }
            if (blockSlot != -1) {
                PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(blockSlot));
            }
        }
    }

    public final EventListener<EventWorld> onWorld = e -> {
        if (autodisable.getSetting("World change").isEnabled()) {
            toggle(ToggleType.AUTO);
            NotificationManager.post(new Notification(NotificationType.WARNING, this.getName(), "Disabled " + this.getName().toLowerCase() + " due to world change"));
        }
    };

    public final EventListener<EventFlag> onFlag = e -> {
        if (autodisable.getSetting("Flag").isEnabled()) {
            toggle(ToggleType.AUTO);
            NotificationManager.post(new Notification(NotificationType.WARNING, this.getName(), "Disabled " + this.getName().toLowerCase() + " to reduce flag"));
        }
    };

    public final EventListener<EventPacketSend> onPacketSend = e -> {
        if (mode.is("Vulcan Exploit") && e.getPacket() instanceof C09PacketHeldItemChange c09) {
            currentSlot = c09.getSlotId();
            e.cancel();
        }
    };

    public final EventListener<EventStrafe> onStrafe = e -> {
        switch (mode.getMode()) {
            case "Test" -> {
                final S12PacketEntityVelocity s12 = new S12PacketEntityVelocity(mc.thePlayer);
                final float xyz = (float) Math.atan(s12.getMotionX() * s12.getMotionZ());
                final float xyz2 = (float) Math.atan(mc.thePlayer.motionX * mc.thePlayer.motionZ);
                if (mc.thePlayer.onGround) {
                    if (MoveUtils.isMoving()) {
                        mc.thePlayer.jump();
                        MoveUtils.setMotion(MoveUtils.getSpeed() * 1.24F);
                    }
                }
            }
            case "Headhitter" -> {
                if (MoveUtils.isMoving()) {
                    if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    } else {
                        switch (mc.thePlayer.offGroundTicks) {
                            case 0 -> {
                                mc.thePlayer.jump();
                                mc.thePlayer.motionY = 0.2f;
                            }
                            case 1 -> mc.thePlayer.motionY = MoveUtils.HEAD_HITTER_MOTION;
                        }
                    }
                }
            }
            case "Vulcan Exploit" -> {
                if (MoveUtils.isMoving() && blockSlot > -1) {
                    if (lastPacket < mc.thePlayer.ticksExisted) {
                        lastPacket = mc.thePlayer.ticksExisted + 2;
                        PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(mc.thePlayer).down(), EnumFacing.UP.getIndex(), InventoryUtils.getStackInSlot(blockSlot + 36), 0f, 1f, 0f));
                    }

                    int speed = mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0;
                    switch (mc.thePlayer.offGroundTicks) {
                        case 0 -> {
                            switch (speed) {
                                case 0 -> MoveUtils.strafe(0.55f);
                                case 1 -> MoveUtils.strafe(0.65f - 0.11f);
                                default -> MoveUtils.strafe(0.85f - 0.18f);
                            }

                            mc.thePlayer.motionY = 0.2f;
                        }
                        case 1 -> {
                            mc.thePlayer.motionY = MoveUtils.HEAD_HITTER_MOTION;

                            switch (speed) {
                                case 0 -> MoveUtils.strafe(0.45f - 0.02f);
                                case 1 -> MoveUtils.strafe(0.6f - 0.11f);
                                default -> MoveUtils.strafe(0.75f - 0.18f);
                            }
                        }
                        case 2 -> {
                            switch (speed) {
                                case 0 -> MoveUtils.strafe(0.4f - 0.03f);
                                case 1 -> MoveUtils.strafe(0.55f - 0.11f);
                                default -> MoveUtils.strafe(0.65f - 0.18f);
                            }
                        }
                    }
                }
            }
        }
    };

    public final EventListener<EventMotion> onMotion = event -> {
        //KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        if (mc.thePlayer != null) {
            if (mc.thePlayer.getHealth() <= 0.1 && autodisable.getSetting("Death").isEnabled()) {
                toggle(ToggleType.AUTO);
                NotificationManager.post(new Notification(NotificationType.WARNING, this.getName(), "Disabled " + this.getName().toLowerCase() + " due to death"));
            }
        }

        if (event.isPre()) {
            EntityPlayer.enableCameraYOffset = false;

            if (mc.thePlayer.posY > startY && smoothCamera.isEnabled()) {
                EntityPlayer.enableCameraYOffset = true;
                EntityPlayer.cameraYPosition = startY;
            }

            if (mc.thePlayer.fallDistance > 0) {
                mpj_flip = true;
            }

            if (!mc.theWorld.getCollisionBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -0.2, 0.0)).isEmpty() && mpj_flip) {
                mpj_flip = false;
            }
        }

        switch (mode.getMode()) {
            case "Vanilla" -> {
                if (MoveUtils.isMoving()) {
                    if(mc.thePlayer.onGround && autojump.isEnabled()){
                        mc.thePlayer.jump();
                    }
                    MoveUtils.strafe(speed.getValue().floatValue());
                } else {
                    MoveUtils.strafe(0f);
                }
            }
            case "NCP" -> {
                if (MoveUtils.isMoving()) {
                    mc.timer.timerSpeed = timerboost.getValue().floatValue();
                    if (event.isOnGround()) {
                        mc.thePlayer.jump();
                    }
                    if (mc.thePlayer.motionY <= 0.2 && downmotion.isEnabled() && mc.thePlayer.fallDistance < 1) {
                        mc.thePlayer.motionY -= 0.05;
                    }
                    MoveUtils.strafe(0.25f);
                }
            }
            case "Verus" -> {
                if (MoveUtils.isMoving()) {
                    mc.timer.timerSpeed = 1f;
                    MoveUtils.strafe(0.3f);
                    if (event.isOnGround()) {
                        mc.thePlayer.jump();
                    }
                }
            }
            case "Vulcan" -> {
                if (MoveUtils.isMoving()) {
                    if (KillAura.isattacking) {
                        mc.thePlayer.setSprinting(false);
                        if (event.isOnGround()) {
                            MoveUtils.strafe(0.4f);
                            mc.thePlayer.jump();
                        }
                        if (!event.isOnGround() && mc.thePlayer.moveStrafing != 0) {
                            MoveUtils.strafe(0.22f);
                        }
                        if (mc.thePlayer.motionY <= 0.05 && mc.thePlayer.fallDistance < 1 && (mc.theWorld.getBlockState(new BlockPos(event.getX(), event.getY() + 2, event.getZ())).getBlock() instanceof BlockAir)) {
                            mc.thePlayer.motionY -= 0.06;
                        }
                    } else {
                        if (event.isOnGround()) {
                            MoveUtils.strafe(0.28f);
                            mc.thePlayer.jump();
                        }
                        if (!event.isOnGround() && mc.thePlayer.moveStrafing != 0) {
                            MoveUtils.strafe(0.22f);
                        }
                        if (mc.thePlayer.motionY <= 0.05 && mc.thePlayer.fallDistance < 1 && (mc.theWorld.getBlockState(new BlockPos(event.getX(), event.getY() + 2, event.getZ())).getBlock() instanceof BlockAir)) {
                            mc.thePlayer.motionY -= 0.06;
                        }
                    }
                }
            }
            case "AAC" -> {
                if (MoveUtils.isMoving()) {
                    if (event.isOnGround()) {
                        mc.thePlayer.jump();
                    }
                    if (!event.isOnGround()) {
                        state = State.Jumping;
                    }
                    if (state == State.Jumping) {
                        mc.timer.timerSpeed = 0.2f;
                        if (mc.thePlayer.fallDistance >= 0.5F) {
                            this.state = State.Falling;
                        }
                    }
                    if (state == State.Falling) {
                        mc.timer.timerSpeed = 1.5f;
                        if (mc.thePlayer.onGround) {
                            this.state = State.None;
                        }
                    }
                }
            }
            case "Legit" -> {
                mc.gameSettings.keyBindJump.setPressed(true);
            }
            case "Watchdog" -> {
                if (mc.thePlayer.onGround && MoveUtils.isMoving()) {
                    mc.thePlayer.jump();
                    MoveUtils.strafe(0.48f);
                }
                event.setYaw(getMovementDirection(mc.thePlayer.moveForward, mc.thePlayer.moveStrafing, mc.thePlayer.rotationYaw));
            }
            case "Matrix Jump Boost" -> {
                if (event.isPre() && MoveUtils.isMoving() && !mc.theWorld.getCollisionBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0)).isEmpty() && !mpj_flip) {
                    mc.thePlayer.onGround = true;
                    mc.thePlayer.jump();
                    mc.thePlayer.jumpMovementFactor = 0.026423f;
                }
            }
            case "Matrix Timer" -> {
                if (!MoveUtils.isMoving() || event.isPost()) return;

                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                }
                if (mc.thePlayer.fallDistance <= 0.22) {
                    mc.timer.timerSpeed = 3f;
                    mc.thePlayer.jumpMovementFactor = 0.026423f;
                } else if (mc.thePlayer.fallDistance < 1.25f) {
                    mc.timer.timerSpeed = 0.47f;
                }
            }
        }
    };

    private final EventListener<EventMove> onMove = TargetStrafe::strafe;

    @Override
    public void onDisable() {
        EntityPlayer.enableCameraYOffset = false;
        blockSlot = -1;
        mc.timer.timerSpeed = 1f;
        if (mode.is("Vulcan Exploit")) {
            PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(currentSlot));
        }
    }

    public enum State {
        None, Jumping, Falling
    }

    public float getMovementDirection(float forward, float strafing, float yaw) {
        if (forward == 0.0F && strafing == 0.0F) return yaw;
        boolean reversed = (forward < 0.0F);
        float strafingYaw = 90.0F * ((forward > 0.0F) ? 0.5F : (reversed ? -0.5F : 1.0F));
        if (reversed) yaw += 180.0F;
        if (strafing > 0.0F) {
            yaw -= strafingYaw;
        } else if (strafing < 0.0F) {
            yaw += strafingYaw;
        }
        return yaw;
    }
}
