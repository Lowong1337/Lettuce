package best.lettuce.modules.impl.movement;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventFlag;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.game.EventWorld;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.event.impl.network.EventPacketSend;
import best.lettuce.event.impl.player.EventCollide;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.event.impl.player.EventMove;
import best.lettuce.gui.notification.Notification;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.MultipleBoolProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.math.TimerUtils;
import best.lettuce.utils.packet.PacketUtils;
import best.lettuce.utils.player.MoveUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.Objects;

public class Fly extends Module {
    public Fly() {
        super("Fly", Category.MOVEMENT, "Allows you to walk in the air.");
        addProperties(mode, speed, autodisable);
    }

    public final ModeProperty mode = new ModeProperty("Mode", "Vanilla", "Vanilla", "Creative", "Airwalk", "Verus", "Verus 2", "4FMC");
    public final NumberProperty speed = new NumberProperty("Speed", 5, 1, 10, 0.1, () -> mode.getMode().equalsIgnoreCase("vanilla"));
    public final MultipleBoolProperty autodisable = new MultipleBoolProperty("Disable on", new BooleanProperty("Death", true), new BooleanProperty("Flag", true), new BooleanProperty("World change", true));

    public final EventListener<EventTick> onTick = e -> {
        this.setSuffix(mode.getMode());
    };

    public boolean isHurted;
    double y;
    double launchY;
    TimerUtils gay = new TimerUtils();

    public final EventListener<EventCollide> onCollide = e -> {
        if (mc.thePlayer == null) return;

        if (mode.isEither("Airwalk", "4FMC") && e.getBlock() instanceof BlockAir && e.getY() <= launchY) {
            e.setAxisAlignedBB(AxisAlignedBB.fromBounds(e.getX(), e.getY(), e.getZ(), e.getX() + 1, launchY, e.getZ() + 1));
        }
    };

    public final EventListener<EventMove> onMove = e -> {
        if (mode.is("4FMC")) {
            PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ), 1, null, 0f, 1f, 0f));
        }
    };

    public final EventListener<EventMotion> onMotion = e -> {
        switch (mode.getMode()) {
            case "Vanilla" -> {
                mc.thePlayer.motionX = 0.0;
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.motionZ = 0.0;
                MoveUtils.strafe(speed.getValue().floatValue());
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.thePlayer.motionY += speed.getValue().floatValue();
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.thePlayer.motionY -= speed.getValue().floatValue();
                }
            }
            case "Creative" -> mc.thePlayer.capabilities.isFlying = true;
            case "Verus" -> {
                //PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY -mc.thePlayer.fallDistance, mc.thePlayer.posZ, true));
                mc.thePlayer.motionX = 0.0;
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.motionZ = 0.0;
                if (mc.thePlayer.hurtTime > 0) {
                    isHurted = true;
                }
                if (isHurted) {
                    mc.thePlayer.fallDistance = 0;
                    MoveUtils.strafe(0.25f);
                    mc.thePlayer.onGround = true;
                    e.setOnGround(false);
                }
            }
            case "Verus 2" -> {
                mc.thePlayer.setSprinting(false);
                if (mc.thePlayer.hurtTime > 0) {
                    isHurted = true;
                }
                MoveUtils.strafe(0.3f);
                if (mc.thePlayer.onGround) {
                    y = mc.thePlayer.posY;
                }
                if (mc.thePlayer.posY <= y) {
                    mc.thePlayer.jump();
                    //PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(e.getX(), e.getY(), e.getZ(), false));
                }
                //PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(e.getX(), e.getY(), e.getZ(), true));
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    if (gay.hasTimeElapsed(200)) {
                        y += 1;
                        gay.reset();
                    }
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    if (gay.hasTimeElapsed(200)) {
                        y -= 1;
                        gay.reset();
                    }
                }
            }
            case "4FMC" -> e.setYaw(90f);
        }
    };

    public static int getBowSlot() {
        for (int i = 0; i < 9; i++) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBow && itemStack.stackSize > 0) {
                return i;
            }
        }
        return -1;
    }

    /*public boolean havearrow() {
        for (int i = 0; i <= mc.thePlayer.inventory.getSizeInventory()){
            final ItemStack is = mc.thePlayer.inventory.mainInventory[i];
            if(is != null && is.getItem() instanceof )
        }
    }*/

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

    public final EventListener<EventPacketReceive> onPacket = event -> {
        if (mc.thePlayer != null) {
            if (mc.thePlayer.isDead && autodisable.getSetting("Death").isEnabled()) {
                toggle(ToggleType.AUTO);
                NotificationManager.post(new Notification(NotificationType.WARNING, this.getName(), "Disabled " + this.getName().toLowerCase() + " due to death."));
            }
        }
        switch (mode.getMode()) {
            case "Verus" -> {
                if (isHurted) {
                    if (event.getPacket() instanceof C03PacketPlayer packet) {
                        C03PacketPlayer.C04PacketPlayerPosition packet2 = (C03PacketPlayer.C04PacketPlayerPosition) event.getPacket();
                        packet.setOnGround(true);
                        packet2.setOnGround(true);
                    }
                }
                if (event.getPacket() instanceof S32PacketConfirmTransaction) {
                    event.setCancelled(true);
                }
            }
            case "Verus 2" -> {
                if (event.getPacket() instanceof S32PacketConfirmTransaction || event.getPacket() instanceof S00PacketKeepAlive) {
                    event.setCancelled(true);
                    //Lettuce.INSTANCE.text("cancelled " + event.getPacket());
                }
                if (event.getPacket() instanceof C03PacketPlayer packet) {
                    C03PacketPlayer.C04PacketPlayerPosition packet2 = (C03PacketPlayer.C04PacketPlayerPosition) event.getPacket();
                    packet.setOnGround(true);
                    packet2.setOnGround(true);
                }
            }
        }
    };

    public final EventListener<EventPacketSend> onPacketSend = event -> {
        switch (mode.getMode()) {
            case "Verus 2" -> {
                if (event.getPacket() instanceof C03PacketPlayer c03) {
                    c03.setOnGround(true);
                }
                if (event.getPacket() instanceof C16PacketClientStatus) {
                    event.setCancelled(true);
                }
            }
        }
    };

    @Override
    public void onEnable() {
        launchY = mc.thePlayer.posY;
        switch (mode.getMode()) {
            case "Verus" -> {
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 4.3, mc.thePlayer.posZ, true));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
            }
            case "Verus 2" -> {
                y = mc.thePlayer.posY;
            }
        }
    }

    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.thePlayer.capabilities.isFlying = false;
            if (!Objects.equals(mode.getMode(), "Verus 2")) {
                mc.thePlayer.motionX = 0.0;
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.motionZ = 0.0;
            }
            isHurted = false;
        }
    }
}
