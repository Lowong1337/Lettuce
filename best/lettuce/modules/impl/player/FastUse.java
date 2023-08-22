package best.lettuce.modules.impl.player;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.packet.PacketUtils;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.network.play.client.C03PacketPlayer;

public class FastUse extends Module {
    public FastUse(){
        super("Fast Use", Category.PLAYER, "Allows you to eat like Avocado!");
        addProperties(mode, speed, timer);
    }

    public final ModeProperty mode = new ModeProperty("Mode", "Basic", "Basic");
    public final NumberProperty speed = new NumberProperty("Speed", 5, 1, 30, 1, () -> mode.is("Basic"));
    public final NumberProperty timer = new NumberProperty("Timer", 1, 0.1, 4, 0.1, () -> mode.is("Basic"));

    public final EventListener<EventTick> onTick = e -> {
        setSuffix(mode.getMode());
    };

    public final EventListener<EventMotion> onUpdate = e -> {
        switch (mode.getMode()){
            case "Basic" -> {
                if(mc.thePlayer.isUsingItem()){
                    if(mc.thePlayer.getHeldItem() != null){
                        if(mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || mc.thePlayer.getHeldItem().getItem() instanceof ItemAppleGold || mc.thePlayer.getHeldItem().getItem() instanceof ItemFishFood || mc.thePlayer.getHeldItem().getItem() instanceof ItemSeedFood){
                            for (int i = 1; i <= speed.getValue().intValue(); i++){
                                PacketUtils.sendPacket(new C03PacketPlayer(mc.thePlayer.onGround));
                            }
                            mc.timer.timerSpeed = timer.getValue().floatValue();
                        }
                    }
                }
                else {
                    mc.timer.timerSpeed = 1;
                }
            }
        }
    };
}
