package us.supremeprison.kitpvp.modules.NanoSuit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.util.Vector;
import us.supremeprison.kitpvp.core.event.UserInitializeEvent;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.user.User;

import java.util.HashSet;

/**
 * @author Connor Hollasch
 * @since 3/11/2015
 */
@SuppressWarnings("unused")
public class NanoSuit extends Module {

    private static HashSet<String> applicable_for_forwards_fling = new HashSet<>();
    private static HashSet<String> in_double_jump = new HashSet<>();

    private static HashSet<String> cooldown = new HashSet<>();

    @Override
    public void onDisable() {
        applicable_for_forwards_fling.clear();
        in_double_jump.clear();
        cooldown.clear();
    }

    @EventHandler
    public void onUserJoin(UserInitializeEvent event) {
        User user = event.getUser();
        if (hasNanosuit(user.getPlayer()) && user.getPlayer().getGameMode() != GameMode.CREATIVE)
            user.getPlayer().setAllowFlight(true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!hasNanosuit(event.getPlayer()) &&
                event.getPlayer().getAllowFlight() &&
                event.getPlayer().getGameMode() != GameMode.CREATIVE)

            event.getPlayer().setAllowFlight(false);
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

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (in_double_jump.contains(event.getPlayer().getName()) && applicable_for_forwards_fling.contains(event.getPlayer().getName())) {
            applicable_for_forwards_fling.remove(event.getPlayer().getName());
            event.getPlayer().setVelocity(event.getPlayer().getEyeLocation().getDirection().normalize().add(new Vector(0, 0.1, 0)).multiply(1.2));
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.IRONGOLEM_THROW, 1, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(final InventoryClickEvent event) {
        Runnable tick1dleay = new Runnable() {
            public void run() {
                check((Player) event.getWhoClicked());
            }
        };
        schedule(tick1dleay, 1);
    }

    @EventHandler
    public void onPlayerGamemodeChange(final PlayerGameModeChangeEvent event) {
        Runnable tick1dleay = new Runnable() {
            public void run() {
                check(event.getPlayer());
            }
        };
        schedule(tick1dleay, 1);
    }

    private void check(Player player) {
        if (hasNanosuit(player) && player.getGameMode() != GameMode.CREATIVE) {
            player.setAllowFlight(true);
        } else {
            player.setAllowFlight(false);
        }
    }

    @EventHandler
    public void onPlayerDoubleJump(PlayerToggleFlightEvent event) {
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

                event.getPlayer().setVelocity(new Vector(0, 0.8, 0));
                event.getPlayer().setFlying(false);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.IRONGOLEM_HIT, 1, 1);
            } else {
                event.getPlayer().setFlying(false);
                event.getPlayer().setAllowFlight(false);
            }
        }
    }

    private static boolean hasNanosuit(Player player) {
        EntityEquipment ee = player.getEquipment();
        if (ee.getHelmet() == null || ee.getChestplate() == null || ee.getLeggings() == null || ee.getBoots() == null)
            return false;

        return (ee.getHelmet().getType() == Material.CHAINMAIL_HELMET
                && ee.getChestplate().getType() == Material.CHAINMAIL_CHESTPLATE
                && ee.getLeggings().getType() == Material.CHAINMAIL_LEGGINGS
                && ee.getBoots().getType() == Material.CHAINMAIL_BOOTS);
    }
}
