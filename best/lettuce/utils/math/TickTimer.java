package best.lettuce.utils.math;

public class TickTimer {
    public int tick = 0;

    public void update() {
        tick++;
    }

    public void reset() {
        tick = 0;
    }
}
