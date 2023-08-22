package best.lettuce.modules;

import best.lettuce.modules.impl.combat.*;
import best.lettuce.modules.impl.exploit.*;
import best.lettuce.modules.impl.player.NoRotate;
import best.lettuce.modules.impl.misc.*;
import best.lettuce.modules.impl.player.*;
import best.lettuce.modules.impl.render.*;
import best.lettuce.modules.impl.movement.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    public static boolean reloadModules;

    public void init() {

        //Combat
        add(new AntiBot());
        add(new KillAura());
        add(new TargetStrafe());
        add(new Velocity());

        //Exploits
        add(new AntiGuiClose());
        add(new Disabler());
        add(new ResourcePackSpoof());
        add(new SelfDamage());
        add(new PingSpoof());

        //Miscs
        add(new AutoHypixel());
        add(new KillSult());
        add(new StreamerMode());
        add(new Timer());

        //Movement
        add(new Fly());
        add(new Jesus());
        add(new NoSlowDown());
        add(new Sneak());
        add(new Speed());
        add(new Sprint());
        add(new Step());
        add(new Strafe());

        //Players
        add(new AutoTool());
        //add(new Blink());
        add(new ChestStealer());
        add(new InventoryManager());
        add(new InventoryMove());
        add(new NoFall());
        add(new NoVoid());
        add(new Scaffold());
        add(new AutoEat());
        add(new FastUse());
        add(new NoRotate());

        //Renders
        add(new Ambience());
        add(new Animations());
        add(new Camera());
        add(new ClickGUI());
        add(new ESP2D());
        add(new HUD());
        add(new Scoreboard());
        add(new Spotify());
        add(new VisualEffects());
        add(new LSD());
        add(new Cape());
        add(new TestShits());
        add(new ItemPhysics());
    }

    public void add(Module module) {
        modules.put(module.getClass(), module);
    }

    private HashMap<Object, Module> modules = new HashMap<>();

    public List<Module> getModules() {
        return new ArrayList<>(this.modules.values());
    }

    public void setModules(HashMap<Object, Module> modules) {
        this.modules = modules;
    }

    public List<Module> getModulesInCategory(Category c) {
        return this.modules.values().stream().filter(m -> m.getCategory() == c).collect(Collectors.toList());
    }

    public Module get(Class<? extends Module> mod) {
        return this.modules.get(mod);
    }

    public <T extends Module> T getModule(Class<T> mod) {
        return (T) this.modules.get(mod);
    }

    public List<Module> getModulesThatContainText(String text) {
        return this.getModules().stream().filter(m -> m.getName().toLowerCase().contains(text.toLowerCase())).collect(Collectors.toList());
    }

    public Module getModuleByName(String name) {
        return this.modules.values().stream()
                .filter(m -> m.getName().replaceAll("\\s+", "").equalsIgnoreCase(name.replaceAll("\\s+", "")))
                .findFirst()
                .orElse(null);
    }

    public List<Module> getModulesContains(String text) {
        return this.modules.values().stream().filter(m -> m.getName().toLowerCase().contains(text.toLowerCase())).collect(Collectors.toList());
    }

    public final List<Module> getToggledModules() {
        return this.modules.values().stream().filter(Module::isEnabled).collect(Collectors.toList());
    }
}
