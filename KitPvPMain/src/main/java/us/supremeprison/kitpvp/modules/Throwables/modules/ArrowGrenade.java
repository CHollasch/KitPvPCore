package us.supremeprison.kitpvp.modules.Throwables.modules;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import us.supremeprison.kitpvp.core.KitPvP;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.ParticleEffect;
import us.supremeprison.kitpvp.modules.Killstreak.KillstreakReward;
import us.supremeprison.kitpvp.modules.Kits.Kit;
import us.supremeprison.kitpvp.modules.Throwables.ThrowableItem;

/**
 * @author Connor Hollasch
 * @since 6/2/2015
 */
public class ArrowGrenade extends ThrowableItem {

    protected static boolean activeOnMap = false;

    @Override
    public void onCreate(final Player player, final Item item) {
        activeOnMap = true;

        Runnable delay = new Runnable() {
            @Override
            public void run() {
                final Location origin = item.getLocation();
                item.remove();

                for (double i = 0; i < 5; i += 0.1) {
                    final double y = i;
                    scheduleAsync(new Runnable() {
                        @Override
                        public void run() {
                            final Location arrows = origin.clone().add(0, y, 0);
                            ParticleEffect.SMOKE_LARGE.display(0.3f, 0.1f, 0.3f, 0, 40, arrows);

                            for (int i = 0; i < 360; i += 18) {
                                double _x = Math.cos(Math.toRadians(i + (y * 40)));
                                double _y = Math.sin(Math.toRadians(i + (y * 40)));

                                double height = Math.sin(Math.toRadians(y * 120));

                                final Vector velocity = new Vector(_x, height, _y);
                                schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        Arrow shoot = (Arrow) item.getWorld().spawnEntity(arrows, EntityType.ARROW);
                                        shoot.setMetadata("arrow_grenade", new FixedMetadataValue(KitPvP.getPlugin_instance(), true));
                                        shoot.setVelocity(velocity);
                                        shoot.setBounce(false);
                                        shoot.setFireTicks(20 * 5);
                                    }
                                });
                            }

                            arrows.getWorld().playSound(arrows, Sound.FIZZ, 1f, 1f);
                        }
                    }, (int) (i * 15));
                }
            }
        };

        schedule(delay, 20 * 2);
        schedule(new Runnable() {
            @Override
            public void run() {
                activeOnMap = false;
            }
        }, 130);
    }

    @Override
    public boolean canCreate() {
        return !activeOnMap;
    }

    @EventHandler
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();

        if (damager instanceof Projectile) {
            Projectile shot = (Projectile) damager;
            if (shot.hasMetadata("arrow_grenade")) {
                event.setDamage(event.getDamage() * 4);
            }
        }
    }

    public static class ArrowGrenadeReward implements KillstreakReward {

        @Override
        public String getName() {
            return "Arrow Grenade";
        }

        protected static ItemStack icon = Common.craftItem(Material.SUGAR, 1, "&f&l<&eArrow &7Grenade&f&l>");

        @Override
        public ItemStack getIcon() {
            return icon;
        }

        @Override
        public int getKills() {
            return 5;
        }

        @Override
        public void giveToPlayer(Player player) {
            ItemStack icon = this.icon.clone();
            icon.setAmount(5);
            player.getInventory().addItem(icon);
        }

        @Override
        public String getDescription() {
            return "Create a barrage of flaming arrows that deal 4x damage!";
        }
    }
}
