package best.lettuce.modules.impl.render;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.gui.clickguis.dropdown.DropdownClickGUI;
import best.lettuce.gui.clickguis.modern.ModernClickGUI;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ColorProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class ClickGUI extends Module {

    public static final ModeProperty clickguiMode = new ModeProperty("Style", "Dropdown", "Dropdown");
    public static final ColorProperty color = new ColorProperty("Color", Color.RED);
    public static final BooleanProperty rescale = new BooleanProperty("Rescale GUI", true);

    public static final DropdownClickGUI dropdownClickGui = new DropdownClickGUI();

    public static int prevGuiScale;

    public ClickGUI() {
        super("ClickGUI", Category.RENDER, "Displays modules");
        this.addProperties(clickguiMode, color, rescale);
        this.setKey(Keyboard.KEY_RSHIFT);
    }

    public final EventListener<EventTick> onTick = e -> {
        if(mc.ingameGUI == null){
            toggle(ToggleType.MANUAL);
        }
    };

    public void toggle() {
        this.onEnable();
    }

    public void onEnable() {
        this.toggle(ToggleType.AUTO);
        if (rescale.isEnabled()) {
            prevGuiScale = mc.gameSettings.guiScale;
            mc.gameSettings.guiScale = 2;
        }
        switch (clickguiMode.getMode()){
            case "Dropdown": mc.displayGuiScreen(dropdownClickGui); break;
        }
    }
}