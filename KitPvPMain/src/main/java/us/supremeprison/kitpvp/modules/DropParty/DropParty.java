package us.supremeprison.kitpvp.modules.DropParty;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFirework;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
import us.supremeprison.kitpvp.core.KitPvP;
import us.supremeprison.kitpvp.core.command.CommandModule;
import us.supremeprison.kitpvp.core.command.DynamicCommandRegistry;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.ParticleEffect;
import us.supremeprison.kitpvp.core.util.config.ClassConfig;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;
import us.supremeprison.kitpvp.core.util.messages.Form;
import us.supremeprison.kitpvp.modules.Economy.Economy;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author Connor Hollasch
 * @since 3/9/2015
 */
@SuppressWarnings("unused")
@ModuleDependency
public class DropParty extends Module {

    //Setup player
    private static Player setup = null;

    private static int dp_task_id;

    @ConfigOption("DP-TIME-CYCLE")
    private int dp_time_cycle = 5400;

    @ConfigOption("DP-MESSAGE")
    private String dp_message = "&c(&aDROP PARTY&c) &7The drop party is now starting!";

    private int dp_time_left = dp_time_cycle;

    @ConfigOption("FLOOR-BLOCKS")
    private List<String> raw_floor_locations = Lists.newArrayList();
    private HashSet<Location> floor_locations = new HashSet<>();

    @ConfigOption("FLOOR-HEIGHT")
    private int floor_height = 26;

    private Location center = new Location(Bukkit.getWorlds().get(0), 4, 25, 0);

    @ConfigOption("FLOOR-TYPE")
    private MaterialData floor_type = Material.STAINED_CLAY.getNewData((byte) 9);

    @Override
    public void onEnable() {
        for (String location : raw_floor_locations) {
            Location l = ClassConfig.getLocationSerializer().load(location);
            l.setY(floor_height);

            floor_locations.add(l);
        }

        Runnable dp_task = new Runnable() {
            @Override
            public void run() {
                if (dp_time_left-- <= 0) {
                    dp_time_left = dp_time_cycle;
                    launchDropParty();
                }
            }
        };
        dp_task_id = schedule(dp_task, 20, 20);

        DynamicCommandRegistry.registerCommand(new CommandModule("dpsetup", new String[]{"dropparty", "dropsetup", "droppartysetup", "dpset"}, false, "dropparty.setup") {
            @Override
            public void onCommand(CommandSender sender, String[] args) {
                Player player = (Player) sender;

                if (args.length == 0) {
                    Form.at(player, "error", "Please specify an action!");
                    return;
                }

                switch (args[0].toLowerCase()) {
                    case "addfloor":
                    case "af":
                    case "floor": {
                        if (setup == null || !(setup.equals(player))) {
                            setup = player;
                            Form.at(player, "info", "You are now setting up the drop party!");
                        } else {
                            setup = null;
                            Form.at(player, "info", "You are no longer setting up the drop party!");
                        }
                        return;
                    }
                    case "force":
                    case "start":
                    case "forcestart": {
                        dp_time_left = 5;
                        Form.at(player, "info", "Drop party will start in 5 seconds!");
                        return;
                    }
                    default: {
                        Form.at(player, "error", "No such sub command " + args[0] + "!");
                    }
                }
            }
        });
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(dp_task_id);
        for (Location loc : floor_locations) {
            loc.getBlock().setType(floor_type.getItemType());
            loc.getBlock().setData(floor_type.getData());
        }
    }

