package best.lettuce.modules.impl.render;

import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import net.minecraft.util.ResourceLocation;

public class LSD extends Module {
    public LSD() {
        super("LSD", Category.RENDER, "Use drug when hacking hypixel");
    }

    public void onEnable() {
        this.mc.entityRenderer.loadShader(new ResourceLocation("/shaders/post/wobble.json"));
    }

    public void onDisable() {
        this.mc.entityRenderer.stopUseShader();
    }

}
