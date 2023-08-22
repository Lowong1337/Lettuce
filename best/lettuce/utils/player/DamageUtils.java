package best.lettuce.utils.player;

import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.packet.PacketUtils;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Random;

public class DamageUtils implements MinecraftInstance {
    public static void positionalDamage(PositionType type, boolean groundCheck, boolean hurtTimeCheck, int packets) {
        if (mc.thePlayer == null || (groundCheck && !mc.thePlayer.onGround) || (hurtTimeCheck && mc.thePlayer.hurtTime > 0)) return;

        if (packets < 1) packets = (int) Math.ceil(getRequiredFallDistance());
        double height = getRequiredFallDistance()/packets;

        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;

        for (int i = 0; i < packets; i++) {
            switch (type) {
                case CO4 -> {
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y + height, z, false));
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                }
                case CO6 -> {
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(x, y + height, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                }
            }
        }

        PacketUtils.sendPacketNoEvent(new C03PacketPlayer(true));
    }

    public static void bowDamage(EventMotion e, int tick) {
        if (mc.thePlayer == null) return;

        int currentSlot = mc.thePlayer.inventory.currentItem;
        int bowSlot = InventoryUtils.getItemSlot(Items.bow);
        int arrowSlot = InventoryUtils.findItemInInventory(Items.arrow);

        if (bowSlot == -1 || arrowSlot == -1) return;

        e.setPitch(-(89f + new Random().nextFloat()));

        switch (tick) {
            case 1 -> PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(bowSlot));
            case 3 -> PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(InventoryUtils.getStackInSlot(bowSlot + 36)));
            case 6 -> PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            case 10 -> PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(currentSlot));
        }
    }

    public static double getRequiredFallDistance() {
        return 3.1 + (mc.thePlayer.isPotionActive(Potion.jump)
                        ? mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1.0
                        : 0.0);
    }

    public enum PositionType {
        CO4, CO6
    }
}
