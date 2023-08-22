package best.lettuce.modules.impl.misc;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.NumberProperty;

public class Timer extends Module {
    public final NumberProperty timer = new NumberProperty("Timer Speed", 1, 1, 10, 1);

    public Timer() {
        super("Timer", Category.MISC, "Changes the game speed.");
        this.addProperties(this.timer);
    }

    public final EventListener<EventMotion> onTick = e -> mc.timer.timerSpeed = timer.getValue().floatValue();

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
    }
}
