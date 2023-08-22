package best.lettuce.gui.clickguis.dropdown.components.properties;

import best.lettuce.gui.clickguis.dropdown.components.SettingComponent;
import best.lettuce.modules.impl.render.ClickGUI;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.HeaderProperty;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;

import java.awt.*;

public class HeaderComponent extends SettingComponent<HeaderProperty> {

    public HeaderComponent(HeaderProperty booleanSetting) {
        super(booleanSetting);
    }

    @Override
    public void initGui() {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        boolean hovering = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);
        //RoundedUtils.drawRound(x + 3, y + lettuceFont20.getMiddleOfBox(height) - 1.5f, width - 6, lettuceFont20.getHeight() + 2, 1.5f, ColorUtils.interpolateColorC(new Color(0,0,0, 50), new Color(0,0,0, 100), toggleAnimation.getOutput().floatValue()));
        lettuceBoldFont20.drawCenteredString(getSetting().name, x + width / 2, y + lettuceBoldFont20.getMiddleOfBox(height), -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {}
}