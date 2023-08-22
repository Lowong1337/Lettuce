package best.lettuce.modules.impl.render;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

public class Ambience extends Module {
    public Ambience() {
        super("Ambience", Category.RENDER, "Changes the world's time and weather.");
        addProperties(customtime, time, weather);
    }

    public final BooleanProperty customtime = new BooleanProperty("Custom time", true);
    public final NumberProperty time = new NumberProperty("World time", 6000, 0, 24000, 1000, customtime::isEnabled);
    public final ModeProperty weather = new ModeProperty("Weather", "Clear", "Clear", "Rain", "Thunder");

    float thunder;
    float rain;

    public final EventListener<EventMotion> onMotion = event -> {
        mc.theWorld.setWorldTime(time.getValue().longValue());
        switch (weather.getMode()) {
            case "Clear" -> {
                mc.theWorld.setThunderStrength(0);
                mc.theWorld.setRainStrength(0);
            }
            case "Rain" -> mc.theWorld.setRainStrength(2);
            case "Thunder" -> {
                mc.theWorld.setRainStrength(2);
                mc.theWorld.setThunderStrength(2);
            }
        }
    };

    public final EventListener<EventPacketReceive> onPacketReceive = event -> {
        if (event.getPacket() instanceof S03PacketTimeUpdate) {
            event.setCancelled(true);
        }
    };

    @Override
    public void onDisable() {
        mc.theWorld.setRainStrength(mc.theWorld.getRainStrength(rain));
        mc.theWorld.setThunderStrength(mc.theWorld.getThunderStrength(thunder));
        mc.theWorld.setWorldTime(mc.theWorld.getWorldTime());
    }
}
