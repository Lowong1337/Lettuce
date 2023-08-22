package best.lettuce.gui.clickguis.dropdown.components.properties;

import best.lettuce.gui.clickguis.dropdown.components.SettingComponent;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.ContinualAnimation;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.math.MathUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class AlphaComponent extends SettingComponent<NumberProperty> {

    private final Animation hoverAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);

    private final Animation[] textAnimations = new Animation[]{new DecelerateAnimation(250, 1), new DecelerateAnimation(250, 1)};

    private boolean dragging;
    private final ContinualAnimation animationWidth = new ContinualAnimation();

    private boolean selected;

    public int rgb;

    public AlphaComponent(NumberProperty numberSetting) {
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
                case Keyboard.KEY_LEFT: getSetting().setValue(getSetting().getValue() - increment); break;
                case Keyboard.KEY_RIGHT: getSetting().setValue(getSetting().getValue() + increment); break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        NumberProperty numberSetting = getSetting();

        float sliderX = x + 5;
        float sliderWidth = width - 10;
        float sliderY = y + 13;
        float sliderHeight = 1;

        textAnimations[0].setDirection(dragging ? Direction.BACKWARDS : Direction.FORWARDS);
        textAnimations[1].setDirection(selected && !dragging ? Direction.FORWARDS : Direction.BACKWARDS);

        boolean hovering = MouseUtils.isHovering(sliderX, sliderY - 2, sliderWidth, sliderHeight + 4, mouseX, mouseY);

        hoverAnimation.setDirection(hovering || dragging ? Direction.FORWARDS : Direction.BACKWARDS);

        double currentValue = numberSetting.getValue();

        if (dragging) {
            float percent = Math.min(1, Math.max(0, (mouseX - sliderX) / sliderWidth));
            double newValue = MathUtils.interpolate(numberSetting.getMinValue(), numberSetting.getMaxValue(), percent);
            numberSetting.setValue(newValue);
        }

        float widthPercentage = (float) (((currentValue) - numberSetting.getMinValue()) / (numberSetting.getMaxValue() - numberSetting.getMinValue()));

        Color colorWhite = new Color(255, 255, 255, 100);
        Color colorRgb = new Color(rgb);
        Color colorAlpha = new Color(colorRgb.getRed(), colorRgb.getGreen(), colorRgb.getBlue(), 100);

        ColorUtils.resetColor();
        mc.getTextureManager().bindTexture(new ResourceLocation(NAME + "/alpha.png"));
        RoundedUtils.drawRoundTextured(x + 3, y, width - 6, height - 19, 1, 1);
        RoundedUtils.drawGradientRound(x + 3, y, width - 6, height - 19, 1, colorWhite, colorWhite, colorAlpha, colorAlpha);
        GlStateManager.disableTexture2D();
        ColorUtils.resetColor();

        animationWidth.animate((sliderWidth + 4) * widthPercentage, 50);
        float animatedWidth = animationWidth.getOutput();
        float size = 6.5f;
        RoundedUtils.drawRound(sliderX + animatedWidth - size / 2f - 2, y - 1, size, size + height - 23.5f, 1, Color.WHITE);

        countSize = 1.2f;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        float sliderY = y + 13;
        float sliderHeight = 1;

        if (!MouseUtils.isHovering(x, y, width, height, mouseX, mouseY)) {
            selected = false;
        }

        if (isClickable(sliderY + sliderHeight) && MouseUtils.isHovering(x + 3, y, width - 6, height - 18, mouseX, mouseY) && button == 0) {
            selected = true;
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (dragging) dragging = false;
    }
}