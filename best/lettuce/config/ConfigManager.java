package best.lettuce.config;

import best.lettuce.Lecture;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.Property;
import best.lettuce.modules.property.impl.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigManager {
    public static final List<LocalConfig> localConfigs = new ArrayList<>();
    public static boolean loadVisuals;
    public static File defaultConfig;
    public static File configlist;
    public static final File file = new File(Minecraft.getMinecraft().mcDataDir, "/Lecture/configs");
    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    @Getter
    public String currentConfig;
    public void collectConfigs() {
        localConfigs.clear();
        file.mkdirs();

        //For each config in the config folder it adds it to the list and removes the ".json" from the name
        Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(f -> localConfigs.add(new LocalConfig(f.getName().split("\\.")[0])));
    }

    /**
     * Saving config method
     *
     * @see ConfigManager#serialize() to serialize the modules and settings
     */
    public boolean saveConfig(String name, String content) {
        best.lettuce.config.LocalConfig localConfig = new best.lettuce.config.LocalConfig(name);
        localConfig.getFile().getParentFile().mkdirs();
        try {
            Files.write(localConfig.getFile().toPath(), content.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveConfig(String name) {
        return saveConfig(name, serialize());
    }

    public boolean delete(String configName) {
        List<LocalConfig> configsMatch = localConfigs.stream().filter(localConfig -> localConfig.getName().equals(configName)).collect(Collectors.toList());
        try {
            LocalConfig configToDelete = configsMatch.get(0);
            Files.deleteIfExists(configToDelete.getFile().toPath());
        } catch (IOException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            //NotificationManager.post(NotificationType.WARNING, "Config Manager", "Failed to delete config!");
            return false;
        }
        return true;
    }

    public void saveDefaultConfig() {
        defaultConfig.getParentFile().mkdirs();
        try {
            Files.write(defaultConfig.toPath(), serialize().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to save " + defaultConfig);
        }
    }

    public String serialize() {
        for (Module module : Lecture.INSTANCE.getModuleManager().getModules()) {
            //if (module.getCategory().equals(Category.SCRIPTS)) continue;
            List<ConfigProperty> settings = new ArrayList<>();
            for (Property setting : module.getProperties()) {
                ConfigProperty cfgSetting = new ConfigProperty(null, null);
                cfgSetting.name = setting.name;
                cfgSetting.value = setting.getConfigValue();
                settings.add(cfgSetting);
            }
            module.cfgProperties = settings.toArray(new ConfigProperty[0]);
        }
        return gson.toJson(Lecture.INSTANCE.getModuleManager().getModules());
    }

    public String readConfigData(Path configPath) {
        try {
            return new String(Files.readAllBytes(configPath));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean loadConfig(String data) {
        return loadConfig(data, false);
    }

    public boolean loadConfig(String data, boolean keybinds) {
        this.currentConfig = data;
        Module[] modules = gson.fromJson(data, Module[].class);

        for (Module module : Lecture.INSTANCE.getModuleManager().getModules()) {
            if (!keybinds) {
                if (!loadVisuals && module.getCategory().equals(Category.RENDER)) continue;
            }
            //if (module.getCategory().equals(Category.SCRIPTS)) continue;

            for (Module configModule : modules) {
                if (module.getName().equalsIgnoreCase(configModule.getName())) {
                    try {
                        if (module.isEnabled() != configModule.isEnabled()) {
                            module.toggle(Module.ToggleType.AUTO);
                        }
                        for (Property setting : module.getProperties()) {
                            for (ConfigProperty cfgSetting : configModule.cfgProperties) {
                                if (setting.name.equals(cfgSetting.name)) {
                                    if (setting instanceof KeybindProperty) {
                                        if (keybinds) {
                                            KeybindProperty keybindSetting = (KeybindProperty) setting;
                                            keybindSetting.setCode(Double.valueOf(String.valueOf(cfgSetting.value)).intValue());
                                        }
                                    }
                                    if (setting instanceof BooleanProperty) {
                                        ((BooleanProperty) setting).setState(Boolean.parseBoolean(String.valueOf(cfgSetting.value)));
                                    }
                                    if (setting instanceof ModeProperty) {
                                        ModeProperty ms = (ModeProperty) setting;
                                        String value = String.valueOf(cfgSetting.value);
                                        if (ms.modes.contains(value)) {
                                            ms.setCurrentMode(value);
                                        } else {
                                            ms.setCurrentMode(ms.modes.get(0));
                                            //Lettuce.LOGGER.info(String.format("The value of setting %s in module %s was reset", ms.name, module.getName()));
                                        }
                                    }
                                    if (setting instanceof NumberProperty) {
                                        NumberProperty ss = (NumberProperty) setting;
                                        double value;
                                        try {
                                            value = Double.parseDouble(String.valueOf(cfgSetting.value));
                                        } catch (NumberFormatException e) {
                                            value = ss.getDefaultValue();
                                            //Lettuce.LOGGER.info(String.format("The value of setting %s in module %s was reset", ss.name, module.getName()));
                                        }
                                        ss.setValue(value);
                                    }
                                    if (setting instanceof MultipleBoolProperty) {
                                        MultipleBoolProperty mbs = (MultipleBoolProperty) setting;
                                        try {
                                            LinkedTreeMap<String, Boolean> boolMap = (LinkedTreeMap<String, Boolean>) cfgSetting.value;
                                            for (String s : boolMap.keySet()) {
                                                BooleanProperty childSetting = mbs.getSetting(s);
                                                if (childSetting != null && boolMap.get(s) != null) {
                                                    childSetting.setState(boolMap.get(s));
                                                }
                                            }
                                        } catch (Exception ignored) {}
                                    }
                                    if (setting instanceof ColorProperty) {
                                        ColorProperty colorSetting = (ColorProperty) setting;
                                        int color = Double.valueOf(String.valueOf(cfgSetting.value)).intValue();
                                        Color c = new Color(color, true); // Create Color object with alpha
                                        colorSetting.setColor(c);
                                    }
                                    if (setting instanceof StringProperty) {
                                        String value = String.valueOf(cfgSetting.value);
                                        if (value != null) {
                                            ((StringProperty) setting).setString(value);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }
}

