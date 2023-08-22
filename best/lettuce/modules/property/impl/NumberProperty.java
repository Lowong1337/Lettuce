package best.lettuce.modules.property.impl;

import best.lettuce.modules.property.Property;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.function.Supplier;

public class NumberProperty extends Property {

    private final double maxValue, minValue, increment, defaultValue;

    @Expose @SerializedName("value")
    private Double value;

    public NumberProperty(String name, double defaultValue, double minValue, double maxValue, double increment) {
        this.name = name;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.increment = increment;
    }

    public NumberProperty(String name, double defaultValue, double minValue, double maxValue, double increment, Supplier<Boolean> shown) {
        this.name = name;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.increment = increment;
        this.shown = shown;
    }

    private static double clamp(double value, double min, double max) {
        value = Math.max(min, value);
        value = Math.min(max, value);
        return value;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(double value) {
        value = clamp(value, this.minValue, this.maxValue);
        value = Math.round(value * (1.0 / this.increment)) / (1.0 / this.increment);
        this.value = value;
    }

    public double getIncrement() {
        return increment;
    }

    @Override
    public Double getConfigValue() {
        return getValue();
    }
}
