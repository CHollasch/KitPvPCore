package us.supremeprison.kitpvp.core.util.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * @author Connor Hollasch
 * @since 3/29/2015
 */
public class LocationSerializer implements ConfigSerializable<Location> {

    @Override
    public Location load(String in) {
        String[] parts = in.split(",");
        return new Location(Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5]));
    }

    @Override
    public String save(Location location) {
        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    @Override
    public Class<? extends Location> getWrappedType() {
        return Location.class;
    }
}
