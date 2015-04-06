package us.supremeprison.kitpvp.core.util.math;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Connor Hollasch
 * @since 2/26/2015
 */
public class TrigUtils {

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

    public static Collection<Location> getPointsInCircle(Location origin, int radius, int points) {
        Collection<Location> values = new ArrayList<>();

        for (int i = 0 ; i < 360 ; i+=(360 / points)) {
            double sin = SIN_VALUES[i];
            double cos = COS_VALUES[i];
            values.add(origin.clone().add(cos * radius, 0, sin * radius));
        }

        return values;
    }
}
