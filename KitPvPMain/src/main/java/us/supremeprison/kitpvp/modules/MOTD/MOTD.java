package us.supremeprison.kitpvp.modules.MOTD;

import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;

import java.util.Arrays;
import java.util.List;

/**
 * @author Connor Hollasch
 * @since 3/16/2015
 */
@SuppressWarnings("unused")
@ModuleDependency
public class MOTD extends Module {

    @ConfigOption("LINES")
    private List<String> motd = Arrays.asList(new String[]{"&f&m*--*&r &c&lNebula &f&m*--*", "&f&m*----*&r &c&lPvP &f&m*----*"});

    @EventHandler
    public void onPlayerPing(ServerListPingEvent event) {
        String compile = "";

        for (String line : motd) {
            compile += ChatColor.translateAlternateColorCodes('&', line + "\n");
        }

        event.setMotd(compile);
    }
}
