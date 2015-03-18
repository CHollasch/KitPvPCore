package us.supremeprison.kitpvp.core.util.math;

/**
 * @author Connor Hollasch
 * @since 2/26/2015
 */
public class TrigLookup {

    public static double[] SIN_VALUES = new double[360];
    public static double[] COS_VALUES = new double[360];

    static {
        for (int i = 0; i < 360; i++) {
            SIN_VALUES[i] = Math.sin(Math.toRadians(i));
            COS_VALUES[i] = Math.cos(Math.toRadians(i));
        }
    }

    public static int refAngleDegs(int angle) {
        if (angle < 0) {
            while (angle < 0)
                angle+=360;
        } else {
            while (angle > 360)
                angle-=360;
        }

        if (angle == 0)
            return angle;

        return angle-1;
    }
}
