package best.lettuce.gui;

import best.lettuce.config.ConfigManager;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.render.RoundedUtils;

import java.awt.*;
import java.io.File;
import java.util.Objects;

public class ConfigPanel implements Screen {

    public int width;
    public int height;

    public final Animation anim = new DecelerateAnimation(200, 1f);
    public final Animation widenanim = new DecelerateAnimation(300, 1f);
    public final Animation hightanim = new DecelerateAnimation(800, 1f);
    public final Animation textanim = new DecelerateAnimation(200, 1f);

    @Override
    public void initGui() {
        width = mc.displayWidth;
        height = mc.displayHeight;
        widenanim.setDirection(Direction.BACKWARDS);
        hightanim.setDirection(Direction.BACKWARDS);
        textanim.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }
}
