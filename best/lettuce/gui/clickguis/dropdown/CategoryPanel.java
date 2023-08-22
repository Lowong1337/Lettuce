package best.lettuce.gui.clickguis.dropdown;

import best.lettuce.Lecture;
import best.lettuce.gui.Screen;
import best.lettuce.gui.clickguis.dropdown.components.ModuleRect;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.ModuleManager;
import best.lettuce.modules.property.Property;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.math.MathUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.render.RoundedUtils;
import best.lettuce.utils.render.StencilUtils;
import lombok.Getter;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryPanel implements Screen {

    private final Category category;

    private final float rectWidth = 120;
    private final float categoryRectHeight = 15;
    @Getter
    private boolean typing;

    public final Animation[] openingAnimations;
    private List<ModuleRect> moduleRects;

    public CategoryPanel(Category category, Animation[] openingAnimations) {
        this.category = category;
        this.openingAnimations = openingAnimations;
    }

    @Override
    public void initGui() {
        if (moduleRects == null) {
            moduleRects = new ArrayList<>();
            for (Module module : Lecture.INSTANCE.getModuleManager().getModulesInCategory(category).stream().sorted(Comparator.comparing(Module::getName)).collect(Collectors.toList())) {
                moduleRects.add(new ModuleRect(module));
            }
        }
        if (moduleRects != null) {
            moduleRects.forEach(ModuleRect::initGui);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (moduleRects != null) {
            moduleRects.forEach(moduleRect -> moduleRect.keyTyped(typedChar, keyCode));
        }
    }

    public void onDrag(int mouseX, int mouseY) {
        category.getDrag().onDraw(mouseX, mouseY);
    }

    float actualHeight = 0;

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        if (moduleRects == null) {
            return;
        }

        if (ModuleManager.reloadModules) {
            moduleRects.clear();
            for (Module module : Lecture.INSTANCE.getModuleManager().getModulesInCategory(category).stream().sorted(Comparator.comparing(Module::getName)).collect(Collectors.toList())) {
                moduleRects.add(new ModuleRect(module));
            }
            ModuleManager.reloadModules = false;
            return;
        }

        if (openingAnimations == null) return;

        float alpha = Math.min(1, openingAnimations[0].getOutput().floatValue());

        //Multiply it by the alpha again so that it eases faster
        float alphaValue = alpha * alpha * .5f;

        int textColor = ColorUtils.applyOpacity(-1, alpha);

        float x = category.getDrag().getX(), y = category.getDrag().getY();

        float allowedHeight = DropdownClickGUI.allowedClickGuiHeight;

        boolean hoveringMods = MouseUtils.isHovering(x, y + categoryRectHeight, rectWidth, allowedHeight, mouseX, mouseY);

        ColorUtils.resetColor();
        float realHeight = Math.min(actualHeight, DropdownClickGUI.allowedClickGuiHeight);

        RoundedUtils.drawRound(x - .75f, y - .5f, rectWidth + 1.5f, realHeight + categoryRectHeight + 1.5f, 5, ColorUtils.tripleColor(20, alphaValue));

        StencilUtils.initStencilToWrite();
        RoundedUtils.drawRound(x + 1, y + categoryRectHeight + 5, rectWidth - 2, realHeight - 6, 3, Color.BLACK);
        Gui.drawRect2(x, y + categoryRectHeight, rectWidth, 10, Color.BLACK.getRGB());
        StencilUtils.readStencilBuffer(1);

        double scroll = category.getScroll().getScroll();
        double count = 0;

        float rectHeight = 14;

        for (ModuleRect moduleRect : getModuleRects()) {
            moduleRect.alpha = alpha;
            moduleRect.x = x - .5f;
            moduleRect.height = rectHeight;
            moduleRect.panelLimitY = y + categoryRectHeight - 2;
            moduleRect.y = (float) (y + categoryRectHeight + (count * rectHeight) + MathUtils.roundToHalf(scroll));
            moduleRect.width = rectWidth + 1;
            moduleRect.drawScreen(mouseX, mouseY);

            // count ups by one but then accounts for setting animation opening
            count += 1 + (moduleRect.getSettingSize() * (16 / 14f));
        }

        typing = getModuleRects().stream().anyMatch(ModuleRect::isTyping);

        actualHeight = (float) (count * rectHeight);

        if (hoveringMods) {
            category.getScroll().onScroll(25);
            float hiddenHeight = (float) ((count * rectHeight) - allowedHeight);
            category.getScroll().setMaxScroll(Math.max(0, hiddenHeight));
        }

        StencilUtils.uninitStencilBuffer();
        ColorUtils.resetColor();
        icontestFont26.drawString(category.icon, x + 3, y + lettuceBoldFont22.getMiddleOfBox(categoryRectHeight) + (category.name.equals(Category.COMBAT.name) || category.name.equals(Category.RENDER.name)? 1.5f : 2), textColor);
        lettuceBoldFont22.drawCenteredString(category.name, x + rectWidth / 2, y + lettuceBoldFont22.getMiddleOfBox(categoryRectHeight), textColor);
    }

    public void renderEffects() {
        float x = category.getDrag().getX(), y = category.getDrag().getY();

        float alpha = Math.min(1, openingAnimations[0].getOutput().floatValue());
        alpha *= alpha;

        float allowedHeight = Math.min(actualHeight, DropdownClickGUI.allowedClickGuiHeight);

        RoundedUtils.drawRound(x - .75f, y - .5f, rectWidth + 1.5f, allowedHeight + categoryRectHeight + 1.5f, 5, Color.BLACK);
        RoundedUtils.drawRound(x - .75f, y - .5f, rectWidth + 1.5f, allowedHeight + categoryRectHeight + 1.5f, 5, ColorUtils.applyOpacity(Color.BLACK, alpha));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean canDrag = MouseUtils.isHovering(category.getDrag().getX(), category.getDrag().getY(), rectWidth, categoryRectHeight, mouseX, mouseY);
        category.getDrag().onClick(mouseX, mouseY, button, canDrag);
        getModuleRects().forEach(moduleRect -> moduleRect.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        category.getDrag().onRelease(state);
        getModuleRects().forEach(moduleRect -> moduleRect.mouseReleased(mouseX, mouseY, state));
    }

    private final List<String> searchTerms = new ArrayList<>();
    private String searchText;
    private final List<ModuleRect> moduleRectFilter = new ArrayList<>();

    public List<ModuleRect> getModuleRects() {
        if (!searchBar.isFocused()) {
            return moduleRects;
        }

        String search = searchBar.getSearchField().getText();

        if (search.equals(searchText)) {
            return moduleRectFilter;
        } else {
            searchText = search;
        }

        moduleRectFilter.clear();
        for (ModuleRect moduleRect : moduleRects) {
            searchTerms.clear();
            Module module = moduleRect.module;

            searchTerms.add(module.getName());
            searchTerms.add(module.getCategory().name);
            /*if (!module.getAuthor().isEmpty()) {
                searchTerms.add(module.getAuthor());
            }*/
            for (Property setting : module.getProperties()) {
                searchTerms.add(setting.name);
            }

            moduleRect.setSearchScore(FuzzySearch.extractOne(search, searchTerms).getScore());
        }

        moduleRectFilter.addAll(moduleRects.stream().filter(moduleRect -> moduleRect.getSearchScore() > 60).sorted(Comparator.comparingInt(ModuleRect::getSearchScore).reversed()).collect(Collectors.toList()));

        return moduleRectFilter;
    }
}
