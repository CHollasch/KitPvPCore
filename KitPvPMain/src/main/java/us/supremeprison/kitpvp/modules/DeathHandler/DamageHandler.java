package us.supremeprison.kitpvp.modules.DeathHandler;

import com.google.common.collect.Lists;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.util.Vector;
import us.supremeprison.kitpvp.core.KitPvP;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.Damager;
import us.supremeprison.kitpvp.modules.Killstreak.Killstreak;
import us.supremeprison.kitpvp.modules.Stats.Stats;

import java.util.HashMap;
import java.util.List;

import static org.bukkit.ChatColor.*;
import static us.supremeprison.kitpvp.core.util.Damager.Util.createNewDamageEvenet;

/**
 * @author Connor Hollasch
 * @since 3/18/2015
 */
public class DamageHandler implements Listener {

    private final DeathHandler module;
    public static final String DAMAGE_META_TAG = "damage";

    public DamageHandler(DeathHandler module) {
        this.module = module;
    }

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
            damage_evnet = createNewDamageEvenet(attacker.getName(), event.getDamage(), "Attacked with " + Common.namify(attacker.getItemInHand().getType()));
        } else if (damager instanceof Projectile) {
            //Indirect shot projectile damaging player
            Projectile projectile = (Projectile) damager;
            if (!(projectile.getShooter() instanceof Player))
                return;

            Player shooter = (Player) projectile.getShooter();
            damage_evnet = createNewDamageEvenet(shooter.getName(), event.getDamage(), "Shot with an " + projectile.getType().getName());
        } else if (damager instanceof LightningStrike) {
            damage_evnet = create(damaged.getName(), event.getDamage(), "Lightning");
        }

        if (!damaged_user.getUserdata().contains(DAMAGE_META_TAG)) {
            DamageSet ds = new DamageSet();
            damaged_user.getUserdata().put(DAMAGE_META_TAG, ds);
        }

        DamageSet damage_set = damaged_user.getUserdata().get(DAMAGE_META_TAG);
        damage_set.addDamage(damage_evnet);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();
        User user = User.fromPlayer(player);

        if (!user.getUserdata().contains(DAMAGE_META_TAG)) {
            DamageSet ds = new DamageSet();
            user.getUserdata().put(DAMAGE_META_TAG, ds);
        }

        DamageSet ds = user.getUserdata().get(DAMAGE_META_TAG);
        Damager taken = fromCause(player, event.getCause(), event.getDamage());

        if (taken == null)
            return;

        ds.addDamage(taken);
    }

    public static void applyDamageEvent(Player damaged, Damager damager) {
        User user = User.fromPlayer(damaged);

        if (!user.getUserdata().contains(DAMAGE_META_TAG)) {
            DamageSet ds = new DamageSet();
            user.getUserdata().put(DAMAGE_META_TAG, ds);
        }

        DamageSet ds = user.getUserdata().get(DAMAGE_META_TAG);
        ds.addDamage(damager);
    }

    @EventHandler
    public void onPlayerHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();
        User user = User.fromPlayer(player);

        if (!user.getUserdata().contains(DAMAGE_META_TAG)) {
            DamageSet ds = new DamageSet();
            user.getUserdata().put(DAMAGE_META_TAG, ds);
        }

        DamageSet ds = user.getUserdata().get(DAMAGE_META_TAG);
        ds.addHealed(event.getAmount());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow)
            event.getEntity().remove();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        //Constants
        event.setDeathMessage(null);
        event.setDroppedExp(0);
        event.getDrops().clear();
        event.setKeepLevel(true);

        final Player died = event.getEntity();
        Killstreak.getModule_instance().reset(died);

        if (died.hasPermission("kitpvp.donor")) {
            //Spiral arrows!
            final Location start = died.getEyeLocation().clone().add(0.5, 0, 0.5);

            for (int i = 0; i < 360; i += 10) {
                final int x = i;
                final double _x = Math.toRadians(x);
                Bukkit.getScheduler().scheduleSyncDelayedTask(KitPvP.getPlugin_instance(), new Runnable() {
                    public void run() {
                        double cos = Math.cos(_x);
                        double sin = Math.sin(_x);

                        Arrow arrow = (Arrow) died.getWorld().spawnEntity(start.clone().add(cos, 0, sin), EntityType.ARROW);
                        arrow.setFireTicks(20 * 5);
                        arrow.setShooter(died);
                        arrow.setVelocity(new Vector(cos, 0.3, sin).normalize().divide(new Vector(2, 1, 2)));
                    }
                }, i / 10);
            }
        }

        User user = User.fromPlayer(died);

        DamageSet ds = user.getUserdata().get(DAMAGE_META_TAG);
        //Display death stats

        FancyMessage post_death_data = new FancyMessage()
                .color(GREEN)
                .style(BOLD)
                .text("           Hover for details on how you died.");

        List<String> death_tooltip = Lists.newArrayList();
        death_tooltip.add(translateAlternateColorCodes('&', "&f&m=*--&7&m-&8- &f&m*-*--&7&m( &8&m*&7&m (&f&m--*&r &c&lDetails &f&m*--&7&m) &8&m* &7&m)&f&m--*-*&r &8-&7&m-&f&m--*="));
        death_tooltip.add("");
        death_tooltip.add(translateAlternateColorCodes('&', " &a&lKiller&7: &f" +
                (ds.getKiller() == null || ds.getKiller().equals(died) ? "Yourself" : ds.getKiller().getName())
                + " &7(&c" + ds.getDamage(ds.getKiller()) + " damage&7)"));

        int index = 0;

        HashMap<Player, Double> assistors = ds.getAssistors();
        death_tooltip.add("");
        if (assistors.size() != 0) {
            death_tooltip.add(translateAlternateColorCodes('&', " &a&lAssistors&7:"));
            for (Player key : assistors.keySet()) {
                String keyName = (key == null ? "Environment" : key.getName());
                Double value = assistors.get(key);
                death_tooltip.add(translateAlternateColorCodes('&',
                        "  &a&l" + (++index) + "&7.  &f" + keyName + " &7(&c" + value + " damage&7)"));
            }
            death_tooltip.add("");
        }
        death_tooltip.add(translateAlternateColorCodes('&', " &a&lLast damage taken&7:"));
        index = 0;
        for (Damager damaged : ds.getAllDamageCauses()) {
            death_tooltip.add(translateAlternateColorCodes('&', "  &a&l" + (++index) + "&7. &7(&c" +
                    (damaged.getDamager() == null || damaged.getDamager().equals(died) ? "Yourself" : damaged.getDamager())
                    + "&7) &f» &7(&c" + damaged.getDamage() + " damage&7) &f» &7(&c" + damaged.getDescription() + "&7)"));
        }
        death_tooltip.add("");
        death_tooltip.add(translateAlternateColorCodes('&', " &a&lAmount damaged&7: &f" + ds.getDamaged()));
        death_tooltip.add(translateAlternateColorCodes('&', " &a&lAmount healed&7: &f" + ds.getHealed()));
        death_tooltip.add("");
        death_tooltip.add(translateAlternateColorCodes('&', " &r&7&o&nClick message in chat for stats."));
        death_tooltip.add("");
        death_tooltip.add(translateAlternateColorCodes('&', "&f&m=*--&7&m-&8- &f&m*-*--&7&m( &8&m*&7&m (&f&m--*&r &c&lDetails &f&m*--&7&m) &8&m* &7&m)&f&m--*-*&r &8-&7&m-&f&m--*="));

        post_death_data.tooltip(death_tooltip.toArray(new String[0]));
        post_death_data.command("/stats");

        List<String> msg = module.death_message_formatting;
        for (String line : msg) {
            if (line.equalsIgnoreCase("{feed}")) {
                post_death_data.send(died);
            } else {
                died.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        user.getUserdata().put(DAMAGE_META_TAG, new DamageSet());

        Stats.addDeaths(died.getPlayer(), 1);

        Player actual_killer = Bukkit.getPlayer(ds.getKiller() == null || ds.getKiller().equals(died) ? "Yourself" : ds.getKiller().getName());
        if (actual_killer == null)
            return;

        if (!(actual_killer.isOnline()))
            return;

        Killstreak.getModule_instance().addKill(actual_killer);

        User killer = User.fromPlayer(actual_killer);
        Stats.addKills(killer.getPlayer(), 1);
    }

    private Damager fromCause(Player player, EntityDamageEvent.DamageCause cause, double damage) {
        String yourself = player.getName();
        switch (cause) {
            case DROWNING:
                return create(yourself, damage, "Drowning");
            case FALL:
                return create("World", damage, "Falling");
            case FIRE_TICK:
            case FIRE:
                return create("World", damage, "Fire");
            case LAVA:
                return create("World", damage, "Lava");
            case BLOCK_EXPLOSION:
                return create("World", damage, "Explosion");
            case STARVATION:
                return create(yourself, damage, "Starvation");
            case SUFFOCATION:
                return create("World", damage, "Suffocation");
            case SUICIDE:
                return create(yourself, player.getHealth(), "Suicide :o");
            case THORNS:
                return create(yourself, damage, "Thorns");
            case CONTACT:
                return create(yourself, damage, "Cactus");
            default:
                return null;
        }
    }

    private static Damager create(String player, double damage, String desc) {
        return Damager.Util.createNewDamageEvenet(player, damage, desc);
    }
}
