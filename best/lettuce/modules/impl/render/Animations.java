package best.lettuce.modules.impl.render;

import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.NumberProperty;

public class Animations extends Module {
    public ModeProperty mode = new ModeProperty("Mode", "1.8", "1.8", "1.7", "Exhibition", "Swank", "Swang", "Swing", "Stella", "Smooth", "Swong", "Smoke", "Slide", "Remix");
    public NumberProperty swingSpeed = new NumberProperty("Swing Speed", 1.0D, 0.1D, 2.0D, 0.1D);
    public NumberProperty itemDistance = new NumberProperty("Item Distance", 1.0f, 1.0f, 3.0f, 0.1f);
    public NumberProperty itemHeight = new NumberProperty("Item Height", 0.0f, 0.0f, 0.5f, 0.05f);
    public NumberProperty itemSize = new NumberProperty("Item Size", 1f, 0.1f, 2.0f, 0.05f);

    public Animations() {
        super("Animations", Category.RENDER, "Old animations.");
        addProperties(mode, swingSpeed, itemDistance, itemHeight, itemSize);
    }
}
