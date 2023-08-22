package best.lettuce.utils.non_utils.scroll;

import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.SmoothStepAnimation;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Mouse;

public class Scroll {

    @Getter @Setter
    private float maxScroll = Float.MAX_VALUE, minScroll = 0, rawScroll = 0;
    private float scroll;
    @Getter
    private Animation scrollAnimation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);

    public void onScroll(int ms) {
        scroll = rawScroll - scrollAnimation.getOutput().floatValue();
        rawScroll += Mouse.getDWheel() / 4f;
        rawScroll = Math.max(Math.min(minScroll, rawScroll), -maxScroll);
        scrollAnimation = new SmoothStepAnimation(ms, rawScroll - scroll, Direction.BACKWARDS);
    }

    public boolean isScrollAnimationDone() {
        return scrollAnimation.isDone();
    }

    public float getScroll() {
        scroll = rawScroll - scrollAnimation.getOutput().floatValue();
        return scroll;
    }
}