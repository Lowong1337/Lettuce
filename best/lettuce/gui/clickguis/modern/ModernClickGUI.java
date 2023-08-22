package best.lettuce.gui.clickguis.modern;

import best.lettuce.modules.Category;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.non_utils.drag.Drag;
import best.lettuce.utils.render.RoundedUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModernClickGUI extends GuiScreen implements MinecraftInstance {

    public boolean hover;

    public List<CategoryRect> categories;

    @Override
    public void initGui() {
        if(categories == null){
            int count = 1;
            categories = new ArrayList<>();
            for (Category category : Category.values()) {
                categories.add(new CategoryRect(category, this.width / 2f - 240, 2 + 50 + 25 * count, 50, 20));
                count++;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        RoundedUtils.drawRound(this.width /2f - 250, this.height / 2f - 150, 500, 300, 10, new Color(0,0,0, 220));
        icontestFont40.drawString(FontUtils.LETTUCE, this.width /2f - 30, this.height / 2f - 150 + 11, Color.WHITE);
        lettuceBoldFont32.drawString("Lettuce", this.width /2f - 30 + icontestFont40.getStringWidth(FontUtils.LETTUCE), this.height / 2f - 150 + 10, Color.WHITE);
        if(categories != null) {
            categories.forEach(categoryRect -> categoryRect.drawScreen(mouseX, mouseY));
        }
    }
}
