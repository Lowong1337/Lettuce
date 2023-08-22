package best.lettuce.modules.impl.combat;

import best.lettuce.Lettuce;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.event.impl.network.EventPacketSend;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;

import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;

public class Velocity extends Module {

    private final ModeProperty mode = new ModeProperty("Mode", "Percentage", "Percentage", "Reverse", "Matrix", "Grim", "Watchdog");
    private final NumberProperty horizontal = new NumberProperty("Horizontal", 0, 0, 100, 1, () -> mode.is("Percentage"));
    private final NumberProperty vertical = new NumberProperty("Vertical", 0, 0, 100, 1, () -> mode.is("Percentage"));
    private final NumberProperty chance = new NumberProperty("Chance", 100, 0, 100, 1);
    private final BooleanProperty onlyWhileMoving = new BooleanProperty("Only while moving", false);
    private final BooleanProperty explosions = new BooleanProperty("Explosions", true);
    private final BooleanProperty debug = new BooleanProperty("Debug", true);
    private final BooleanProperty staffCheck = new BooleanProperty("Staff check", false, () -> mode.is("Percentage"));
    private final BooleanProperty airVelo = new BooleanProperty("Air Velocity", true, () -> mode.is("Watchdog"));

    private long lastDamageTimestamp, lastAlertTimestamp;

    public int cancell = 6, rps = 8, grimct = 0, udate = 0;

    public Velocity() {
        super("Velocity", Category.COMBAT, "Reduces your knockback");
        this.addProperties(mode, horizontal, vertical, chance, onlyWhileMoving, explosions, debug, staffCheck, airVelo);
    }

    public final EventListener<EventPacketSend> onSend = e -> {
        if (mode.is("C0F Cancel")) {
            if (e.getPacket() instanceof C0FPacketConfirmTransaction && mc.thePlayer.hurtTime > 0) {
                e.cancel();
            }
        }
    };

    public final EventListener<EventTick> onTick = e -> this.setSuffix(mode.getMode());

    public final EventListener<EventPacketReceive> onReceive = e -> {
        //if ((onlyWhileMoving.isEnabled() && !MovementUtils.isMoving()) || (chance.getValue() != 100 && MathUtils.getRandomInRange(0, 100) > chance.getValue())) return;
        Packet<?> packet = e.getPacket();
        switch (mode.getMode()) {
            case "Percentage" -> {
                if (packet instanceof S12PacketEntityVelocity s12) {
                    if (mc.thePlayer != null && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                        if (cancel(e)) return;
                        s12.motionX *= horizontal.getValue() / 100.0;
                        s12.motionZ *= horizontal.getValue() / 100.0;
                        s12.motionY *= vertical.getValue() / 100.0;
                    }
                } else if (packet instanceof S27PacketExplosion s27) {
                    if (cancel(e) || !explosions.isEnabled()) return;
                    s27.motionX *= horizontal.getValue() / 100.0;
                    s27.motionZ *= horizontal.getValue() / 100.0;
                    s27.motionY *= vertical.getValue() / 100.0;
                }
            }
            case "Reverse" -> {
                if (packet instanceof S12PacketEntityVelocity s12) {
                    if (mc.thePlayer != null && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                        if (cancel(e)) return;
                        s12.motionX *= -1;
                        s12.motionZ *= -1;
                    }
                } else if (packet instanceof S27PacketExplosion s27) {
                    if (cancel(e) || !explosions.isEnabled()) return;
                    s27.motionX *= -1;
                    s27.motionZ *= -1;
                    s27.motionY *= -1;
                }
            }
            case "Grim" -> {
                if(mc.thePlayer != null /*&& mc.thePlayer.onGround*/) {
                    if (e.getPacket() instanceof S12PacketEntityVelocity) {
                        e.setCancelled(true);
                        grimct = cancell;
                    }
                    if (e.getPacket() instanceof S32PacketConfirmTransaction && grimct > 0) {
                        e.setCancelled(true);
                        grimct--;
                    }
                }
            }
            case "Watchdog" -> {
                if (mc.thePlayer == null) return;
                int horizontalValue = 0;
                int verticalValue = airVelo.isEnabled() ? (mc.thePlayer.onGround ? 100 : 0) : 100;

                if (packet instanceof S12PacketEntityVelocity s12) {
                    if (mc.thePlayer != null && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                        if (cancel(e)) return;
                        s12.motionX *= horizontalValue / 100.0;
                        s12.motionZ *= horizontalValue / 100.0;
                        s12.motionY *= verticalValue / 100.0;
                    }
                } else if (packet instanceof S27PacketExplosion s27) {
                    if (cancel(e) || !explosions.isEnabled()) return;
                    s27.motionX *= horizontalValue / 100.0;
                    s27.motionZ *= horizontalValue / 100.0;
                    s27.motionY *= verticalValue / 100.0;
                }
            }
            /*case "C0F Cancel" -> {
                if (packet instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) e.getPacket();
                    if (mc.thePlayer != null && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                        e.cancel();
                    }
                }
                if (packet instanceof S27PacketExplosion) {
                    e.cancel();
                }
            }
            case "Stack" -> {
                if (packet instanceof S12PacketEntityVelocity) {
                    cancel = !cancel;
                    if (cancel) {
                        e.cancel();
                    }
                }
                if (packet instanceof S27PacketExplosion) {
                    e.cancel();
                }
            }
            case "Matrix" -> {
                if (packet instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) e.getPacket();
                    if (mc.thePlayer != null && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                        s12.motionX *= 5 / 100.0;
                        s12.motionZ *= 5 / 100.0;
                        s12.motionY *= 100 / 100.0;
                    }
                }
            }
            case "Tick" -> {
                if (packet instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) e.getPacket();
                    if (mc.thePlayer != null && s12.getEntityID() == mc.thePlayer.getEntityId() && mc.thePlayer.ticksExisted % 3 == 0) {
                        s12.motionX *= 5 / 100.0;
                        s12.motionZ *= 5 / 100.0;
                        s12.motionY *= 100 / 100.0;
                    }
                }
            }*/
        }

        if (e.getPacket() instanceof S19PacketEntityStatus s19) {
            if (mc.thePlayer != null && s19.getEntityId() == mc.thePlayer.getEntityId() && s19.getOpCode() == 2) {
                lastDamageTimestamp = System.currentTimeMillis();
            }
        }
        if (e.getPacket() instanceof  S12PacketEntityVelocity) {
            if (debug.isEnabled() && mc.thePlayer.hurtTime > 0) {
                Lettuce.text("§c§oKnockback Tick: " + mc.thePlayer.ticksExisted, false);
            }
        }
    };

    public final EventListener<EventMotion> onMotion = event -> {
        switch (mode.getMode()){
            case "Grim" -> {
                if (mc.thePlayer != null /*&& mc.thePlayer.onGround*/) {
                    udate++;

                    if (rps > 0) {
                        if (udate >= 0 || udate >= rps) {
                            udate = 0;
                            if (grimct > 0) {
                                grimct--;
                            }
                        }
                    }
                }
            }
        }
    };

    private boolean cancel(EventPacketReceive e) {
        if (staffCheck.isEnabled() && System.currentTimeMillis() - lastDamageTimestamp > 500) {
            if (System.currentTimeMillis() - lastAlertTimestamp > 250) {
                NotificationManager.post(NotificationType.WARNING, "Velocity", "Suspicious knockback detected!", 2);
                lastAlertTimestamp = System.currentTimeMillis();
            }
            return true;
        }
        if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
            e.cancel();
            return true;
        }
        return false;
    }
}