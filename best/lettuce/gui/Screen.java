package best.lettuce.gui;

import best.lettuce.utils.MinecraftInstance;

public interface Screen extends MinecraftInstance {

    default void onDrag(int mouseX, int mouseY) {}

    void initGui();

    void keyTyped(char typedChar, int keyCode);

    void drawScreen(int mouseX, int mouseY);

    void mouseClicked(int mouseX, int mouseY, int button);

    void mouseReleased(int mouseX, int mouseY, int state);

}