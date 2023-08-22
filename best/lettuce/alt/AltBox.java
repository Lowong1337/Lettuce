package best.lettuce.alt;

import best.lettuce.Lettuce;
import best.lettuce.modules.impl.render.Animations;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class AltBox extends Gui {
    public boolean isHovered;

    @Getter
    public String cfgname;

    @Getter
    public float x, y, width, height;

    public Animation animation = new DecelerateAnimation(300, 1);

    public AltBox(int x, int y, int widthIn, int heightIn, String name) {
        this.x = x;
        this.y = y;
        this.width = widthIn;
        this.height = heightIn;
        this.cfgname = name;
    }

    public void drawBox(int mouseX, int mouseY){
        this.isHovered = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);
        animation.setDirection(isHovered ? Direction.FORWARDS : Direction.BACKWARDS);
        //Lettuce.text(String.valueOf(isHovered));
        RoundedUtils.drawRound(x, y, width, height, 4, ColorUtils.interpolateColorC(new Color(0,0,0, 10), new Color(0,0,0, 15), animation.getOutput().floatValue()));
        MinecraftInstance.lettuceFont20.drawString(cfgname, 15, height - 170 + 25, ColorUtils.interpolateColorC(new Color(255,255,255, 0), Color.WHITE, animation.getOutput().floatValue()));
        ColorUtils.resetColor();
    }

    //public void
}
