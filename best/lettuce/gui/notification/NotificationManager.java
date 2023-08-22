package best.lettuce.gui.notification;

import best.lettuce.Lecture;
import best.lettuce.modules.impl.render.HUD;
import lombok.*;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    @Getter @Setter
    private static float toggleTime = 2;

    @Getter
    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public static void post(NotificationType type, String title, String description) {
        post(new Notification(type, title, description));
    }

    public static void post(NotificationType type, String title, String description, float time) {
        post(new Notification(type, title, description, time));
    }

    public static void post(Notification notification) {
        if (Lecture.INSTANCE.getModuleManager().getModule(HUD.class).isEnabled() && HUD.hudOptions.getSetting("Notifications").isEnabled()) {
            notifications.add(notification);
        }
    }
}