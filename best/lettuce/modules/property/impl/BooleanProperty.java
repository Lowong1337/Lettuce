package best.lettuce.modules.property.impl;

import best.lettuce.modules.property.Property;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.function.Supplier;

public class BooleanProperty extends Property {

    @Expose @SerializedName("name")
    private boolean state;

    public BooleanProperty(String name, boolean state) {
        this.name = name;
        this.state = state;
    }

    public BooleanProperty(String name, boolean state, Supplier<Boolean> shown) {
        this.name = name;
        this.state = state;
        this.shown = shown;
    }

    public boolean isEnabled() {
        return state;
    }

    public void toggle() {
        setState(!isEnabled());
    }

    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    public Boolean getConfigValue() {
        return isEnabled();
    }
}

