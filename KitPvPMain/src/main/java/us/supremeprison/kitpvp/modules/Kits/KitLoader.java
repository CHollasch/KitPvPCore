package us.supremeprison.kitpvp.modules.Kits;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.supremeprison.kitpvp.core.KitPvP;
import us.supremeprison.kitpvp.core.command.CommandModule;
import us.supremeprison.kitpvp.core.command.DynamicCommandRegistry;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.user.attachment.Attachment;
import us.supremeprison.kitpvp.core.user.attachment.common.StringAttachment;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.KitSerializer;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;
import us.supremeprison.kitpvp.modules.Kits.inventory.KitInventoryBase;

import java.io.File;
import java.util.*;

/**
 * @author Connor Hollasch
 * @since 3/25/2015
 */
@ModuleDependency(depends_on = {"modulemanager", "rank", "economy"})
public class KitLoader extends Module {

    @Getter
    private LinkedHashMap<Integer, Kit> global_ranked_kits = new LinkedHashMap<>();

    @Getter
    private LinkedHashMap<String, Kit> global_donor_kits = new LinkedHashMap<>();

    @Getter
    private HashMap<Kit, Double> kit_costs = new HashMap<>();

    @ConfigOption("KIT-MAP")
    private HashMap<String, String> kit_configuration = new HashMap<String, String>();

    @ConfigOption("INVENTORY-TITLE")
    @Getter
    private String kit_inventory_title = Common.center("&7Kit GUI");

    @Override
    public void onEnable() {
        Attachment<String> player_kits = new StringAttachment("kits", "");
        User.getAttachments_manager().put(player_kits);

        DynamicCommandRegistry.registerCommand(new CommandModule("kit", new String[]{"kits", "kitgui"}, false) {
            @Override
            public void onCommand(CommandSender sender, String[] args) {
                Player player = (Player) sender;
                new KitInventoryBase(player, KitLoader.this, false).openInventory();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lKit GUI opened!"));
            }
        });

        //Create kits folder
        File kits_folder = new File(parent_plugin.getDataFolder().getAbsolutePath() + File.separator + "Kits");
        if (!kits_folder.exists())
            kits_folder.mkdirs();

        for (String kit_path : kit_configuration.keySet()) {
            String parse = kit_configuration.get(kit_path);

            //Simple fix for file path
            if (!kit_path.endsWith(".inventory"))
                kit_path += ".inventory";

            if (!(parse.contains("-"))) {
                //Someone didn't format this correctly...
                logKitLoadError(kit_path, "Invalid permission format, e.g \"&erank-1&f\"");
                continue;
            }

            File kits_file = new File(kits_folder.getAbsolutePath() + File.separator + kit_path);
            if (!kits_file.exists() || !kits_file.getName().endsWith(".inventory")) {
                logKitLoadError(kit_path, "Could not find kit file " + kits_file.getAbsolutePath());
                continue;
            }

            ItemStack[][] file_contents = KitSerializer.createKitArmorAndContents(kits_file);
            HashMap<String, String> data = new HashMap<>();
            for (String split : parse.split("[:]")) {
                String[] parts = split.split("[\\-]");
                if (parts.length == 1) {
                    logKitLoadError(kit_path, "Kit metadata requires two arguments, e.g \"&erank-1&f\"");
                    continue;
                }

                data.put(parts[0].toLowerCase(), parts[1].toLowerCase());
            }

            if (data.containsKey("rank")) {
                try {
                    int num = Integer.parseInt(data.get("rank"));

                    Kit kit = new Kit(file_contents[1], file_contents[0], num);
                    if (data.containsKey("price"))
                        kit.setCost(Double.parseDouble(data.get("price")));

                    if (kit.getCost() != 0.0 && !(num == 0))
                        kit_costs.put(kit, kit.getCost());
                    global_ranked_kits.put(num, kit);
                } catch (NumberFormatException ex) {
                    logKitLoadError(kit_path, "Invalid rank or price number \"&e" + data.get("rank") + (data.containsKey("price") ? " or " + data.get("price") : "") + "&f\"");
                    continue;
                }
            } else if (data.containsKey("permission")) {
                String permission = data.get("permission");

                Kit kit = new Kit(file_contents[1], file_contents[0], permission);

                try {
                    if (data.containsKey("price"))
                        kit.setCost(Double.parseDouble(data.get("price")));
                } catch (NumberFormatException ex) {
                    logKitLoadError(kit_path, "Invalid price for donor kit \"&e" + data.get("price") + "&f\"");
                } finally {
                    if (kit.getCost() != 0.0)
                        kit_costs.put(kit, kit.getCost());
                    if (data.containsKey("title"))
                        kit.setTitle(data.get("title"));

                    global_donor_kits.put(permission, kit);
                }
            }
        }
    }

    public Kit getDonorKit(int index) {
        Iterator<Kit> kits = global_donor_kits.values().iterator();
        int remain = index;
        while (remain >= 0 && kits.hasNext()) {
            if (remain == 0)
                return kits.next();

            kits.next();
        }
        return null;
    }

    private void logKitLoadError(String kit, String reason) {
        parent_plugin.logMessage(this, "&cERROR&7: &fCould not load kit &e" + kit);
        parent_plugin.logMessage(this, "     &7: &f" + reason);
    }
}
