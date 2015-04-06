package us.supremeprison.kitpvp.modules.Kits.inventory;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.inventory.OpenInventoryData;
import us.supremeprison.kitpvp.modules.Economy.Economy;
import us.supremeprison.kitpvp.modules.Kits.Kit;
import us.supremeprison.kitpvp.modules.Kits.KitLoader;
import us.supremeprison.kitpvp.modules.Rank.Rank;

import java.util.*;

/**
 * @author Connor Hollasch
 * @since 4/5/2015
 */
public class KitInventoryBase extends OpenInventoryData {

    private boolean donor;
    private KitLoader kit_loader;

    public KitInventoryBase(Player player, KitLoader kit_loader, boolean donor) {
        super(player);

        this.kit_loader = kit_loader;
        this.donor = donor;
    }

    private HashMap<Integer, Kit> kit_map = new HashMap<>();

    @Override
    public Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', kit_loader.getKit_inventory_title()));
        HashSet<String> kits = new HashSet<>(Arrays.asList(User.fromPlayer(player).getAttachments().getAttachment("kits").toString().split(",")));

        if (donor) {
            Iterator<Kit> donor_kits = kit_loader.getGlobal_donor_kits().values().iterator();
            int index = 0;
            while (donor_kits.hasNext()) {
                Kit next = donor_kits.next();

                ItemStack stack;
                if (kits.contains("permission-" + next.getRequired_permission())) {
                    //Owned
                    stack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0, (byte) 5);
                } else if (player.hasPermission(next.getRequired_permission())) {
                    //Can buy
                    stack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0, (byte) 4);
                } else {
                    //Cannot buy
                    stack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0, (byte) 14);
                }

                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lKit&7: &f" + next.getTitle()));
                List<String> lore = Lists.newArrayList();
                lore.add(ChatColor.translateAlternateColorCodes('&', "&a&lCosts&7: &f" + Economy.formatCash(next.getCost())));
                lore.add("");
                lore.add(ChatColor.translateAlternateColorCodes('&', "&a&lRequires&7: &f" + next.getTitle()));
                meta.setLore(lore);
                stack.setItemMeta(meta);

                kit_map.put(index, next);

                inventory.setItem(index, stack);
                index++;
            }
        } else {
            for (int i = 0, j = 1; i <= Rank.getMax_rank(); i++) {
                Kit next = kit_loader.getGlobal_ranked_kits().get(i);

                if (next == null)
                    continue;

                ItemStack stack;
                if (kits.contains("rank-" + i) || j == 1) {
                    //Owned
                    stack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0, (byte) 5);
                } else if (Rank.getRank(player) >= i) {
                    //Can buy
                    stack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0, (byte) 4);
                } else {
                    //Cannot buy
                    stack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0, (byte) 14);
                }

                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lKit&7: &f" + j));
                List<String> lore = Lists.newArrayList();
                lore.add(ChatColor.translateAlternateColorCodes('&', "&a&lCosts&7: &f" + Economy.formatCash(next.getCost())));
                lore.add("");
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Unlocked at rank &c" + next.getRequired_rank()));

                kit_map.put(j - 1, next);

                meta.setLore(lore);
                stack.setItemMeta(meta);
                inventory.setItem(j - 1, stack);

                ++j;
            }
        }

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null)
                inventory.setItem(slot, Common.craftItem(Material.STAINED_GLASS_PANE, (byte)15, " "));
        }

        if (!donor)
            inventory.setItem(inventory.getSize()-1, Common.craftItem(Material.ARROW, "&a&lDonor kits&7..."));
        else
            inventory.setItem(inventory.getSize()-9, Common.craftItem(Material.ARROW, "&7...&a&lRank kits"));

        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack click = event.getCurrentItem();
        if (click == null)
            return;

        if (click.getType().equals(Material.ARROW)) {
            closeInventory();

            if (event.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.GRAY + "...")) {
                //Back to regular
                new KitInventoryBase(player, kit_loader, false).openInventory();
            } else {
                //To donor
                new KitInventoryBase(player, kit_loader, true).openInventory();
            }
            return;
        }

        if (event.getRawSlot() >= event.getInventory().getSize()) {
            return;
        }

        Kit kit = kit_map.get(event.getRawSlot());

        if (click.getDurability() == 4) {
            //Is buying
            double cost = (kit_loader.getKit_costs().containsKey(kit) ? kit_loader.getKit_costs().get(kit) : -1);
            if (cost == -1) {
                player.sendMessage(ChatColor.RED + "You cannot buy this kit!");
                return;
            }

            if (Economy.getMoney(player) >= cost) {
                Economy.setMoney(player, Economy.getMoney(player)-cost);
                player.sendMessage(ChatColor.GREEN + "Purchased a new kit!");

                //Make sure to save in case of crash
                User user = User.fromPlayer(player);
                String kits = user.getAttachments().getAttachment("kits");
                kits += (",rank-" + kit.getRequired_rank());
                user.getAttachments().changeAttachment("kits", kits);
                user.save(true);

                closeInventory();
                openInventory();

                //Load kit
                loadKit(kit);

                return;
            }

            player.sendMessage(ChatColor.RED + "You cannot afford this kit!");
        } else if (click.getDurability() == 5) {
            //Has!
            loadKit(kit);
        }
    }

    private void loadKit(Kit kit) {
        player.getInventory().clear();

        ItemStack[] armor = kit.getArmor();
        ItemStack[] contents = kit.getContents();

        player.getInventory().setArmorContents(armor);
        player.getInventory().setContents(contents);

        player.getWorld().playSound(player.getLocation(), Sound.HORSE_ARMOR, 1, 1);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lKit loaded&7..."));
    }
}
