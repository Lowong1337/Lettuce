package best.lettuce.modules.property.impl;

import best.lettuce.modules.property.Property;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModeProperty extends Property {

    public final List<String> modes;
    private int modeIndex;

    @Expose @SerializedName("value")
    private String currentMode;

    public ModeProperty(String name, String defaultMode, String... modes) {
        this.name = name;
        this.modes = Arrays.asList(modes);
        this.modeIndex = this.modes.indexOf(defaultMode);
        if (currentMode == null) currentMode = defaultMode;
    }

    public ModeProperty(String name, String defaultMode, Supplier<Boolean> shown, String... modes) {
        this.name = name;
        this.modes = Arrays.asList(modes);
        this.modeIndex = this.modes.indexOf(defaultMode);
        if (currentMode == null) currentMode = defaultMode;
        this.shown = shown;
    }

    public String getMode() {
        return currentMode;
    }

    public boolean is(String mode) {
        return currentMode.equalsIgnoreCase(mode);
    }

    public boolean isEither(String... modes) {
        return Arrays.stream(modes).anyMatch(currentMode::equalsIgnoreCase);
    }

    public void cycleForwards() {
        modeIndex++;
        if (modeIndex > modes.size() - 1) modeIndex = 0;
        currentMode = modes.get(modeIndex);
    }

    public void cycleBackwards() {
        modeIndex--;
        if (modeIndex < 0) modeIndex = modes.size() - 1;
        currentMode = modes.get(modeIndex);
    }

    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }

    @Override
    public String getConfigValue() {
        return currentMode;
    }
}
