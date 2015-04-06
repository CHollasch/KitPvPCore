package us.supremeprison.kitpvp.modules.Throwables.modules;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import us.supremeprison.kitpvp.core.util.ParticleEffect;
import us.supremeprison.kitpvp.modules.Throwables.ThrowableItem;

/**
 * @author Connor Hollasch
 * @since 3/18/2015
 */
public class StarfieldBomb extends ThrowableItem {

    @Override
    public void onCreate(final Item item) {
        final Location clone = item.getLocation();

        Runnable delay = new Runnable() {
            public void run() {
                //Do work
                for (int i = 0; i < (20 * 5); i++) {
                    final int x = i;
                    scheduleAsync(new Runnable() {
                        public void run() {
                            ParticleEffect.FIREWORKS_SPARK.display(clone, 1f, 1f, 1f, 0.1f, 30);
                            ParticleEffect.LARGE_SMOKE.display(clone, 0.2f, 0.2f, 0.2f, 0f, 40);

                            for (final Player near : getNearby(clone, 3)) {

                                if (x % 20 == 0) {
                                    final int random = (int) (Math.random() * near.getInventory().getSize());
                                    final ItemStack find = near.getInventory().getItem(random);
                                    if (find != null) {
                                        schedule(new Runnable() {
                                            @Override
                                            public void run() {
                                                near.getInventory().setItem(random, null);
                                                Item drop = near.getWorld().dropItem(near.getLocation(), find);
                                                drop.setVelocity(new Vector(Math.random()-.5, Math.random()-.5, Math.random()-.5));
                                                drop.setPickupDelay((20 * 5) - x);
                                            }
                                        });
                                    }
                                }

                                Vector make = clone.toVector();
                                make.subtract(near.getLocation().toVector());
                                make.setY(0);
                                make.normalize();
                                make.divide(new Vector(3, 3, 3));

                                near.setVelocity(make);
                            }

                            clone.getWorld().playSound(clone, Sound.AMBIENCE_CAVE, 1, 2);
                        }
                    }, i);
                }
                item.remove();
            }
        };
        schedule(delay, 20 * 2);
    }
}
