package us.supremeprison.kitpvp.core.util.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import us.supremeprison.kitpvp.core.KitPvP;

/**
 * @author Connor Hollasch
 * @since 4/6/2015
 */
public class InventoryListener implements Listener {

    @EventHandler
    private void onEventClick(InventoryClickEvent event) {
        String name = event.getWhoClicked().getName();
        if (KitPvP.getPlugin_instance().getOpen_inventories().containsKey(name))
            KitPvP.getPlugin_instance().getOpen_inventories().get(event.getWhoClicked().getName()).onClick(event);
    }

    @EventHandler
    public void onPlayerCloseInv(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        OpenInventoryData oid = KitPvP.getPlugin_instance().getOpen_inventories().get(player.getName());

        if (oid != null && player.equals(oid.player)) {
            //handle close
            oid.remove();
        }
    }

}
