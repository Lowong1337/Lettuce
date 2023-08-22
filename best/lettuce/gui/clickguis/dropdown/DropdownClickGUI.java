package best.lettuce.gui.clickguis.dropdown;

import best.lettuce.Lettuce;
import best.lettuce.alt.AltBox;
import best.lettuce.config.ConfigManager;
import best.lettuce.gui.ConfigPanel;
import best.lettuce.modules.Category;
import best.lettuce.modules.impl.render.ClickGUI;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.animation.impl.EaseBackIn;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RenderUtils;
import best.lettuce.utils.render.RoundedUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DropdownClickGUI extends GuiScreen implements MinecraftInstance {

    public final Animation[] openingAnimations = new Animation[]{new EaseBackIn(400, 1, 1f), new EaseBackIn(400, .4f, 1f)};

    private List<CategoryPanel> categoryPanels;
    private final ConfigPanel configPanel = new ConfigPanel();

    public final Animation anim = new DecelerateAnimation(200, 1f);
    public final Animation widenanim = new DecelerateAnimation(300, 1f);
    public final Animation hightanim = new DecelerateAnimation(800, 1f);
    public final Animation textanim = new DecelerateAnimation(200, 1f);

    public List<AltBox> cfgs = new ArrayList<>();

    public boolean binding;
    public boolean hovered;
    public boolean hovered2;

    public static float allowedClickGuiHeight = 0;

    @Override
    public void onDrag(int mouseX, int mouseY) {
        for (CategoryPanel catPanels : categoryPanels) {
            catPanels.onDrag(mouseX, mouseY);
        }
    }

    @Override
    public void initGui() {
        Animation fade = openingAnimations[0], opening = openingAnimations[1];
        widenanim.setDirection(Direction.BACKWARDS);
        hightanim.setDirection(Direction.BACKWARDS);
        textanim.setDirection(Direction.BACKWARDS);
        fade.setDirection(Direction.FORWARDS);
        opening.setDirection(Direction.FORWARDS);

        if (categoryPanels == null) {
            categoryPanels = new ArrayList<>();
            for (Category category : Category.values()) {
                categoryPanels.add(new CategoryPanel(category, openingAnimations));
            }
        }
        int i = 1;
        for (File file : Objects.requireNonNull(ConfigManager.file.listFiles())){
            //RoundedUtils.drawRound(10, height - hightanim.getOutput().floatValue() * 175 + 25 * i, widenanim.getOutput().floatValue() * 90, 20, 1.5f, new Color(0,0,0, 90));
            if(cfgs.contains(cfgs.add(new AltBox(10, height - 175 + 25 * i, 90, 20, file.getName())))) {
                cfgs.add(new AltBox(10, height - 175 + 25 * i, 90, 20, file.getName()));
            }
            //MinecraftInstance.lettuceFont20.drawString(file.getName().split("\\.")[0], 15, height - hightanim.getOutput().floatValue() * 170 + 25 * i, ColorUtils.interpolateColorC(new Color(255,255,255, 0), Color.WHITE, hightanim.getOutput().floatValue()));
            //
            i++;
        }
        searchBar.initGui();

        for (CategoryPanel catPanels : categoryPanels) {
            catPanels.initGui();
        }
        configPanel.initGui();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE && !binding) {
            if (searchBar.isFocused()) {
                searchBar.getSearchField().setText("");
                searchBar.getSearchField().setFocused(false);
                widenanim.setDirection(Direction.BACKWARDS);
                hightanim.setDirection(Direction.BACKWARDS);
                return;
            }

            searchBar.getOpenAnimation().setDirection(Direction.BACKWARDS);

            Animation fade = openingAnimations[0], opening = openingAnimations[1];
            fade.setDirection(Direction.BACKWARDS);
            opening.setDirection(Direction.BACKWARDS);
            widenanim.setDirection(Direction.BACKWARDS);
            hightanim.setDirection(Direction.BACKWARDS);
            textanim.setDirection(Direction.BACKWARDS);
        }
        searchBar.keyTyped(typedChar, keyCode);
        categoryPanels.forEach(categoryPanel -> categoryPanel.keyTyped(typedChar, keyCode));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.hovered = MouseUtils.isHovering(5, this.height - 25, 20, 20, mouseX, mouseY);
        this.hovered2 = MouseUtils.isHovering(5, this.height - 25 - hightanim.getOutput().floatValue() * 150, 20 + widenanim.getOutput().floatValue() * 80, 20 + hightanim.getOutput().floatValue() * 150, mouseX, mouseY) && widenanim.getDirection() == Direction.FORWARDS;
        if (this.hovered || widenanim.getDirection() == Direction.FORWARDS) {
            anim.setDirection(Direction.FORWARDS);
        }
        if (!this.hovered && widenanim.getDirection() != Direction.FORWARDS) {
            anim.setDirection(Direction.BACKWARDS);
        }
        ScaledResolution sr = new ScaledResolution();
        allowedClickGuiHeight = 2 * sr.getHeight() / 3f;

        binding = categoryPanels.stream().anyMatch(CategoryPanel::isTyping) || searchBar.isTyping();

        RenderUtils.scaleStart(sr.getWidth() / 2f, sr.getHeight() / 2f, openingAnimations[1].getOutput().floatValue() + .6f);

        Gui.drawRect2(0, 0, width, height, ColorUtils.applyOpacity(0, searchBar.getFocusAnimation().getOutput().floatValue() * .25f));

        if (openingAnimations[1].finished(Direction.BACKWARDS)) {
            MinecraftInstance.mc.displayGuiScreen(null);
            return;
        }

        boolean focusedConfigGui = searchBar.isTyping();
        int fakeMouseX = focusedConfigGui ? 0 : mouseX, fakeMouseY = focusedConfigGui ? 0 : mouseY;

        for (CategoryPanel catPanels : categoryPanels) {
            catPanels.drawScreen(fakeMouseX, fakeMouseY);
        }
        RenderUtils.scissorStart(0, this.height - 30 - hightanim.getOutput().floatValue() * 147, 30 + widenanim.getOutput().floatValue() * 77, 30 + hightanim.getOutput().floatValue() * 147);
        RoundedUtils.drawRound(5, this.height - 25 - hightanim.getOutput().floatValue() * 150, 20 + widenanim.getOutput().floatValue() * 80, 20 + hightanim.getOutput().floatValue() * 150, 5f, ColorUtils.interpolateColorC(new Color(0, 0, 0, 100), new Color(0, 0, 0, 150), anim.getOutput().floatValue()));
        MinecraftInstance.icontestFont26.drawString(FontUtils.CONFIG, 9f, this.height - 18 - hightanim.getOutput().floatValue() * 150, ColorUtils.interpolateColorC(Color.WHITE, Color.WHITE.darker(), anim.getOutput().floatValue()));
        MinecraftInstance.lettuceBoldFont22.drawCenteredString("Configs", 55, this.height - 170, ColorUtils.interpolateColorC(new Color(255, 255, 255, 0), Color.WHITE, textanim.getOutput().floatValue()));
        RoundedUtils.drawRound(80, this.height - 25, 15, 15, 1.2f, new Color(0, 0, 0, 50));
        MinecraftInstance.icontestFont26.drawString(FontUtils.SAVE, 83, this.height - 21.5f, ColorUtils.interpolateColorC(Color.WHITE, Color.WHITE.darker(), anim.getOutput().floatValue()));
        cfgs.forEach(altBox -> altBox.drawBox(mouseX, mouseY));
        RenderUtils.scissorEnd();

        RenderUtils.scaleEnd();
    }

    public void renderEffects() {
        ScaledResolution sr = new ScaledResolution();
        RenderUtils.scaleStart(sr.getWidth() / 2f, sr.getHeight() / 2f, openingAnimations[1].getOutput().floatValue() + .6f);
        for (CategoryPanel catPanels : categoryPanels) {
            catPanels.renderEffects();
        }
        RenderUtils.scaleEnd();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        searchBar.mouseClicked(mouseX, mouseY, mouseButton);
        categoryPanels.forEach(cat -> cat.mouseClicked(mouseX, mouseY, mouseButton));
        if (hovered) {
            if (mouseButton == 0) {
                textanim.setDirection(textanim.getDirection() == Direction.BACKWARDS ? Direction.FORWARDS : Direction.BACKWARDS);
                widenanim.setDirection(widenanim.getDirection() == Direction.BACKWARDS ? Direction.FORWARDS : Direction.BACKWARDS);
                hightanim.setDirection(hightanim.getDirection() == Direction.BACKWARDS ? Direction.FORWARDS : Direction.BACKWARDS);
                if (widenanim.getDirection() == Direction.FORWARDS && hightanim.getDirection() == Direction.FORWARDS) {
                    ConfigManager cfm = new ConfigManager();
                    cfm.collectConfigs();
                }
            }
        } else if(!hovered2) {
            textanim.setDirection(Direction.BACKWARDS);
            widenanim.setDirection(Direction.BACKWARDS);
            hightanim.setDirection(Direction.BACKWARDS);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        searchBar.mouseReleased(mouseX, mouseY, state);
        categoryPanels.forEach(cat -> cat.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    public void onGuiClosed() {
        if (ClickGUI.rescale.isEnabled()) {
            MinecraftInstance.mc.gameSettings.guiScale = ClickGUI.prevGuiScale;
        }
        Lettuce.INSTANCE.getConfigManager().saveDefaultConfig();
        cfgs.clear();
    }
}