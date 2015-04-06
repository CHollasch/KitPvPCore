package us.supremeprison.kitpvp.modules.NanoSuit;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.util.Vector;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.util.ParticleEffect;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Connor Hollasch
 * @since 3/11/2015
 */
@SuppressWarnings("unused")
@ModuleDependency
public class NanoSuit extends Module {

    private static HashSet<String> applicable_for_forwards_fling = new HashSet<>();
    private static HashSet<String> in_double_jump = new HashSet<>();

    private static HashSet<String> cooldown = new HashSet<>();

    @Override
    public void onEnable() {
        Runnable update = new Runnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (hasNanosuit(player)) {
                        if (player.getGameMode() == GameMode.SURVIVAL
                                && !player.getAllowFlight()
                                && player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)

                            player.setAllowFlight(true);
                        else if (player.getGameMode() == GameMode.SURVIVAL && player.isFlying()) {
                            player.setFlying(false);
                        }
                    }
                }
            }
        };
        scheduleAsync(update, 5, 5);
    }

    @Override
    public void onDisable() {
        applicable_for_forwards_fling.clear();
        in_double_jump.clear();
        cooldown.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getAllowFlight() && event.getPlayer().getGameMode() != GameMode.CREATIVE)
            event.getPlayer().setAllowFlight(false);
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        Block block = loc.add(0, -1, 0).getBlock();

        if (in_double_jump.contains(player.getName())) {
            if (block.getType() != Material.AIR) {
                in_double_jump.remove(player.getName());
                applicable_for_forwards_fling.remove(player.getName());
                if (hasNanosuit(event.getPlayer()))
                    event.getPlayer().setAllowFlight(true);

                Runnable cooldown = new Runnable() {
                    public void run() {
                        NanoSuit.this.cooldown.remove(event.getPlayer().getName());
                    }
                };

                schedule(cooldown, 30);
                this.cooldown.add(event.getPlayer().getName());
            } else if (player.isFlying()) {
                player.setFlying(false);
            }
        }
    }

    @ConfigOption("FORWARD-FLING.UPWARDS-FLING")
    private double upwards_fling = 0.1d;

    @ConfigOption("FORWARD-FLING.FORWARD-FLING")
    private double forward_fling = 1.2d;

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (in_double_jump.contains(event.getPlayer().getName()) && applicable_for_forwards_fling.contains(event.getPlayer().getName())) {
            applicable_for_forwards_fling.remove(event.getPlayer().getName());
            event.getPlayer().setVelocity(event.getPlayer().getEyeLocation().getDirection().normalize().add(new Vector(0, upwards_fling, 0)).multiply(forward_fling));
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.IRONGOLEM_THROW, 1, 1);
            ParticleEffect.CLOUD.display(event.getPlayer().getLocation(), 0.2f, 0f, 0.2f, 0.06f, 30);
        }
    }

    @ConfigOption("UPWARDS-FLING")
    private double initial_upwards_fling = 0.8d;

    @EventHandler
    public void onPlayerDoubleJump(final PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        if (hasNanosuit(player)) {
            if (!(in_double_jump.contains(event.getPlayer().getName()))) {
                if (cooldown.contains(event.getPlayer().getName()) && event.isFlying()) {
                    event.setCancelled(true);
                    return;
                }

                in_double_jump.add(event.getPlayer().getName());
                applicable_for_forwards_fling.add(event.getPlayer().getName());

                event.getPlayer().setVelocity(event.getPlayer().getVelocity().add(new Vector(0, initial_upwards_fling, 0)));
                event.getPlayer().setFlying(false);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.IRONGOLEM_HIT, 1, 1);
                schedule(new Runnable() {
                    public void run() {
                        event.getPlayer().setAllowFlight(false);
                    }
                }, 1);
                ParticleEffect.CLOUD.display(event.getPlayer().getLocation(), 0.2f, 0f, 0.2f, 0.06f, 30);
            } else {
                event.getPlayer().setFlying(false);
                event.getPlayer().setAllowFlight(false);
            }
        }
    }

    @ConfigOption("NANOSUIT-ITEMS")
    private HashMap<String, String> nano_suit_items = new HashMap<String, String>() {
        {
            put("slot1", "CHAINMAIL_HELMET");
            put("slot2", "CHAINMAIL_CHESTPLATE");
            put("slot3", "CHAINMAIL_LEGGINGS");
            put("slot4", "CHAINMAIL_BOOTS");
        }
    };

    private boolean hasNanosuit(Player player) {
        EntityEquipment ee = player.getEquipment();
        if (ee.getHelmet() == null || ee.getChestplate() == null || ee.getLeggings() == null || ee.getBoots() == null)
            return false;

        int slot = 0;

        return (nano_suit_items.get("slot" + (++slot)).equalsIgnoreCase(ee.getHelmet().getType().toString())
                && nano_suit_items.get("slot" + (++slot)).equalsIgnoreCase(ee.getChestplate().getType().toString())
                && nano_suit_items.get("slot" + (++slot)).equalsIgnoreCase(ee.getLeggings().getType().toString())
                && nano_suit_items.get("slot" + (++slot)).equalsIgnoreCase(ee.getBoots().getType().toString()));
    }
}
