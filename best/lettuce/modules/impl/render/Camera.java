package best.lettuce.modules.impl.render;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.event.impl.render.EventRender2D;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ColorProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.MultipleBoolProperty;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.awt.*;

public class Camera extends Module {
    //Options
    public final MultipleBoolProperty options = new MultipleBoolProperty("Options",
            new BooleanProperty("Hurt Camera", true),
            new BooleanProperty("Full Bright", true),
            new BooleanProperty("Remove Fire Overlay", true),
            new BooleanProperty("Remove Pumpkin Overlay", true),
            new BooleanProperty("Remove Blindness Effect", true));

    //Hurt Cam
    public final ModeProperty hurtCamera = new ModeProperty("Hurt Camera Mode", "Cancel",
            () -> options.isEnabled("Hurt Camera"), "Cancel", "Border");
    public final MultipleBoolProperty borders = new MultipleBoolProperty("Borders", () -> (options.isEnabled("Hurt Camera") && hurtCamera.is("Border")),
            new BooleanProperty("Top", true),
            new BooleanProperty("Bottom", true));
    public final ColorProperty hurtCamColor = new ColorProperty("Border Color", Color.RED, () -> (options.isEnabled("Hurt Camera") && hurtCamera.is("Border")));

    //Full Bright
    public final ModeProperty fullBrightMode = new ModeProperty("Full Bright Mode", "Gamma", () -> options.isEnabled("Full Bright"), "Gamma", "Night Vision");

    public Camera() {
        super("Camera", Category.RENDER, "Change some things that your Minecraft displays.");
        addProperties(this.options, this.hurtCamera, this.borders, this.hurtCamColor, this.fullBrightMode);
    }

    public final EventListener<EventMotion> onMotion = event -> {
        if (options.isEnabled("Full Bright")) {
            switch (fullBrightMode.getMode()) {
                case "Gamma": mc.gameSettings.gammaSetting = 100; break;
                case "Night Vision": mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, 99999, 0)); break;
            }
        }
    };

    public final EventListener<EventRender2D> onGuiRender = e -> {
        if (hurtCamera.is("Border") && options.isEnabled("Hurt Camera")) {
            ScaledResolution sr = new ScaledResolution();
            Color color = hurtCamColor.getColor();
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), 12 * mc.thePlayer.hurtTime);
            if (this.mc.thePlayer.hurtTime >= 0.5D) {
                int width = sr.getWidth(), height = sr.getHeight(), heightDivision = 6;
                if (borders.isEnabled("Top")) Gui.drawGradientRect(0, 0, width, height / heightDivision, c.getRGB(), 0);
                if (borders.isEnabled("Bottom")) Gui.drawGradientRect(0, height - height / heightDivision, width, height, 0, c.getRGB());
            }
        }
    };

    @Override
    public void onDisable() {
        mc.gameSettings.gammaSetting = 1.0f;
        mc.thePlayer.removePotionEffect(Potion.nightVision.id);
    }
}
