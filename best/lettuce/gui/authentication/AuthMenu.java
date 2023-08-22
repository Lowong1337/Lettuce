package best.lettuce.gui.authentication;

import best.lettuce.Lecture;
import best.lettuce.gui.mainmenu.LettuceMainMenu;
import best.lettuce.gui.mainmenu.buttons.MenuButton;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.impl.render.HUD;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.non_utils.text.TextField;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AuthMenu extends GuiScreen implements MinecraftInstance {
    private final TextField textField = new TextField(lettuceFont20);
    public Animation animation = new DecelerateAnimation(2000, 1);
    public Animation rectanimation = new DecelerateAnimation(1000, 1);

    private final List<MenuButton> buttons = Arrays.asList(
            new MenuButton("Login"),
            new MenuButton("Get HWID")
    );

    public void initGui() {
        buttons.forEach(MenuButton::initGui);
        animation.setDirection(Direction.FORWARDS);
        rectanimation.setDirection(Direction.BACKWARDS);
    }

    protected void keyTyped(char typedChar, int keyCode) {
        textField.setFocused(true);
        if (keyCode == Keyboard.KEY_RETURN) {
            textField.setFocused(false);
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            textField.setFocused(false);
            return;
        }
        textField.keyTyped(typedChar, keyCode);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.renderAuthShittyBackGround();
        Lecture.INSTANCE.getModuleManager().getModule(HUD.class).drawNotifications();
        float buttonWidth = 140;
        float buttonHeight = 25;

        Color c = ColorUtils.interpolateColorC(ColorUtils.interpolateColorsBackAndForth(12, 1, new Color(106, 181, 253), new Color(111, 119, 253), true), Color.RED, rectanimation.getOutput().floatValue());

        //RoundedUtils.drawRoundOutline(this.width / 2 - 80, (this.height / 2 - 95) * animation.getOutput().floatValue(), 160, 190, 10, 1, new Color(0,0,0, 150), c);

        textField.setHeight(20);
        textField.setWidth(120);
        textField.setBackgroundText("UUID CAI CON CAC...");
        textField.setOutline(c);
        textField.setFill(new Color(0,0,0, 200));
        textField.setTextAlpha(.50f);
        textField.setMaxStringLength(60);
        textField.drawTextBox();
        textField.setXPosition(width / 2f - textField.getWidth() / 2f - 3.5f);
        textField.setYPosition((height / 2f - 35) * animation.getOutput().floatValue());

        //lettuceFont20.drawStringWithShadow(status, width / 2f - lettuceFont20.getStringWidth(status) / 2f,(height / 2f - textField.getHeight() / 2f) - 90, Color.WHITE.getRGB());
        lettuceBoldFont32.drawCenteredString("Welcome", this.width / 2f, (this.height / 2f - 85) * animation.getOutput().floatValue(), randomColor());

        lettuceBoldFont32.drawCenteredString("Lecture Client", (float) (Math.random() * 50 + this.width / 3.5f), (float) (Math.random() * 50 + (this.height / 2f - 65) * animation.getOutput().floatValue()), randomColor());
        lettuceBoldFont32.drawCenteredString("Lecture Client", (float) (Math.random() * 50 + this.width / 1.5f), (float) (Math.random() * 50 + (this.height / 2f - 65) * animation.getOutput().floatValue()), randomColor());

        lettuceBoldFont32.drawCenteredString("Lecture Client", (float) (Math.random() * 50 + this.width / 3.5f), (float) (Math.random() * 50 + (this.height / 2f + 65) * animation.getOutput().floatValue()), randomColor());
        lettuceBoldFont32.drawCenteredString("Lecture Client", (float) (Math.random() * 50 + this.width / 1.5f), (float) (Math.random() * 50 + (this.height / 2f + 65) * animation.getOutput().floatValue()), randomColor());

        lettuceBoldFont32.drawCenteredString("Lecture Client", (float) (Math.random() * 50 + this.width / 3.5f), (float) (Math.random() * 50 + (this.height / 2f - 185) * animation.getOutput().floatValue()), randomColor());
        lettuceBoldFont32.drawCenteredString("Lecture Client", (float) (Math.random() * 50 + this.width / 1.5f), (float) (Math.random() * 50 + (this.height / 2f - 185) * animation.getOutput().floatValue()), randomColor());

        lettuceFont32.drawCenteredString("Tsukasa User", this.width / 2f, (this.height / 2f - 65) * animation.getOutput().floatValue(), Color.green);

        int count = 0;
        for (MenuButton button : buttons) {
            button.x = width / 2f - buttonWidth / 2f;
            button.y = (((height / 2f - buttonHeight / 2f) + 25) + count) * animation.getOutput().floatValue();
            button.width = buttonWidth;
            button.height = buttonHeight;
            button.clickAction = () -> {
                switch (button.text) {
                    case "Login": {
//                        if (loginuuid(textField.getText(), Hwid.getHWID())) {
//                            PositionedSoundRecord sound = PositionedSoundRecord.create(new ResourceLocation("Lecture/sound/loginsuccess.ogg"));
//                            MinecraftInstance.mc.getSoundHandler().playSound(sound);
//                            MinecraftInstance.mc.displayGuiScreen(new LettuceMainMenu());
//                        } else {
//                            PositionedSoundRecord sound = PositionedSoundRecord.create(new ResourceLocation("Lecture/sound/loginfail.ogg"));
//                            MinecraftInstance.mc.getSoundHandler().playSound(sound);
//                            rectanimation.setDirection(Direction.FORWARDS);
//                            NotificationManager.post(NotificationType.DISABLE,  "", "User does not not exist or HWID is not correct!", 3);
//                        }
                        PositionedSoundRecord sound = PositionedSoundRecord.create(new ResourceLocation("Lecture/sound/loginsuccess.ogg"));
                        MinecraftInstance.mc.getSoundHandler().playSound(sound);
                        MinecraftInstance.mc.displayGuiScreen(new LettuceMainMenu());
                    }
                    break;

                    case "Get HWID": {
//                        String hwid = Hwid.getHWID();
//                        StringSelection stringSelection = new StringSelection(hwid);
//                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//                        clipboard.setContents(stringSelection, null);
                        NotificationManager.post(NotificationType.SUCCESS,  "", "WTF IS THIS HWID SYSTEM???", 10);
                        NotificationManager.post(NotificationType.SUCCESS,  "", "IS THIS SONATA.XYZ BASE????", 10);
                    }
                    break;
                }
            };
            button.drawScreen(mouseX, mouseY);
            count += buttonHeight + 5;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static Color randomColor() {
        Random random = new Random();

        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        return new Color(red, green, blue);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        buttons.forEach(button -> button.mouseClicked(mouseX, mouseY, mouseButton));
        textField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean doesGuiPauseGame() {
        return false;
    }
}
