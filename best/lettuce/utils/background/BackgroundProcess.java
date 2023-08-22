package best.lettuce.utils.background;

import best.lettuce.Lecture;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventKeyPressed;
import best.lettuce.modules.Module;

public class BackgroundProcess {
    private final EventListener<EventKeyPressed> onKeyPress = e -> {
        for (Module module : Lecture.INSTANCE.getModuleManager().getModules()) {
            if (module.getKeybind().getCode() == e.getKey()) {
                module.toggle(Module.ToggleType.MANUAL);
            }
        }
    };
}
