package best.lettuce.modules.impl.render;

import best.lettuce.Lettuce;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.render.EventRender2D;
import best.lettuce.event.impl.render.EventShader;
import best.lettuce.gui.notification.Notification;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.ModuleManager;
import best.lettuce.modules.property.impl.*;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.AbstractFontRenderer;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.math.MathUtils;
import best.lettuce.utils.non_utils.drag.Dragging;
import best.lettuce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HUD extends Module {

    //Initialize all the possibles values.
    public static final String[] colorModes = new String[]{"Static", "Fade", "Double Colors", "Rainbow"};
    public static final String[] fontModes = new String[]{"Lettuce", "Minecraft", "Tahoma", "Neverlose"};

    public static final MultipleBoolProperty hudOptions = new MultipleBoolProperty("HUD Options",
            new BooleanProperty("Potion HUD", true),
            new BooleanProperty("Watermark", true),
            new BooleanProperty("Arraylist", true),
            new BooleanProperty("Notifications", true),
            new BooleanProperty("Armor HUD", true));

    public final HeaderProperty fontz = new HeaderProperty("Font Options");
    public final ModeProperty font = new ModeProperty("HUD Font", "Lettuce", fontModes);
    public final MultipleBoolProperty fontOptions = new MultipleBoolProperty("Font Options",
            new BooleanProperty("Bold", false),
            new BooleanProperty("Shadow", true));
    public final ModeProperty fontMode = new ModeProperty("Font Mode", "Normal", "Lowercase", "Normal", "Uppercase");

    public final HeaderProperty colorz = new HeaderProperty("Color Options");
    public final ModeProperty colorMode = new ModeProperty("Color Mode", "Static", colorModes);
    public final ColorProperty color1 = new ColorProperty("Color", new Color(0xffa028d4));
    public final ColorProperty color2 = new ColorProperty("Secondary Color", new Color(0xff0008ff), () -> (colorMode.is("Double Colors")));
    public final NumberProperty saturation = new NumberProperty("Rainbow Saturation", 1, 0, 1, .05, () -> (colorMode.is("Rainbow")));
    public final NumberProperty colorSpeed = new NumberProperty("Color Speed", 15, 2, 30, 1, () -> (hudOptions.getSetting("Arraylist").isEnabled()));
    public final NumberProperty colorIndex = new NumberProperty("Color Separation", 20, 5, 100, 1, () -> (hudOptions.getSetting("Arraylist").isEnabled()));

    public final HeaderProperty watermarkz = new HeaderProperty("Watermark Options");
    public final ModeProperty watermarkMode = new ModeProperty("Watermark Mode", "Basic", "Basic", "Khoi", "Logo");
    public final StringProperty watermark = new StringProperty("Watermark Text", "Lettuce", () -> (hudOptions.isEnabled("Watermark") && !watermarkMode.is("Logo")));

    public final HeaderProperty arraylistz = new HeaderProperty("Arraylist Options");
    public final MultipleBoolProperty arraylistcustom = new MultipleBoolProperty("Arraylist Customizations",
            () -> (hudOptions.isEnabled("Arraylist")),
            new BooleanProperty("Background", true),
            new BooleanProperty("Show render modules", false));
    public final ModeProperty animation = new ModeProperty("Animation", "Scale", () -> (hudOptions.getSetting("Arraylist").isEnabled()), "Slide", "Scale", "Fly");
    public final NumberProperty height = new NumberProperty("Height", 11, 9, 20, .5f, () -> (hudOptions.getSetting("Arraylist").isEnabled()));
    public final MultipleBoolProperty rectangle = new MultipleBoolProperty("Borders", () -> (hudOptions.isEnabled("Arraylist")),
            new BooleanProperty("Top", true),
            new BooleanProperty("Left", true),
            new BooleanProperty("Right", true),
            new BooleanProperty("Bottom", true));
    public final ColorProperty backgroundColor = new ColorProperty("Background Color", Color.BLACK, () -> arraylistcustom.getSetting("Background").isEnabled() && hudOptions.getSetting("Arraylist").isEnabled(), .5f);

    public final HeaderProperty notificationz = new HeaderProperty("Notification Options");
    public final BooleanProperty rounded = new BooleanProperty("Rounded", false, () -> hudOptions.isEnabled("Notifications"));
    public final NumberProperty notitime = new NumberProperty("Notification time", 2, 1, 10, .5);
    public final BooleanProperty toggleNotifications = new BooleanProperty("Toggle Notifications", true);

    public final ModeProperty chatfont = new ModeProperty("Chat Font", "Lettuce", fontModes);

    public int offsetValue = 0;

    public String longest = "";

    Module lastModule;
    int lastCount;

    public List<Module> modules;

    public static Color color;

    public Dragging arraylistDrag = createDrag(this, "arraylist", 2, 1);

    public HUD() {
        super("HUD", Category.RENDER, "Customizes the client's look.");
        this.addProperties(
                hudOptions,

                fontz, font, fontOptions, fontMode,
                colorz, colorMode, color1, color2, saturation, colorSpeed, colorIndex,
                watermarkz, watermarkMode, watermark,
                arraylistz, arraylistcustom, animation, height, rectangle, backgroundColor,
                notificationz, rounded, notitime, toggleNotifications
        );
    }

    public void getModulesAndSort() {
        if (modules == null || ModuleManager.reloadModules) {
            modules = Lettuce.INSTANCE.getModuleManager().getModules();
        }
        modules.sort(Comparator.<Module>comparingDouble(m -> {
            String name = get(m.getName() + (m.hasMode() ? " " + m.getSuffix() : ""));
            return getFont().getStringWidth(applyText(name));
        }).reversed());
    }

    public final EventListener<EventShader> onShader = e -> {
        if (e.isBloom()) {
            if (e.getBloomOptions().isEnabled("Notifications")) {
                drawNotificationsEffects(true);
            }

            if (e.getBloomOptions().isEnabled("Arraylist")) {
                drawArraylistEffects(true);
            }
        } else {
            drawArraylistEffects(true);
            drawNotificationsEffects(true);
        }
    };

    public final EventListener<EventRender2D> on2D = e -> {
        //Arraylist
        drawArraylist();

        //Notifications
        drawNotifications();

        //Watermark
        drawWatermark();
    };

    public void drawWatermark() {
        String text = watermark.getString();
        if (!hudOptions.isEnabled("Watermark")) return;
        Color[] colors = {color1.getColor(), color2.getColor()};
        if (Objects.equals(text, "")) {
            text = "Lettuce";
        }
        switch (watermarkMode.getMode()) {
            case "Basic" ->
                    getFont().drawString(text.charAt(0) + EnumChatFormatting.WHITE.toString() + text.substring(1), 3, 3, getColor(colors, 1).getRGB(), fontOptions.isEnabled("Shadow"));
            case "Khoi" -> {
                getFont().drawString(EnumChatFormatting.WHITE + text + EnumChatFormatting.WHITE + " (" + EnumChatFormatting.RESET + Lettuce.VERSION + EnumChatFormatting.WHITE + ")", 3, 3, getColor(colors, 1).getRGB(), fontOptions.isEnabled("Shadow"));
                getFont().drawString(EnumChatFormatting.WHITE + "(" + EnumChatFormatting.RESET + "User" + EnumChatFormatting.WHITE + "): " + EnumChatFormatting.GRAY + Minecraft.getMinecraft().thePlayer.getName(), 3, 12, getColor(colors, 1).getRGB(), fontOptions.isEnabled("Shadow"));
                getFont().drawString(EnumChatFormatting.WHITE + "(" + EnumChatFormatting.RESET + "FPS" + EnumChatFormatting.WHITE + "): " + EnumChatFormatting.GRAY + Minecraft.getDebugFPS(), 3, 21, getColor(colors, 1).getRGB(), fontOptions.isEnabled("Shadow"));
                getFont().drawString(EnumChatFormatting.WHITE + "(" + EnumChatFormatting.RESET + "BPS" + EnumChatFormatting.WHITE + "): " + EnumChatFormatting.GRAY + MathUtils.round(Math.sqrt(MathUtils.square(mc.thePlayer.motionX) + MathUtils.square(mc.thePlayer.motionZ)) * 20.0D * mc.timer.timerSpeed, 2.0D), 3, 30, getColor(colors, 1).getRGB(), fontOptions.isEnabled("Shadow"));
            }
            case "Logo" -> {
                icontestFont90.drawString(FontUtils.LETTUCE, 3, 5 + getFont().getHeight(), getColor(colors, 1).getRGB(), fontOptions.isEnabled("Shadow"));
                getFont().drawString(MinecraftInstance.VERSION, icontestFont90.getStringWidth(FontUtils.LETTUCE) - 5, 7, Color.GRAY.getRGB(), fontOptions.isEnabled("Shadow"));
            }
        }
    }

    public void drawArraylist() {
        if (!hudOptions.isEnabled("Arraylist")) return;
        getModulesAndSort();

        String longestModule = "";
        float longestWidth = 0;
        double yOffset = 0;
        ScaledResolution sr = new ScaledResolution();
        int count = 0;
        for (Module module : modules) {
            if (!(arraylistcustom.getSetting("Show render modules").isEnabled()) && module.getCategory() == Category.RENDER)
                continue;
            final Animation moduleAnimation = module.getAnimation();

            moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);

            if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;

            String displayText = get(module.getName() + (module.hasMode() ? (
                    //module.getCategory().equals(Category.SCRIPTS) ? " §c" :
                    " §7") + module.getSuffix() : ""));
            displayText = applyText(displayText);
            float textWidth = getFont().getStringWidth(displayText);

            if (textWidth > longestWidth) {
                longestModule = displayText;
                longestWidth = textWidth;
            }

            double xValue = sr.getWidth() - (arraylistDrag.getX());

            boolean flip = xValue <= sr.getWidth() / 2f;
            float x = (float) (flip ? xValue : sr.getWidth() - (textWidth + arraylistDrag.getX()));

            float alphaAnimation = 1;

            float y = (float) (yOffset + arraylistDrag.getY());

            float heightVal = height.getValue().floatValue() + 1;

            switch (animation.getMode()) {
                case "Slide" -> {
                    if (flip) {
                        x -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (sr.getWidth() - (arraylistDrag.getX() - textWidth)));
                    } else {
                        x += Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (arraylistDrag.getX() + textWidth));
                    }
                }
                case "Scale" -> {
                    if (!moduleAnimation.isDone()) {
                        RenderUtils.scaleStart(x + getFont().getStringWidth(displayText) / 2f, y + heightVal / 2 - getFont().getHeight() / 2f, moduleAnimation.getOutput().floatValue());
                    }
                    alphaAnimation = moduleAnimation.getOutput().floatValue();
                }
                case "Fly" -> {
                    if (flip) {
                        x -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (sr.getWidth() - (arraylistDrag.getX() - textWidth)));
                        y -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (sr.getWidth() - (arraylistDrag.getY() - textWidth)));
                    } else {
                        x += Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (arraylistDrag.getX() + textWidth));
                        y -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (arraylistDrag.getY() + textWidth));
                    }
                }
            }

            int index = (int) (count * colorIndex.getValue());
            Color[] colors = {color1.getColor(), color2.getColor()};

            Color textcolor = getColor(colors, index);
            color = textcolor;

            if (arraylistcustom.getSetting("Background").isEnabled()) {
                float offset = font.is("Minecraft") ? 4 : 5;
                Color color = backgroundColor.getColor();
                Gui.drawRect2(x - 2, y - 1, getFont().getStringWidth(displayText) + offset, heightVal, ColorUtils.applyOpacity(color, backgroundColor.getAlpha() * alphaAnimation).getRGB());
            }

            if (rectangle.isEnabled("Top")) {
                if (count == 0) {
                    Gui.drawRect2(x - 3, y - 2, textWidth + (flip ? 7 : 6), 1, textcolor.getRGB());
                }
            }

            if (rectangle.isEnabled("Right")) {
                if (flip) {
                    Gui.drawRect2(x - 3, y - 1, 1, heightVal, textcolor.getRGB());
                } else {
                    Gui.drawRect2(x + textWidth + 2, y - 1, 1, heightVal, textcolor.getRGB());
                }
            }

            if (rectangle.isEnabled("Left")) {
                if (count != 0) {
                    String modText = applyText(get(lastModule.getName() + (lastModule.hasMode() ? " " + lastModule.getSuffix() : "")));
                    float texWidth = getFont().getStringWidth(modText) - textWidth;
                    //Draws the difference of width rect and also the rect on the side of the text
                    if (flip) {
                        Gui.drawRect2(x + textWidth + 3, y - 1, 1, heightVal, textcolor.getRGB());
                        Gui.drawRect2(x + textWidth + 3, y - 1, texWidth + 1, 1, textcolor.getRGB());
                    } else {
                        Gui.drawRect2(x - (3 + texWidth), y - 1, texWidth, 1, textcolor.getRGB());
                        Gui.drawRect2(x - 3, y - 1, 1, heightVal, textcolor.getRGB());
                    }
                } else {
                    //Draws the rects for the first module in the count
                    if (flip) {
                        Gui.drawRect2(x + textWidth + 3, y - 1, 1, heightVal, textcolor.getRGB());
                    } else {
                        Gui.drawRect2(x - 3, y - 1, 1, heightVal, textcolor.getRGB());
                    }
                }
            }
            if (rectangle.isEnabled("Bottom")) {
                if (count == (lastCount - 1)) {
                    Gui.drawRect2(x - 3, y + heightVal - 1, textWidth + (flip ? 7 : 6), 1, textcolor.getRGB());
                }
            }

            float textYOffset = font.is("Minecraft") ? .5f : 0;
            y += textYOffset;
            Color color = ColorUtils.applyOpacity(textcolor, alphaAnimation);

            getFont().drawString(displayText, x, y + getFont().getMiddleOfBox(heightVal), color.getRGB(), fontOptions.isEnabled("Shadow"));

            if (animation.is("Scale") && !moduleAnimation.isDone()) {
                RenderUtils.scaleEnd();
            }

            lastModule = module;

            yOffset += moduleAnimation.getOutput().floatValue() * heightVal;
            count++;
        }
        lastCount = count;
        longest = longestModule;
    }

    public void drawArraylistEffects(boolean bloom) {
        if (!hudOptions.isEnabled("Arraylist") || modules == null) return;
        float yOffset = 0;
        ScaledResolution sr = new ScaledResolution();
        int count = 0;
        for (Module module : modules) {
            if (!(arraylistcustom.getSetting("Show render modules").isEnabled()) && module.getCategory() == Category.RENDER)
                continue;
            final Animation moduleAnimation = module.getAnimation();
            if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;

            String displayText = get(module.getName() + (module.hasMode() ? " §7" + module.getSuffix() : ""));
            displayText = applyText(displayText);
            float textWidth = getFont().getStringWidth(displayText);

            float xValue = sr.getWidth() - (arraylistDrag.getX());

            boolean flip = xValue <= sr.getWidth() / 2f;
            float x = flip ? xValue : sr.getWidth() - (textWidth + arraylistDrag.getX());

            float y = yOffset + arraylistDrag.getY();

            float heightVal = height.getValue().floatValue() + 1;
            boolean scaleIn = false;
            switch (animation.getMode()) {
                case "Slide" -> {
                    if (flip) {
                        x -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (sr.getWidth() - (arraylistDrag.getX() + textWidth)));
                    } else {
                        x += Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (arraylistDrag.getX() + textWidth));
                    }
                }
                case "Fly" -> {
                    if (flip) {
                        x -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (sr.getWidth() - (arraylistDrag.getX() - textWidth)));
                        y -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (sr.getWidth() - (arraylistDrag.getY() - textWidth)));
                    } else {
                        x += Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (arraylistDrag.getX() + textWidth));
                        y -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (arraylistDrag.getY() + textWidth));
                    }
                }
                case "Scale" -> {
                    if (!moduleAnimation.isDone()) {
                        RenderUtils.scaleStart(x + getFont().getStringWidth(displayText) / 2f, y + heightVal / 2 - getFont().getHeight() / 2f, moduleAnimation.getOutput().floatValue());
                    }
                    scaleIn = true;
                }
            }

            int index = (int) (count * colorIndex.getValue());
            Color[] colors = {color1.getColor(), color2.getColor()};

            Color textcolor = getColor(colors, index);

            if (arraylistcustom.getSetting("Background").isEnabled()) {
                float offset = font.is("Minecraft") ? 4 : 5;
                //int rectColor = bloom ? textcolor.getRGB() : Color.BLACK.getRGB();
                Color rectColor2 = ColorUtils.applyOpacity(ColorUtils.interpolateColorC(Color.BLACK, textcolor, bloom ? 0.65f : 0), 70);

                Gui.drawRect2(x - 2, y, getFont().getStringWidth(displayText) + offset, heightVal, scaleIn ? ColorUtils.applyOpacity(rectColor2.getRGB(), moduleAnimation.getOutput().floatValue()) : rectColor2.getRGB());

                float offset2 = font.is("Minecraft") ? 1 : 0;

                int rectangleColor = textcolor.getRGB();

                if (scaleIn) {
                    rectangleColor = ColorUtils.applyOpacity(rectangleColor, moduleAnimation.getOutput().floatValue());
                }

                if (rectangle.isEnabled("Right")) {
                    if (flip) {
                        Gui.drawRect2(x - 3, y, 9, heightVal, textcolor.getRGB());
                    } else {
                        Gui.drawRect2(x + textWidth - 7, y, 9, heightVal, rectangleColor);
                    }
                } else {
                    if (count == 0) {
                        Gui.drawRect2(x - 2, y - (rectangle.isEnabled("Top") || (rectangle.isEnabled("Left") && rectangle.isEnabled("Right")) ? 1 : 0), textWidth + 5 - (offset2), 9, rectangleColor);
                    }
                }
            }

            if (animation.is("Scale") && !moduleAnimation.isDone()) {
                RenderUtils.scaleEnd();
            }

            yOffset += moduleAnimation.getOutput().floatValue() * heightVal;
            count++;
        }
    }

    public void drawNotifications() {
        ScaledResolution sr = new ScaledResolution();
        float yOffset = 0;
        int notificationHeight, notificationWidth, actualOffset;

        NotificationManager.setToggleTime(notitime.getValue().floatValue());

        for (Notification notification : NotificationManager.getNotifications()) {
            Animation animation = notification.getAnimation();
            animation.setDirection(notification.getTimerUtil().hasTimeElapsed((long) notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);

            if (animation.finished(Direction.BACKWARDS)) {
                NotificationManager.getNotifications().remove(notification);
                continue;
            }

            float x, y;
            animation.setDuration(200);
            actualOffset = 3;
            notificationHeight = 23;
            notificationWidth = lettuceFont20.getStringWidth(notification.getDescription()) + 25;
            x = sr.getWidth() - (notificationWidth + 5) * animation.getOutput().floatValue();
            y = sr.getHeight() - (yOffset + 18 + offsetValue + notificationHeight + 15);
            notification.drawLettuce(x, y, notificationWidth, notificationHeight);
            yOffset += (notificationHeight + actualOffset) * animation.getOutput().floatValue();
        }
    }

    public void drawNotificationsEffects(boolean bloom) {
        ScaledResolution sr = new ScaledResolution();
        float yOffset = 0;
        int notificationHeight, notificationWidth, actualOffset;

        for (Notification notification : NotificationManager.getNotifications()) {
            Animation animation = notification.getAnimation();
            animation.setDirection(notification.getTimerUtil().hasTimeElapsed((long) notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);

            if (animation.finished(Direction.BACKWARDS)) {
                NotificationManager.getNotifications().remove(notification);
                continue;
            }

            float x, y;

            animation.setDuration(200);
            actualOffset = 3;
            notificationHeight = 23;
            notificationWidth = lettuceFont20.getStringWidth(notification.getDescription()) + 25;
            x = sr.getWidth() - (notificationWidth + 5) * animation.getOutput().floatValue();
            y = sr.getHeight() - (yOffset + 18 + offsetValue + notificationHeight + 15);
            if(!rounded.isEnabled()) {
                notification.blurLettuce(x, y, notificationWidth, notificationHeight, bloom);
            }
            yOffset += (notificationHeight + actualOffset) * animation.getOutput().floatValue();
        }
    }

    public String applyText(String text) {
        if (font.is("Minecraft") && fontOptions.isEnabled("Bold")) {
            return "§l" + text.replace("§7", "§7§l");
        }
        return text;
    }

    public AbstractFontRenderer getchatFont(boolean allowBold) {
        if (fontOptions.isEnabled("Bold") && allowBold) {
            return switch (chatfont.getMode()) {
                default -> mc.fontRendererObj;
                case "Lettuce" -> lettuceBoldFont20;
                case "Tahoma" -> tahomaBold20;
                case "Neverlose" -> neverloseBold20;
            };
        } else {
            return switch (chatfont.getMode()) {
                default -> mc.fontRendererObj;
                case "Lettuce" -> lettuceFont20;
                case "Tahoma" -> tahoma20;
                case "Neverlose" -> neverlose20;
            };
        }
    }

    public final EventListener<EventTick> onTick = e -> {

    };

    public AbstractFontRenderer getFont(boolean allowBold) {
        HUD hud = Lettuce.INSTANCE.getModuleManager().getModule(HUD.class);
        if (hud.fontOptions.isEnabled("Bold") && allowBold) {
            return switch (hud.font.getMode()) {
                default -> mc.fontRendererObj;
                case "Lettuce" -> lettuceBoldFont20;
                case "Tahoma" -> tahomaBold20;
                case "Neverlose" -> neverloseBold20;

            };
        } else {
            return switch (hud.font.getMode()) {
                default -> mc.fontRendererObj;
                case "Lettuce" -> lettuceFont20;
                case "Tahoma" -> tahoma20;
                case "Neverlose" -> neverlose20;
            };
        }
    }

    public AbstractFontRenderer getFont() {
        return getFont(true);
    }

    public String get(String text) {
        return switch (fontMode.getMode()) {
            case "Lowercase" -> text.toLowerCase();
            case "Uppercase" -> text.toUpperCase();
            default -> text;
        };
    }

    public Color getColor(Color[] colors, int index) {
        return switch (colorMode.getMode()) {
            default -> color1.getColor();
            case "Fade" ->
                    ColorUtils.interpolateColorsBackAndForth(colorSpeed.getValue().intValue(), index, colors[0], colors[0].darker().darker(), false);
            case "Double Colors" ->
                    ColorUtils.interpolateColorsBackAndForth(colorSpeed.getValue().intValue(), index, colors[0], colors[1], false);
            case "Rainbow" ->
                    ColorUtils.rainbow(colorSpeed.getValue().intValue(), index, saturation.getValue().floatValue(), 1, 1);
        };
    }
}