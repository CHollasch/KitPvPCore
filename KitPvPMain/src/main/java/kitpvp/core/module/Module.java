package kitpvp.core.module;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import kitpvp.core.KitPvP;

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

    protected boolean validatePermission(Player player, String permission) {
        if (player.hasPermission(permission))
            return true;

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l(!) &7You don't have access to this command!"));
        return false;
    }

    protected boolean validateIsPlayer(Player player, String input, boolean can_player_be_same) {
        Player find = Bukkit.getPlayer(input);
        if (find == null) {
            player.sendMessage(ChatColor.RED + "No such player!");
            return false;
        }

        if (!can_player_be_same && find.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot target yourself!");
            return false;
        }

        return true;
    }

    protected boolean validateIsNumber(Player player, String input) {
        try {
            Double.parseDouble(input);
        } catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED + "No such number \"" + input + "\"!");
            return false;
        }

        return true;
    }
}
