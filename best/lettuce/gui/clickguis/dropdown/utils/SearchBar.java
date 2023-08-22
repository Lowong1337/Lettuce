package best.lettuce.gui.clickguis.dropdown.utils;

import best.lettuce.Lecture;
import best.lettuce.gui.Screen;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.non_utils.text.TextField;
import best.lettuce.utils.render.RoundedUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.Color;

@Getter @Setter
public class SearchBar implements Screen {

    private boolean focused, typing, hoveringBottomOfScreen;
    private final Animation focusAnimation = new DecelerateAnimation(175, 1).setDirection(Direction.BACKWARDS), hoverAnimation = new DecelerateAnimation(175, 1).setDirection(Direction.BACKWARDS), openAnimation = new DecelerateAnimation(250, 1).setDirection(Direction.BACKWARDS);
    private final TextField searchField = new TextField(lettuceFont18);
    private float alpha;

    @Override
    public void initGui() {
        openAnimation.setDirection(Direction.FORWARDS);
        searchField.setText("");
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            searchField.setFocused(false);
            return;
        }

        if (GuiScreen.isCtrlKeyDown() && keyCode == Keyboard.KEY_F) {
            searchField.setFocused(true);
            Lecture.INSTANCE.getModuleManager().getModules().forEach(module -> module.setExpanded(false));
            return;
        }

        searchField.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        focused = searchField.isFocused() || !searchField.getText().isEmpty();
        typing = searchField.isFocused();
        ScaledResolution sr = new ScaledResolution();

        float width = sr.getWidth(), height = sr.getHeight();
        hoveringBottomOfScreen = MouseUtils.isHovering(width / 2f - 120, sr.getHeight() - (100), 240, 100, mouseX, mouseY);

        hoverAnimation.setDirection(hoveringBottomOfScreen && !focused ? Direction.FORWARDS : Direction.BACKWARDS);
        focusAnimation.setDirection(focused ? Direction.FORWARDS : Direction.BACKWARDS);
        float focusAnim = focusAnimation.getOutput().floatValue();

        float hover = hoverAnimation.getOutput().floatValue();


        float openAnim = Math.min(1, alpha);
        float searchAlpha = Math.min(1, hover + focusAnim);
        lettuceFont26.drawCenteredString("Do to open the search bar", sr.getWidth() / 2f, sr.getHeight() - 75, ColorUtils.applyOpacity(-1, (.3f * (1 - searchAlpha)) * openAnim));

        searchField.setWidth(200);
        searchField.setHeight(25);
        searchField.setFont(lettuceFont24);
        searchField.setXPosition(sr.getWidth() / 2f - 100);

        searchField.setYPosition(sr.getHeight() - (70 + (25 * hover) + (60 * focusAnim)));
        searchField.setRadius(5);
        searchField.setAlpha(Math.max(hover * .85f, focusAnim));
        searchField.setTextAlpha(searchField.getAlpha());
        searchField.setFill(ColorUtils.tripleColor(17));
        searchField.setOutline(null);
        searchField.setBackgroundText("Search");
        searchField.drawTextBox();
    }

    public void drawEffects() {
        ScaledResolution sr = new ScaledResolution();
        float hover = hoverAnimation.getOutput().floatValue();
        float focusAnim = focusAnimation.getOutput().floatValue();

        float openAnim = Math.min(1, alpha);
        float searchAlpha = Math.min(1, hover + focusAnim);

        lettuceFont26.drawCenteredString("Do to open the search bar", sr.getWidth() / 2f, sr.getHeight() - 75, ColorUtils.applyOpacity(Color.BLACK, (1 * (1 - searchAlpha)) * openAnim));
        RoundedUtils.drawRound(searchField.getXPosition(), searchField.getYPosition(), 200, searchField.getHeight(), 5, ColorUtils.applyOpacity(Color.BLACK, Math.max(hover, focusAnim)));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean focused = searchField.isFocused();
        searchField.mouseClicked(mouseX, mouseY, button);
        if (!focused && searchField.isFocused()) {
            Lecture.INSTANCE.getModuleManager().getModules().forEach(module -> module.setExpanded(false));
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {}
}