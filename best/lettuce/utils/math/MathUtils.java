package best.lettuce.utils.math;

import lombok.experimental.UtilityClass;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.optifine.CustomColors.random;

@UtilityClass
public class MathUtils {
    public double roundToHalf(double d) {
        return Math.round(d * 2) / 2.0;
    }

    public Double interpolate(double oldValue, double newValue, double interpolationValue){
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue){
        return interpolate(oldValue, newValue, (float) interpolationValue).floatValue();
    }

    public static float interpolateInt(float oldValue, float newValue, double interpolationValue){
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }

    public static double round(double num, double increment) {
        BigDecimal bd = new BigDecimal(num);
        bd = (bd.setScale((int) increment, RoundingMode.HALF_UP));
        return bd.doubleValue();
    }

    public double getRandomInRange(double max, double min) {
        return min + (max - min) * random.nextDouble();
    }

    public double[] yawPos(float yaw, double value) {
        return new double[]{-MathHelper.sin(yaw) * value, MathHelper.cos(yaw) * value};
    }

    public static double square(double squareX) {
        squareX *= squareX;
        return squareX;
    }
}
