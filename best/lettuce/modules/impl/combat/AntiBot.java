package best.lettuce.modules.impl.combat;

import best.lettuce.Lettuce;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.event.impl.player.EventAttack;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.utils.math.TimerUtils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AntiBot extends Module {
    public static final List<UUID> conmemay = new ArrayList<>();

    public AntiBot(){
        super("Anti Bot", Category.COMBAT, "Removes anticheats' bots.");
        this.addProperties(mode);
    }

    public final ModeProperty mode = new ModeProperty("Mode", "Watchdog", "Watchdog", "Matrix");

    public final EventListener<EventTick> onTick = event -> this.setSuffix(mode.getMode());

    public EntityLivingBase target;

    public TimerUtils timesiceattacked = new TimerUtils();
    public final EventListener<EventAttack> onAttack = event -> {
        target = event.getTarget();
        timesiceattacked.reset();
    };

    public final EventListener<EventMotion> onUpdate = event -> {
        if(mode.is("Matrix")){
            if(target != null && target.isDead || timesiceattacked.hasTimeElapsed(1000)){
                target = null;
            }
            if(target != null) {
                //Lettuce.text(target.getName());
            }
        }
    };
    public final EventListener<EventPacketReceive> onPacketReceive = e -> {
        switch (mode.getMode()){
            case "Watchdog" -> {
                if (e.getPacket() instanceof S38PacketPlayerListItem) {
                    S38PacketPlayerListItem packet = new S38PacketPlayerListItem();
                    for (S38PacketPlayerListItem.AddPlayerData playerData : packet.getEntries()) {
                        if (packet.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) continue;
                        NetworkPlayerInfo networkplayerinfo = new NetworkPlayerInfo(playerData);
                        if (mc.getNetHandler().getPlayerInfoMap().stream().noneMatch(i2 -> i2.getGameProfile().getName().equals(networkplayerinfo.getGameProfile().getName()))) continue;
                        this.conmemay.add(networkplayerinfo.getGameProfile().getId());
                        e.setCancelled(true);
                    }
                }
                if(mc.theWorld != null) {
                    for (EntityPlayer ep : mc.theWorld.playerEntities) {
                        if (ep.getName().contains("§c")) {
                            if (!ep.onGround && ep.moveForward != 0 && ep.moveStrafing != 0 && mc.thePlayer.getDistanceToEntity(ep) <= 4) {
                                mc.theWorld.removeEntity(ep);
                            }
                        }
                    }
                }
            }
            case "Matrix" -> {
                if(e.getPacket() instanceof S38PacketPlayerListItem s38){
                    Lettuce.text(String.valueOf(s38.getID()));
                }
                if(target != null && e.getPacket() instanceof S0CPacketSpawnPlayer){
                    S0CPacketSpawnPlayer packet = new S0CPacketSpawnPlayer();
                    mc.theWorld.removeEntityFromWorld(packet.getEntityID());
                }
            }
        }
    };

    @Override
    public void onDisable() {
        this.conmemay.clear();
    }
}
