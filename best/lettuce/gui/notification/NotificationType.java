package best.lettuce.gui.notification;

import best.lettuce.utils.fonts.FontUtils;
import lombok.*;

import java.awt.*;

@Getter @AllArgsConstructor
public enum NotificationType {
    SUCCESS(new Color(20, 250, 90), FontUtils.CHECKMARK),
    DISABLE(new Color(255, 30, 30), FontUtils.XMARK),
    INFO(Color.WHITE, FontUtils.INFO),
    WARNING(Color.YELLOW, FontUtils.WARNING);
    private final Color color;
    private final String icon;
}