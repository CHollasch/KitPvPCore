package us.supremeprison.kitpvp.core.user.attachment;

import java.util.HashMap;

/**
 * @author Connor Hollasch
 * @since 3/18/2015
 */
public class EasyUserdata {

    private HashMap<String, Object> userdata = new HashMap<>();

    public void put(String label, Object value) {
        userdata.remove(label.toLowerCase());
        userdata.put(label.toLowerCase(), value);
    }

    public <T> T get(String label) {
        return (T) userdata.get(label.toLowerCase());
    }

    public boolean contains(String label) {
        return userdata.containsKey(label.toLowerCase());
    }

    public void remove(String label) {
        userdata.remove(label);
    }
}
