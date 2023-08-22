package best.lettuce.modules;

import best.lettuce.Lettuce;
import best.lettuce.config.ConfigProperty;
import best.lettuce.config.DragManager;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.impl.player.Scaffold;
import best.lettuce.modules.impl.render.HUD;
import best.lettuce.modules.property.Property;
import best.lettuce.modules.property.impl.KeybindProperty;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.non_utils.drag.Dragging;
import best.lettuce.utils.non_utils.multithread.Multithreading;
import com.google.gson.annotations.*;
import lombok.*;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class Module implements MinecraftInstance {

    private final String description;
    private final Category category;
    private final CopyOnWriteArrayList<Property> settingsList = new CopyOnWriteArrayList<>();

    private String suffix;

    @Expose
    @SerializedName("enabled")
    private boolean enabled;
    @Expose
    @SerializedName("settings")
    public ConfigProperty[] cfgProperties;
    @Expose
    @SerializedName("name")
    private final String name;

    @Getter
    @Setter
    private boolean expanded;

    public static int categoryCount;

    @Getter
    private final Animation animation = new DecelerateAnimation(250, 1).setDirection(Direction.BACKWARDS);

    private final KeybindProperty keybind = new KeybindProperty(Keyboard.KEY_NONE);

    public Module(String name, Category category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
        addProperties(keybind);
    }

    public boolean isInGame() {
        return mc.theWorld != null && mc.thePlayer != null;
    }

    public void addProperties(Property... settings) {
        settingsList.addAll(Arrays.asList(settings));
    }

    public String getName() {
        return this.name;
    }

    public List<Property> getProperties() {
        return settingsList;
    }

    public String getDescription() {
        return this.description;
    }

    public Category getCategory() {
        return this.category;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean hasMode() {
        return suffix != null;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle(ToggleType type) {
        this.enabled = !this.enabled;
        boolean toggleNotifications = Lettuce.INSTANCE.getModuleManager().getModule(HUD.class).toggleNotifications.isEnabled();
        if (this.enabled) {
            this.onEnable();
            Lettuce.INSTANCE.getEventManager().register(this);
            try {
                if (toggleNotifications && type == ToggleType.MANUAL && !Objects.equals(this.name, "ClickGUI")) {
                    NotificationManager.post(NotificationType.SUCCESS, "Toggled", "Enabled " + this.name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (this instanceof Scaffold scaffold) {
                scaffold.anim.setDirection(Direction.BACKWARDS);
                Multithreading.schedule(() -> Lettuce.INSTANCE.getEventManager().unregister(this), 0, TimeUnit.MILLISECONDS);
            } else {
                Lettuce.INSTANCE.getEventManager().unregister(this);
            }
            try {
                if (toggleNotifications && type == ToggleType.MANUAL && !Objects.equals(this.name, "ClickGUI")) {
                    NotificationManager.post(NotificationType.DISABLE, "Toggled", "Disabled " + this.name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.onDisable();
        }
    }

    public void onEnable() {}

    public void onDisable() {}

    public KeybindProperty getKeybind() {
        return keybind;
    }

    public void setKey(int code) {
        this.keybind.setCode(code);
    }

    public Dragging createDrag(Module module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }

    public Module get(Class<? extends Module> module) {
        return Lettuce.INSTANCE.getModuleManager().get(module);
    }

    public boolean isModuleEnabled(Class<? extends Module> module) {
        return Lettuce.INSTANCE.getModuleManager().get(module).isEnabled();
    }

    public void toggle(Class<? extends Module> module) {
        Lettuce.INSTANCE.getModuleManager().get(module).toggle(ToggleType.MANUAL);
    }

    public enum ToggleType {
        MANUAL, AUTO
    }
}