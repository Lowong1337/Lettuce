package best.lettuce.modules.impl.misc;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;

public class BedwarsBot extends Module {
    public BedwarsBot() {
        super("Bedwars Bot", Category.MISC, "none");
    }

    public final EventListener<EventPacketReceive> onReceive = e -> {

    };
}
