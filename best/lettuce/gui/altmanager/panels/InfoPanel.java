package best.lettuce.gui.altmanager.panels;

import best.lettuce.gui.altmanager.Panel;
import best.lettuce.utils.color.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class InfoPanel extends Panel {

    private final List<String[]> controlInfo = new ArrayList<>();

    public InfoPanel() {
        setHeight(135);
        controlInfo.add(new String[]{"CTRL+V", "Paste a combo or combo list anywhere on the screen to import it"});
        controlInfo.add(new String[]{"DELETE", "When an alt is selected, you can delete it by pressing the delete key"});
        controlInfo.add(new String[]{"CTRL+A", "Selects the entire alt list"});
        controlInfo.add(new String[]{"CTRL+C", "Copies the combos of the currently selected alts"});
        controlInfo.add(new String[]{"SHIFT+CLICK", "Allows you to select a specfic range of alts"});
        controlInfo.add(new String[]{"DOUBLE-CLICK", "Logs into the selected alt"});
    }

    @Override
    public void initGui() {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        super.drawScreen(mouseX, mouseY);
        lettuceBoldFont32.drawCenteredString("Information", getX() + getWidth() / 2f, getY() + 3, ColorUtils.applyOpacity(-1, .75f));

        float controlY = getY() + lettuceBoldFont32.getHeight() + 8;
        for (String[] control : controlInfo) {
            lettuceBoldFont18.drawString(control[0] + " -", getX() + 12, controlY, ColorUtils.applyOpacity(-1, .5f));
            lettuceFont18.drawString(control[1], getX() + lettuceBoldFont18.getStringWidth(control[0] + " -") + 14, controlY, ColorUtils.applyOpacity(-1, .35f));

            controlY += lettuceBoldFont18.getHeight() + 6;
        }

        String text = "Combos must be formatted in the following format: ";
        String text2 = "email:password";
        float textWidth = lettuceFont18.getStringWidth(text);
        float text2Width = lettuceBoldFont18.getStringWidth(text2);
        float middleX = getX() + getWidth() / 2f - (textWidth + text2Width) / 2f;

        lettuceFont18.drawString(text, middleX, controlY + 4, ColorUtils.applyOpacity(-1, .5f));
        lettuceBoldFont18.drawString(text2, middleX + textWidth, controlY + 4, ColorUtils.applyOpacity(-1, .5f));

        lettuceFont18.drawCenteredString("Combo lists must have a new line seperating each combo", getX() + getWidth() / 2f, controlY + 16, ColorUtils.applyOpacity(-1, .5f));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }
}
