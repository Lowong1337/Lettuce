package best.lettuce.modules.impl.misc;

import best.lettuce.Lecture;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventChatMessage;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.event.impl.render.EventRender2D;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.impl.render.HUD;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.modules.property.impl.StringProperty;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.math.TimerUtils;
import best.lettuce.utils.non_utils.multithread.Multithreading;
import best.lettuce.utils.render.RoundedUtils;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class AutoHypixel extends Module {

    private final BooleanProperty autoGG = new BooleanProperty("Auto GG", true);
    private final StringProperty autoGGMessage = new StringProperty("Auto GG Message", "gg", autoGG::isEnabled);
    private final BooleanProperty autoPlay = new BooleanProperty("Auto Play", true);
    private final NumberProperty autoPlayDelay = new NumberProperty("Play Delay", 2, 1, 10, 1, autoPlay::isEnabled);
    private final BooleanProperty autoHubOnBan = new BooleanProperty("Leave game on ban", false);

    public int second;

    public TimerUtils timer = new TimerUtils();

    public final Animation anim = new DecelerateAnimation(250, 1);

    public Color c = new Color(0,0,0);

    public AutoHypixel() {
        super("Auto Hypixel", Category.MISC, "auto play stuffs");
        this.addProperties(autoGG, autoGGMessage, autoPlay, autoPlayDelay, autoHubOnBan);
    }

    public final EventListener<EventMotion> onMotion = event -> {
        if(timer.hasTimeElapsed(1000)){
            if(second > 0){
                second -= 1;
                timer.reset();
            }
            else {
                anim.setDirection(Direction.FORWARDS);
            }
        }
    };

    public final EventListener<EventChatMessage> onChat = e -> {
        String message = e.message.getUnformattedText(), strippedMessage = StringUtils.stripControlCodes(message);
        if (autoHubOnBan.isEnabled() && strippedMessage.equals("A player has been removed from your game.")) {
            mc.thePlayer.sendChatMessage("/lobby");
            NotificationManager.post(NotificationType.WARNING, "Auto Hypixel", "A player in your lobby got banned.");
        }
        String m = e.message.toString();
        if (m.contains("ClickEvent{action=RUN_COMMAND, value='/play ")) {
            if (autoGG.isEnabled() && !strippedMessage.startsWith("You died!")) {
                mc.thePlayer.sendChatMessage("/ac " + autoGGMessage.getString());
            }
            if (autoPlay.isEnabled()) {
                sendToGame(m.split("action=RUN_COMMAND, value='")[1].split("'}")[0]);
            }
        }
    };

    public final EventListener<EventRender2D> onrender2d = e -> {
        HUD hud = Lecture.INSTANCE.getModuleManager().getModule(HUD.class);
        switch (hud.colorMode.getMode()){
            case "Rainbow": c = ColorUtils.rainbow(hud.colorSpeed.getValue().intValue(), 0, hud.saturation.getValue().floatValue(), 1, 1); break;
            case "Double Colors": c = ColorUtils.interpolateColorsBackAndForth(15, 0, hud.color1.getColor(), hud.color2.getColor(), false); break;
            case "Fade": c = ColorUtils.interpolateColorsBackAndForth(15, 0, hud.color1.getColor(), hud.color1.getColor().darker().darker(), false); break;
            case "Static": c = ColorUtils.interpolateColorsBackAndForth(15, 0, hud.color1.getColor(), hud.color1.getColor(), false); break;
        }
        RoundedUtils.drawRound(e.getWidth() / 2f - 55, e.getHeight() / 2f - 200 - anim.getOutput().floatValue() * 150, 110, 45, 5, new Color(43, 45, 48));
        RoundedUtils.drawRound(e.getWidth() / 2f - 55, e.getHeight() / 2f - 200 - anim.getOutput().floatValue() * 150, 110, MinecraftInstance.icontestFont26.getHeight() + 5, 0, c);
        MinecraftInstance.icontestFont26.drawString(FontUtils.GAMEPAD, e.getWidth() / 2f - 54, e.getHeight() / 2f - 200 + MinecraftInstance.icontestFont26.getMiddleOfBox(MinecraftInstance.icontestFont26.getHeight() + 5) - anim.getOutput().floatValue() * 150, Color.WHITE);
        hud.getFont().drawCenteredString(EnumChatFormatting.BOLD + "Auto Play", e.getWidth() / 2f, e.getHeight() / 2f - 200 + hud.getFont().getMiddleOfBox(hud.getFont().getHeight() + 5) - anim.getOutput().floatValue() * 150, Color.WHITE);
        hud.getFont().drawCenteredString("Joining new game in", e.getWidth() / 2f, e.getHeight() / 2f - 200 + hud.getFont().getMiddleOfBox(hud.getFont().getHeight() + 5) + 16 - anim.getOutput().floatValue() * 150, Color.WHITE);
        hud.getFont().drawCenteredString(second + " seconds", e.getWidth() / 2f, e.getHeight() / 2f - 200 + hud.getFont().getMiddleOfBox(hud.getFont().getHeight() + 5) + 30 - anim.getOutput().floatValue() * 150, Color.WHITE);
    };

    private void sendToGame(String mode) {
        int delay = autoPlayDelay.getValue().intValue();
        second = delay;
        timer.reset();
        anim.setDirection(Direction.BACKWARDS);
        Multithreading.schedule(() -> mc.thePlayer.sendChatMessage(mode), (long) delay, TimeUnit.SECONDS);
    }
}