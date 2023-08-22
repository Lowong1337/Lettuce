package best.lettuce.gui.clickguis.dropdown.components;

import best.lettuce.Lettuce;
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
import best.lettuce.utils.render.RoundedUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigRect implements Screen {
    @Getter @Setter
    private int searchScore;
    private final Animation toggleAnimation = new EaseInOutQuad(300, 1);
    private final Animation hoverAnimation = new EaseOutSine(400, 1, Direction.BACKWARDS);
    private final Animation settingAnimation = new DecelerateAnimation(250, 1).setDirection(Direction.BACKWARDS);

    public String cfgname;
    @Getter
    private boolean typing;
    public float x, y, width, height, panelLimitY, alpha;

    @Getter
    private double settingSize = 1;

    public ConfigRect(String configname) {
        cfgname = configname;
    }

    @Override
    public void initGui() {
        settingAnimation.setDirection(Direction.BACKWARDS);
        toggleAnimation.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    private double actualSettingCount;

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        toggleAnimation.setDirection(Direction.FORWARDS);

        boolean hoveringModule = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);

        hoverAnimation.setDirection(hoveringModule ? Direction.FORWARDS : Direction.BACKWARDS);
        hoverAnimation.setDuration(hoveringModule ? 250 : 400);

        Color rectColor = new Color(35, 37, 43, (int) (255 * alpha));
        Color textColor = ColorUtils.applyOpacity(Color.WHITE, alpha);

        float textAlpha = .5f;
        Color moduleTextColor = cfgname.equals(Lettuce.INSTANCE.getConfigManager().getCurrentConfig()) ? ClickGUI.color.getColor() : ColorUtils.applyOpacity(textColor, textAlpha + (.4f * toggleAnimation.getOutput().floatValue()));

        Color toggleColor = ColorUtils.interpolateColorC(ColorUtils.applyOpacity(Color.BLACK, .15f), ColorUtils.applyOpacity(Color.WHITE, .12f), hoverAnimation.getOutput().floatValue());
        rectColor = ColorUtils.interpolateColorC(rectColor, toggleColor, toggleAnimation.getOutput().floatValue());

        ColorUtils.resetColor();
        Gui.drawRect2(x, y, width, height, ColorUtils.interpolateColor(rectColor, ColorUtils.brighter(rectColor, .8f), hoverAnimation.getOutput().floatValue()));

        ColorUtils.resetColor();

        lettuceFont18.drawString((cfgname.equals(Lettuce.INSTANCE.getConfigManager().getCurrentConfig()) ? "§l" : "") + cfgname, x + 5, y + lettuceFont18.getMiddleOfBox(height), moduleTextColor);

        icontestFont20.drawString(FontUtils.TRASH_TOP,x + width - 15, y + iconFont20.getMiddleOfBox(height) + 0.7f, Color.RED);
        icontestFont20.drawString(FontUtils.TRASH_BODY,x + width - 15, y + iconFont20.getMiddleOfBox(height) + 1, Color.RED);

        double settingHeight = (actualSettingCount) * settingAnimation.getOutput();
        actualSettingCount = 0;
        settingSize = settingHeight;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hoveringModule = isClickable(y, panelLimitY) && MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);

        if (hoveringModule) {
            switch (button) {
                case 0 -> {
                    toggleAnimation.setDirection(!cfgname.equals(Lettuce.INSTANCE.getConfigManager().getCurrentConfig())  ? Direction.FORWARDS : Direction.BACKWARDS);
                    Lettuce.INSTANCE.getConfigManager().loadConfig(cfgname, false);
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    public boolean isClickable(float y, float panelLimitY) {
        return y > panelLimitY && y < panelLimitY + DropdownClickGUI.allowedClickGuiHeight;
    }
}