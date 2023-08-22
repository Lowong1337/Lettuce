package best.lettuce.modules.property.impl;

import best.lettuce.modules.property.Property;

import java.util.function.Supplier;

public class StringProperty extends Property {

    private String string = "";

    public StringProperty(String name) {
        this.name = name;
    }

    public StringProperty(String name, Supplier<Boolean> shown) {
        this.name = name;
        this.shown = shown;
    }

    public StringProperty(String name, String defaultValue) {
        this.name = name;
        this.string = defaultValue;
    }

    public StringProperty(String name, String defaultValue, Supplier<Boolean> shown) {
        this.name = name;
        this.string = defaultValue;
        this.shown = shown;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public String getConfigValue() {
        return string;
    }
}