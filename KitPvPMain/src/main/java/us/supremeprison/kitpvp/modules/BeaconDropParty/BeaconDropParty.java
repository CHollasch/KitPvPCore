package us.supremeprison.kitpvp.modules.BeaconDropParty;

import org.bukkit.Bukkit;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;

/**
 * @author Connor Hollasch
 * @since 3/9/2015
 */
@SuppressWarnings("unused")
@ModuleDependency
public class BeaconDropParty extends Module {

    private static int dp_task_id;

    @ConfigOption("DP-TIME-CYCLE")
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
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(dp_task_id);
    }

    public void launchDropParty() {

    }
}
