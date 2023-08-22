package best.lettuce.gui.clickguis.dropdown.components.properties;

import best.lettuce.gui.clickguis.dropdown.components.SettingComponent;
import best.lettuce.modules.impl.render.ClickGUI;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;

import java.awt.*;

public class BooleanComponent extends SettingComponent<BooleanProperty> {

    public BooleanComponent(BooleanProperty booleanSetting) {
        super(booleanSetting);
    }

    private final Animation toggleAnimation = new DecelerateAnimation(300, 1);
    private final Animation switchanimation = new DecelerateAnimation(300, 1) {
    };

    @Override
    public void initGui() {
        toggleAnimation.setDirection(Direction.FORWARDS);
        switchanimation.setDirection(Direction.FORWARDS);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float switchWidth = 14;
        float switchHeight = 7;
        float circleheight = 5;
        float booleanX = x + width - (switchWidth + 5.5f);
        float booleanY = y + height / 2f - switchHeight / 2f;
        float switchpos = booleanX + 1;

                toggleAnimation.setDirection(getSetting().isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                switchanimation.setDirection(getSetting().isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        ColorUtils.resetColor();

        lettuceFont16.drawString(getSetting().name, x + 5, y + lettuceFont16.getMiddleOfBox(height), ColorUtils.applyOpacity(textColor, .5f + (.5f * toggleAnimation.getOutput().floatValue())));

        int color2 = ColorUtils.interpolateColor(Color.GRAY.darker(), ClickGUI.color.getColor(), toggleAnimation.getOutput().floatValue());

        ColorUtils.resetColor();
        RoundedUtils.drawRound(booleanX, booleanY, switchWidth, switchHeight, 3, new Color(color2));
        RoundedUtils.drawRound(switchpos + switchanimation.getOutput().floatValue() * 7f, booleanY + 1f, circleheight, circleheight, 2, Color.WHITE);

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        float switchWidth = 14;
        float switchHeight = 7;
        float booleanX = x + width - (switchWidth + 5.5f);
        float booleanY = y + height / 2f - switchHeight / 2f;

        boolean hovering = MouseUtils.isHovering(booleanX - 2, booleanY - 2, switchWidth + 4, switchHeight + 4, mouseX, mouseY);

        if (isClickable(booleanY + switchHeight) && hovering && button == 0) {
            getSetting().toggle();
        }
        //Lettuce.INSTANCE.text(String.valueOf(switchHeight));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }
}