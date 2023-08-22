package best.lettuce.modules.property.impl;

import best.lettuce.modules.property.Property;

import java.util.*;
import java.util.function.Supplier;

public class MultipleBoolProperty extends Property {

    private final Map<String, BooleanProperty> boolProperties;

    public MultipleBoolProperty(String name, String... booleanSettingNames) {
        this.name = name;
        boolProperties = new LinkedHashMap<>();
        Arrays.stream(booleanSettingNames).forEach(boolName -> boolProperties.put(boolName.toLowerCase(), new BooleanProperty(boolName, false)));
    }

    public MultipleBoolProperty(String name, Supplier<Boolean> shown, String... booleanSettingNames) {
        this.name = name;
        boolProperties = new LinkedHashMap<>();
        Arrays.stream(booleanSettingNames).forEach(boolName -> boolProperties.put(boolName.toLowerCase(), new BooleanProperty(boolName, false)));
        this.shown = shown;
    }

    public MultipleBoolProperty(String name, BooleanProperty... booleanSettings) {
        this.name = name;
        boolProperties = new LinkedHashMap<>();
        Arrays.stream(booleanSettings).forEach(booleanSetting -> boolProperties.put(booleanSetting.name.toLowerCase(), booleanSetting));
    }

    public MultipleBoolProperty(String name, Supplier<Boolean> shown, BooleanProperty... booleanSettings) {
        this.name = name;
        boolProperties = new LinkedHashMap<>();
        Arrays.stream(booleanSettings).forEach(booleanSetting -> boolProperties.put(booleanSetting.name.toLowerCase(), booleanSetting));
        this.shown = shown;
    }

    public BooleanProperty getSetting(String settingName) {
        return boolProperties.computeIfAbsent(settingName.toLowerCase(), k -> null);
    }

    public boolean isEnabled(String settingName) {
        return boolProperties.get(settingName.toLowerCase()).isEnabled();
    }

    public Collection<BooleanProperty> getBoolSettings() {
        return boolProperties.values();
    }

    @Override
    public HashMap<String, Boolean> getConfigValue() {
        HashMap<String, Boolean> booleans = new LinkedHashMap<>();
        for (BooleanProperty booleanSetting : boolProperties.values()) {
            booleans.put(booleanSetting.name, booleanSetting.isEnabled());
        }
        return booleans;
    }
}

