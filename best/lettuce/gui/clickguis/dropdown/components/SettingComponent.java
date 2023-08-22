package best.lettuce.gui.clickguis.dropdown.components;

import best.lettuce.gui.Screen;
import best.lettuce.gui.clickguis.dropdown.DropdownClickGUI;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.Property;
import lombok.Getter;

import java.awt.*;

public abstract class SettingComponent<T extends Property> implements Screen {
    @Getter
    private final T setting;

    public float x, y, width, height, alpha;
    public boolean typing;
    public float panelLimitY;
    public Color settingRectColor, textColor;
    public float countSize = 1;

    public SettingComponent(T setting) {
        this.setting = setting;
    }

    public boolean isClickable(float bottomY) {
        return bottomY > panelLimitY && bottomY < panelLimitY + 17 + DropdownClickGUI.allowedClickGuiHeight;
    }
}