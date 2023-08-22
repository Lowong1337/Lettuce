package best.lettuce.modules.impl.render;

import best.lettuce.Lecture;
import best.lettuce.event.impl.render.EventShader;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.MultipleBoolProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.render.RenderUtils;
import best.lettuce.utils.render.blur.KawaseBloom;
import best.lettuce.utils.render.blur.KawaseBlur;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;

import java.awt.*;

public class VisualEffects extends Module {

    public final BooleanProperty blur = new BooleanProperty("Blur", true);
    public final NumberProperty iterations = new NumberProperty("Blur Iterations", 2, 1, 8, 1);
    public final NumberProperty offset = new NumberProperty("Blur Offset", 3, 1, 10, 1);
    public static final BooleanProperty bloom = new BooleanProperty("Bloom", true);
    public static MultipleBoolProperty glowOptions = new MultipleBoolProperty("Glow Options", bloom::isEnabled,
            new BooleanProperty("Arraylist", true),
            new BooleanProperty("ClickGui", false),
            new BooleanProperty("Notifications", false),
            new BooleanProperty("Spotify", true));
    public final NumberProperty shadowRadius = new NumberProperty("Bloom Iterations", 3, 1, 8, 1, bloom::isEnabled);
    public final NumberProperty shadowOffset = new NumberProperty("Bloom Offset", 1, 1, 10, 1, bloom::isEnabled);

    public VisualEffects() {
        super("Visual Effects", Category.RENDER, "blurs shit");
        addProperties(blur, iterations, offset, bloom, glowOptions, shadowRadius, shadowOffset);
    }

    public void stuffToBlur(boolean bloom) {

        ScaledResolution sr = new ScaledResolution();

        if (mc.currentScreen instanceof GuiChat) {
            Gui.drawRect2(2, sr.getHeight() - 14, sr.getWidth() - 4, 12, Color.BLACK.getRGB());
        }



        /*if (mc.currentScreen == ClickGUIMod.dropdownClickGui || mc.currentScreen == ClickGUIMod.modernClickGui || mc.currentScreen == ClickGUIMod.compactClickgui) {
            Lettuce.INSTANCE.getSideGui().drawForEffects(bloom);
            Lettuce.INSTANCE.getSearchBar().drawEffects();
        }*/

        ColorUtils.resetColor();
        //mc.ingameGUI.getChatGUI().renderChatBox();
        ColorUtils.resetColor();

        mc.ingameGUI.renderScoreboardBlur(sr);

    }

    public void blurStuff(boolean bloom) {
        //ClickGUI
        if (mc.currentScreen == ClickGUI.dropdownClickGui) {
            if (bloom) {
                if (glowOptions.isEnabled("ClickGui")) {
                    ClickGUI.dropdownClickGui.renderEffects();
                }
            } else {
                ClickGUI.dropdownClickGui.renderEffects();
            }
        }

    }

    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    public void blurScreen() {
        if (!isEnabled()) return;
        if (blur.isEnabled()) {
            stencilFramebuffer = RenderUtils.createFrameBuffer(stencilFramebuffer);

            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);

            Lecture.INSTANCE.getEventManager().dispatch(new EventShader(false, glowOptions));
            blurStuff(false);

            stencilFramebuffer.unbindFramebuffer();

            KawaseBlur.renderBlur(stencilFramebuffer.framebufferTexture, iterations.getValue().intValue(), offset.getValue().intValue());
        }

        if (bloom.isEnabled()) {
            stencilFramebuffer = RenderUtils.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);

            Lecture.INSTANCE.getEventManager().dispatch(new EventShader(true, glowOptions));
            blurStuff(true);

            stencilFramebuffer.unbindFramebuffer();

            KawaseBloom.renderBlur(stencilFramebuffer.framebufferTexture, shadowRadius.getValue().intValue(), shadowOffset.getValue().intValue());
        }
    }
}