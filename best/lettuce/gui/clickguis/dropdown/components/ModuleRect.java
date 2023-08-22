package best.lettuce.gui.clickguis.dropdown.components;

import best.lettuce.gui.Screen;
import best.lettuce.gui.clickguis.dropdown.DropdownClickGUI;
import best.lettuce.gui.clickguis.dropdown.components.properties.*;
import best.lettuce.modules.Module;
import best.lettuce.modules.impl.render.ClickGUI;
import best.lettuce.modules.property.Property;
import best.lettuce.modules.property.impl.*;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.animation.impl.EaseInOutQuad;
import best.lettuce.utils.animation.impl.EaseOutSine;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleRect implements Screen {

    public final Module module;
    @Getter @Setter
    private int searchScore;
    private final Animation toggleAnimation = new EaseInOutQuad(300, 1);
    private final Animation hoverAnimation = new EaseOutSine(400, 1, Direction.BACKWARDS);
    private final Animation settingAnimation = new DecelerateAnimation(250, 1).setDirection(Direction.BACKWARDS);

    @Getter
    private boolean typing;
    public float x, y, width, height, panelLimitY, alpha;

    @Getter
    private double settingSize = 1;
    private final List<SettingComponent> settingComponents;

    public ModuleRect(Module module) {
        this.module = module;
        settingComponents = new ArrayList<>();
        for (Property setting : module.getProperties()) {
            if (setting instanceof KeybindProperty) {
                settingComponents.add(new KeybindComponent((KeybindProperty) setting));
            }
            if (setting instanceof BooleanProperty) {
                settingComponents.add(new BooleanComponent((BooleanProperty) setting));
            }
            if (setting instanceof ModeProperty) {
                settingComponents.add(new ModeComponent((ModeProperty) setting));
            }
            if (setting instanceof NumberProperty) {
                settingComponents.add(new NumberComponent((NumberProperty) setting));
            }
            if (setting instanceof MultipleBoolProperty) {
                settingComponents.add(new MultipleBoolComponent((MultipleBoolProperty) setting));
            }
            if (setting instanceof StringProperty) {
                settingComponents.add(new StringComponent((StringProperty) setting));
            }
            if (setting instanceof ColorProperty) {
                settingComponents.add(new ColorComponent((ColorProperty) setting));
            }
            if (setting instanceof HeaderProperty) {
                settingComponents.add(new HeaderComponent((HeaderProperty) setting));
            }
        }
    }

    @Override
    public void initGui() {
        settingAnimation.setDirection(Direction.BACKWARDS);
        toggleAnimation.setDirection(Direction.BACKWARDS);

        if (settingComponents != null) {
            settingComponents.forEach(SettingComponent::initGui);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (module.isExpanded()) {
            for (SettingComponent settingComponent : settingComponents) {
                if (!settingComponent.getSetting().isShown()) continue;
                settingComponent.keyTyped(typedChar, keyCode);
            }
        }
    }

    private double actualSettingCount;

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        toggleAnimation.setDirection(Direction.FORWARDS);
        settingAnimation.setDirection(module.isExpanded() ? Direction.FORWARDS : Direction.BACKWARDS);

        boolean hoveringModule = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);

        hoverAnimation.setDirection(hoveringModule ? Direction.FORWARDS : Direction.BACKWARDS);
        hoverAnimation.setDuration(hoveringModule ? 250 : 400);

        Color rectColor = new Color(35, 37, 43, (int) (255 * alpha));
        Color textColor = ColorUtils.applyOpacity(Color.WHITE, alpha);

        float textAlpha = .5f;
        Color moduleTextColor = module.isEnabled() ? ClickGUI.color.getColor() : ColorUtils.applyOpacity(textColor, textAlpha + (.4f * toggleAnimation.getOutput().floatValue()));

        Color toggleColor = ColorUtils.interpolateColorC(ColorUtils.applyOpacity(Color.BLACK, .15f), ColorUtils.applyOpacity(Color.WHITE, .12f), hoverAnimation.getOutput().floatValue());
        rectColor = ColorUtils.interpolateColorC(rectColor, toggleColor, toggleAnimation.getOutput().floatValue());

        ColorUtils.resetColor();
        Gui.drawRect2(x, y, width, height, ColorUtils.interpolateColor(rectColor, ColorUtils.brighter(rectColor, .8f), hoverAnimation.getOutput().floatValue()));

        ColorUtils.resetColor();

        lettuceFont18.drawString((module.isEnabled() ? "§l" : "") + module.getName(), x + 5, y + lettuceFont18.getMiddleOfBox(height), moduleTextColor);

        Color settingRectColor = ColorUtils.tripleColor(30, .5f);

        float arrowX = x + width - 12;
        if (settingComponents.size() > 0) {
            float arrowY = y + iconFont20.getMiddleOfBox(height) + 1;
            RenderUtils.rotateStart(arrowX, arrowY, iconFont20.getStringWidth(FontUtils.DROPDOWN_ARROW), iconFont20.getHeight(), 180 * settingAnimation.getOutput().floatValue());
            iconFont20.drawString(FontUtils.DROPDOWN_ARROW, arrowX, arrowY, ColorUtils.applyOpacity(textColor, .5f));
            RenderUtils.rotateEnd();
        }

        double settingHeight = (actualSettingCount) * settingAnimation.getOutput();
        actualSettingCount = 0;
        if (module.isExpanded() || !settingAnimation.isDone()) {
            float settingRectHeight = 16;

            if (!settingAnimation.isDone()) {
                RenderUtils.scissorStart(x, y + height, width, settingHeight * settingRectHeight);
            }

            typing = false;
            for (SettingComponent settingComponent : settingComponents) {
                if (!settingComponent.getSetting().isShown()) continue;

                settingComponent.panelLimitY = panelLimitY;
                settingComponent.settingRectColor = settingRectColor;
                settingComponent.textColor = textColor;
                settingComponent.alpha = alpha;
                settingComponent.x = x;
                settingComponent.y = (float) (y + height + ((actualSettingCount * settingRectHeight)));
                settingComponent.width = width;
                settingComponent.typing = typing;

                if (settingComponent instanceof ModeComponent modeComponent) {
                    modeComponent.realHeight = settingRectHeight * modeComponent.normalCount;
                }

                if (settingComponent instanceof MultipleBoolComponent multipleBoolComponent) {
                    multipleBoolComponent.realHeight = settingRectHeight * multipleBoolComponent.normalCount;
                }

                if (settingComponent instanceof ColorComponent colorComponent) {
                    colorComponent.realHeight = settingRectHeight;
                }

                settingComponent.height = settingRectHeight * settingComponent.countSize;

                settingComponent.drawScreen(mouseX, mouseY);

                if (settingComponent.typing) typing = true;

                actualSettingCount += settingComponent.countSize;
            }

            if (!settingAnimation.isDone() || GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) {
                RenderUtils.scissorEnd();
            }
        }
        settingSize = settingHeight;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hoveringModule = isClickable(y, panelLimitY) && MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);
        if (module.isExpanded() && settingAnimation.finished(Direction.FORWARDS)) {
            for (SettingComponent settingComponent : settingComponents) {
                if (!settingComponent.getSetting().isShown()) continue;
                settingComponent.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (hoveringModule) {
            switch (button) {
                case 0 -> {
                    toggleAnimation.setDirection(!module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                    module.toggle(Module.ToggleType.MANUAL);
                }
                case 1 -> module.setExpanded(!module.isExpanded());
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (module.isExpanded()) {
            for (SettingComponent settingComponent : settingComponents) {
                if (!settingComponent.getSetting().isShown()) continue;
                settingComponent.mouseReleased(mouseX, mouseY, state);
            }
        }
    }

    public boolean isClickable(float y, float panelLimitY) {
        return y > panelLimitY && y < panelLimitY + DropdownClickGUI.allowedClickGuiHeight;
    }
}