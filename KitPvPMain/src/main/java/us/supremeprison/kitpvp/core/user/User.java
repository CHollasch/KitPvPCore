package us.supremeprison.kitpvp.core.user;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.supremeprison.kitpvp.core.KitPvP;
import us.supremeprison.kitpvp.core.database.MySQLVars;
import us.supremeprison.kitpvp.core.event.UserInitializeEvent;
import us.supremeprison.kitpvp.core.user.attachment.Attachment;
import us.supremeprison.kitpvp.core.user.attachment.AttachmentManager;
import us.supremeprison.kitpvp.core.user.attachment.EasyUserdata;

import java.sql.ResultSet;
import java.sql.SQLException;
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
    private static AttachmentManager attachments_manager = new AttachmentManager();

    @Getter
    @Setter
    private static volatile ConcurrentHashMap<String, User> online_user_map = new ConcurrentHashMap<>();

    public static User fromPlayer(Player player) {
        return online_user_map.get(player.getUniqueId().toString());
    }

    public static User removePlayer(Player player) {
        return online_user_map.remove(player.getUniqueId().toString());
    }

    //===================================================================

    @Getter
    private UUID player_uuid;

    @Getter
    private Player player;

    @Getter
    private PlayerAttachmentData attachments;

    @Getter
    private EasyUserdata userdata;

    public User(UUID uuid) {
        this.player_uuid = uuid;
        if (Bukkit.getPlayer(uuid) != null)
            player = Bukkit.getPlayer(uuid);
        online_user_map.put(uuid.toString(), this);

        attachments = new PlayerAttachmentData(null);
        userdata = new EasyUserdata();
        loadUserdata();
    }

    private void loadUserdata() {
        Runnable asyncSQL = new Runnable() {
            @Override
            public void run() {
                try {
                    ResultSet all_attachment_data = MySQLVars.GET_ALL_ATTACHMENTS.getResultSet(player_uuid.toString());
                    if (all_attachment_data.next()) {
                        JSONParser parser = new JSONParser();
                        try {
                            String attachmentData = all_attachment_data.getString("attachment_data");
                            JSONObject attachment = (JSONObject) parser.parse(all_attachment_data.getString("attachment_data"));
                            for (Object rawKey : attachment.keySet()) {
                                String key = rawKey.toString();
                                Object value = attachment.get(key);
                                Attachment<?> model = attachments_manager.getAttachment(key);
                                attachments.putDeserializedAttachment(key, model.deserialize(value.toString()));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    save(false);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        };
        Bukkit.getScheduler().scheduleAsyncDelayedTask(KitPvP.getPlugin_instance(), asyncSQL);
    }

    public void setPlayer(Player player) {
        this.player = player;
        Bukkit.getPluginManager().callEvent(new UserInitializeEvent(this));
    }

    public void save(boolean async) {
        if (async)
            Bukkit.getScheduler().scheduleAsyncDelayedTask(KitPvP.getPlugin_instance(), new Runnable() {
                @Override
                public void run() {
                    save();
                }
            });
        else
            save();
    }

    private void save() {
        MySQLVars.INSERT_INTO_ATTACHMENTS.executeQuery(player_uuid.toString(),
                buildAttachmentJSON().toJSONString());
    }

    private JSONObject buildAttachmentJSON() {
        JSONObject object = new JSONObject();

        for (Attachment attachment : attachments_manager.getAllAttachments()) {
            String key = attachment.getAttachment_label();
            String value = attachments.isRegistered(attachment) ?
                    attachment.serialize(attachments.getAttachment(attachment.getAttachment_label()))
                    : attachment.serialize(attachment.getDefault_value());

            object.put(key, value);
        }

        return object;
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
            try {
                User.fromPlayer(event.getPlayer()).setPlayer(event.getPlayer());
            } catch (Exception ex) {
                event.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', "&c&lERROR&7: &fYour userdata could not be loaded, please rejoin again!"));
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            try {
                User.removePlayer(event.getPlayer()).save(true);
            } catch (NullPointerException ex) {
                KitPvP.getPlugin_instance().logMessage("Could not save userdata for &f" + event.getPlayer().getUniqueId().toString() + "&e!");
            }
        }
    }
}
