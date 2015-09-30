package us.supremeprison.kitpvp.modules.SpaceCrate;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.Enchantments;
import us.supremeprison.kitpvp.core.util.ParticleEffect;
import us.supremeprison.kitpvp.core.util.Todo;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;
import us.supremeprison.kitpvp.core.util.hologram.HologramManager;
import us.supremeprison.kitpvp.core.util.math.TrigUtils;
import us.supremeprison.kitpvp.modules.Killstreak.Killstreak;
import us.supremeprison.kitpvp.modules.Killstreak.KillstreakReward;

import java.util.*;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
@SuppressWarnings("unused")
@ModuleDependency(depends_on = {"modulemanager", "killstreak"})
@Todo("Fill the dropped chest with items")
public class SpaceCrate extends Module {

    @ConfigOption("SPACE-CRATE-ITEM")
    private static String space_container_material = Material.GHAST_TEAR.toString();

    private HashSet<Item> alive_space_crate = new HashSet<>();
    private HashMap<Block, MaterialData> old_states = new HashMap<>();

    private HashMap<Block, Inventory> space_container_inventories = new HashMap<>();
    private ArrayList<String> raw_space_items = new ArrayList<String>() {
        {
            add("DIAMOND_SWORD 1 sharpness:1 unbreaking:1");
        }
    };

    private List<ItemStack> space_crate_items = new ArrayList<>();

    private HashMap<Block, Hologram> holograms = new HashMap<>();

    @Override
    public void onEnable() {
        Killstreak.getModule_instance().addKillstreakReward(new SpaceCrateReward());
        for (String item : raw_space_items) {
            String[] parts = item.split(" ");

            Material material;
            byte data = 0x0;

            if (parts[0].contains(":")) {
                data = Byte.parseByte(parts[0].split(":")[1]);
                material = Material.getMaterial(parts[0].split(":")[0]);
            } else {
                material = Material.getMaterial(parts[0]);
            }

            int amount = Integer.parseInt(parts[1]);

            String[] shiftCopy = new String[parts.length - 2];
            System.arraycopy(parts, 2, shiftCopy, 0, shiftCopy.length);

            ItemStack stack = new ItemStack(material, amount, (short) 0, data);
            ItemMeta meta = stack.getItemMeta();

            HashMap<Enchantment, Integer> enchMap = new HashMap<>();

            for (String part : shiftCopy) {
                if (part.startsWith("name")) {
                    //Item name
                    String name = part.split(":")[1];
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                } else if (part.startsWith("lore")) {
                    //Lore
                    String[] lore = part.split(":")[1].split("[\\|]");
                    Common.arrayModifier(lore, new Common.ArrayModifier<String>() {
                        @Override
                        public String changeAtIndex(String at, int index) {
                            return ChatColor.translateAlternateColorCodes('&', at);
                        }
                    });
                    meta.setLore(Arrays.asList(lore));
                } else {
                    String[] enchantmentData = part.split(":");

                    Enchantment enchantment = Enchantments.getByName(enchantmentData[0]);
                    int enchantAmount = Integer.parseInt(enchantmentData[1]);

                    enchMap.put(enchantment, enchantAmount);
                }
            }

            stack.setItemMeta(meta);
            stack.addUnsafeEnchantments(enchMap);
        }
    }

    @Override
    public void onDisable() {
        for (Item item : alive_space_crate) {
            item.remove();
        }

        for (Block all : old_states.keySet()) {
            MaterialData remember = old_states.get(all);
            all.setType(remember.getItemType());
            all.setData(remember.getData());
        }

        space_container_inventories.clear();
        alive_space_crate.clear();
        old_states.clear();
        holograms.clear();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand().getType().toString().equalsIgnoreCase(space_container_material)) {
            Common.removeOneInHand(player);

            final Item thrown = player.getWorld().dropItem(player.getEyeLocation(),
                    new ItemStack(Material.valueOf(space_container_material.replace(" ", "_").toUpperCase()), 1));
            thrown.setPickupDelay(20 * 60 * 60);
            thrown.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(1.3));
            alive_space_crate.add(thrown);

