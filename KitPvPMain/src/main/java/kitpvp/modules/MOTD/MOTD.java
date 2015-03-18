package kitpvp.modules.MOTD;

import kitpvp.core.module.Module;
import kitpvp.core.util.config.ConfigOption;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.Arrays;
import java.util.List;

/**
 * @author Connor Hollasch
 * @since 3/16/2015
 */
@SuppressWarnings("unused")
public class MOTD extends Module {

    @ConfigOption(configuration_section = "LINES")
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
