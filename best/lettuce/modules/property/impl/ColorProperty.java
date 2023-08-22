package best.lettuce.modules.property.impl;

import best.lettuce.modules.property.Property;
import best.lettuce.utils.color.ColorUtils;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Getter @Setter
public class ColorProperty extends Property {

    private float hue = 0, saturation = 1, brightness = 1, alpha;

    private final boolean hasAlpha;

    public ColorProperty(String name, Color defaultColor) {
        this.name = name;
        this.setColor(defaultColor);
        this.alpha = 1;
        this.hasAlpha = false;
        this.shown = () -> true;
    }

    public ColorProperty(String name, Color defaultColor, Supplier<Boolean> shown) {
        this.name = name;
        this.setColor(defaultColor);
        this.shown = shown;
        this.alpha = 1;
        this.hasAlpha = false;
    }

    public ColorProperty(String name, Color defaultColor, float alpha) {
        this.name = name;
        this.setColor(defaultColor);
        this.alpha = alpha;
        this.hasAlpha = true;
        this.shown = () -> true;
    }

    public ColorProperty(String name, Color defaultColor, Supplier<Boolean> shown, float alpha) {
        this.name = name;
        this.setColor(defaultColor);
        this.shown = shown;
        this.alpha = alpha;
        this.hasAlpha = true;
    }

    public Color getStaticColor(boolean allowAlpha) {
        if (hasAlpha && allowAlpha) {
            int rgb = Color.HSBtoRGB(hue, saturation, brightness);
            int alphaValue = Math.round(alpha * 255); // Scale alpha to 0-255 range
            return new Color((rgb & 0x00FFFFFF) | (alphaValue << 24), true);
        } else {
            return Color.getHSBColor(hue, saturation, brightness);
        }
    }

    public Color getColor(boolean allowAlpha) {
        return getStaticColor(allowAlpha);
    }

    public Color getColor() {
        return getStaticColor(true);
    }

    public void setColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        if (hasAlpha) {
            alpha = color.getAlpha() / 255f;
        }
    }

    @Override
    public Object getConfigValue() {
        return getColor().getRGB();
    }
}