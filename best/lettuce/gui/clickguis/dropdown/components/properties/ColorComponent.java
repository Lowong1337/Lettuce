package best.lettuce.gui.clickguis.dropdown.components.properties;

import best.lettuce.gui.clickguis.dropdown.components.SettingComponent;
import best.lettuce.modules.impl.render.ClickGUI;
import best.lettuce.modules.property.impl.ColorProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.ContinualAnimation;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.animation.impl.EaseOutSine;
import best.lettuce.utils.animation.impl.SmoothAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.GLUtils;
import best.lettuce.utils.render.RenderUtils;
import best.lettuce.utils.render.RoundedUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ColorComponent extends SettingComponent<ColorProperty> {

    private final Animation hoverAnimation = new DecelerateAnimation(250, 1);

    private final Animation openAnimation = new EaseOutSine(250, 1, Direction.BACKWARDS);
    private final Animation errorAnimation = new DecelerateAnimation(250, 1);

    private final SmoothAnimation continualAnimation = new SmoothAnimation(2000, 1, Direction.FORWARDS);

    private final ContinualAnimation animationWidth = new ContinualAnimation();

    public float realHeight;
    public float openedHeight;
    private boolean opened;

    private boolean draggingPicker;
    private boolean draggingHue;

    private final NumberProperty alphaValue = new NumberProperty("Alpha", 1.0, 0.0, 1.0, 0.01);
    private final AlphaComponent numberComponent;

    public ColorComponent(ColorProperty setting) {
        super(setting);
        numberComponent = new AlphaComponent(alphaValue);
        alphaValue.setValue(getSetting().getAlpha());
    }

    @Override
    public void initGui() {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        ColorProperty colorSetting = getSetting();

        openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);

        float spacing = 4;
        float colorHeight = 6.5f;
        float colorWidth = 20;
        float colorX = x + width - (colorWidth + spacing);
        float colorY = y + (realHeight / 2) - (colorHeight / 2);
        float colorRadius = 2;

        float openAnim = openAnimation.getOutput().floatValue();

        boolean hovered = MouseUtils.isHovering(x + 5, y, width, 14, mouseX, mouseY);
        {
            RenderUtils.scissorStart(x + 5, y, 89, 14);
            float output = 0;
            if (lettuceFont16.getStringWidth(colorSetting.name) > 86) {
                if (hovered) {
                    output = (lettuceFont16.getStringWidth(colorSetting.name) - 86) * continualAnimation.getOutput().floatValue();
                } else {
                    continualAnimation.setDirection(Direction.FORWARDS);
                    continualAnimation.reset();
                }

                if (continualAnimation.isDone()) {
                    continualAnimation.changeDirection();
                }

                lettuceFont16.drawString(colorSetting.name, x - output + 5, y + lettuceFont16.getMiddleOfBox(realHeight), textColor);

            } else {
                lettuceFont16.drawString(colorSetting.name, x + 5, y + lettuceFont16.getMiddleOfBox(realHeight), textColor);
            }
            RenderUtils.scissorEnd();
        }

        hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
        getSetting().setAlpha(alphaValue.getValue().floatValue());

        Color actualColor = getSetting().getColor();

        RoundedUtils.drawRound(colorX, colorY, colorWidth, colorHeight, colorRadius, hovered ? ColorUtils.interpolateColorC(actualColor, actualColor.darker(), hoverAnimation.getOutput().floatValue()) : actualColor);

        if (opened || !openAnimation.isDone()) {

            float[] hsb = {colorSetting.getHue(), colorSetting.getSaturation(), colorSetting.getBrightness()};

            float gradientX = x + 6;
            float gradientY = colorY + colorHeight + 6;
            float gradientWidth = width - 12;
            float gradientHeight = 10 + (55 * openAnim) - 20;
            float radius = 2;

            float colorAlpha = alpha;

            float min = Math.min(1, Math.max(0, (mouseX - gradientX) / gradientWidth));
            if (draggingHue) {
                colorSetting.setHue(min);
            }

            if (draggingPicker) {
                colorSetting.setBrightness(Math.min(1, Math.max(0, 1 - ((mouseY - gradientY) / gradientHeight))));
                colorSetting.setSaturation(min);
            }

            Color firstColor = ColorUtils.applyOpacity(Color.getHSBColor(hsb[0], 1, 1), colorAlpha);
            RoundedUtils.drawRound(gradientX, gradientY, gradientWidth, gradientHeight, radius, ColorUtils.applyOpacity(firstColor, colorAlpha));

            Color secondColor = Color.getHSBColor(hsb[0], 0, 1);

            RoundedUtils.drawGradientHorizontal(gradientX, gradientY, gradientWidth, gradientHeight, radius + .5f, ColorUtils.applyOpacity(secondColor, colorAlpha), ColorUtils.applyOpacity(secondColor, 0));

            Color thirdColor = Color.getHSBColor(hsb[0], 1, 0);

            RoundedUtils.drawGradientVertical(gradientX, gradientY, gradientWidth, gradientHeight, radius, ColorUtils.applyOpacity(thirdColor, 0), ColorUtils.applyOpacity(thirdColor, colorAlpha));

            float pickerY = (gradientY - 2) + (gradientHeight * (1 - hsb[2]));
            float pickerX = (gradientX) + (gradientWidth * hsb[1] - 1);
            pickerY = Math.max(Math.min(gradientY + gradientHeight - 2, pickerY), gradientY - 2);
            pickerX = Math.max(Math.min(gradientX + gradientWidth - 2, pickerX), gradientX - 2);

            Color whiteColor = ColorUtils.applyOpacity(Color.WHITE, colorAlpha * openAnim);
            ColorUtils.color(whiteColor.getRGB());
            GLUtils.startBlend();
            RenderUtils.drawImage(new ResourceLocation(NAME + "/colorpicker2.png"), pickerX, pickerY, 4, 4);
            GLUtils.endBlend();

            float hueY = gradientY + gradientHeight + 5;
            float hueHeight = 4;
            ColorUtils.resetColor();
            mc.getTextureManager().bindTexture(new ResourceLocation(NAME + "/hue.png"));
            RoundedUtils.drawRoundTextured(gradientX, hueY, gradientWidth, hueHeight, 1, colorAlpha * openAnim);

            float sliderSize = 6.5f;
            float sliderX = gradientX + (gradientWidth * hsb[0]) - (sliderSize / 2);
            animationWidth.animate(sliderX, 50);
            RoundedUtils.drawRound(animationWidth.getOutput(), hueY + ((hueHeight / 2f) - sliderSize / 2f), sliderSize, sliderSize, 1, whiteColor);

            Animation error2Anim = errorAnimation;
            error2Anim.setDirection(error2Anim.getDirection());

            float newYVal = hueY + hueHeight + 4 + (5 * error2Anim.getOutput().floatValue());

            Color textColor = ColorUtils.applyOpacity(Color.WHITE, colorAlpha);
            float realHeightLeft = openedHeight - (newYVal - y);
            float componentHeight = realHeightLeft / 2f;
            int count = 0;

            if (getSetting().isHasAlpha()) {
                numberComponent.x = gradientX - 3;
                numberComponent.y = (newYVal) + (count * componentHeight) - 3;
                numberComponent.width = gradientWidth + 6;
                numberComponent.height = componentHeight + 10;
                numberComponent.settingRectColor = ColorUtils.applyOpacity(settingRectColor, colorAlpha);
                numberComponent.textColor = textColor;
                numberComponent.panelLimitY = panelLimitY;
                numberComponent.alpha = openAnim * alpha;
                numberComponent.rgb = getSetting().getColor(false).getRGB();

                numberComponent.drawScreen(mouseX, mouseY);
            }
        }

        openedHeight = realHeight * (6.75f * openAnim);
        countSize = 1 + ((getSetting().isHasAlpha() ? 4.3f : 3.8f) * openAnim);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        float spacing = 4;
        float colorHeight = 6.5f;
        float colorWidth = 30;
        float colorX = x + width - (colorWidth + spacing);
        float colorY = y + (realHeight / 2) - (colorHeight / 2);

        float openAnim = openAnimation.getOutput().floatValue();

        boolean hovered = isClickable(colorY + colorHeight) && MouseUtils.isHovering(x + 5, y, width, 14, mouseX, mouseY);

        if (hovered && button == 0) {
            opened = !opened;
        }

        if (opened) {
            if (getSetting().isHasAlpha()) {
                numberComponent.mouseClicked(mouseX, mouseY, button);
            }

            float gradientX = x + 6;
            float gradientY = colorY + colorHeight + 4;
            float gradientWidth = width - 12;
            float gradientHeight = 55 * openAnim - 5;

            if (button == 0) {
                float hueY = gradientY + gradientHeight;
                if (isClickable(hueY + 4) && MouseUtils.isHovering(gradientX, hueY * openAnim, gradientWidth, 6, mouseX, mouseY)) {
                    draggingHue = true;
                }
                if (isClickable(gradientY + gradientHeight) && MouseUtils.isHovering(gradientX, gradientY, gradientWidth, gradientHeight, mouseX, mouseY)) {
                    draggingPicker = true;
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        draggingHue = false;
        draggingPicker = false;
        numberComponent.mouseReleased(mouseX, mouseY, state);
    }
}