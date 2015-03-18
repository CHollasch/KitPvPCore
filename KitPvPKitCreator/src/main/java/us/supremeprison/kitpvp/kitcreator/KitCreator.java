package us.supremeprison.kitpvp.kitcreator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.ParseException;

import java.io.*;

/**
 * @author Connor Hollasch
 * @since 3/17/2015
 */
public class KitCreator extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!getDataFolder().exists())
            getDataFolder().mkdirs();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return true;
        }

        if (!(sender.isOp())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please specify a kit file name!");
            return true;
        }

        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("load")) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Please specify a kit to load!");
                return true;
            }

            File load = new File(getDataFolder().getAbsolutePath() + File.separator + args[1] + ".inventory");
            if (!(load.exists())) {
                sender.sendMessage(ChatColor.RED + "No such kit!");
                return true;
            }

            try {
                player.sendMessage(ChatColor.GREEN + "Loading kit...");

                BufferedReader reader = new BufferedReader(new FileReader(load));
                String contents = "";
                String armor = "";

                boolean append_armor = false;
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.equals("\n"))
                        continue;

                    if (line.startsWith("===")) {
                        append_armor = true;
                        continue;
                    }

                    if (append_armor) {
                        armor += line;
                    } else {
                        contents += line;
                    }
                }

                reader.close();

                ItemStack[] armor_items = KitSerializer.jsonToInventory(armor);
                ItemStack[] content_items = KitSerializer.jsonToInventory(contents);

                player.getInventory().setContents(content_items);
                player.getInventory().setArmorContents(armor_items);

                player.sendMessage(ChatColor.RED + "Kit successfully loaded!");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return true;
        }

        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();

        player.sendMessage(ChatColor.GREEN + "Saving kit...");
        File kit_file = new File(getDataFolder().getAbsolutePath() + File.separator + args[0] + ".inventory");
        try {
            if (!(kit_file.exists())) {
                kit_file.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(kit_file));
            writer.write(KitSerializer.inventoryToJson(contents));
            writer.write("\n=============================================================\n");
            writer.write(KitSerializer.inventoryToJson(armor));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendMessage(ChatColor.RED + "Kit schematic saved as " + ChatColor.WHITE + kit_file.getAbsolutePath());

        return true;
    }
}
