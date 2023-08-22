package best.lettuce.modules.property.impl;

import best.lettuce.modules.property.Property;
import org.lwjgl.input.Keyboard;

import java.util.function.Supplier;

public class KeybindProperty extends Property {

    private int code;

    public KeybindProperty(int code) {
        this.name = "Keybind";
        this.code = code;
    }

    public KeybindProperty(int code, Supplier<Boolean> shown) {
        this.name = "Keybind";
        this.code = code;
        this.shown = shown;
    }

    public int getCode() {
        return code == -1 ? Keyboard.KEY_NONE : code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public Integer getConfigValue() {
        return this.getCode();
    }
}
