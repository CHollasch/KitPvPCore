package us.supremeprison.kitpvp.core.util;

import javax.naming.OperationNotSupportedException;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * @author Connor Hollasch
 * @since 3/30/2015
 */
public class RoundingMap<K, V> extends LinkedHashMap<K, V> {

    public K getTopKey(K origin) {
        if (!(origin instanceof Comparable))
            return null;

        Comparable<K> comparable = (Comparable<K>) origin;

        for (K key : keySet()) {
            int compare = comparable.compareTo(key);
            if (compare == 0 || compare <= -1)
                return  key;
        }

        return null;
    }

    public K getBottomKey(K origin) {
        if (!(origin instanceof Comparable))
            return null;

        Comparable<K> comparable = (Comparable<K>) origin;
        K candidate = null;

        for (K key : keySet()) {

            int compare = comparable.compareTo(key);

            if (compare >= 1)
                candidate = key;
            else if (compare == 0 || compare <= -1)
                return candidate;
        }

        return null;
    }

    public V getTopValue(V origin) {
        if (!(origin instanceof Comparable))
            return null;

        Comparable<V> comparable = (Comparable<V>) origin;

        for (K key : keySet()) {
            V value = get(key);

            int compare = comparable.compareTo(value);

            if (compare == 0 || compare <= -1)
                return value;
        }

        return null;
    }

    public V getBottomValue(V origin) {
        if (!(origin instanceof Comparable))
            return null;

        Comparable<V> comparable = (Comparable<V>) origin;
        V candidate = null;

        for (K key : keySet()) {
            V value = get(key);

            int compare = comparable.compareTo(value);

            if (compare >= 1)
                candidate = value;
            else if (compare == 0 || compare <= -1)
                return candidate;
        }

        return null;
    }

    public static class IntegerComparator implements Comparator<Integer> {
        public int compare(Integer o1, Integer o2) {
            return (o1.intValue() == o2.intValue() ? 0 : (o1 > o2 ? 1 : -1));
        }
    }
}
