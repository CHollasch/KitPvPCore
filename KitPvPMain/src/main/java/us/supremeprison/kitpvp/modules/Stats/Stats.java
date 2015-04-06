package us.supremeprison.kitpvp.modules.Stats;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.supremeprison.kitpvp.core.command.CommandModule;
import us.supremeprison.kitpvp.core.command.DynamicCommandRegistry;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.user.attachment.common.IntegerAttachment;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;
import us.supremeprison.kitpvp.modules.Rank.Rank;

/**
 * @author Connor Hollasch
 * @since 3/15/2015
 */
@SuppressWarnings("unused")
@ModuleDependency
public class Stats extends Module {

    @ConfigOption("STATS-MESSAGE-FORMAT")
    private String message_format = "&c&l{stat-name} &f&l» &7{stat-number}";

    @Override
    public void onEnable() {
        User.getAttachments_manager().put(new IntegerAttachment("kills", 0));
        User.getAttachments_manager().put(new IntegerAttachment("deaths", 0));

        DynamicCommandRegistry.registerCommand(new CommandModule("stats", new String[]{"kills", "deaths"}, false) {
            @Override
            public void onCommand(CommandSender sender, String[] args) {
                Player player = (Player) sender;
                int kills = getKills(player);
                int deaths = getDeaths(player);

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message_format
                        .replace("{stat-name}", "Kills")
                        .replace("{stat-number}", getKills(player) + "")));

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message_format
                        .replace("{stat-name}", "Deaths")
                        .replace("{stat-number}", getDeaths(player) + "")));

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message_format
                        .replace("{stat-name}", "Kill Death Ratio")
                        .replace("{stat-number}", Rank.getKdr_decimal_format().format(getKDR(player)))));
            }
        });
    }

    public static int getKills(Player player) {
        return User.fromPlayer(player).getAttachments().getAttachment("kills");
    }

    public static int getDeaths(Player player) {
        return User.fromPlayer(player).getAttachments().getAttachment("deaths");
    }

    public static void addKills(Player player, int kills) {
        User.fromPlayer(player).getAttachments().changeAttachment("kills", getKills(player) + kills);
    }

    public static void addDeaths(Player player, int deaths) {
        User.fromPlayer(player).getAttachments().changeAttachment("deaths", getDeaths(player) + deaths);
    }

    public static double getKDR(Player player) {
        int kills = getKills(player);
        int deaths = getDeaths(player);

        return (deaths == 0 ? (kills == 0 ? 1 : 0) : (double)kills/(double)deaths);
    }
}
