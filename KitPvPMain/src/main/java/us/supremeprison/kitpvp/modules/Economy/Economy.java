package us.supremeprison.kitpvp.modules.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.supremeprison.kitpvp.core.command.CommandModule;
import us.supremeprison.kitpvp.core.command.DynamicCommandRegistry;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.Immutable;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.user.attachment.Attachment;
import us.supremeprison.kitpvp.core.user.attachment.common.DoubleAttachment;
import us.supremeprison.kitpvp.core.util.KeyRoundingMap;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;
import us.supremeprison.kitpvp.core.util.messages.Form;

import java.text.NumberFormat;
import java.util.LinkedHashMap;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
@SuppressWarnings("unused")
@Immutable(from = Immutable.From.ALL)
@ModuleDependency
public class Economy extends Module {

    private static final NumberFormat cashFormat = NumberFormat.getCurrencyInstance();

    @ConfigOption("ITEM-WORTH")
    public static LinkedHashMap<String, Double> item_worth = new LinkedHashMap<String, Double>() {
        {
            put(Material.IRON_INGOT.toString(), 25000.0);
            put(Material.GOLD_INGOT.toString(), 100000.0);
            put(Material.DIAMOND.toString(), 500000.0);
            put(Material.EMERALD.toString(), 1000000.0);
            put(Material.getMaterial(377).toString(), 100000000.0);
        }
    };

    @ConfigOption("STARTING-BALANCE")
    private double starting_balance = 0.0;

    @Override
    public void onEnable() {
        //Attach economy modules to players
        Attachment<Double> economy_attachment = new DoubleAttachment("economy", starting_balance);
        User.getAttachments_manager().put(economy_attachment);
        parent_plugin.getServer().getPluginManager().registerEvents(new DeathMoney(), parent_plugin);

        //Register economy command
        DynamicCommandRegistry.registerCommand(new CommandModule("eco", new String[]{"economy", "money", "bal", "balance", "ebal", "ebalance", "emoney"}, false) {
            public void onCommand(CommandSender sender, String[] args) {
                Player player = (Player) sender;
                User user = User.fromPlayer(player);
                Double balance = getMoney(player);

                if (args.length == 0) {
                    Form.at(player, "eco", "&f" + cashFormat.format(balance));
                    return;
                }

                switch (args[0].toLowerCase()) {
                    case "add": {
                        if (!validatePermission(player, "kitpvp.economy.add"))
                            return;

                        if (args.length <= 2) {
                            Form.at(player, "error", "Format: \"/eco add [player] [amount > 0]\".");
                            return;
                        }

                        Player addTo = Bukkit.getPlayer(args[1]);
                        if (!validateIsPlayer(player, args[1], true))
                            return;

                        if (!validateIsNumber(player, args[2]))
                            return;

                        double add = Double.parseDouble(args[2]);
                        setMoney(addTo, balance + add);
                        Form.at(player, "eco", "Added &f" + cashFormat.format(add) + "&e to &f" + addTo.getName() + "'s &ebalance.");
                        return;
                    }
                    case "remove": {
                        if (!validatePermission(player, "kitpvp.economy.add"))
                            return;

                        if (args.length <= 2) {
                            player.sendMessage(ChatColor.RED + "Format: \"/eco add [player] [amount > 0]\"!");
                            return;
                        }

                        Player addTo = Bukkit.getPlayer(args[1]);
                        if (!validateIsPlayer(player, args[1], true))
                            return;

                        if (!validateIsNumber(player, args[2]))
                            return;

                        double remove = Double.parseDouble(args[2]);
                        if (balance < remove) {
                            player.sendMessage(ChatColor.RED + addTo.getName() + " does not have enough money!");
                            return;
                        }

                        setMoney(addTo, balance - remove);
                        Form.at(player, "eco", "Removed &f" + cashFormat.format(remove) + "&e from &f" + addTo.getName() + "'s &ebalance.");
                        return;
                    }
                    case "set": {
                        if (!validatePermission(player, "kitpvp.economy.add"))
                            return;

                        if (args.length <= 2) {
                            player.sendMessage(ChatColor.RED + "Format: \"/eco add [player] [amount > 0]\"!");
                            return;
                        }

                        Player addTo = Bukkit.getPlayer(args[1]);
                        if (!validateIsPlayer(player, args[1], true))
                            return;

                        if (!validateIsNumber(player, args[2]))
                            return;

                        double set = Double.parseDouble(args[2]);
                        if (set < 0) {
                            player.sendMessage(ChatColor.RED + "Must specify a positive number!");
                            return;
                        }

                        setMoney(addTo, set);
                        Form.at(player, "eco", "Set &f" + addTo.getName() + "'s &ebalance to &f" + cashFormat.format(set) + "&e.");
                        return;
                    }
                }
            }
        });
    }

    public static Material[] chanceRandomBill(int amount) {
        KeyRoundingMap<Double, String> cashValueChances = new KeyRoundingMap<>();

        double total = 0.0;
        for (double x : item_worth.values()) {
            total += x;
        }

        double max = 0.0;

        for (String key : item_worth.keySet()) {
            double value = item_worth.get(key);
            double next = (total / value);
            if (max < next)
                max = next;

            cashValueChances.put(next, key);
        }

        double random = Math.random() * max;
        Material[] all = new Material[amount];

        for (int i = 0; i < amount; i++) {
            all[i] = Material.getMaterial(cashValueChances.get(cashValueChances.getTopKey(random)));
            random = Math.random() * max;
        }

        return all;
    }

    public static String getUserCashString(Player player) {
        return cashFormat.format(getMoney(player));
    }

    public static String formatCash(Double number) {
        return cashFormat.format(number);
    }

    public static void transferMoney(Player from, Player to) {
        User fUser = User.fromPlayer(from);
        User tUser = User.fromPlayer(to);

    }

    public static double getMoney(Player player) {
        return getMoney(User.fromPlayer(player));
    }

    private static double getMoney(User user) {
        return user.getAttachments().getAttachment("economy");
    }

    public static void setMoney(Player player, double balance) {
        setMoney(User.fromPlayer(player), balance);
    }

    public static void setMoney(User user, double balance) {
        user.getAttachments().changeAttachment("economy", balance);
    }
}
