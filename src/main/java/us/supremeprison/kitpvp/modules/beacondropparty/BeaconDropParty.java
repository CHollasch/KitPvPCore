package us.supremeprison.kitpvp.modules.BeaconDropParty;

import org.bukkit.Bukkit;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.Depend;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;

/**
 * @author Connor Hollasch
 * @since 3/9/2015
 */
@SuppressWarnings("unused")
@Depend
public class BeaconDropParty extends Module {

    private static int dp_task_id;

    @ConfigOption(configuration_section = "DP-TIME-CYCLE")
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
        dp_task_id = schedule(dp_task, 20, 20);
        parent_plugin.logMessage(this, "Successfully registered drop party task with a " + dp_time_cycle + " second cycle");
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(dp_task_id);
    }

    public void launchDropParty() {

    }
}
