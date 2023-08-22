package best.lettuce.modules.impl.render;

import best.lettuce.gui.clickguis.modern.ModernClickGUI;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;

public class TestShits extends Module {

    public static final ModernClickGUI modernClickGUI = new ModernClickGUI();
    public TestShits(){
        super("Test", Category.RENDER, "Shit");
    }

    public void onEnable(){
        toggle(ToggleType.AUTO);
        mc.displayGuiScreen(modernClickGUI);
    }
}
