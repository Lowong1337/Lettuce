package best.lettuce.utils.animation.impl;

import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;

public class SmoothAnimation extends Animation {

    public SmoothAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public SmoothAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        return 1 - Math.pow(1 - x, 2);
    }
}