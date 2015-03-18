package us.supremeprison.kitpvp.core.user;

import us.supremeprison.kitpvp.core.KitPvP;
import us.supremeprison.kitpvp.core.database.MySQLVars;
import us.supremeprison.kitpvp.core.event.UserInitializeEvent;
import us.supremeprison.kitpvp.core.user.attachment.Attachment;
import us.supremeprison.kitpvp.core.user.attachment.AttachmentManager;
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

    public User(UUID uuid) {
        this.player_uuid = uuid;
        if (Bukkit.getPlayer(uuid) != null)
            player = Bukkit.getPlayer(uuid);
        online_user_map.put(uuid.toString(), this);

        attachments = new PlayerAttachmentData(null);
        loadUserdata();
    }

    private void loadUserdata() {
        Runnable asyncSQL = new Runnable() {
            @Override
            public void run() {
                try {
                    ResultSet all_attachment_data = MySQLVars.GET_ALL_ATTACHMENTS.getResultSet(player_uuid.toString());
                    while (all_attachment_data.next()) {
                        String label = all_attachment_data.getString("attachment_label");
                        String data = all_attachment_data.getString("attachment_data");
                        Attachment<?> attachment_model = attachments_manager.getAttachment(label);
                        attachments.putDeserializedAttachment(label, attachment_model.deserialize(data));
                    }

                    for (Attachment predefined_attachments : attachments_manager.getAllAttachments()) {
                        if (attachments.isRegistered(predefined_attachments))
                            continue;

                        MySQLVars.INSERT_INTO_ATTACHMENTS.executeQuery(player_uuid.toString(),
                                predefined_attachments.getAttachment_label(),
                                predefined_attachments.serialize(predefined_attachments.getDefault_value()));
                    }
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
        for (String attachment_label : attachments.protectedGetAllAttachments().keySet()) {
            Object value = attachments.protectedGetAllAttachments().get(attachment_label);
            Attachment matching = attachments_manager.getAttachment(attachment_label);
            if (matching == null) {
                MySQLVars.REMOVE_ATTACHMENT.executeQuery(player_uuid.toString(), attachment_label);
            } else {
                MySQLVars.INSERT_INTO_ATTACHMENTS.executeQuery(player_uuid.toString(), attachment_label, matching.serialize(value));
            }
        }
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
            User.fromPlayer(event.getPlayer()).setPlayer(event.getPlayer());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            User.removePlayer(event.getPlayer()).save(true);
        }
    }
}
