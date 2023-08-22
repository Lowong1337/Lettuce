package best.lettuce.gui.clickguis.dropdown.components.properties;

import best.lettuce.gui.clickguis.dropdown.components.SettingComponent;
import best.lettuce.modules.property.impl.KeybindProperty;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class KeybindComponent extends SettingComponent<KeybindProperty> {

    private boolean binding;

    private final Animation clickAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);
    private final Animation hoverAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);

    public KeybindComponent(KeybindProperty keybindSetting) {
        super(keybindSetting);
    }

    @Override
    public void initGui() {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (binding) {
            if (keyCode == Keyboard.KEY_SPACE || keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE) {
                getSetting().setCode(Keyboard.KEY_NONE);
            } else {
                getSetting().setCode(keyCode);
            }

            typing = false;
            binding = false;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {

        clickAnimation.setDirection(binding ? Direction.FORWARDS : Direction.BACKWARDS);

        String bind = binding ? "Press a key..." : Keyboard.getKeyName(getSetting().getCode());

        float fullTextWidth = lettuceFont16.getStringWidth("Bind: §l" + bind);

        float startX = x + 6;
        float startY = y + lettuceFont16.getMiddleOfBox(height);

        boolean hovering = MouseUtils.isHovering(startX - 3, startY - 2, fullTextWidth + 6, lettuceFont16.getHeight() + 4, mouseX, mouseY);
        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);

        Color rectColor = ColorUtils.brighter(settingRectColor, .5f - (.25f * hoverAnimation.getOutput().floatValue()));
        RoundedUtils.drawRound(startX - 3, startY - 2, fullTextWidth + 6, lettuceFont16.getHeight() + 4, 4, rectColor);

        lettuceFont16.drawString("Bind: §l" + bind, startX, y + lettuceFont16.getMiddleOfBox(height), textColor);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        String bind = Keyboard.getKeyName(getSetting().getCode());
        String text = "§fBind: §r" + bind;
        float textWidth = lettuceFont18.getStringWidth(text);
        float startX = x + 6;
        float startY = y + lettuceFont18.getMiddleOfBox(height);
        float rectHeight = lettuceFont18.getHeight() + 4;

        boolean hovering = MouseUtils.isHovering(startX - 3, startY - 2, textWidth + 6, lettuceFont18.getHeight() + 4, mouseX, mouseY);

        if (isClickable(startY + rectHeight) && hovering && button == 0) {
            binding = true;
            typing = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }
}