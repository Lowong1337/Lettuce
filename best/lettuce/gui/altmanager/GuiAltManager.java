package best.lettuce.gui.altmanager;

import best.lettuce.Lecture;
import best.lettuce.gui.Screen;
import best.lettuce.gui.altmanager.helpers.Alt;
import best.lettuce.gui.altmanager.helpers.AltManagerUtils;
import best.lettuce.gui.altmanager.panels.AltPanel;
import best.lettuce.gui.altmanager.panels.LoginPanel;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.impl.render.HUD;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.non_utils.button.ToggleButton;
import best.lettuce.utils.non_utils.text.TextField;
import best.lettuce.gui.mainmenu.LettuceMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiAltManager extends GuiScreen {
    private final AltManagerUtils utils = new AltManagerUtils();
    public Alt currentSessionAlt;
    private long initTime = System.currentTimeMillis();
    private List<Panel> panels;
    public final TextField searchField = new TextField(MinecraftInstance.lettuceFont20);
    public final ToggleButton filterBanned = new ToggleButton("Filter banned accounts");
    private final AltPanel.AltRect altRect = new AltPanel.AltRect(null);

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(new LettuceMainMenu());
            searchField.setFocused(false);
        }
        searchField.keyTyped(typedChar, keyCode);
        panels.forEach(panel -> panel.keyTyped(typedChar, keyCode));
    }

    public GuiAltManager(){
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new LoginPanel());
            panels.add(new AltPanel());
        }
    }

    @Override
    public void initGui() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new LoginPanel());
            panels.add(new AltPanel());
        }

        /*if (mc.gameSettings.guiScale != 2) {
            mc.gameSettings.guiScale = 2;
            mc.resize(mc.displayWidth - 1, mc.displayHeight);
            mc.resize(mc.displayWidth + 1, mc.displayHeight);
        }*/

        panels.forEach(Screen::initGui);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        utils.writeAltsToFile();

        renderBackground();

        AltPanel altPanel = (AltPanel) panels.get(1);
        int count = 0;
        int seperation = 0;
        for (Panel panel : panels) {
            boolean notAltPanel = !(panel instanceof AltPanel);
            if (notAltPanel) {
                panel.setX(16);
                panel.setY(20 + seperation);
                panel.setWidth(325);
            } else {
                panel.setX(36 + 325);
            }
            panel.drawScreen(mouseX, mouseY);
            if (notAltPanel) {
                seperation += panel.getHeight() + (count >= 1 ? 10 : 25);
            }
            count++;
        }

        if (currentSessionAlt != null) {
            altRect.setAlt(currentSessionAlt);
            altRect.setHeight(40);
            altRect.setX(altPanel.getX() + 10);
            altRect.setY(altPanel.getY() - (altRect.getHeight() + 10));
            altRect.setWidth(Math.min(160, searchField.getXPosition() - 10 - altPanel.getX()));
            altRect.setClickable(true);
            altRect.setSelected(false);
            altRect.setBackgroundColor(ColorUtils.tripleColor(27));
            altRect.setRemoveShit(true);
            altRect.drawScreen(mouseX, mouseY);
            MinecraftInstance.lettuceBoldFont22.drawCenteredString("Current Account", altRect.getX() + altRect.getWidth() / 2f,
                    altRect.getY() - (MinecraftInstance.lettuceBoldFont22.getHeight() + 5), -1);
        }

        /* Search bar */
        searchField.setRadius(5);
        searchField.setFill(ColorUtils.tripleColor(17, 1));
        searchField.setOutline(ColorUtils.applyOpacity(Color.WHITE, 0));

        searchField.setHeight(20.5F);
        searchField.setWidth(145.5F);

        searchField.setXPosition(width - searchField.getRealWidth() - 20);
        searchField.setYPosition(45);
        searchField.setBackgroundText("Search");
        searchField.drawTextBox();
        /* End search bar */

        /* Filter banned button */
        filterBanned.setX(searchField.getXPosition() + 85);
        filterBanned.setBypass(true);
        filterBanned.setAlpha(1);
        filterBanned.setY(searchField.getYPosition() - (filterBanned.getWH() + 10));
        filterBanned.drawScreen(mouseX, mouseY);
        /* End filter banned button */

        Lecture.INSTANCE.getModuleManager().getModule(HUD.class).drawNotifications();
        if (Alt.stage != 0) {
            AltPanel.loadingAltRect = null;
        }
        switch (Alt.stage) {
            case 1: {
                NotificationManager.post(NotificationType.INFO, "Alt Manager", "Invalid credentials!", 3);
                Alt.stage = 0;
            }
            break;

            case 2: {
                NotificationManager.post(NotificationType.SUCCESS, "Alt Manager", "Logged in successfully!", 3);
                Alt.stage = 0;
            }
            break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        filterBanned.mouseClicked(mouseX, mouseY, mouseButton);
        panels.forEach(panel -> panel.mouseClicked(mouseX, mouseY, mouseButton));
        if (currentSessionAlt != null) {
            altRect.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        panels.forEach(panel -> panel.mouseReleased(mouseX, mouseY, state));
    }

    public AltManagerUtils getUtils() {
        return utils;
    }

    public AltPanel getAltPanel() {
        return (AltPanel) panels.get(1);
    }

    public boolean isTyping() {
        return searchField.isFocused() || ((LoginPanel) panels.get(0)).textFields.stream().anyMatch(TextField::isFocused);
    }
}