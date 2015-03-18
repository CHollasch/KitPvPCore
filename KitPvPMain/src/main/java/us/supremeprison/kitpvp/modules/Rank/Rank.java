package us.supremeprison.kitpvp.modules.Rank;

import us.supremeprison.kitpvp.core.command.CommandModule;
import us.supremeprison.kitpvp.core.command.DynamicCommandRegistry;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.user.attachment.Attachment;
import us.supremeprison.kitpvp.core.user.attachment.common.IntegerAttachment;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;
import us.supremeprison.kitpvp.core.util.messages.Form;
import us.supremeprison.kitpvp.modules.Economy.Economy;
import us.supremeprison.kitpvp.modules.Stats.Stats;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * @author Connor Hollasch
 * @since 3/17/2015
 */
@SuppressWarnings("unused")
public class Rank extends Module {

    @ConfigOption(configuration_section = "RANK-PREFIX")
    private String prefix = "&f&l<&9%kdr%&f&l> <&c%rank%&f&l>&r";

    @ConfigOption(configuration_section = "MAX-RANK")
    private int max_rank = 50;

    @Override
    public void onEnable() {
        Attachment<Integer> rankAttachment = new IntegerAttachment("rank", 1);
        User.getAttachments_manager().put(rankAttachment);

        DynamicCommandRegistry.registerCommand(new CommandModule("rankup", new String[]{}, false) {
            @Override
            public void onCommand(CommandSender sender, String[] args) {
                Player player = (Player) sender;
                User user = User.fromPlayer(player);
                int current_rank = getRank(player);

                if (current_rank >= max_rank) {
                    Form.at(player, "error", "You are currently the highest rank.");
                    return;
                }

                int next = cost(current_rank + 1);
                double bal = Economy.getMoney(player);

                if (bal < next) {
                    Form.at(player, "error", "You cannot afford to rankup.");
                    return;
                }

                user.getAttachments().changeAttachment("rank", (current_rank + 1));
                Economy.setMoney(player, bal - next);
                Form.at(player, "rank", "You ranked up to level &f" + (current_rank + 1) + "&e.");
                Form.at(player, "eco", "You now have &f" + Economy.getUserCashString(player));
            }
        });
    }

    private static int cost(int rank) {
        int cost = (int) ((50000 * rank) * Math.log(rank * rank));
        return cost;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        double kills = Stats.getKills(event.getPlayer());
        double deaths = Stats.getDeaths(event.getPlayer());

        double kdr = (deaths == 0 ? kills : kills/deaths);

        String format = prefix.replace("%rank%", getRank(event.getPlayer()) + "")
                .replace("%kdr%", kdr + "")
                + " " + event.getFormat();
        System.out.println(format);
        event.setFormat(format);
    }

    public static int getRank(Player player) {
        return User.fromPlayer(player).getAttachments().getAttachment("rank");
    }
}
