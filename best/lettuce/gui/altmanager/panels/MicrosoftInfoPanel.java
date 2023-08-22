package best.lettuce.gui.altmanager.panels;

import best.lettuce.gui.altmanager.Panel;
import best.lettuce.utils.color.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class MicrosoftInfoPanel extends Panel {

    private final List<String[]> steps = new ArrayList<>();

    public MicrosoftInfoPanel() {
        setHeight(135);
        steps.add(new String[]{"1", "Type the email and password either as a combo or in each respective field"});
        steps.add(new String[]{"2", "Click the microsoft login button"});
        steps.add(new String[]{"3", "Your browser will open with a microsoft login panel"});
        steps.add(new String[]{"INFO", "Make sure that you are logged out of all microsoft accounts so that you are prompted with a login panel"});
        steps.add(new String[]{"4", "The email and password will be copied to the clipboard"});
        steps.add(new String[]{"5", "Follow all the steps Microsoft gives you to log into your account"});
        steps.add(new String[]{"6", "Enjoy! You are now logged in to your microsoft account"});
    }

    @Override
    public void initGui() {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }


    @Override
    public void drawScreen(int mouseX, int mouseY) {
        setHeight(119);
        super.drawScreen(mouseX, mouseY);
        lettuceBoldFont26.drawCenteredString("How to use Microsoft Login", getX() + getWidth() / 2f, getY() + 4, ColorUtils.applyOpacity(-1, .75f));

        float controlY = getY() + lettuceBoldFont32.getHeight() + 8;
        for (String[] control : steps) {
            if (control[0].equals("INFO")) {
                lettuceFont16.drawCenteredString("Make sure that you are logged out of all microsoft accounts so that you are", getX() + getWidth() / 2f, controlY, ColorUtils.applyOpacity(-1, .75f));

                controlY += lettuceBoldFont16.getHeight() + 4;

                lettuceFont16.drawCenteredString("prompted with a new login panel", getX() + getWidth() / 2f, controlY, ColorUtils.applyOpacity(-1, .75f));

                controlY += lettuceBoldFont16.getHeight() + 6;
                continue;
            }

            lettuceBoldFont16.drawString(control[0] + ". ", getX() + 12, controlY, ColorUtils.applyOpacity(-1, .5f));
            lettuceFont16.drawString(control[1], getX() + lettuceBoldFont16.getStringWidth(control[0] + ". ") + 14, controlY, ColorUtils.applyOpacity(-1, .35f));

            controlY += lettuceBoldFont16.getHeight() + 6;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }
}
