package best.lettuce.gui.mainmenu;

import best.lettuce.Lecture;
import best.lettuce.gui.mainmenu.buttons.MenuButton;
import best.lettuce.richpresence.LettuceRichPresence;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RenderUtils;
import best.lettuce.utils.render.RoundedUtils;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class LettuceMainMenu extends GuiScreen implements MinecraftInstance {

    public boolean hoverDiscord;
    public boolean hoverFlushed;

    public Animation a = new DecelerateAnimation(500, 1);
    public Animation a2 = new DecelerateAnimation(500, 1);

    private final List<MenuButton> buttons = Arrays.asList(
            new MenuButton("Singleplayer"),
            new MenuButton("Multiplayer"),
            new MenuButton("Settings"),
            new MenuButton("Exit"));

    public void initGui() {
        LettuceRichPresence.update("Playing Minecraft");
        buttons.forEach(MenuButton::initGui);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        String discordText = "Click to join Tsukasa Discord server.";
        a.setDirection(hoverDiscord ? Direction.FORWARDS : Direction.BACKWARDS);
        a2.setDirection(hoverFlushed ? Direction.FORWARDS : Direction.BACKWARDS);
        hoverDiscord = MouseUtils.isHovering(8, 8, 25 + a.getOutput().floatValue() * (lettuceFont20.getStringWidth(discordText) + 5), 25, mouseX, mouseY);
        hoverFlushed = MouseUtils.isHovering(this.width - 128, 8, 120, 60, mouseX, mouseY);
        renderBackground();
        //lettuceBoldFont60.drawCenteredStringWithShadow(MinecraftInstance.NAME, width / 2f, this.height / 2f - 80, -1);
        lettuceFont20.drawStringWithShadow("The rebirth of us made you guys feel sick, right?", 1, this.height - 20, Color.WHITE.getRGB());
        lettuceFont20.drawStringWithShadow("Hi! Sonata Client or Sonata.XYZ", 1, this.height - 30, Color.WHITE.getRGB());
        lettuceFont20.drawStringWithShadow("Shit pasted made with     by " + Lecture.AUTHOR + ".", 1, this.height - 10, Color.WHITE.getRGB());
        icontestFont20.drawStringWithShadow(FontUtils.HEART, 1 + lettuceFont20.getStringWidth("Shit pasted Made with "), this.height - 8, Color.RED.getRGB());
        //lettuceFont20.drawStringWithShadow("A big appreciate to" + EnumChatFormatting.BOLD + " Vu" + EnumChatFormatting.RESET + " for supporting us ", this.width - lettuceFont20.getStringWidth("A big appreciate to" + EnumChatFormatting.BOLD + " Vu" + EnumChatFormatting.RESET + " for supporting us ") - icontestFont20.getStringWidth(FontUtils.HEART) - 2, this.height - 10, Color.WHITE.getRGB());
        //icontestFont20.drawStringWithShadow(FontUtils.HEART, this.width - icontestFont20.getStringWidth(FontUtils.HEART) - 2, this.height - 8, Color.RED.getRGB());
        lettuceBoldFont60.drawStringWithShadow("Dumbass Client", (float) (Math.random() * 100 + (width / 2f)) - 150, (float) (Math.random() * 150 + height / 2f) - 250, Color.WHITE.getRGB());
        lettuceBoldFont60.drawStringWithShadow("Lettuce mangan kaya pitik jago hugy sandi", (float) (Math.random() * 100 + (width / 6f)), (float) (Math.random() * 150 + height / 2f) - 250, Color.GREEN.getRGB());
        RoundedUtils.drawRound(8, 8, 25 + a.getOutput().floatValue() * (lettuceFont20.getStringWidth(discordText) + 10), 25, 12f, ColorUtils.interpolateColorC(new Color(0, 0, 0, 100), new Color(0, 0, 0, 150), a.getOutput().floatValue()));
        RenderUtils.scissorStart(7, 6, 25 + a.getOutput().floatValue() * (lettuceFont20.getStringWidth(discordText) + 10) + 3, 30);
        icontestFont40.drawCenteredString(FontUtils.DISCORD, 20f, icontestFont20.getMiddleOfBox(25) + 2.5f, Color.WHITE);
        lettuceFont20.drawString(discordText, 35, icontestFont40.getMiddleOfBox(25) + 12, Color.WHITE);
        RenderUtils.scissorEnd();
        //RoundedUtils.drawRound(40 + a.getOutput().floatValue() * (lettuceFont20.getStringWidth(discordText) + 5), 8, 25, 25, 12.5f, ColorUtils.interpolateColorC(new Color(0, 0, 0, 100), new Color(0, 0, 0, 150), a.getOutput().floatValue()));
        MinecraftInstance.mc.getTextureManager().bindTexture(new ResourceLocation("Lecture/banner.png"));
        RoundedUtils.drawRoundTextured(this.width - 128, 8, 120, 60, 12.5f, 1);
        lettuceBoldFont26.drawCenteredStringWithShadow("Sponsored by", this.width - 69.5f, 25, ColorUtils.interpolateColor(Color.WHITE, Color.WHITE.darker(), a2.getOutput().floatValue()));
        lettuceBoldFont26.drawCenteredStringWithShadow("TopAlts.store", this.width - 69.5f, 40, ColorUtils.interpolateColor(Color.WHITE, Color.WHITE.darker(), a2.getOutput().floatValue()));
        float buttonWidth = 140;
        float buttonHeight = 25;

        int count = 0;
        for (MenuButton button : buttons) {
            button.x = (width / 2f - buttonWidth / 2f) - 50f;
            button.y = height / 2f - buttonHeight / 2f - 25 + count;
            button.width = (float) (buttonWidth + Math.random() * 100);
            button.height = (float) (buttonHeight + Math.random());
            button.clickAction = () -> {
                switch (button.text) {
                    case "Singleplayer": {
                        MinecraftInstance.mc.displayGuiScreen(new GuiSelectWorld(this));
                        break;
                    }
                    case "Multiplayer": {
                        MinecraftInstance.mc.displayGuiScreen(new GuiMultiplayer(this));
                        break;
                    }
                    case "Settings": {
                        MinecraftInstance.mc.displayGuiScreen(new GuiOptions(this, MinecraftInstance.mc.gameSettings));
                        break;
                    }
                    case "Exit": {
                        MinecraftInstance.mc.shutdown();
                        break;
                    }
                }
            };
            button.drawScreen(mouseX, mouseY);
            count += buttonHeight + 5;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        buttons.forEach(button -> button.mouseClicked(mouseX, mouseY, mouseButton));
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (hoverDiscord && mouseButton == 0) {
            try {
                java.awt.Desktop.getDesktop().browse(new URI("https://discord.gg/UKSsMcm575"));

            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        if(hoverFlushed && mouseButton == 0) {
            try {
                java.awt.Desktop.getDesktop().browse(new URI("https://topalts.store"));

            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean doesGuiPauseGame() {
        return false;
    }
}