package best.lettuce.gui.clickguis.modern;

import best.lettuce.gui.Screen;
import best.lettuce.modules.Category;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public class CategoryRect implements Screen {

    public ModernClickGUI gui = new ModernClickGUI();

    @Getter
    public Category category;
    @Getter @Setter
    public float x, y, width, height;
    public CategoryRect(Category c, float x, float y, float width, float height) {
        this.category = c;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void initGui() {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        lettuceBoldFont22.drawString(category.name, x + icontestFont26.getStringWidth(category.icon) + 5, y + lettuceBoldFont22.getMiddleOfBox(height), Color.WHITE);
        icontestFont26.drawString(category.icon, x, y + icontestFont26.getMiddleOfBox(height), Color.WHITE);
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        this.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }
}
