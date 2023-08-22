package best.lettuce.utils.non_utils.button;

import best.lettuce.gui.Screen;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.CustomFont;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;
import lombok.*;

import java.awt.*;

@Setter @Getter @RequiredArgsConstructor
public class ActionButton implements Screen {
    private float x, y, width, height, alpha;
    private boolean bypass = false;
    private final String name;
    private boolean bold = false;
    private CustomFont font;
    private Color color = new Color(0,0,0 ,90);
    private Runnable clickAction;

    private final Animation hoverAnimation = new DecelerateAnimation(250, 1);

    @Override
    public void initGui() {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        boolean hovering = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);

        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);

        Color rectColor = ColorUtils.interpolateColorC(color, color.brighter(), hoverAnimation.getOutput().floatValue());
        //RoundedUtils.drawRound(x, y, width, height, 5, color.brighter());
        RoundedUtils.drawRoundOutline(x, y, width, height, 5,0.1f, new Color(0,0,0, 90), Color.WHITE);
        if (font != null) {
            lettuceFont20.drawCenteredString(name, x + width / 2f, y + font.getMiddleOfBox(height), ColorUtils.applyOpacity(-1, alpha));
        } else {
            if (bold) {
                //lettuceFont20.drawCenteredString(name, x + width / 2f, y + lettuceFont18.getMiddleOfBox(height), ColorUtils.applyOpacity(-1, alpha));
            } else {
                lettuceFont18.drawCenteredString(name, x + width / 2f, y + lettuceFont18.getMiddleOfBox(height), ColorUtils.applyOpacity(-1, alpha));
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hovering = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);

        if (hovering && button == 0) {
            //TODO: remove this if statement
            if (clickAction != null) {
                clickAction.run();
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {}
}