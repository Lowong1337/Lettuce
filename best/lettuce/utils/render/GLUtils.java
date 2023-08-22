package best.lettuce.utils.render;

import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.GlStateManager;

import static org.lwjgl.opengl.GL11.*;

@UtilityClass
public class GLUtils {
    public void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void endBlend() {
        GlStateManager.disableBlend();
    }

    public void setup2DRendering(boolean blend) {
        if (blend) {
            startBlend();
        }
        GlStateManager.disableTexture2D();
    }

    public void end2DRendering() {
        GlStateManager.enableTexture2D();
        endBlend();
    }
}
