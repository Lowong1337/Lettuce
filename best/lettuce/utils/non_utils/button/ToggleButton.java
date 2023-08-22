package best.lettuce.utils.non_utils.button;

import best.lettuce.gui.Screen;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RenderUtils;
import best.lettuce.utils.render.RoundedUtils;
import lombok.*;

import java.awt.*;

@Getter @Setter @RequiredArgsConstructor
public class ToggleButton implements Screen, MinecraftInstance {

    @Getter @Setter
    private float x, y, alpha;
    private boolean enabled;
    private final String name;
    private boolean bypass;
    private final float WH = 10;

    private final Animation toggleAnimation = new DecelerateAnimation(250, 1);

    @Override
    public void initGui() {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        int textColor = ColorUtils.applyOpacity(-1, alpha);
        lettuceFont16.drawString(name, x - (lettuceFont16.getStringWidth(name) + 5), y + lettuceFont16.getMiddleOfBox(WH), textColor);

        toggleAnimation.setDirection(enabled ? Direction.FORWARDS : Direction.BACKWARDS);

        float toggleAnim = toggleAnimation.getOutput().floatValue();
        Color roundColor = ColorUtils.interpolateColorC(ColorUtils.tripleColor(64), new Color(70, 220, 130), toggleAnim);
        RoundedUtils.drawRound(x, y, WH, WH, WH / 2f - .25f, roundColor);

        if (enabled || !toggleAnimation.isDone()) {
            RenderUtils.scaleStart(x + getWH() / 2f, y + getWH() / 2f, toggleAnim);
            icontestFont16.drawString(FontUtils.CHECKMARK, x + 2.5f, y + 3.5f, ColorUtils.applyOpacity(textColor, toggleAnim));
            RenderUtils.scaleEnd();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            if (bypass && MouseUtils.isHovering(x, y, WH, WH, mouseX, mouseY)) {
                enabled = !enabled;
            }
            /*else if (isHovering(x, y, WH, WH, mouseX, mouseY)) {
                enabled = !enabled;
            }*/
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {}

    public float getActualX() {
        return x - ((lettuceFont16.getStringWidth(name) + 5));
    }
}