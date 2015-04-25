package us.supremeprison.kitpvp.modules.Killstreak;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Connor Hollasch
 * @since 4/7/2015
 */
public interface KillstreakReward {

    public String getName();

    public ItemStack getIcon();

    public int getKills();

    public void giveToPlayer(Player player);
}
