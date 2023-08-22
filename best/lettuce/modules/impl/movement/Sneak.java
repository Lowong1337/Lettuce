package best.lettuce.modules.impl.movement;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.network.EventPacketSend;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.utils.packet.PacketUtils;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public class Sneak extends Module {
    public final ModeProperty modes = new ModeProperty("Modes", "Vanilla", "Vanilla", "NCP");

    public Sneak() {
        super("Sneak", Category.MOVEMENT, "Sneak");
        addProperties(modes);
    }

    private boolean snuck = false;

    @Override
    public void onEnable() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (modes.is("Vanilla")) {
            PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (modes.is("Vanilla")) {
            PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
        }
        snuck = false;
    }

    public final EventListener<EventPacketSend> onSend = event -> {
        if (event.getPacket() instanceof C0BPacketEntityAction packet) {
            if (packet.getAction().equals(C0BPacketEntityAction.Action.START_SNEAKING) && modes.is("Vanilla") && !snuck) {
                snuck = true;
            } else if (packet.getAction().equals(C0BPacketEntityAction.Action.START_SNEAKING) || packet.getAction().equals(C0BPacketEntityAction.Action.STOP_SNEAKING)) {
                event.cancel();
            }
        }
    };

    public final EventListener<EventMotion> onMotion = event -> {
        if (modes.is("NCP")) {
            if (event.isPre()) {
                PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
            } else {
                PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
            }
        }
    };
}
