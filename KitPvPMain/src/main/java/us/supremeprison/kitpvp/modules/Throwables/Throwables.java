package us.supremeprison.kitpvp.modules.Throwables;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.modules.Throwables.modules.StarfieldBomb;

import java.util.HashMap;

/**
 * @author Connor Hollasch
 * @since 4/6/2015
 */
public class Throwables extends Module {

    private HashMap<Material, ThrowableItem> throwables = new HashMap<Material, ThrowableItem>() {
        {
            put(Material.NETHER_STAR, new StarfieldBomb());
        }
    };

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack holding = player.getItemInHand();

        if (holding == null)
            return;

        if (throwables.containsKey(holding.getType())) {
            //Is throwable material
            Common.removeOneInHand(player);

            ItemStack stack = holding.clone();
            stack.setAmount(1);

            Item item = player.getWorld().dropItem(player.getEyeLocation(), stack);
            item.setPickupDelay(20 * 3600);
            item.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(1.3));

            throwables.get(holding.getType()).onCreate(item);
        }
    }
}
