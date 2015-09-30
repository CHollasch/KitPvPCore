package us.supremeprison.kitpvp.core.util;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

;

/**
 * @author Connor Hollasch
 * @since 12/18/2014
 */
public class Common {

    public static String center(String input) {
        int max_trim = 34;
        for (char c : input.toCharArray()) {
            if (c == '&')
                max_trim += 2;
        }

        int pad = (max_trim - input.length()) / 2;
        for (int x = 0; x < pad; x++)
            input = (" " + input);

        return input;
    }

    public static String trimTitle(String input) {
        return (input.length() > 32 ? input.substring(0, 32) : input);
    }

    public static ItemStack craftItem(Material type, int amount, byte data, String name, String... lore) {
        ItemStack stack = new ItemStack(type, amount, (short) 0, data);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> translated = Lists.newArrayList();
        for (String raw : lore) {
            translated.add(ChatColor.translateAlternateColorCodes('&', raw));
        }
        meta.setLore(translated);
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack craftItem(Material type, int amount, String name, String... lore) {
        return craftItem(type, amount, (byte) 0, name, lore);
    }

    public static ItemStack craftItem(Material type, byte data, String name, String... lore) {
        return craftItem(type, 1, data, name, lore);
    }

    public static ItemStack craftItem(Material type, short data, String name, boolean _unused) {
        ItemStack stack = new ItemStack(type, 1, data, (byte) 0);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack craftItem(Material type, String name, String... lore) {
        return craftItem(type, 1, name, lore);
    }

    public static void removeOneInHand(Player player) {
        ItemStack inHand = player.getItemInHand();
        if (inHand.getAmount() == 1)
            player.setItemInHand(null);
        else {
            inHand.setAmount(inHand.getAmount() - 1);
            player.setItemInHand(inHand);
        }
    }

    public static boolean empty(Inventory inventory) {
        ItemStack[] items = inventory.getContents();

        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && !items[i].getType().equals(Material.AIR))
                return false;
        }

        return true;
    }

    public static String namify(Material type) {
        if (type.equals(Material.AIR))
            return "Hand";

        StringBuilder sb = new StringBuilder();
        String[] parts = type.toString().toLowerCase().split("_");

        for (String part : parts) {
            sb.append(part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase());
            sb.append(" ");
        }

        if (sb.toString().endsWith(" "))
            return sb.toString().substring(0, sb.length() - 1);

        return sb.toString();
    }

    public static <T> void arrayModifier(T[] array, ArrayModifier<T> modifier) {
        for (int i = 0; i < array.length; i++) {
            T pos = array[i];
            array[i] = modifier.changeAtIndex(pos, i);
        }
    }

    public static interface ArrayModifier<T> {
        public T changeAtIndex(T at, int index);
    }

    public static LinkedHashMap sortHashMapByValues(Map passedMap) {
        List map_keys = new ArrayList<>(passedMap.keySet());
        List map_values = new ArrayList<>(passedMap.values());

        if (hasNull(map_values)) {
            return new LinkedHashMap();
        }

        Collections.sort(map_values);

        LinkedHashMap sorted_map = new LinkedHashMap();

        Iterator values_iterator = map_values.iterator();
        while (values_iterator.hasNext()) {
            Object value = values_iterator.next();
            Iterator key_iterator = map_keys.iterator();

            while (key_iterator.hasNext()) {
                Object key = key_iterator.next();

                Object comp_passed_map = passedMap.get(key);
                Object comp_direct_value = value;

                if (comp_passed_map != null && comp_passed_map.equals(comp_direct_value)) {
                    passedMap.remove(key);
                    map_keys.remove(key);

                    sorted_map.put(key, value);
                    break;
                }
            }
        }

        return sorted_map;
    }

    private static boolean hasNull(Collection stuff) {
        for (Object o : stuff) {
            if (o == null) {
                return true;
            }
        }

        return false;
    }

    public static void give(Player player, ItemStack itemStack) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getEyeLocation(), itemStack);
        } else {
            player.getInventory().addItem(itemStack);
        }
    }
}
