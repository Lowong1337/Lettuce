package best.lettuce.gui.clickguis.dropdown.components.properties;

import best.lettuce.gui.clickguis.dropdown.components.SettingComponent;
import best.lettuce.modules.impl.render.ClickGUI;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.MultipleBoolProperty;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.AbstractFontRenderer;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;

import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleBoolComponent extends SettingComponent<MultipleBoolProperty> {

    public float realHeight;
    public float normalCount;

    private final HashMap<BooleanProperty, Animation[]> booleanSettingAnimations = new HashMap<>();

    private final List<BooleanProperty> sortedSettings;

    public MultipleBoolComponent(MultipleBoolProperty setting) {
        super(setting);

        sortedSettings = setting.getBoolSettings().stream().sorted(Comparator.comparingDouble(this::getEnabledWidth)).collect(Collectors.toList());

        for (BooleanProperty booleanSetting : sortedSettings) {
            booleanSettingAnimations.put(booleanSetting, new Animation[]{new DecelerateAnimation(250, 1), new DecelerateAnimation(250, 1)});
        }

        normalCount = 2;
    }

    @Override
    public void initGui() {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    private float additionalHeight = 0;

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float boxHeight = 15;
        float boxY = y + realHeight / 2f - (boxHeight / 2f) + 4;
        float boxX = x + 6;
        float boxWidth = width - 10;

        Color rectColor = new Color(30, 30, 30, 100);

        RoundedUtils.drawRound(x + 6, boxY + 1, width - 12, (boxHeight - 2) + additionalHeight, 2, rectColor);

        lettuceFont14.drawString(getSetting().name, x + 6, y + 4, textColor);

        float addHeight = 0;
        float xOffset = 2;
        float yOffset = 3;
        float spacing = 3;

        //The available space for the settings
        float avaliableWidth = boxWidth - 2;

        //Sort the boolean settings to have the smallest width ones at the top

        for (BooleanProperty setting : sortedSettings) {
            float enabledWidth = getEnabledWidth(setting);
            float enabledHeight = getFont(setting).getHeight() + 4;

            //If the width exceeds the available space, we need to add a new line
            if (xOffset + enabledWidth > avaliableWidth) {
                xOffset = 2;
                yOffset += enabledHeight + spacing;

                //Calculates the amount of space that the new line will take
                addHeight += (yOffset + enabledHeight + spacing) - ((boxHeight) + addHeight);
            }

            float enabledX = boxX + xOffset;
            float enabledY = boxY + yOffset;

            boolean hovering = MouseUtils.isHovering(enabledX, enabledY, enabledWidth, enabledHeight, mouseX, mouseY);

            Animation hoverAnimation = booleanSettingAnimations.get(setting)[0];
            Animation toggleAnimation = booleanSettingAnimations.get(setting)[1];

            hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
            toggleAnimation.setDirection(setting.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);

            Color rectColorBool = ColorUtils.interpolateColorC(settingRectColor.brighter().brighter(), ClickGUI.color.getColor(), toggleAnimation.getOutput().floatValue());

            rectColorBool = ColorUtils.interpolateColorC(rectColorBool, rectColorBool.brighter(), hoverAnimation.getOutput().floatValue());

            RoundedUtils.drawRound(enabledX, enabledY, enabledWidth, enabledHeight, 2, rectColorBool);

            getFont(setting).drawString(setting.name, enabledX + 2, enabledY + 2, textColor);

            xOffset += enabledWidth + spacing;
        }

        additionalHeight = addHeight;

        float increment = (((boxY - y) + boxHeight + addHeight + 3) - realHeight) / (realHeight / normalCount);

        countSize = normalCount + increment;
    }

    private float getEnabledWidth(BooleanProperty setting) {
        return (getFont(setting).getStringWidth(setting.name) + 4);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        float boxHeight = 15;
        float boxY = y + realHeight / 2f - (boxHeight / 2f) + 4;
        float boxX = x + 6;
        float boxWidth = width - 10;

        float xOffset = 2;
        float yOffset = 3;
        float spacing = 3;

        //The available space for the settings
        float avaliableWidth = boxWidth - 2;

        //Sort the boolean settings to have the smallest width ones at the top

        for (BooleanProperty setting : sortedSettings) {
            float enabledWidth = getEnabledWidth(setting);
            float enabledHeight = getFont(setting).getHeight() + 4;

            //If the width exceeds the available space, we need to add a new line
            if (xOffset + enabledWidth > avaliableWidth) {
                xOffset = 2;
                yOffset += enabledHeight + spacing;
            }

            float enabledX = boxX + xOffset;
            float enabledY = boxY + yOffset;
            boolean hovered = MouseUtils.isHovering(enabledX, enabledY, enabledWidth, enabledHeight, mouseX, mouseY);

            if (isClickable(enabledY + enabledHeight) && hovered && button == 0) {
                setting.toggle();
            }

            xOffset += enabledWidth + spacing;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {}

    public AbstractFontRenderer getFont(BooleanProperty setting) {
        if (setting.isEnabled()) {
            return lettuceBoldFont14;
        } else {
            return lettuceFont14;
        }
    }
}