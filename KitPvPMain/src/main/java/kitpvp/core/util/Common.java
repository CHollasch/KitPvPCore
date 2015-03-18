package kitpvp.core.util;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Connor Hollasch
 * @since 12/18/2014
 */
public class Common {

	public static String center(String input) {
		int max_trim = 34;
		for (char c : input.toCharArray()) {
			if (c == '&')
				max_trim+=2;
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
			inHand.setAmount(inHand.getAmount()-1);
			player.setItemInHand(inHand);
		}
	}

	public static ItemStack addCustomMetadata(ItemStack item, List<String> nbt_data) {
		if (!(item instanceof CraftItemStack)) {
			item = CraftItemStack.asCraftCopy(item);
		}

		NBTTagCompound tag = getTag(item);
		if (tag == null) {
			tag = new NBTTagCompound();
		}

		NBTTagList tag_list = new NBTTagList();
		for (String nbt : nbt_data) {
			tag_list.add(new NBTTagString(nbt));
		}

		tag.set("custom_metadata", tag_list);

		return setTag(item, tag);
	}

	public static List<String> getCustomMetadata(ItemStack item) {
		if (!(item instanceof CraftItemStack)) {
			item = CraftItemStack.asCraftCopy(item);
		}
		NBTTagCompound tag = getTag(item);
		if (tag == null) {
			tag = new NBTTagCompound();
		}

		List<String> custom_metadata = new ArrayList<>();

		if (tag.hasKey("custom_metadata")) {
			NBTTagList custom_metadata_list = (NBTTagList) tag.get("custom_metadata");

			for (int i = 0; i < custom_metadata_list.size(); i++) {
				custom_metadata.add(custom_metadata_list.getString(i));
			}
		}
		return custom_metadata;
	}

	private static NBTTagCompound getTag(ItemStack item) {
		if (item instanceof CraftItemStack) {
			try {
				Field field = CraftItemStack.class.getDeclaredField("handle");
				field.setAccessible(true);
				return ((net.minecraft.server.v1_7_R4.ItemStack) field.get(item)).tag;
			} catch (Exception e) {
			}
		}
		return null;
	}

	public static ArrayList<ItemStack[]> makePages(ItemStack[] all, int max_per_page) {
		ArrayList<ItemStack[]> pages = Lists.newArrayList();
		if (all.length == 0) {
			pages.add(new ItemStack[0]);
			return pages;
		}

		ItemStack[] current = new ItemStack[max_per_page];

		for (int i = 0, j = 0; i < all.length; i++, j++) {
			if (i % max_per_page == 0 && i >= max_per_page) {
				pages.add(current);
				j = -1;
				current = new ItemStack[max_per_page];
				continue;
			}

			current[j] = all[i];
		}

		if (current[0] != null)
			pages.add(current);

		return pages;
	}

	private static ItemStack setTag(ItemStack item, NBTTagCompound tag) {
		CraftItemStack craftItem = null;
		if (item instanceof CraftItemStack) {
			craftItem = (CraftItemStack) item;
		} else {
			craftItem = CraftItemStack.asCraftCopy(item);
		}

		net.minecraft.server.v1_7_R4.ItemStack nmsItem = null;
		try {
			Field field = CraftItemStack.class.getDeclaredField("handle");
			field.setAccessible(true);
			nmsItem = ((net.minecraft.server.v1_7_R4.ItemStack) field.get(item));
		} catch (Exception e) {
		}
		if (nmsItem == null) {
			nmsItem = CraftItemStack.asNMSCopy(craftItem);
		}

		nmsItem.tag = tag;
		try {
			Field field = CraftItemStack.class.getDeclaredField("handle");
			field.setAccessible(true);
			field.set(craftItem, nmsItem);
		} catch (Exception e) {
		}

		return craftItem;
	}

	public static boolean empty(Inventory inventory) {
		ItemStack[] items = inventory.getContents();

		for (int i = 0; i < items.length; i++) {
			if (items[i] != null && !items[i].getType().equals(Material.AIR))
				return false;
		}

		return true;
	}
}
