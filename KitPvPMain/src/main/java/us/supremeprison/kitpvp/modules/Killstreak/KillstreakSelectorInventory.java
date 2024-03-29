package us.supremeprison.kitpvp.modules.Killstreak;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.inventory.OpenInventoryData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Connor Hollasch
 * @since 4/8/2015
 */
public class KillstreakSelectorInventory extends OpenInventoryData {

    private Killstreak module;

    private HashMap<Integer, KillstreakReward> inventoryRewards = new HashMap<>();

    public KillstreakSelectorInventory(Player player) {
        super(player);
        module = Killstreak.getModule_instance();
    }

    public Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, 54, Common.center(ChatColor.translateAlternateColorCodes('&', " &8Killstreak Rewards")));
        int i = 0;

        for (; i < 9; i++) {
            inventory.setItem(i, Common.craftItem(Material.STAINED_GLASS_PANE, (byte) 15, " "));
        }

        ItemStack[] center_items = new ItemStack[]{
                Common.craftItem(Material.STAINED_GLASS_PANE, (byte) 4, " "), //Yellow
                Common.craftItem(Material.STAINED_GLASS_PANE, (byte) 1, " "), //Orange
                Common.craftItem(Material.STAINED_GLASS_PANE, (byte) 14, " ") //Red
        };

        inventory.setItem(9, center_items[0]);
        inventory.setItem(10, center_items[1]);
        inventory.setItem(11, center_items[2]);

        i = 12;

        LinkedHashMap<KillstreakReward, Integer> killstreakRewards = Common.sortHashMapByValues(Killstreak.getModule_instance().getPlayer_killstreak_rewards(player));
        for (KillstreakReward key : killstreakRewards.keySet()) {
            int kills = key.getKills();
            inventory.setItem(i, getTaggedStack(key, kills));
            i++;
        }

        inventory.setItem(15, center_items[2]);
        inventory.setItem(16, center_items[1]);
        inventory.setItem(17, center_items[0]);

        i = 18;

        for (; i < 27; i++) {
            inventory.setItem(i, Common.craftItem(Material.STAINED_GLASS_PANE, (byte) 15, " "));
        }

        for (KillstreakReward reward : Killstreak.getModule_instance().getKillstreak_rewards_sorted().keySet()) {
            int kills = reward.getKills();
            inventory.setItem(i, getTaggedStack(reward, kills));
            inventoryRewards.put(i, reward);
            i++;
        }

        return inventory;
    }

    private ItemStack getTaggedStack(KillstreakReward reward, int kills) {
        ItemStack stack = reward.getIcon().clone();
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore() == null ? new ArrayList<String>() : meta.getLore();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&e" + kills + " &fkills."));
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getRawSlot() >= event.getInventory().getSize())
            return;

        event.setCancelled(true);
        int slot = event.getSlot();

        if (slot >= 27) {
            KillstreakReward reward = inventoryRewards.get(slot);

            if (reward == null) {
                return;
            }

            User user = User.fromPlayer(player);
            List<KillstreakReward> current = user.getAttachments().getAttachment(Killstreak.KS_REWARD_ATTACHMENT);

            if (current.contains(reward)) {
                return;
            }

            if (current.size() >= 3) {
                KillstreakReward replace = null;

                for (KillstreakReward find : current) {
                    if (current == null) {
                        continue;
                    }

                    if (replace == null) {
                        replace = find;
                        continue;
                    }

                    if (find.getKills() <= reward.getKills() && replace.getKills() >= find.getKills()) {
                        replace = find;
                        continue;
                    }
                }

                if (replace != null) {
                    current.remove(replace);
                }
            }

            current.add(reward);

            user.getAttachments().changeAttachment(Killstreak.KS_REWARD_ATTACHMENT, current);
            user.save(false);

            closeInventory();
            openInventory();
        }
    }
}