    public void launchDropParty() {
        if (Bukkit.getOnlinePlayers().size() <= 0) {
            return;
        }

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', dp_message));

        final List<Location> linked_floors = Lists.newLinkedList();
        linked_floors.addAll(floor_locations);

        final int task = schedule(new Runnable() {
            int angle = 0;
            int height = 0;

            @Override
            public void run() {
                angle += 5;
                height += 2;
                center.getWorld().playSound(center, Sound.AMBIENCE_CAVE, 3, 0.6f);

                double h = Math.sin(Math.toRadians(height));

                double x = Math.cos(Math.toRadians(angle)) * 3 + (h / 2);
                double y = h + 5;
                double z = Math.sin(Math.toRadians(angle)) * 3 + (h / 2);

                ParticleEffect.FIREWORKS_SPARK.display(0f, 0f, 0f, 0, 1, center.clone().add(x, y, z));
                if (angle % 90 == 0) {
                    for (double off = 0, new_angle = 0, rot = 0; off <= 3; off += (3.0 / 180), new_angle += 5, rot += 10) {
                        final double _x = Math.cos(Math.toRadians(rot)) * off + x;
                        final double _y = y + (new_angle / 50);
                        final double _z = Math.sin(Math.toRadians(rot)) * off + z;

                        final double _off = off;
                        schedule(new Runnable() {
                            @Override
                            public void run() {
                                ParticleEffect.VILLAGER_HAPPY.display(0f, 0f, 0f, 0, 1, center.clone().add(_x, _y, _z));

                                if (_off >= 3 - (3.0 / 180)) {
                                    instantFirework(center.clone().add(_x, _y, _z));
                                }
                            }
                        }, (int) new_angle / 10);
                    }
                }
            }
        }, 1, 1);

        float duration = 0;

        for (final Location pos : floor_locations) {
            schedule(new Runnable() {
                @Override
                public void run() {
                    pos.getWorld().playEffect(pos, Effect.STEP_SOUND, pos.getBlock().getTypeId(), pos.getBlock().getData());
                    pos.getBlock().setType(Material.NETHERRACK);
                    if (Math.random() > .9) {
                        pos.getBlock().setType(Material.AIR);
                        pos.getWorld().spawnFallingBlock(pos.clone().add(0, 50, 0), Material.OBSIDIAN.getId(), (byte) 0);
                    }
                }
            }, (int) (duration += .3));
        }

        //break
        duration += 40;

        for (int i = 0; i < 15 + (Math.random() * 10); i++) {
            final Location spawn = linked_floors.get((int) (Math.random() * linked_floors.size())).clone();
            spawn.add(0, 1, 0);

            duration += 30;

            //Begin effects
            for (int j = spawn.getBlockY() + 20; j >= spawn.getBlockY(); j--) {
                final int _j = j;
                schedule(new Runnable() {
                    @Override
                    public void run() {
                        Location spot = spawn.clone();
                        spot.setY(_j);

                        ParticleEffect.LAVA.display(0f, 0f, 0f, 1, 3, spot);

                        if (_j == spawn.getBlockY()) {
                            ParticleEffect.EXPLOSION_LARGE.display(.3f, .3f, .3f, 1, 5, spot);
                            spot.getWorld().playSound(spot, Sound.ZOMBIE_WOODBREAK, 1, 1);
                            float duration = 0f;
                            final Material[] bills = Economy.chanceRandomBill(30 + (int) (Math.random() * 30));
                            for (int i = 0; i < bills.length; i++, duration += 0.3) {
                                final int _i = i;
                                schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        Item drop = spawn.getWorld().dropItem(spawn, Common.craftItem(bills[_i], UUID.randomUUID().toString()));
                                        drop.setVelocity(new Vector(range(-.5, .5), Math.random() * .7, range(-.5, .5)));
                                    }
                                }, (int) duration);
                            }
                        }
                    }
                }, (int) (duration += 1));
            }
        }

        schedule(new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(task);

                float delay = 0;
                for (final Location loc : floor_locations) {
                    schedule(new Runnable() {
                        @Override
                        public void run() {
                            loc.getBlock().setType(floor_type.getItemType());
                            loc.getBlock().setData(floor_type.getData());
                        }
                    }, (int) (delay += .3));
                }
            }
        }, (int) (duration + 40));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (setup == null) {
            return;
        }

        if (setup.equals(event.getPlayer())) {
            Block block = event.getBlock();
            floor_locations.add(block.getLocation());
            raw_floor_locations.add(ClassConfig.getLocationSerializer().save(block.getLocation()));
            KitPvP.getPlugin_instance().getConfiguration_manager().saveAll(this, "MODULES.DROPPARTY.");
            Form.at(setup, "info", "Location added to drop party");
            return;
        }
    }

    private static final Random random = new Random();

    private static double range(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    public static void instantFirework(Location loc) {
        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta data = firework.getFireworkMeta();
        data.addEffect(randomEffects());
        firework.setFireworkMeta(data);
        NBTTagCompound nbtData = new NBTTagCompound();
        nbtData.setInt("Life", 1);
        nbtData.setInt("LifeTime", 2);
        ((CraftFirework) firework).getHandle().a(nbtData);
    }

    private static FireworkEffect randomEffects() {
        Random r = new Random();

        FireworkEffect.Builder effect = FireworkEffect.builder();
        effect.flicker(r.nextBoolean());
        effect.trail(r.nextBoolean());
        effect.with(FireworkEffect.Type.values()[r.nextInt(FireworkEffect.Type.values().length)]);
        effect.withColor(Color.fromRGB(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
        effect.withFade(Color.fromRGB(r.nextInt(255), r.nextInt(255), r.nextInt(255)));

        return effect.build();
    }
}
