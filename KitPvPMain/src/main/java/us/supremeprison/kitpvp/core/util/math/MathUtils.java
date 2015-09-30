package us.supremeprison.kitpvp.core.util.math;

/**
 * @author Connor Hollasch
 * @since 6/2/2015
 */
public class MathUtils {

    public static double range(double min, double max) {
        return min + (Math.random() * ((max - min) + 1));
    }

    public static int range(int min, int max) {
        return (int) range((double) min, (double) max);
    }
}
