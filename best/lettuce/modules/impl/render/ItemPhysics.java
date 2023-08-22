package best.lettuce.modules.impl.render;

import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.NumberProperty;

public class ItemPhysics extends Module {
    public ItemPhysics(){
        super("Item Physics", Category.RENDER, "");
        addProperties(speed);
    }

    public final NumberProperty speed = new NumberProperty("Speed", 1,0.1, 5, 0.1);
}
