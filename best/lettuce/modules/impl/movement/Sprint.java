package best.lettuce.modules.impl.movement;

import best.lettuce.Lettuce;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.impl.player.Scaffold;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.utils.player.MoveUtils;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", Category.MOVEMENT, "Automatically sprints.");
        this.addProperties(legit);
    }

    public final BooleanProperty legit = new BooleanProperty("Legit", false);

    public final EventListener<EventTick> onTick = e -> this.setSuffix(legit.isEnabled() ? "Legit" : null);

    public final EventListener<EventMotion> onMotion = e -> {
        final NoSlowDown noslow = Lettuce.INSTANCE.getModuleManager().getModule(NoSlowDown.class);
        /*if (legit.isEnabled() && MoveUtils.isMoving() && mc.thePlayer.getFoodStats().getFoodLevel() > 6 && !mc.thePlayer.isCollidedHorizontally || !legit.isEnabled() && MoveUtils.isMoving() && mc.thePlayer.getFoodStats().getFoodLevel() > 6 && !mc.thePlayer.isCollidedHorizontally && mc.thePlayer.moveForward > 0) {
            if (mc.thePlayer.isUsingItem() && !noslow.isEnabled() || mc.thePlayer.isSneaking() || mc.thePlayer.isUsingItem() && noslow.canSlow)
                return;
            Scaffold scaffold = Lettuce.INSTANCE.getModuleManager().getModule(Scaffold.class);
            if (scaffold.isEnabled()) {
                mc.thePlayer.setSprinting(scaffold.sprinting.isEnabled());
            } else {
                mc.thePlayer.setSprinting(true);
            }
        }*/
        if(legit.isEnabled()){
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), MoveUtils.isMoving());
        }
        else {
            mc.thePlayer.setSprinting(MoveUtils.isMoving());
        }
    };

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindSprint.setPressed(mc.gameSettings.keyBindSprint.isKeyDown());
    }
}