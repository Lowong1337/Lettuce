package best.lettuce.gui.notification;

import best.lettuce.Lettuce;
import best.lettuce.modules.impl.render.HUD;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.math.TimerUtils;
import best.lettuce.utils.render.RoundedUtils;
import lombok.Getter;
import net.minecraft.client.gui.Gui;

import java.awt.*;

@Getter
public class Notification implements MinecraftInstance {

    private final NotificationType notificationType;
    private final String title, description;
    private final float time;
    private final TimerUtils timerUtil;
    private final Animation animation;

    public Notification(NotificationType type, String title, String description) {
        this(type, title, description, NotificationManager.getToggleTime());
    }

    public Notification(NotificationType type, String title, String description, float time) {
        this.title = title;
        this.description = description;
        this.time = (long) (time * 1000);
        timerUtil = new TimerUtils();
        this.notificationType = type;
        animation = new DecelerateAnimation(250, 1);
    }

    public void drawLettuce(float x, float y, float width, float height){
        Color color = ColorUtils.applyOpacity(ColorUtils.interpolateColorC(Color.BLACK, getNotificationType().getColor(), .65f), .7f * 100);
        HUD hud = Lettuce.INSTANCE.getModuleManager().getModule(HUD.class);

        float percentage = Math.min((timerUtil.getTime() / getTime()), 1);
        if(hud.rounded.isEnabled()){
            RoundedUtils.drawRound(x, y, width, height, 4, new Color(0, 0, 0, 70));
            RoundedUtils.drawRound(x, y, (float) (width * percentage), height, 4, color);
        }
        else {
            Gui.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 70).getRGB());
            Gui.drawRect(x, y, x + (width * percentage), y + height, color.getRGB());
        }

        Color notificationColor = ColorUtils.applyOpacity(getNotificationType().getColor(), 70);
        Color textColor = ColorUtils.applyOpacity(Color.WHITE, 80);

        //Icon
        FontUtils.icontestFont40.drawString(getNotificationType().getIcon(), x + (notificationType == NotificationType.INFO ? 6.5f : 3), (y + FontUtils.icontestFont40.getMiddleOfBox(height) - 2), notificationColor);

        //lettuceBoldFont22.drawString(getTitle(), x + 10 + FontUtils.iconFont35.getStringWidth(getNotificationType().getIcon()), y + 4, textColor);
        lettuceFont20.drawString(getDescription(), x + (notificationType == NotificationType.INFO ? 1f : 2.8f) + FontUtils.iconFont35.getStringWidth(getNotificationType().getIcon()), y + 8f, textColor);
    }

    public void blurLettuce(float x, float y, float width, float height, boolean glow) {
        Color color = ColorUtils.applyOpacity(ColorUtils.interpolateColorC(Color.BLACK, getNotificationType().getColor(), glow ? 0.65f : 0), 70);
        float percentage = Math.min((timerUtil.getTime() / getTime()), 1);
        Gui.drawRect(x, y, x + width, y + height, Color.BLACK.getRGB());
        Gui.drawRect(x, y, x + (width * percentage), y + height, color.getRGB());
        ColorUtils.resetColor();
    }
}