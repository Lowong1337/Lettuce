package net.minecraft.client.gui;

import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.render.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiButton extends Gui
{
    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
    protected int width;
    protected int height;
    public int xPosition;
    public int yPosition;
    public String displayString;
    public int id;
    public boolean enabled;
    public boolean visible;
    protected boolean hovered;
    public final Animation animation = new DecelerateAnimation(600, 1);

    public GuiButton(int buttonId, int x, int y, String buttonText)
    {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        this.width = 200;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
    }

    protected int getHoverState(boolean mouseOver)
    {
        int i = 1;

        if (!this.enabled)
        {
            i = 0;
        }
        else if (mouseOver)
        {
            i = 2;
        }

        return i;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            animation.setDirection(hovered? Direction.FORWARDS : Direction.BACKWARDS);
            //RenderUtils.drawBorderedRoundedRect(this.xPosition, this.yPosition,  width, height, 6, 1, new Color(0, 0, 0, 20).getRGB(), new Color(0, 0, 0, 50).getRGB());
            RoundedUtils.drawRound(this.xPosition, this.yPosition, this.width ,this.height, 6f, new Color(0, 0, 0, 80));
            RoundedUtils.drawRound(this.xPosition, yPosition, animation.getOutput().floatValue() * this.width, height, 6, new Color(255,255,255, 20));
            //Gui.drawRect2(xPosition, yPosition, width, height, new Color(0, 0, 0, 80).getRGB());
            ColorUtils.resetColor();
            this.mouseDragged(mc, mouseX, mouseY);
            int color2 = this.hovered ? Color.YELLOW.brighter().brighter().getRGB() : Color.WHITE.darker().getRGB();
            MinecraftInstance.lettuceFont20.drawCenteredStringWithShadow(this.displayString + EnumChatFormatting.RED + " Tsukasa.tokyo", this.xPosition + (float) this.width / 2, this.yPosition + (float) (this.height - 8) / 2, ColorUtils.interpolateColor(Color.WHITE.darker(), new Color(210, 210, 32, 255), animation.getOutput().floatValue()));
        }
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
    }

    public void mouseReleased(int mouseX, int mouseY)
    {
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    public boolean isMouseOver()
    {
        return this.hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY)
    {
    }

    public void playPressSound(SoundHandler soundHandlerIn)
    {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public int getButtonWidth()
    {
        return this.width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
}
