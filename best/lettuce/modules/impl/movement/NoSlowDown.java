package best.lettuce.modules.impl.movement;

import best.lettuce.event.Event;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.event.impl.network.EventPacketSend;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.event.impl.player.EventNoSlow;
import best.lettuce.event.impl.player.EventUpdate;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.impl.combat.KillAura;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.utils.packet.PacketUtils;
import best.lettuce.utils.player.MoveUtils;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Mouse;

import java.util.Objects;

public class NoSlowDown extends Module {
    public NoSlowDown() {
        super("No Slow Down", Category.MOVEMENT, "Cancels the slow down effect when using items.");
        addProperties(mode, sword, bow, food, potion);
    }

    public final ModeProperty mode = new ModeProperty("Mode", "Cancel", "Cancel", "Grim", "NCP", "Watchdog", "Matrix", "Test");
    public final BooleanProperty sword = new BooleanProperty("Sword", true);
    public final BooleanProperty bow = new BooleanProperty("Bow", true);
    public final BooleanProperty food = new BooleanProperty("Food", true);
    public final BooleanProperty potion = new BooleanProperty("Potion", true);

    public boolean blocking;
    public boolean canSlow = true;
    public int slot;

    public boolean enabled = false, should_send_block_placement = false;

    public final EventListener<EventNoSlow> onSlow = Event::cancel;

    public final EventListener<EventTick> onTick = e -> this.setSuffix(mode.getMode());

    public final EventListener<EventMotion> onMotion = event -> {
        C07PacketPlayerDigging C07 = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
        C08PacketPlayerBlockPlacement C08 = new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem());
        if (mc.thePlayer.isUsingItem()) {
            slot = mc.thePlayer.inventory.currentItem;
            canSlow = mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && !sword.isEnabled() || mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && !bow.isEnabled() || mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && !food.isEnabled() || mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !potion.isEnabled();
            switch (mode.getMode()) {
                case "Grim": {
                    if (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                        PacketUtils.sendPacket(new C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9));
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    } else if (event.isPre() &&  mc.thePlayer.getHeldItem().getItem() instanceof ItemSword || mc.gameSettings.keyBindUseItem.isKeyDown()) PacketUtils.sendPacket(C08);
                    break;
                }
                case "NCP": {
                    if (event.isPre() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                        PacketUtils.sendPacket(C07);
                    } else {
                        PacketUtils.sendPacket(C08);
                    }
                }
                case "Watchdog": {
                    if (mc.thePlayer.isUsingItem()) {
                        if (mc.thePlayer.ticksExisted % 3 == 0) {
                            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        } else {
                            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                        }
                    }
                    if (Objects.requireNonNull(event.getType()) == Event.Type.PRE) {
                        if (should_send_block_placement) {
                            float yaw = mc.thePlayer.rotationYaw;
                            int[] aimInfo = mc.thePlayer.getPlayerAimInfo(yaw, 90, 4.5);
                            if (aimInfo[0] == 2) {
                                mc.playerController.placeBlock(mc.thePlayer.inventory.currentItem, aimInfo[2], aimInfo[3], aimInfo[4], 2, aimInfo[5], aimInfo[6], aimInfo[7]);
                            }
                            this.should_send_block_placement = false;
                        }

                        if (!this.enabled && mc.thePlayer.isUsingItem() && !this.isHoldingSword()) {
                            this.enabled = true;
                            int x = (int) mc.thePlayer.posX, y = (int) mc.thePlayer.posY, z = (int) mc.thePlayer.posZ;
                            int[] aimInfo = mc.thePlayer.getRightMouseOver();
                            if (aimInfo[0] == 2) {
                                mc.playerController.placeBlock(mc.thePlayer.inventory.currentItem, aimInfo[2], aimInfo[3], aimInfo[4], 2, aimInfo[5], aimInfo[6], aimInfo[7]);
                            } else {
                                event.setPitch(90);
                                this.should_send_block_placement = true;
                            }

                            mc.playerController.placeBlock(2, x, y, z, 2, x, y, z);
                        } else if (this.enabled) {
                            if (!mc.thePlayer.isUsingItem()) {
                                PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                                PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                this.enabled = false;
                            }
                        }
                    }
                    break;
                }
                case "Test": {
                    if (MoveUtils.isMoving() && mc.thePlayer.isUsingItem()) {
                        if (event.isPre()) {
                            PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        } else {
                            PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                        }
                    }
                    break;
                }
            }
        }
    };

    public final EventListener<EventUpdate> onUpdate = event -> {
        if (mode.is("Watchdog")) {
            if (mc.thePlayer.isUsingItem() || KillAura.targetmob != null) {
                if (this.isHoldingSword()) {
                    if ((mc.thePlayer.ticksExisted & 2) == 0) {
                        PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                        PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    }
                }

                if (this.isHoldingSword()) {
                    if (mc.thePlayer.isUsingItem()) {
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement());
                        this.blocking = true;
                    }
                }
            } else if (this.blocking) {
                PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                this.blocking = false;
            }
        }
    };

    public final EventListener<EventPacketReceive> onReceive = e -> {
        if (mode.is("Matrix")) {
            if (e.getPacket() instanceof S09PacketHeldItemChange) {
                S09PacketHeldItemChange s09 = (S09PacketHeldItemChange) e.getPacket();
                e.setCancelled(true);
                if (mc.theWorld == null || mc.thePlayer == null) return;
                PacketUtils.sendPacket(new C09PacketHeldItemChange(s09.getHeldItemHotbarIndex()));
                PacketUtils.sendPacket(new C09PacketHeldItemChange(slot));
            }
        }
    };

    public final EventListener<EventPacketSend> onSend = e -> {
        if (mode.is("Watchdog")) {
            if (this.enabled && e.getPacket() instanceof C07PacketPlayerDigging) {
                e.cancel();
            } else if (Mouse.isButtonDown(mc.gameSettings.keyBindUseItem.getKeyCode()) && !this.enabled && e.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                Item item = mc.thePlayer.inventory.getCurrentItem().getItem();
                if (item instanceof ItemFood) {
                    e.cancel();
                }
            }
        }
    };

    public boolean isHoldingSword() {
        if (mc.thePlayer.getHeldItem().getItem() != null) {
            return mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
        }
        return false;
    }

    @Override
    public void onEnable() {
        enabled = false;
    }

    @Override
    public void onDisable() {
        canSlow = false;
    }
}