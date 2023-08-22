package best.lettuce.utils.non_utils.drag;

import best.lettuce.Lecture;
import best.lettuce.modules.Module;
import best.lettuce.modules.impl.render.HUD;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.math.MathUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.List;

public class Dragging implements MinecraftInstance {
    @Expose
    @SerializedName("x")
    private float xPos;
    @Expose
    @SerializedName("y")
    private float yPos;

    public float initialXVal;
    public float initialYVal;

    private float startX, startY;
    private boolean dragging;

    private float width, height;

    @Expose
    @SerializedName("name")
    private String name;

    private final Module module;

    public Animation hoverAnimation = new DecelerateAnimation(100, 1, Direction.BACKWARDS);

    public Dragging(Module module, String name, float initialXVal, float initialYVal) {
        this.module = module;
        this.name = name;
        this.xPos = initialXVal;
        this.yPos = initialYVal;
        this.initialXVal = initialXVal;
        this.initialYVal = initialYVal;
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getX() {
        return xPos;
    }

    public void setX(float x) {
        this.xPos = x;
    }

    public float getY() {
        return yPos;
    }

    public void setY(float y) {
        this.yPos = y;
    }

    public final void onDraw(int mouseX, int mouseY) {
        ScaledResolution sr = new ScaledResolution();
        HUD hud = Lecture.INSTANCE.getModuleManager().getModule(HUD.class);
        boolean hovering = MouseUtils.isHovering(xPos, yPos, width, height, mouseX, mouseY);
        if (dragging) {
            xPos = mouseX - startX;
            yPos = mouseY - startY;
            if (xPos < 0) {
                xPos = 0;
            }
            if (xPos > sr.getWidth()) {
                xPos = sr.getWidth();
            }
            if (yPos < 0) {
                yPos = 0;
            }
            if (yPos > sr.getHeight()) {
                yPos = sr.getHeight();
            }
        }
        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
        if (!hoverAnimation.isDone() || hoverAnimation.finished(Direction.FORWARDS)) {
            RoundedUtils.drawRoundOutline(xPos - 4, yPos - 4, width + 8, height + 8, 6, 1,
                    new Color(30, 30, 30, 0),
                    ColorUtils.applyOpacity(hud.getColor(new Color[]{hud.color1.getColor(), hud.color2.getColor()}, 1), hoverAnimation.getOutput().floatValue()));
        }
    }

    public final void onDrawArraylist(HUD hud, int mouseX, int mouseY) {
        ScaledResolution sr = new ScaledResolution();
        //Lettuce.INSTANCE.text(xPos + " " + yPos);
        List<Module> modules = Lecture.INSTANCE.getModuleManager().getToggledModules();

        String longest = getLongestModule(hud);

        width = (float) MathUtils.roundToHalf(hud.getFont().getStringWidth(longest) + 5);
        height = (float) MathUtils.roundToHalf((hud.height.getValue() + 1) * modules.size());

        float textVal = (float) hud.getFont().getStringWidth(longest);
        float xVal = sr.getWidth() - (textVal + 8 + xPos);

        if (sr.getWidth() - xPos <= sr.getWidth() / 2f) {
            xVal += textVal;
        }

        boolean hovering = MouseUtils.isHovering(xVal, yPos - 8, width + 20, height + 16, mouseX, mouseY);

        if (dragging) {
            xPos = -(mouseX - startX);
            yPos = mouseY - startY;
            if (xPos < 3) {
                xPos = hud.rectangle.isEnabled("Right") ? 3 : 2;
            }
            if (xPos > sr.getWidth() - 2) {
                xPos = sr.getWidth() - 2;
            }
            if (yPos < 0) {
                yPos = hud.rectangle.isEnabled("Top") ? 2 : 1;
            }

        }
        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);

        if (!hoverAnimation.isDone() || hoverAnimation.finished(Direction.FORWARDS)) {
            RoundedUtils.drawRoundOutline(xVal, yPos - 4, width + 10, height + 6, 6, 1,
                    new Color(30, 30, 30, 0),
                    ColorUtils.applyOpacity(hud.getColor(new Color[]{hud.color1.getColor(), hud.color2.getColor()}, 1), hoverAnimation.getOutput().floatValue()));
        }
    }

    public final void onClick(int mouseX, int mouseY, int button) {
        boolean canDrag = MouseUtils.isHovering(xPos, yPos, width, height, mouseX, mouseY);
        if (button == 0 && canDrag) {
            dragging = true;
            startX = (int) (mouseX - xPos);
            startY = (int) (mouseY - yPos);
        }
    }

    public final void onClickArraylist(HUD arraylistMod, int mouseX, int mouseY, int button) {
        ScaledResolution sr = new ScaledResolution();

        String longest = getLongestModule(arraylistMod);

        float textVal = (float) arraylistMod.getFont().getStringWidth(longest);
        float xVal = sr.getWidth() - (textVal + 8 + xPos);

        if (sr.getWidth() - xPos <= sr.getWidth() / 2f) {
            xVal += textVal;
        }

        boolean canDrag = MouseUtils.isHovering(xVal, yPos - 4, width + 10, height + 6, mouseX, mouseY);

        if (button == 0 && canDrag) {
            dragging = true;
            startX = (int) (mouseX + xPos);
            startY = (int) (mouseY - yPos);
        }
    }

    public final void onRelease(int button) {
        if (button == 0) dragging = false;
    }

    private String getLongestModule(HUD arraylistMod) {
        return arraylistMod.longest;
    }
}