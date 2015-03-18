package kitpvp.modules.CarePackage;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import kitpvp.core.util.Common;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import kitpvp.core.module.Module;
import kitpvp.core.module.modifiers.ModuleDependency;
import kitpvp.core.util.ParticleEffect;
import kitpvp.core.util.Todo;
import kitpvp.core.util.config.ConfigOption;
import kitpvp.core.util.hologram.HologramManager;
import kitpvp.core.util.math.TrigLookup;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
@SuppressWarnings("unused")
@ModuleDependency
@Todo("Fill the dropped chest with items")
public class CarePackages extends Module {

    @ConfigOption(configuration_section = "CARE-PACKAGE-ITEM")
    private String care_package_material = Material.GHAST_TEAR.toString();

    private HashSet<Item> alive_care_packages = new HashSet<>();
    private HashMap<Block, MaterialData> old_states = new HashMap<>();

    private HashMap<Block, Inventory> care_package_inventories = new HashMap<>();

    @Override
    public void onDisable() {
        for (Item item : alive_care_packages) {
            item.remove();
        }

        for (Block all : old_states.keySet()) {
            MaterialData remember = old_states.get(all);
            all.setType(remember.getItemType());
            all.setData(remember.getData());
        }

        care_package_inventories.clear();
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

                                if (offsetI % 40 == 0) {
                                    next_particle_location.getWorld().playSound(next_particle_location, Sound.FIZZ, 0.3f, 0.5f);
                                }

                                ParticleEffect.LARGE_SMOKE.display(next_particle_location, 0.7f, 0.7f, 0.7f, 0f, 50);
                            }
                        };
                        scheduleAsync(next, i);
                    }

                    Runnable end = new Runnable() {
                        public void run() {
                            if (old_states.containsKey(chest_place_location))
                                return; //for now

                            old_states.put(chest_place_location.getBlock(),
                                    new MaterialData(chest_place_location.getBlock().getType(), chest_place_location.getBlock().getData()));
                            chest_place_location.getBlock().setType(Material.ENDER_CHEST);

                            Inventory inv = Bukkit.createInventory(null, 18, Common.center("Care Package"));
                            care_package_inventories.put(chest_place_location.getBlock(), inv);

                            final Hologram care_package_holo = HologramManager.buildHologram(chest_place_location.clone().add(0.5, 0.9, 0.5),
                                    ChatColor.translateAlternateColorCodes('&', "&c&lCare Package"),
                                    ChatColor.translateAlternateColorCodes('&', "&7Open to find a bunch of loot!"));

                            schedule(new Runnable() {
                                public void run() {
                                    if (care_package_holo.isDeleted())
                                        return;

                                    HologramManager.removeHologram(care_package_holo);
                                }
                            }, 20 * 10);

                            //Find the top center of the chest for spiral effect
                            final Location start = chest_place_location.getBlock().getLocation().clone().add(0.5, 1, 0.5);
                            //Create 1/2 of a curve for our trails to follow
                            for (int i = 0; i < 180; i+=2) {
                                final int refI = i;
                                Runnable nextParticlePlace = new Runnable() {
                                    public void run() {
                                        //Find the sin of our outer "i" value for proper y curve and outwards spiral
                                        double sin = TrigLookup.SIN_VALUES[refI] * 2;
                                        for (int i = 0; i < 360; i+=36) {
                                            //Create our particle instance location
                                            Location play = start.clone().add(TrigLookup.COS_VALUES[TrigLookup.refAngleDegs(i+refI)] * sin,
                                                    sin/2, TrigLookup.SIN_VALUES[TrigLookup.refAngleDegs(i+refI)] * sin);
                                            //Display the spark effect
                                            ParticleEffect.FIREWORKS_SPARK.display(play, 0f, 0f, 0f, 0f, 1);
                                        }
                                    }
                                };
                                scheduleAsync(nextParticlePlace, i/2);
                            }

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

    @EventHandler
    public void onCarePackageOpen(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        if (care_package_inventories.containsKey(event.getClickedBlock())) {
            event.setCancelled(true);
            event.getPlayer().openInventory(care_package_inventories.get(event.getClickedBlock()));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!Common.empty(event.getInventory()))
            return;

        if (care_package_inventories.values().contains(event.getInventory())) {
            //find key!
            Block find = null;
            for (Block key : care_package_inventories.keySet()) {
                if (care_package_inventories.get(key).equals(event.getInventory())) {
                    MaterialData old = old_states.get(key);
                    ParticleEffect.displayBlockCrack(key.getLocation().clone().add(0.5, 0.5, 0.5), key.getTypeId(), key.getData(), 0.25f, 0.25f, 0.25f, 30);
                    key.getWorld().playSound(key.getLocation(), Sound.GLASS, 1, 1);
                    key.setType(old.getItemType());
                    key.setData(old.getData());
                    old_states.remove(key);

                    find = key;
                }
            }

            care_package_inventories.remove(find);
        }
    }
}
