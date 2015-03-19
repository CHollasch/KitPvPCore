package us.supremeprison.kitpvp.modules.DeathHandler;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.util.Damager;

import static us.supremeprison.kitpvp.core.util.Damager.Util.createNewDamageEvenet;
/**
 * @author Connor Hollasch
 * @since 3/18/2015
 */
public class DamageHandler implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged_entity = event.getEntity();
        Entity damager = event.getDamager();

        if (!(damaged_entity instanceof Player))
            return;

        Player damaged = (Player) damaged_entity;
        User damaged_user = User.fromPlayer(damaged);

        Damager damage_evnet = null;

        if (damager instanceof Player) {
            //Direct player attacking another player
            Player attacker = (Player) damager;
            damage_evnet = createNewDamageEvenet(attacker, event.getDamage(), "Attacked by {PLAYER}");
        } else if (damaged instanceof Projectile) {
            //Indirect shot projectile damaging player
            Projectile projectile = (Projectile) damager;
            if (!(projectile.getShooter() instanceof Player))
                return;

            Player shooter = (Player) projectile.getShooter();
            damage_evnet = createNewDamageEvenet(shooter, event.getDamage(), "Shot by {PLAYER} with " + projectile.getType().getName());
        }

        if (!damaged_user.getUserdata().contains("damage")) {
            DamageSet ds = new DamageSet();
            damaged_user.getUserdata().put("damage", ds);
        }

        DamageSet damage_set = damaged_user.getUserdata().get("damage");
        damage_set.addDamage(damage_evnet);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player died = event.getEntity();
    }
}
