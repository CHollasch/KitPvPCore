package us.supremeprison.kitpvp.core.util.messages;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

/**
 * @author Connor Hollasch
 * @since 3/17/2015
 */
public class Form {

    private static HashMap<String, String> cached_message_formats = new HashMap<>();

    public static void insertTag(String tag, String format) {
        cached_message_formats.put(tag.toLowerCase(), format);
    }

    public static void at(CommandSender sender, String tag, String message, String... args) {
        String find = cached_message_formats.get(tag.toLowerCase());
        if (find == null)
            return;

        find = find.replace("%msg%", message);

        int index = 0;
        for (String arg : args) {
            find = find.replace("{" + index + "}", arg);
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', find));
    }
}
