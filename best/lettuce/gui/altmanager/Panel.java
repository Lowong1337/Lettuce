package best.lettuce.gui.altmanager;

import best.lettuce.gui.Screen;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.render.RoundedUtils;
import lombok.*;

import java.awt.*;

@Getter @Setter
public abstract class Panel implements Screen, MinecraftInstance {
    private float x, y, width, height;

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        RoundedUtils.drawRound(x, y, width, height, 20, new Color(0,0,0,70));
    }
}
