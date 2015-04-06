package us.supremeprison.kitpvp.core.util.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import us.supremeprison.kitpvp.core.KitPvP;

/**
 * @author Connor Hollasch
 * @since 4/5/2015
 */
public abstract class OpenInventoryData implements Listener {

    protected Player player;

    public OpenInventoryData(Player player) {
        this.player = player;

        Bukkit.getPluginManager().registerEvents(this, KitPvP.getPlugin_instance());
    }

    public void openInventory() {
        player.openInventory(createInventory());
        KitPvP.getPlugin_instance().getOpen_inventories().put(player.getName(), this);
    }

    public void closeInventory() {
        remove();
        player.closeInventory();
    }

    protected void remove() {
        HandlerList.unregisterAll(KitPvP.getPlugin_instance().getOpen_inventories().remove(player.getName()));
    }

    public abstract Inventory createInventory();

    public abstract void onClick(InventoryClickEvent event);
}
