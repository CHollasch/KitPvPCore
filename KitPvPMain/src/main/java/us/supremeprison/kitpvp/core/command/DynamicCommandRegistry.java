package us.supremeprison.kitpvp.core.command;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.supremeprison.kitpvp.core.KitPvP;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Connor Hollasch
 * @since 3/12/2015
 */
public class DynamicCommandRegistry implements CommandExecutor {

    private static DynamicCommandRegistry me;

    private static HashMap<String, CommandModule> commands_map = new HashMap<>();
    private static CommandMap command_map;

    public DynamicCommandRegistry() {
        me = this;
        try {
            Field command_field = KitPvP.getPlugin_instance().getServer().getPluginManager().getClass().getDeclaredField("commandMap");
            command_field.setAccessible(true);
            command_map = (CommandMap) command_field.get(KitPvP.getPlugin_instance().getServer().getPluginManager());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void registerCommand(CommandModule command) {
        try {
            Constructor<PluginCommand> command_constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            command_constructor.setAccessible(true);

            PluginCommand cmd = command_constructor.newInstance(command.getCommand(), KitPvP.getPlugin_instance());

            KitPvP.getPlugin_instance().logMessage("Registered a new command! &5/" + command.getCommand());
            cmd.setAliases(Arrays.asList(command.getAliases()));
            command_map.register(KitPvP.getPlugin_instance().getDescription().getName(), cmd);

            commands_map.put(cmd.getName(), command);
            KitPvP.getPlugin_instance().getCommand(cmd.getName()).setExecutor(DynamicCommandRegistry.me);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        CommandModule registered_module = commands_map.get(command.getName());
        if (registered_module == null)
            return false;

        if (!registered_module.isCan_sender_be_console() && !(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return true;
        }

        if (!commandSender.hasPermission(registered_module.getPermission())) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l(!) &7You don't have access to this command!"));
            return true;
        }

        registered_module.onCommand(commandSender, args);
        return true;
    }
}
