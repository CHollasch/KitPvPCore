package us.supremeprison.kitpvp.core.module;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import us.supremeprison.kitpvp.core.KitPvP;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
public abstract class Module implements Listener {

    public KitPvP parent_plugin;

    @Getter
    @Setter
    protected String module_name;

    @Getter
    @Setter
    private boolean isEnabled = false;

    public void onEnable() {}
    public void onDisable() {}

    public int schedule(Runnable runnable) {
        if (parent_plugin.isEnabled())
            return Bukkit.getScheduler().scheduleSyncDelayedTask(parent_plugin, runnable);
        return -1;
    }

    public int schedule(Runnable runnable, int delay) {
        if (parent_plugin.isEnabled())
            return Bukkit.getScheduler().scheduleSyncDelayedTask(parent_plugin, runnable, delay);
        return -1;
    }

    public int schedule(Runnable runnable, int delay, int repeat) {
        if (parent_plugin.isEnabled())
            return Bukkit.getScheduler().scheduleSyncRepeatingTask(parent_plugin, runnable, delay, repeat);
        return -1;
    }

    public int scheduleAsync(Runnable runnable) {
        if (parent_plugin.isEnabled())
            return Bukkit.getScheduler().scheduleAsyncDelayedTask(parent_plugin, runnable);
        return -1;
    }

    public int scheduleAsync(Runnable runnable, int delay) {
        if (parent_plugin.isEnabled())
            return Bukkit.getScheduler().scheduleAsyncDelayedTask(parent_plugin, runnable, delay);
        return -1;
    }

    public int scheduleAsync(Runnable runnable, int delay, int repeat) {
        if (parent_plugin.isEnabled())
            return Bukkit.getScheduler().scheduleAsyncRepeatingTask(parent_plugin, runnable, delay, repeat);
        return -1;
    }
}
