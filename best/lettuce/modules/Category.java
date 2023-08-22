package best.lettuce.modules;

import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.non_utils.scroll.Scroll;
import best.lettuce.utils.non_utils.drag.Drag;
import lombok.Getter;

public enum Category {

    COMBAT("Combat", FontUtils.COMBAT),
    MOVEMENT("Movement", FontUtils.MOVEMENT),
    RENDER("Render", FontUtils.RENDER),
    PLAYER("Player", FontUtils.PLAYER),
    EXPLOIT("Exploit", FontUtils.EXPLOIT),
    MISC("Misc", FontUtils.MISC);
    //SCRIPTS("Scripts", FontUtils.SCRIPT);

    public final String name;
    public final String icon;
    public final int posX;
    public final boolean expanded;

    @Getter
    private final Scroll scroll = new Scroll();

    @Getter
    private final Drag drag;
    public int posY = 20;

    Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
        posX = 20 + (Module.categoryCount * 130);
        drag = new Drag(posX, posY);
        expanded = true;
        Module.categoryCount++;
    }
}
