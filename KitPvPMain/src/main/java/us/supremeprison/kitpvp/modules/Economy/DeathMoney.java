package us.supremeprison.kitpvp.modules.Economy;

import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;
import us.supremeprison.kitpvp.core.KitPvP;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.ParticleEffect;

import java.util.UUID;

/**
 * @author Connor Hollasch
 * @since 3/30/2015
 */
public class DeathMoney implements Listener {

    private Economy econ;

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(KitPvP.getPlugin_instance(), new Runnable() {
            @Override
            public void run() {
                Player died = event.getEntity();
                User user = User.fromPlayer(died);

                //Drop some cash and do cool things for donors
                Material[] drop = Economy.chanceRandomBill((int) (10 + (Math.random() * 10)));
                for (Material d : drop) {
                    synchronized (Bukkit.getServer()) {
                        Item item_drop = died.getWorld().dropItem(died.getLocation().clone(), Common.craftItem(d, 1, UUID.randomUUID().toString()));
                        item_drop.setVelocity(new Vector(Math.random() - .5, Math.random(), Math.random() - .5));
                    }
                }
            }
        });
    }

    @EventHandler
    public void onItemPick(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getItemStack().hasItemMeta() && event.getItem().getItemStack().getItemMeta().getDisplayName() != null) {
            //Check type
            if (Economy.item_worth.containsKey(event.getItem().getItemStack().getType().toString())) {
                double value = Economy.item_worth.get(event.getItem().getItemStack().getType().toString());
                Economy.setMoney(player, Economy.getMoney(player) + value);
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, (float) (Math.random() * 2));
                event.getItem().remove();
                ParticleEffect.INSTANT_SPELL.display(event.getItem().getLocation(), 0.02f, 0.02f, 0.02f, 0f, 5);
                event.setCancelled(true);
            }
        }
    }

    public DeathMoney setEcon(Economy econ) {
        this.econ = econ;
        return this;
    }
}
