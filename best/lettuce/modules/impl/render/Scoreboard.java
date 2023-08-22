package best.lettuce.modules.impl.render;

import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.fonts.AbstractFontRenderer;

public class Scoreboard extends Module {

    public final NumberProperty yOffset = new NumberProperty("Y Offset", 0, 0, 250, 5);

    public final ModeProperty font = new ModeProperty("Scoreboard Font", "Lettuce", "Lettuce", "Minecraft", "Tahoma", "Neverlose");
    public final BooleanProperty redNumbers = new BooleanProperty("Red Numbers", false);

    public Scoreboard() {
        super("Scoreboard", Category.RENDER, "Scoreboard preferences");
        this.addProperties(yOffset, font, redNumbers);
        this.setEnabled(true);
    }

    public AbstractFontRenderer getFont(boolean allowBold) {
        return switch (font.getMode()) {
            default -> mc.fontRendererObj;
            case "Lettuce" -> lettuceFont20;
            case "Tahoma" -> tahoma20;
            case "Neverlose" -> neverlose20;
        };
    }
}