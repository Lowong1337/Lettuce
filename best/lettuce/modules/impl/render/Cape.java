package best.lettuce.modules.impl.render;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;

public class Cape extends Module {
    public Cape() {
        super("Cape", Category.RENDER, "fjspdfjsodp");
        addProperties(style);
    }

    public ModeProperty style = new ModeProperty("Style", "Black", "Black", "White", "Troll", "Gradient");

    public final EventListener<EventTick> onTick = event -> {
        this.setSuffix(String.valueOf(style.getMode()));
    };
}
