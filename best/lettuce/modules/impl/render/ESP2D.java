package best.lettuce.modules.impl.render;

import best.lettuce.Lettuce;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.render.EventRender2D;
import best.lettuce.event.impl.render.EventRender3D;
import best.lettuce.event.impl.render.EventShader;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.impl.combat.AntiBot;
import best.lettuce.modules.property.impl.*;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.AbstractFontRenderer;
import best.lettuce.utils.math.MathUtils;
import best.lettuce.utils.render.ESPUtils;
import best.lettuce.utils.render.GLUtils;
import best.lettuce.utils.render.GradientUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;
import org.lwjgl.util.vector.Vector4f;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class ESP2D extends Module {

    private final MultipleBoolProperty validEntities = new MultipleBoolProperty("Render On",
            new BooleanProperty("Players", true),
            new BooleanProperty("Animals", true),
            new BooleanProperty("Mobs", true),
            new BooleanProperty("Items", false),
            new BooleanProperty("Invisible", true));

    private final MultipleBoolProperty esp = new MultipleBoolProperty("ESP Options",
            new BooleanProperty("Box", true),
            new BooleanProperty("Held Item", true),
            new BooleanProperty("Armor", true),
            new BooleanProperty("Health", true),
            new BooleanProperty("Nametags", true));

    private final ModeProperty boxColorMode = new ModeProperty("Box Color Mode", "Sync", () -> esp.isEnabled("Box"), "Sync", "Custom");
    public final ModeProperty boxColorModes = new ModeProperty("Box Colors", "Static", () -> boxColorMode.is("Custom"), HUD.colorModes);
    public final ColorProperty color1 = new ColorProperty("Color", new Color(0xffa028d4), () -> boxColorMode.is("Custom"));
    public final ColorProperty color2 = new ColorProperty("Secondary Color", new Color(0xff0008ff), () -> (boxColorModes.is("Double Colors") && boxColorMode.is("Custom")));
    public final NumberProperty saturation = new NumberProperty("Rainbow Saturation", 1, 0, 1, .05, () -> (boxColorModes.is("Rainbow") && boxColorMode.is("Custom")));
    public final NumberProperty colorSpeed = new NumberProperty("Color Speed", 15, 2, 30, 1, () -> (!boxColorModes.is("Static") && boxColorMode.is("Custom")));
    public final NumberProperty colorIndex = new NumberProperty("Color Separation", 20, 5, 100, 1, () -> (!boxColorModes.is("Static") && boxColorMode.is("Custom")));

    private final ModeProperty healthBarMode = new ModeProperty("Health Bar Color Mode", "Health", () -> esp.isEnabled("Health"), "Health", "Sync");
    private final MultipleBoolProperty healthText = new MultipleBoolProperty("Display Health On", () -> esp.isEnabled("Health"),
            new BooleanProperty("Bar", true),
            new BooleanProperty("Nametag", true));

    private final BooleanProperty nametagBackground = new BooleanProperty("Nametag Background", true, () -> esp.isEnabled("Nametags"));
    private final ColorProperty nametagBackgroundColor = new ColorProperty("Nametag Background Color", new Color(10, 10, 10), () -> (esp.isEnabled("Nametags") && nametagBackground.isEnabled()), 0.5f);
    private final NumberProperty scale = new NumberProperty("Tag Scale", .75, .35, 1, .05, () -> esp.isEnabled("Nametags"));
    private final BooleanProperty formattedTags = new BooleanProperty("Formatted Tags", true, () -> esp.isEnabled("Nametags"));

    private final NumberFormat df = new DecimalFormat("0.#");

    private Color firstColor = Color.BLACK, secondColor = Color.BLACK, thirdColor = Color.BLACK, fourthColor = Color.BLACK;

    public ESP2D() {
        super("2D ESP", Category.RENDER, "Draws a box in 2D space around entities.");
        addProperties(validEntities, esp, boxColorMode, boxColorModes, color1, color2, saturation, colorSpeed, colorIndex, healthBarMode, healthText, nametagBackground, nametagBackgroundColor, scale, formattedTags);
    }

    private final Map<Entity, Vector4f> entityPosition = new HashMap<>();

//    @Override
//    public void onNametagRenderEvent(NametagRenderEvent e) {
//        if (esp.isEnabled("Nametags)) e.cancel();
//    }

    @Override
    public void onDisable() {
        entityPosition.clear();
    }

    public final EventListener<EventRender3D> on3d = e -> {
        entityPosition.clear();
        for (final Entity entity : mc.theWorld.loadedEntityList) {
            if (shouldRender(entity) && ESPUtils.isInView(entity)) {
                entityPosition.put(entity, ESPUtils.getEntityPositionsOn2D(entity));
            }
        }
    };

    public final EventListener<EventShader> onShader = e -> {
        if (esp.isEnabled("Nametags")) {
            HUD hud = Lettuce.INSTANCE.getModuleManager().getModule(HUD.class);
            for (Entity entity : entityPosition.keySet()) {
                Vector4f pos = entityPosition.get(entity);
                float x = pos.getX(), y = pos.getY(), right = pos.getZ();

                if (entity instanceof EntityLivingBase renderingEntity) {
                    AbstractFontRenderer font = hud.getFont();
                    String ircName = "";
//                    if (IRCUtil.usersMap.containsKey(renderingEntity.getName())) {
//                        ircName = " \2477(\247d" + IRCUtil.usersMap.get(renderingEntity.getName()) + "\2477)";
//                    }
                    String name = (formattedTags.isEnabled() ? renderingEntity.getDisplayName().getFormattedText() : StringUtils.stripControlCodes(renderingEntity.getDisplayName().getUnformattedText())) + ircName;
                    StringBuilder text = new StringBuilder((
                            //FriendCommand.isFriend(renderingEntity.getName()) ? "§d" :
                            "§f") + name);
                    if (healthText.isEnabled("Nametag")) {
                        text.append(String.format(" §7[§r%s HP§7]", df.format(renderingEntity.getHealth())));
                    }
                    double fontScale = scale.getValue();
                    float middle = x + ((right - x) / 2);
                    float textWidth;
                    double fontHeight;
                    textWidth = font.getStringWidth(text.toString());
                    middle -= (textWidth * fontScale) / 2f;
                    fontHeight = font.getHeight() * fontScale;

                    glPushMatrix();
                    glTranslated(middle, y - (fontHeight + 2), 0);
                    glScaled(fontScale, fontScale, 1);
                    glTranslated(-middle, -(y - (fontHeight + 2)), 0);

                    Color backgroundTagColor = Color.BLACK;
                    ColorUtils.resetColor();
                    GLUtils.startBlend();
                    //if (nametagSettings.getSetting("Round").isEnabled()) {

                    //RenderUtils.drawRound(middle - 3, (float) (y - (fontHeight + 7)), textWidth + 6, (float) ((fontHeight / fontScale) + 4), 4, backgroundTagColor);
                    //} else {
                    Gui.drawRect2(middle - 3, (float) (y - (fontHeight + 7)), textWidth + 6,
                            (fontHeight / fontScale) + 4, backgroundTagColor.getRGB());
                    //}

                    glPopMatrix();

                }
            }
        }
    };

    public final EventListener<EventRender2D> onRender = e -> {
        HUD hud = Lettuce.INSTANCE.getModuleManager().getModule(HUD.class);

        if (boxColorMode.is("Custom")) {
            switch (boxColorModes.getMode()) {
                case "Rainbow" -> {
                    firstColor = ColorUtils.rainbow(colorSpeed.getValue().intValue(), 0, saturation.getValue().floatValue(), 1, 1);
                    secondColor = ColorUtils.rainbow(colorSpeed.getValue().intValue(), 90, saturation.getValue().floatValue(), 1, 1);
                    thirdColor = ColorUtils.rainbow(colorSpeed.getValue().intValue(), 180, saturation.getValue().floatValue(), 1, 1);
                    fourthColor = ColorUtils.rainbow(colorSpeed.getValue().intValue(), 270, saturation.getValue().floatValue(), 1, 1);
                }
                case "Double Colors" -> gradientColorWheel(new Color[]{color1.getColor(), color2.getColor()});
                case "Fade" -> gradientColorWheel(new Color[]{color1.getColor(), color1.getColor().darker().darker()});
                case "Static" -> gradientColorWheel(new Color[]{color1.getColor(), color1.getColor()});
            }
        } else {
            switch (hud.colorMode.getMode()) {
                case "Rainbow" -> {
                    firstColor = ColorUtils.rainbow(hud.colorSpeed.getValue().intValue(), 0, hud.saturation.getValue().floatValue(), 1, 1);
                    secondColor = ColorUtils.rainbow(hud.colorSpeed.getValue().intValue(), 90, hud.saturation.getValue().floatValue(), 1, 1);
                    thirdColor = ColorUtils.rainbow(hud.colorSpeed.getValue().intValue(), 180, hud.saturation.getValue().floatValue(), 1, 1);
                    fourthColor = ColorUtils.rainbow(hud.colorSpeed.getValue().intValue(), 270, hud.saturation.getValue().floatValue(), 1, 1);
                }
                case "Double Colors" -> gradientColorWheel(new Color[]{hud.color1.getColor(), hud.color2.getColor()});
                case "Fade" ->
                        gradientColorWheel(new Color[]{hud.color1.getColor(), hud.color1.getColor().darker().darker()});
                case "Static" -> gradientColorWheel(new Color[]{hud.color1.getColor(), hud.color1.getColor()});
            }
        }

        for (Entity entity : entityPosition.keySet()) {
            Vector4f pos = entityPosition.get(entity);
            float x = pos.getX(),
                    y = pos.getY(),
                    right = pos.getZ(),
                    bottom = pos.getW();

            if (entity instanceof EntityLivingBase renderingEntity) {
                AbstractFontRenderer font = hud.getFont();
                if (esp.isEnabled("Nametags")) {
                    String ircName = "";
//                    if (IRCUtil.usersMap.containsKey(renderingEntity.getName())) {
//                        ircName = " \2477(\247d" + IRCUtil.usersMap.get(renderingEntity.getName()) + "\2477)";
//                    }
                    float healthValue = renderingEntity.getHealth() / renderingEntity.getMaxHealth();
                    Color healthColor = healthValue > .75 ? new Color(66, 246, 123) : healthValue > .5 ? new Color(228, 255, 105) : healthValue > .35 ? new Color(236, 100, 64) : new Color(255, 65, 68);
                    String name = (formattedTags.isEnabled() ? renderingEntity.getDisplayName().getFormattedText() : StringUtils.stripControlCodes(renderingEntity.getDisplayName().getUnformattedText())) + ircName;
                    StringBuilder text = new StringBuilder((
                            //FriendCommand.isFriend(renderingEntity.getName()) ? "§d" :
                            "§f") + name);
                    if (healthText.isEnabled("Nametag")) {
                        text.append(String.format(" §7[§r%s HP§7]", df.format(renderingEntity.getHealth())));
                    }
                    double fontScale = scale.getValue();
                    float middle = x + ((right - x) / 2);
                    float textWidth;
                    double fontHeight = font.getHeight() * fontScale;
                    textWidth = font.getStringWidth(text.toString());
                    middle -= (textWidth * fontScale) / 2f;

                    glPushMatrix();
                    glTranslated(middle, y - (fontHeight + 2), 0);
                    glScaled(fontScale, fontScale, 1);
                    glTranslated(-middle, -(y - (fontHeight + 2)), 0);


                    if (nametagBackground.isEnabled()) {
                        Color backgroundTagColor = nametagBackgroundColor.getColor();
                        // if (nametagSettings.getSetting("Round").isEnabled()) {
                        //RenderUtils.drawRound(middle - 3, (float) (y - (fontHeight + 7)), textWidth + 6, (float) ((fontHeight / fontScale) + 4), 4, backgroundTagColor);
                        //} else {
                        Gui.drawRect2(middle - 3, (float) (y - (fontHeight + 7)), textWidth + 6,
                                (fontHeight / fontScale) + 4, backgroundTagColor.getRGB());
                        //}
                    }

                    ColorUtils.resetColor();
                    hud.getFont().drawStringWithShadow(text.toString(), middle, (float) (y - (fontHeight + 5)), healthColor.getRGB());
                    glPopMatrix();
                }

                if (esp.isEnabled("Held Item")) {
                    if (renderingEntity.getHeldItem() != null) {

                        float fontScale = .5f;
                        float middle = x + ((right - x) / 2);
                        float textWidth;
                        String text = renderingEntity.getHeldItem().getDisplayName();
                        textWidth = font.getStringWidth(text);
                        middle -= (textWidth * fontScale) / 2f;

                        glPushMatrix();
                        glTranslated(middle, (bottom + 4), 0);
                        glScaled(fontScale, fontScale, 1);
                        glTranslated(-middle, -(bottom + 4), 0);
                        GlStateManager.bindTexture(0);
                        ColorUtils.resetColor();
                        Gui.drawRect2(middle - 3, bottom + 1, font.getStringWidth(text) + 6, font.getHeight() + 5, nametagBackgroundColor.getColor().getRGB());
                        hud.getFont().drawStringWithShadow(text, middle, bottom + 4, -1);
                        glPopMatrix();
                    }
                }

                if (esp.isEnabled("Armor")) {
                    float scale = .4f;
                    float equipmentX = right + 5;
                    float equipmentY = y - 1;
                    glPushMatrix();
                    glTranslated(equipmentX, equipmentY, 0);
                    glScaled(scale, scale, 1);
                    glTranslated(-equipmentX, -y, 0);
                    ColorUtils.resetColor();
                    RenderHelper.enableGUIStandardItemLighting();
                    float seperation = 0f;
                    float length = (bottom - y) - 2;
                    for (int i = 3; i >= 0; i--) {
                        if (renderingEntity.getCurrentArmor(i) == null) {
                            seperation += (length / 3) / scale;
                            continue;
                        }
                        mc.getRenderItem().renderItemAndEffectIntoGUI(renderingEntity.getCurrentArmor(i), (int) equipmentX, (int) (equipmentY + seperation));
                        seperation += (length / 3) / scale;
                    }

                    RenderHelper.disableStandardItemLighting();
                    glPopMatrix();
                }


                if (esp.isEnabled("Health")) {
                    float healthValue = renderingEntity.getHealth() / renderingEntity.getMaxHealth();
                    Color healthColor = healthValue > .75 ? new Color(66, 246, 123) : healthValue > .5 ? new Color(228, 255, 105) : healthValue > .35 ? new Color(236, 100, 64) : new Color(255, 65, 68);

                    float height = (bottom - y) + 1;
                    Gui.drawRect2(x - 3.5f, y - .5f, 2, height + 1, new Color(0, 0, 0, 180).getRGB());
                    if (healthBarMode.is("Sync")) {
                        GradientUtils.drawGradientTB(x - 3, y, 1, height, .3f, firstColor, fourthColor);
                        GradientUtils.drawGradientTB(x - 3, y + (height - (height * healthValue)), 1, height * healthValue, 1, firstColor, fourthColor);
                    } else {
                        Gui.drawRect2(x - 3, y, 1, height, ColorUtils.applyOpacity(healthColor, .3f).getRGB());
                        Gui.drawRect2(x - 3, y + (height - (height * healthValue)), 1, height * healthValue, healthColor.getRGB());
                    }

                    if (healthText.isEnabled("Bar")) {
                        healthValue *= 100;
                        String health = String.valueOf(MathUtils.round(healthValue, 1)).substring(0, healthValue == 100 ? 3 : 2);
                        String text = health + "%";
                        double fontScale = .5;
                        float textX = x - ((font.getStringWidth(text) / 2f) + 2);
                        float fontHeight = (float) (hud.getFont().getHeight() * fontScale);
                        float newHeight = height - fontHeight;
                        float textY = y + (newHeight - (newHeight * (healthValue / 100)));

                        glPushMatrix();
                        glTranslated(textX - 5, textY, 1);
                        glScaled(fontScale, fontScale, 1);
                        glTranslated(-(textX - 5), -textY, 1);
                        hud.getFont().drawStringWithShadow(text, textX, textY, -1);
                        glPopMatrix();
                    }
                }
            }

            if (esp.isEnabled("Box")) {
                float outlineThickness = .5f;
                ColorUtils.resetColor();
                //top
                GradientUtils.drawGradientLR(x, y, (right - x), 1, 1, firstColor, secondColor);
                //left
                GradientUtils.drawGradientTB(x, y, 1, bottom - y, 1, firstColor, fourthColor);
                //bottom
                GradientUtils.drawGradientLR(x, bottom, right - x, 1, 1, fourthColor, thirdColor);
                //right
                GradientUtils.drawGradientTB(right, y, 1, (bottom - y) + 1, 1, secondColor, thirdColor);

                //Outline

                //top
                Gui.drawRect2(x - .5f, y - outlineThickness, (right - x) + 2, outlineThickness, Color.BLACK.getRGB());
                //Left
                Gui.drawRect2(x - outlineThickness, y, outlineThickness, (bottom - y) + 1, Color.BLACK.getRGB());
                //bottom
                Gui.drawRect2(x - .5f, (bottom + 1), (right - x) + 2, outlineThickness, Color.BLACK.getRGB());
                //Right
                Gui.drawRect2(right + 1, y, outlineThickness, (bottom - y) + 1, Color.BLACK.getRGB());


                //top
                Gui.drawRect2(x + 1, y + 1, (right - x) - 1, outlineThickness, Color.BLACK.getRGB());
                //Left
                Gui.drawRect2(x + 1, y + 1, outlineThickness, (bottom - y) - 1, Color.BLACK.getRGB());
                //bottom
                Gui.drawRect2(x + 1, (bottom - outlineThickness), (right - x) - 1, outlineThickness, Color.BLACK.getRGB());
                //Right
                Gui.drawRect2(right - outlineThickness, y + 1, outlineThickness, (bottom - y) - 1, Color.BLACK.getRGB());

            }
        }
    };

    private void gradientColorWheel(Color[] colors) {
        firstColor = ColorUtils.interpolateColorsBackAndForth(15, 0, colors[0], colors[1], false);
        secondColor = ColorUtils.interpolateColorsBackAndForth(15, 90, colors[0], colors[1], false);
        thirdColor = ColorUtils.interpolateColorsBackAndForth(15, 180, colors[0], colors[1], false);
        fourthColor = ColorUtils.interpolateColorsBackAndForth(15, 270, colors[0], colors[1], false);
    }

    private boolean shouldRender(Entity entity) {
        if (entity.isDead) {
            return false;
        }

        if (entity.isInvisible()) {
            if (entity == mc.thePlayer) {
                return mc.gameSettings.thirdPersonView != 0;
            }
            return validEntities.isEnabled("Invisible");
        }

        if (entity instanceof EntityPlayer) {
            if (entity == mc.thePlayer) {
                return mc.gameSettings.thirdPersonView != 0;
            }

            if (get(AntiBot.class).isEnabled()) {
                return !AntiBot.conmemay.contains(entity.getUniqueID());
            }

            return !entity.getDisplayName().getUnformattedText().contains("[NPC") && validEntities.isEnabled("Players");
        }

        if (entity instanceof EntityCreature) {
            return validEntities.isEnabled("Mobs");
        }

        if (entity instanceof EntityItem) {
            return validEntities.isEnabled("Items");
        }

        if (entity instanceof EntityLiving) {
            return validEntities.isEnabled("Animals");
        }

        return false;
    }

}