            final Runnable create_package_init_effects = new Runnable() {
                private int lifetime = 20 * 3;

                public void run() {
                    final Location chest_place_location = thrown.getLocation();

                    thrown.remove();
                    alive_space_crate.remove(thrown);

                    for (int i = 0; i <= lifetime; i++) {
                        final int offsetI = i * 10;
                        final Location next_particle_location = chest_place_location.clone().add(0.0d, (i / 7.5), 0.0d);
                        Runnable next = new Runnable() {

                            public void run() {
                                //20 particles in circle
                                for (int i = 0; i < 360; i += 18) {
                                    double cos = TrigUtils.COS_VALUES[i];
                                    double sin = TrigUtils.SIN_VALUES[i];

                                    Location clone = next_particle_location.clone().add(
                                            cos * TrigUtils.COS_VALUES[TrigUtils.refAngleDegs(offsetI)] * 2, 0.0d,
                                            sin * TrigUtils.COS_VALUES[TrigUtils.refAngleDegs(offsetI)] * 2);
                                    ParticleEffect.FLAME.display(0f, 0f, 0f, 0f, 1, clone);
                                }

                                if (offsetI % 40 == 0) {
                                    next_particle_location.getWorld().playSound(next_particle_location, Sound.FIZZ, 0.3f, 0.5f);
                                }

                                ParticleEffect.SMOKE_LARGE.display(0.7f, 0.7f, 0.7f, 0f, 50, next_particle_location);
                            }
                        };
                        scheduleAsync(next, i);
                    }

                    Runnable end = new Runnable() {
                        public void run() {
                            if (space_container_inventories.containsKey(chest_place_location.getBlock()))
                                return; //for now

                            old_states.put(chest_place_location.getBlock(),
                                    new MaterialData(chest_place_location.getBlock().getType(), chest_place_location.getBlock().getData()));
                            chest_place_location.getBlock().setType(Material.ENDER_CHEST);

                            Inventory inv = Bukkit.createInventory(null, 18, Common.center("Space Crate"));

                            space_container_inventories.put(chest_place_location.getBlock(), inv);

                            final Hologram space_container_holo = HologramManager.buildHologram(chest_place_location.clone().add(0, 1.5, 0),
                                    ChatColor.translateAlternateColorCodes('&', "&c&lSpace Crate"),
                                    ChatColor.translateAlternateColorCodes('&', "&7Open for pvp goodies!"));
                            holograms.put(chest_place_location.getBlock(), space_container_holo);

                            schedule(new Runnable() {
                                public void run() {
                                    if (space_container_holo.isDeleted())
                                        return;

                                    HologramManager.removeHologram(space_container_holo);
                                }
                            }, 20 * 10);

                            //Find the top center of the chest for spiral effect
                            final Location start = chest_place_location.getBlock().getLocation().clone().add(0.5, 1, 0.5);
                            //Create 1/2 of a curve for our trails to follow
                            for (int i = 0; i < 180; i += 2) {
                                final int refI = i;
                                Runnable nextParticlePlace = new Runnable() {
                                    public void run() {
                                        //Find the sin of our outer "i" value for proper y curve and outwards spiral
                                        double sin = TrigUtils.SIN_VALUES[refI] * 2;
                                        for (int i = 0; i < 360; i += 36) {
                                            //Create our particle instance location
                                            Location play = start.clone().add(TrigUtils.COS_VALUES[TrigUtils.refAngleDegs(i + refI)] * sin,
                                                    sin / 2, TrigUtils.SIN_VALUES[TrigUtils.refAngleDegs(i + refI)] * sin);
                                            //Display the spark effect
                                            ParticleEffect.FIREWORKS_SPARK.display(0f, 0f, 0f, 0f, 1, play);
                                        }
                                    }
                                };
                                scheduleAsync(nextParticlePlace, i / 2);
                            }

                            chest_place_location.getWorld().strikeLightningEffect(chest_place_location);
                            ParticleEffect.CLOUD.display(0f, 0f, 0f, 0.4f, 150, chest_place_location);
                        }
                    };
                    schedule(end, lifetime);
                }
            };
            schedule(create_package_init_effects, 20 * 3);
        }
    }

    @EventHandler
    public void onSpaceCrateOpen(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        if (space_container_inventories.containsKey(event.getClickedBlock())) {
            event.setCancelled(true);
            event.getPlayer().openInventory(space_container_inventories.get(event.getClickedBlock()));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!Common.empty(event.getInventory()))
            return;

        if (space_container_inventories.values().contains(event.getInventory())) {
            //find key!
            Block find = null;
            for (Block key : space_container_inventories.keySet()) {
                if (space_container_inventories.get(key).equals(event.getInventory())) {
                    MaterialData old = old_states.get(key);
                    ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(key.getType(), key.getData()),
                            0.25f, 0.25f, 0.25f, 1, 30, key.getLocation().clone().add(0.5, 0.5, 0.5));
                    key.getWorld().playSound(key.getLocation(), Sound.GLASS, 1, 1);
                    key.setType(old.getItemType());
                    key.setData(old.getData());
                    old_states.remove(key);

                    find = key;
                }
            }

            space_container_inventories.remove(find);
        }
    }

    public static class SpaceCrateReward implements KillstreakReward {

        @Override
        public String getName() {
            return "Space Crate";
        }

        @Override
        public ItemStack getIcon() {
            return Common.craftItem(Material.ENDER_CHEST, "&f&l<&5Space &7Crate&f&l>");
        }

        @Override
        public int getKills() {
            return 4;
        }

        @Override
        public void giveToPlayer(Player player) {
            Common.give(player, Common.craftItem(Material.valueOf(space_container_material), "&5&lSpace Crate"));
        }

        @Override
        public String getDescription() {
            return "Spawn a space crate full of useful pvp items.";
        }
    }
}
