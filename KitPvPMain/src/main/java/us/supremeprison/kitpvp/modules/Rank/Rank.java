package us.supremeprison.kitpvp.modules.Rank;

import lombok.Getter;
import us.supremeprison.kitpvp.core.command.CommandModule;
import us.supremeprison.kitpvp.core.command.DynamicCommandRegistry;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
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

import java.text.DecimalFormat;

/**
 * @author Connor Hollasch
 * @since 3/17/2015
 */
@SuppressWarnings("unused")
@ModuleDependency
public class Rank extends Module {

    @ConfigOption("RANK-PREFIX")
    private String prefix = "&f&l<&9%kdr%&f&l> <&c%rank%&f&l>&r";

    @ConfigOption("MAX-RANK")
    @Getter
    private static int max_rank = 50;

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

    @Getter
    private static DecimalFormat kdr_decimal_format = new DecimalFormat("#,##0.0#");

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        double kdr = Stats.getKDR(event.getPlayer());

        String format = prefix.replace("%rank%", getRank(event.getPlayer()) + "")
                .replace("%kdr%", kdr_decimal_format.format(kdr) + "")
                + " " + event.getFormat();

        event.setFormat(format);
    }

    public static int getRank(Player player) {
        return User.fromPlayer(player).getAttachments().getAttachment("rank");
    }
}
