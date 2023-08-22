package best.lettuce.gui.mainmenu.buttons;

import best.lettuce.Lettuce;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class SinglePlayerButton extends GuiButton {
    public SinglePlayerButton(int id, int xPos, int yPos, String displayString) {
        super(id, xPos, yPos, displayString);
    }

    public Animation animation = new DecelerateAnimation(200, 1);

    public SinglePlayerButton(int id, int xPos, int yPos, int width, int height, String displayString) {
        super(id, xPos, yPos, width, height, displayString);
    }

    protected boolean hovered = false;

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

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        float gay = width + animation.getOutput().floatValue() * 3f;
        if (this.visible)
        {
            Color color2 = ColorUtils.interpolateColorC(new Color(26, 21, 21, 90), new Color(26, 21, 21, 150), animation.getOutput().floatValue());
            Color color = ColorUtils.interpolateColorC(new Color(255, 255, 255, 0), new Color(255, 255, 255, 255), animation.getOutput().floatValue());
            float stringwidth = (float) Lettuce.lettuceFont24.getStringWidth(displayString);
            animation.setDirection(Direction.FORWARDS);
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            //this.hovered = mouseX >= this.xPosition - gay && mouseY >= this.yPosition - gay && mouseX < this.xPosition + gay && mouseY < this.yPosition + gay;
            this.hovered = MouseUtils.isHovering(xPosition - gay, yPosition - gay, gay, gay, mouseX, mouseY);
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            //RenderUtils.drawBorderedRoundedRect(this.xPosition, this.yPosition,  width, height, 8, 1, new Color(0, 0, 0, 0).getRGB(), new Color(0, 0, 0, 50).getRGB());
            RoundedUtils.drawRound(xPosition - gay, yPosition - gay, gay, gay, (float) width / 2, true, color2);
            this.mouseDragged(mc, mouseX, mouseY);
            if(this.hovered){
                animation.setDirection(Direction.FORWARDS);
            }
            else {
                animation.setDirection(Direction.BACKWARDS);
            }
            MinecraftInstance.icontestFont80.drawString(FontUtils.SINGLEPLAYER, xPosition - (float) width / 2 - 15 - animation.getOutput().floatValue(), yPosition - (float) width / 2 - 15 - animation.getOutput().floatValue(), Color.WHITE);
            Lettuce.lettuceFont24.drawStringWithShadow(displayString, xPosition - stringwidth + 10, yPosition - ((float) width) - 20, color);
        }

    }
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && hovered;
    }

    public boolean isMouseOver()
    {
        return this.hovered;
    }
}