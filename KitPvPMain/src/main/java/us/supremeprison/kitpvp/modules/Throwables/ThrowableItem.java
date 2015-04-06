package us.supremeprison.kitpvp.modules.Throwables;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import us.supremeprison.kitpvp.core.KitPvP;

import java.util.List;

/**
 * @author Connor Hollasch
 * @since 3/18/2015
 */
public abstract class ThrowableItem {

    public abstract void onCreate(Item item);

    public int schedule(Runnable runnable) {
        if (KitPvP.getPlugin_instance().isEnabled())
            return Bukkit.getScheduler().scheduleSyncDelayedTask(KitPvP.getPlugin_instance(), runnable);

        return -1;
    }

    public int schedule(Runnable runnable, int delay) {
        if (KitPvP.getPlugin_instance().isEnabled())
            return Bukkit.getScheduler().scheduleSyncDelayedTask(KitPvP.getPlugin_instance(), runnable, delay);
        return -1;
    }

    public int schedule(Runnable runnable, int delay, int repeat) {
        if (KitPvP.getPlugin_instance().isEnabled())
            return Bukkit.getScheduler().scheduleSyncRepeatingTask(KitPvP.getPlugin_instance(), runnable, delay, repeat);
        return -1;
    }

    public int scheduleAsync(Runnable runnable) {
        if (KitPvP.getPlugin_instance().isEnabled())
            return Bukkit.getScheduler().scheduleAsyncDelayedTask(KitPvP.getPlugin_instance(), runnable);
        return -1;
    }

    public int scheduleAsync(Runnable runnable, int delay) {
        if (KitPvP.getPlugin_instance().isEnabled())
            return Bukkit.getScheduler().scheduleAsyncDelayedTask(KitPvP.getPlugin_instance(), runnable, delay);
        return -1;
    }

    public int scheduleAsync(Runnable runnable, int delay, int repeat) {
        if (KitPvP.getPlugin_instance().isEnabled())
            return Bukkit.getScheduler().scheduleAsyncRepeatingTask(KitPvP.getPlugin_instance(), runnable, delay, repeat);
        return -1;
    }

    public List<Player> getNearby(Location location, double radius) {
        List<Player> all = Lists.newArrayList();
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= radius)
                all.add(player);
        }
        return all;
    }
}
