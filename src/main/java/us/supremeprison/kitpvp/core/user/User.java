package us.supremeprison.kitpvp.core.user;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import us.supremeprison.kitpvp.core.KitPvP;
import us.supremeprison.kitpvp.core.event.UserInitializeEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
public class User {

    public static void createUserListener() {
        UserListener ul = new UserListener();
        for (Player player : Bukkit.getOnlinePlayers()) {
            ul.onPlayerPreJoin(new AsyncPlayerPreLoginEvent(player.getName(), player.getAddress().getAddress(), player.getUniqueId()));
            ul.onPlayerJoin(new PlayerJoinEvent(player, null));
        }
    }

    @Getter
    @Setter
    private static volatile ConcurrentHashMap<String, User> online_user_map = new ConcurrentHashMap<>();

    public static User fromPlayer(Player player) {
        return online_user_map.get(player.getUniqueId().toString());
    }

    public static User fromPlayer(UUID uuid) {
        return online_user_map.get(uuid.toString());
    }

    public static void removePlayer(Player player) {
        online_user_map.remove(player.getUniqueId().toString());
    }

    public static void removePlayer(UUID uuid) {
        online_user_map.remove(uuid.toString());
    }

    @Getter
    private UUID player_uuid;

    @Getter
    private Player player;

    public User(UUID uuid) {
        this.player_uuid = uuid;
        if (Bukkit.getPlayer(uuid) != null)
            player = Bukkit.getPlayer(uuid);
        online_user_map.put(uuid.toString(), this);
    }

    public void setPlayer(Player player) {
        this.player = player;
        Bukkit.getPluginManager().callEvent(new UserInitializeEvent(this));
    }

    private static class UserListener implements Listener {

        private UserListener() {
            Bukkit.getPluginManager().registerEvents(UserListener.this, KitPvP.getPlugin_instance());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        private void onPlayerPreJoin(AsyncPlayerPreLoginEvent event) {
            UUID uuid = event.getUniqueId();
            new User(uuid);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
            User.fromPlayer(event.getPlayer()).player = event.getPlayer();
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            User.removePlayer(event.getPlayer());
        }
    }
}
