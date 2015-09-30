package us.supremeprison.kitpvp.core.util;

import java.util.LinkedHashMap;

/**
 * @author Connor Hollasch
 * @since 3/30/2015
 */
public class KeyRoundingMap<K extends Number, V> extends LinkedHashMap<K, V> {

    public K getTopKey(K origin) {
        double o = origin.doubleValue();
        K candidate = null;

        for (K set : keySet()) {
            double s = set.doubleValue();
            if (o <= s && (candidate == null || (Math.abs(o - s) < Math.abs(o - candidate.doubleValue())))) {
                candidate = set;
            }
        }

        return candidate;
    }

    public K getBottomKey(K origin) {
        double o = origin.doubleValue();
        K candidate = null;

        for (K set : keySet()) {
            double s = set.doubleValue();
            if (o >= s && (candidate == null || (Math.abs(o - s) < Math.abs(o - candidate.doubleValue())))) {
                candidate = set;
            }
        }

        return candidate;
    }
}
