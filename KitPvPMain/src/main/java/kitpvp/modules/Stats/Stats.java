package kitpvp.modules.Stats;

import org.bukkit.entity.Player;
import kitpvp.core.module.Module;
import kitpvp.core.user.User;
import kitpvp.core.user.attachment.common.IntegerAttachment;

/**
 * @author Connor Hollasch
 * @since 3/15/2015
 */
@SuppressWarnings("unused")
public class Stats extends Module {

    @Override
    public void onEnable() {
        User.getAttachments_manager().put(new IntegerAttachment("kills", 0));
        User.getAttachments_manager().put(new IntegerAttachment("deaths", 0));
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
}
