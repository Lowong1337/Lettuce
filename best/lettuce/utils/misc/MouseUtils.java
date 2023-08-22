package best.lettuce.utils.misc;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MouseUtils {
    public boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}