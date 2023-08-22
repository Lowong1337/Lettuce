package best.lettuce.gui.clickguis.dropdown.components.properties;

import best.lettuce.gui.clickguis.dropdown.components.SettingComponent;
import best.lettuce.modules.impl.render.ClickGUI;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.ContinualAnimation;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.math.MathUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;
import org.lwjgl.input.Keyboard;

public class NumberComponent extends SettingComponent<NumberProperty> {

    private final Animation hoverAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);

    private final Animation[] textAnimations = new Animation[]{new DecelerateAnimation(250, 1), new DecelerateAnimation(250, 1)};

    private boolean dragging;
    private final ContinualAnimation animationWidth = new ContinualAnimation();

    private boolean selected;

    public NumberComponent(NumberProperty numberSetting) {
        super(numberSetting);
    }

    @Override
    public void initGui() {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (selected) {
            Keyboard.enableRepeatEvents(true);
            double increment = getSetting().getIncrement();
            switch (keyCode) {
                case Keyboard.KEY_LEFT -> getSetting().setValue(getSetting().getValue() - increment);
                case Keyboard.KEY_RIGHT -> getSetting().setValue(getSetting().getValue() + increment);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        NumberProperty numberSetting = getSetting();

        String value = String.valueOf(MathUtils.round(getSetting().getValue(), 2));

        value = value.contains(".") ? value.replaceAll("0*$", "").replaceAll("\\.$", "") : value;

        float sliderX = x + 5;
        float sliderWidth = width - 10;
        float sliderY = y + 13;
        float sliderHeight = 1;

        textAnimations[0].setDirection(dragging ? Direction.BACKWARDS : Direction.FORWARDS);
        textAnimations[1].setDirection(selected && !dragging ? Direction.FORWARDS : Direction.BACKWARDS);

        boolean hovering = MouseUtils.isHovering(sliderX, sliderY - 2, sliderWidth, sliderHeight + 4, mouseX, mouseY);

        hoverAnimation.setDirection(hovering || dragging ? Direction.FORWARDS : Direction.BACKWARDS);

        lettuceFont16.drawString(numberSetting.name + ": ", sliderX, y + 2, textColor);
        lettuceFont16.drawString(value, sliderX + sliderWidth - lettuceFont16.getStringWidth(value), y + 2, textColor);

        RoundedUtils.drawRound(sliderX, sliderY, sliderWidth, sliderHeight, 1.5f, ColorUtils.brighter(settingRectColor, .7f - (.2f * hoverAnimation.getOutput().floatValue())));

        double currentValue = numberSetting.getValue();

        if (dragging) {
            float percent = Math.min(1, Math.max(0, (mouseX - sliderX) / sliderWidth));
            double newValue = MathUtils.interpolate(numberSetting.getMinValue(), numberSetting.getMaxValue(), percent);
            numberSetting.setValue(newValue);
        }

        float widthPercentage = (float) (((currentValue) - numberSetting.getMinValue()) / (numberSetting.getMaxValue() - numberSetting.getMinValue()));

        animationWidth.animate(sliderWidth * widthPercentage, 50);

        float animatedWidth = animationWidth.getOutput();

        RoundedUtils.drawRound(sliderX, sliderY, animatedWidth, sliderHeight, 1.5f, ClickGUI.color.getColor());

        float size = 5f;
        RoundedUtils.drawRound(sliderX + animatedWidth - size / 2f, sliderY - size / 4f - 0.5f, size, size, (size / 2f) - .5f, ClickGUI.color.getColor());

        countSize = 1.2f;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        float sliderX = x + 5;
        float sliderWidth = width - 10;
        float sliderY = y + height / 2f + 2;
        float sliderHeight = 3;
        if (!MouseUtils.isHovering(x, y, width, height, mouseX, mouseY)) {
            selected = false;
        }

        if (isClickable(sliderY + sliderHeight) && MouseUtils.isHovering(sliderX, sliderY - 2, sliderWidth, sliderHeight + 4, mouseX, mouseY) && button == 0) {
            selected = true;
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (dragging) dragging = false;
    }
}