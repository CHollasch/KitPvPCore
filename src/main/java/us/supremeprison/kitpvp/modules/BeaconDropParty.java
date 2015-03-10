package us.supremeprison.kitpvp.modules;

import org.bukkit.Bukkit;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;

/**
 * @author Connor Hollasch
 * @since 3/9/2015
 */
public class BeaconDropParty extends Module {

    @ConfigOption(configuration_section = "DropParty-Time-Cycle")
    private int dp_time_cycle = 5400;

    private int dp_time_left = dp_time_cycle;

    @Override
    public void onEnable() {
        Runnable dp_task = new Runnable() {
            @Override
            public void run() {
                if (dp_time_left-- <= 0) {
                    dp_time_left = dp_time_cycle;
                    launchDropParty();
                }
            }
        };
        Bukkit.getScheduler().scheduleSyncRepeatingTask(parent_plugin, dp_task, 20l, 20l);
    }

    public void launchDropParty() {

    }
}
