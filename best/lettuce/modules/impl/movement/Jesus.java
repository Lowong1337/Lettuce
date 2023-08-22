package best.lettuce.modules.impl.movement;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.utils.player.MoveUtils;

public class Jesus extends Module {
    public Jesus(){
        super("Jesus", Category.MOVEMENT, "Allows you to walk on water.");
        addProperties(mode);
    }

    public final ModeProperty mode = new ModeProperty("Mode", "Solid", "Solid", "Dolphin");

    public final EventListener<EventTick> onTick = e -> {
        this.setSuffix(mode.getMode());
    };

    public final EventListener<EventMotion> onMotion = e -> {
        if(mc.thePlayer.isInWater() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
            switch (mode.getMode()){
                case "Dolphin": {
                    mc.thePlayer.motionY += 0.03999999910593033;
                    //mc.thePlayer.jump();
                    MoveUtils.strafe(0.3f);
                    break;
                }
            }
        }
    };
}
