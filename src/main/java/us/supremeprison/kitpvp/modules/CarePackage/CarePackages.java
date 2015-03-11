package us.supremeprison.kitpvp.modules.CarePackage;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.Depend;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.ParticleEffect;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;
import us.supremeprison.kitpvp.core.util.math.TrigLookup;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
@SuppressWarnings("unused")
@Depend
public class CarePackages extends Module {

    @ConfigOption(configuration_section = "CARE-PACKAGE-ITEM")
    private String care_package_material = Material.GHAST_TEAR.toString();

    private static HashSet<Item> alive_care_packages = new HashSet<>();
    private static HashMap<Location, MaterialData> old_states = new HashMap<>();

    @Override
    public void onDisable() {
        for (Item item : alive_care_packages) {
            item.remove();
        }

        for (Location all : old_states.keySet()) {
            MaterialData remember = old_states.get(all);
            all.getBlock().setType(remember.getItemType());
            all.getBlock().setData(remember.getData());
        }

        alive_care_packages.clear();
        old_states.clear();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand().getType().toString().equalsIgnoreCase(care_package_material)) {
            Common.removeOneInHand(player);

            final Item thrown = player.getWorld().dropItem(player.getEyeLocation(),
                    new ItemStack(Material.valueOf(care_package_material.replace(" ", "_").toUpperCase()), 1));
            thrown.setPickupDelay(20 * 60 * 60);
            thrown.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(1.3));
            alive_care_packages.add(thrown);

            final Runnable create_package_init_effects = new Runnable() {
                private int lifetime = 20 * 3;

                public void run() {
                    final Location chest_place_location = thrown.getLocation();

                    thrown.remove();
                    alive_care_packages.remove(thrown);

                    chest_place_location.getWorld().playSound(chest_place_location, Sound.PORTAL_TRAVEL, 0.3f, 0.5f);

                    for (int i = 0; i <= lifetime; i++) {
                        final int offsetI = i*10;
                        final Location next_particle_location = chest_place_location.clone().add(0.0d, (i/7.5), 0.0d);
                        Runnable next = new Runnable() {

                            public void run() {
                                //20 particles in circle
                                for (int i = 0; i < 360; i+= 18) {
                                    double cos = TrigLookup.COS_VALUES[i];
                                    double sin = TrigLookup.SIN_VALUES[i];

                                    Location clone = next_particle_location.clone().add(
                                            cos * TrigLookup.COS_VALUES[TrigLookup.refAngleDegs(offsetI)]*2, 0.0d,
                                            sin * TrigLookup.COS_VALUES[TrigLookup.refAngleDegs(offsetI)]*2);
                                    ParticleEffect.FLAME.display(clone, 0f, 0f, 0f, 0f, 1);
                                }

                                ParticleEffect.LARGE_SMOKE.display(next_particle_location, 0.7f, 0.7f, 0.7f, 0f, 50);
                            }
                        };
                        schedule(next, i);
                    }

                    Runnable end = new Runnable() {

                        public void run() {
                            old_states.put(chest_place_location,
                                    new MaterialData(chest_place_location.getBlock().getType(), chest_place_location.getBlock().getData()));
                            chest_place_location.getBlock().setType(Material.CHEST);
                            Chest chest = (Chest) chest_place_location.getBlock().getState();
                            //TODO Fill chest
                            chest_place_location.getWorld().strikeLightningEffect(chest_place_location);
                            ParticleEffect.CLOUD.display(chest_place_location, 0f, 0f, 0f, 0.4f, 150);
                        }
                    };
                    schedule(end, lifetime);
                }
            };
            schedule(create_package_init_effects, 20 * 3);
        }
    }
}
