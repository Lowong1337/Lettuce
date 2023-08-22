package best.lettuce.gui.mainmenu.buttons;

import best.lettuce.gui.Screen;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;

public class MenuButton implements Screen {

    public final String text;
    private Animation hoverAnimation;
    public float x, y, width, height;
    public Runnable clickAction;

    public MenuButton(String text) {
        this.text = text;
    }

    @Override
    public void initGui() {
        hoverAnimation = new DecelerateAnimation(500, 1);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {

        boolean hovered = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);
        hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
        Color rectColor = new Color(35, 37, 43, 102);
        rectColor = ColorUtils.interpolateColorC(rectColor, ColorUtils.brighter(rectColor, .4f), hoverAnimation.getOutput().floatValue());

        RoundedUtils.drawRoundOutline(x, y, (float) (width + Math.random() * 100f), height, 12, 1, rectColor, new Color(30, 30, 30, 100));

        lettuceFont22.drawCenteredString(text + EnumChatFormatting.RED + " Tsukasa.tokyo", x + width / 2f, y + lettuceFont22.getMiddleOfBox(height), -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hovered = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);
        if (hovered) clickAction.run();
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }
}