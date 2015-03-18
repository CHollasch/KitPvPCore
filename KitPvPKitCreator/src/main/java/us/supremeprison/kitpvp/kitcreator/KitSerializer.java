/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.supremeprison.kitpvp.kitcreator;

import org.bukkit.*;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Harry
 */
public class KitSerializer {

    private static final JSONParser parser = new JSONParser();

    public static String inventoryToJson(ItemStack[] inventory) {
        JSONObject root_object = new JSONObject();

        JSONArray items_array = new JSONArray();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack current = inventory[i];

            if (current == null)
                current = new ItemStack(Material.AIR, 1);

            JSONObject current_object = new JSONObject();
            current_object.put("material", current.getType().name());
            current_object.put("amount", current.getAmount());
            current_object.put("durability", (short) current.getDurability());
            if (current.hasItemMeta())
                current_object.put("meta", serializeItemMeta(current.getItemMeta()));

            items_array.add(current_object);
        }

        root_object.put("items", items_array);
        root_object.put("size", inventory.length);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(root_object);
    }

    public static ItemStack[] jsonToInventory(String string) throws ParseException {
        JSONObject root_object = (JSONObject) parser.parse(string);

        ItemStack[] inventory = new ItemStack[(int) ((long)root_object.get("size"))];

        JSONArray items_array = (JSONArray) root_object.get("items");
        for (int i = 0; i < items_array.size(); i++) {
            JSONObject object = (JSONObject) items_array.get(i);

            ItemStack stack = new ItemStack(Material.getMaterial((String) object.get("material")), (int) ((long) object.get("amount")), (short) ((long) object.get("durability")));
            if (object.containsKey("meta"))
                deserializeAndApplyItemMeta((JSONObject) object.get("meta"), stack);

            inventory[i] = stack;
        }

        return inventory;
    }

    private static void deserializeAndApplyItemMeta(JSONObject meta_object, ItemStack stack) {
        if (meta_object.containsKey("display_name")) {
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(deserializeChatColors(meta_object.get("display_name").toString()));
            stack.setItemMeta(meta);
        }

        if (meta_object.containsKey("lore")) {
            JSONArray lore_array = (JSONArray) meta_object.get("lore");
            List<String> new_lore = new ArrayList<String>();

            for (Object object : lore_array)
                new_lore.add(deserializeChatColors((String) object));

            ItemMeta meta = stack.getItemMeta();
            meta.setLore(new_lore);
            stack.setItemMeta(meta);
        }

        if (meta_object.containsKey("enchants")) {
            JSONArray enchants_array = (JSONArray) meta_object.get("enchants");

            for (Object enchant_object : enchants_array) {
                JSONObject enchant_obj = (JSONObject) enchant_object;
                stack.addUnsafeEnchantment(
                        Enchantment.getByName((String) enchant_obj.get("name")), (int) ((long) enchant_obj.get("level")));
            }
        }

        if (meta_object.containsKey("interfaces")) {
            JSONArray interfaces_array = (JSONArray) meta_object.get("interfaces");
            for (Object interface_object : interfaces_array)
                NAME_SWITCH:
                        switch ((String) interface_object) {
                            case "BookMeta": {
                                BookMeta book_meta = (BookMeta) stack.getItemMeta();

                                if (meta_object.containsKey("book_title"))
                                    book_meta.setTitle(deserializeChatColors((String) meta_object.get("book_title")));

                                if (meta_object.containsKey("book_author"))
                                    book_meta.setAuthor(deserializeChatColors((String) meta_object.get("book_author")));

                                if (meta_object.containsKey("book_pages")) {
                                    JSONArray pages_array = (JSONArray) meta_object.get("book_pages");
                                    List<String> new_pages = new ArrayList<String>();
                                    for (Object object : pages_array)
                                        new_pages.add(deserializeChatColors((String) object));
                                    book_meta.setPages(new_pages);
                                }

                                stack.setItemMeta(book_meta);
                                break NAME_SWITCH;
                            }
                            case "EnchantmentStorageMeta": {
                                EnchantmentStorageMeta enchant_storage_meta = (EnchantmentStorageMeta) stack.getItemMeta();

                                if (meta_object.containsKey("stored_enchants")) {
                                    JSONArray stored_enchants_array = (JSONArray) meta_object.get("stored_enchants");

                                    for (Object stored_enchant : stored_enchants_array) {
                                        String[] split = ((String) stored_enchant).split("//ENCHANTSPLITTER//");

                                        if (split.length == 2)
                                            enchant_storage_meta.addStoredEnchant(Enchantment.getByName(split[0]), Integer.parseInt(split[1]), true);
                                    }
                                }

                                stack.setItemMeta(enchant_storage_meta);
                                break NAME_SWITCH;
                            }
                            case "LeatherArmorMeta": {
                                LeatherArmorMeta leather_armor_meta = (LeatherArmorMeta) stack.getItemMeta();

                                if (meta_object.containsKey("leather_color"))
                                    leather_armor_meta.setColor(stringToColor((String) meta_object.get("leather_color")));

                                stack.setItemMeta(leather_armor_meta);
                                break NAME_SWITCH;
                            }
                            case "MapMeta": {
                                MapMeta map_meta = (MapMeta) stack.getItemMeta();

                                if (meta_object.containsKey("map_scaling"))
                                    map_meta.setScaling((boolean) meta_object.get("map_scaling"));

                                stack.setItemMeta(map_meta);
                                break NAME_SWITCH;
                            }
                            case "PotionMeta": {
                                PotionMeta potion_meta = (PotionMeta) stack.getItemMeta();

                                if (meta_object.containsKey("custom_effects")) {
                                    JSONArray custom_effect_array = (JSONArray) meta_object.get("custom_effects");

                                    for (Object effect_string : custom_effect_array)
                                        potion_meta.addCustomEffect(stringToPotionEffect((String) effect_string), true);
                                }

                                stack.setItemMeta(potion_meta);
                                break NAME_SWITCH;
                            }
                            case "SkullMeta": {
                                SkullMeta skull_meta = (SkullMeta) stack.getItemMeta();

                                if (meta_object.containsKey("skull_owner"))
                                    skull_meta.setOwner((String) meta_object.get("skull_owner"));

                                stack.setItemMeta(skull_meta);
                                break NAME_SWITCH;
                            }
                            case "FireworkMeta": {
                                FireworkMeta firework_meta = (FireworkMeta) stack.getItemMeta();

                                if (meta_object.containsKey("firework_power"))
                                    firework_meta.setPower((int) ((long) meta_object.get("firework_power")));

                                if (meta_object.containsKey("firework_effects")) {
                                    JSONArray firework_effects_array = (JSONArray) meta_object.get("firework_effects");

                                    for (Object firework_effect_object : firework_effects_array)
                                        firework_meta.addEffect(JSONObjectToFireworkEffect((JSONObject) firework_effect_object));
                                }

                                stack.setItemMeta(firework_meta);
                                break NAME_SWITCH;
                            }
                            default:
                                break NAME_SWITCH;
                        }
        }
    }

    private static JSONObject serializeItemMeta(ItemMeta meta) {
        JSONObject meta_object = new JSONObject();
        if (meta.hasDisplayName())
            meta_object.put("display_name", serializeChatColors(meta.getDisplayName()));

        if (meta.hasLore()) {
            JSONArray lore_array = new JSONArray();
            for (String lore_line : meta.getLore())
                lore_array.add(serializeChatColors(lore_line));

            meta_object.put("lore", lore_array);
        }

        if (meta.hasEnchants()) {
            JSONArray enchants_array = new JSONArray();

            for (Entry<Enchantment, Integer> enchantment : meta.getEnchants().entrySet()) {
                JSONObject enchantment_object = new JSONObject();
                enchantment_object.put("name", enchantment.getKey().getName());
                enchantment_object.put("level", enchantment.getValue());
                enchants_array.add(enchantment_object);
            }

            meta_object.put("enchants", enchants_array);
        }

        JSONArray meta_interfaces_array = new JSONArray();

        for (Class inter_face : meta.getClass().getInterfaces()) {
            String inter_face_name = inter_face.getSimpleName();

            NAME_SWITCH:
            switch (inter_face_name) {
                case "BookMeta": {
                    BookMeta book_meta = (BookMeta) meta;
                    if (book_meta.hasTitle())
                        meta_object.put("book_title", serializeChatColors(book_meta.getTitle()));

                    if (book_meta.hasAuthor())
                        meta_object.put("book_author", serializeChatColors(book_meta.getAuthor()));

                    if (book_meta.hasPages()) {
                        JSONArray page_array = new JSONArray();
                        for (String page : book_meta.getPages())
                            page_array.add(serializeChatColors(page));

                        meta_object.put("book_pages", page_array);
                    }

                    meta_interfaces_array.add(inter_face_name);
                    break NAME_SWITCH;
                }
                case "EnchantmentStorageMeta": {
                    EnchantmentStorageMeta enchant_storage_meta = (EnchantmentStorageMeta) meta;
                    if (enchant_storage_meta.hasStoredEnchants()) {
                        JSONArray stored_enchant_array = new JSONArray();

                        for (Entry<Enchantment, Integer> stored_enchantment : enchant_storage_meta.getStoredEnchants().entrySet())
                            stored_enchant_array.add(stored_enchantment.getKey().getName() + "//ENCHANTSPLITTER//" + stored_enchantment.getValue());

                        meta_object.put("stored_enchants", stored_enchant_array);
                    }

                    meta_interfaces_array.add(inter_face_name);
                    break NAME_SWITCH;
                }
                case "LeatherArmorMeta": {
                    LeatherArmorMeta leather_armor_meta = (LeatherArmorMeta) meta;

                    if (leather_armor_meta.getColor() != null)
                        meta_object.put("leather_color", colorToString(leather_armor_meta.getColor()));

                    meta_interfaces_array.add(inter_face_name);
                    break NAME_SWITCH;
                }
                case "MapMeta": {
                    MapMeta map_meta = (MapMeta) meta;

                    meta_object.put("map_scaling", map_meta.isScaling());

                    meta_interfaces_array.add(inter_face_name);
                    break NAME_SWITCH;
                }
                case "PotionMeta": {
                    PotionMeta potion_meta = (PotionMeta) meta;

                    if (potion_meta.hasCustomEffects()) {
                        JSONArray effect_array = new JSONArray();

                        for (PotionEffect custom_effect : potion_meta.getCustomEffects())
                            effect_array.add(potionEffectToString(custom_effect));

                        meta_object.put("custom_effects", effect_array);
                    }

                    meta_interfaces_array.add(inter_face_name);
                    break NAME_SWITCH;
                }
                case "SkullMeta": {
                    SkullMeta skull_meta = (SkullMeta) meta;

                    if (skull_meta.hasOwner())
                        meta_object.put("skull_owner", skull_meta.getOwner());

                    meta_interfaces_array.add(inter_face_name);
                    break NAME_SWITCH;
                }
                case "FireworkMeta": {
                    FireworkMeta firework_meta = (FireworkMeta) meta;

                    if (firework_meta.hasEffects()) {
                        JSONArray firework_effect_array = new JSONArray();

                        for (FireworkEffect effect : firework_meta.getEffects())
                            firework_effect_array.add(fireworkEffectToJSONObject(effect));

                        meta_object.put("firework_effects", firework_effect_array);
                    }

                    meta_object.put("firework_power", firework_meta.getPower());

                    meta_interfaces_array.add(inter_face_name);
                    break NAME_SWITCH;
                }
                default:
                    break NAME_SWITCH;
            }
        }

        meta_object.put("interfaces", meta_interfaces_array);

        return meta_object;
    }

    private static String colorToString(Color color) {
        return color.getRed() + "_" + color.getGreen() + "_" + color.getBlue();
    }

    private static Color stringToColor(String string) {
        String[] split = string.split("_");
        return Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    private static String potionEffectToString(PotionEffect effect) {
        return effect.getType().getName() + "//POTIONSPLITTER//" + effect.getDuration() + "//POTIONSPLITTER//" + effect.getAmplifier();
    }

    private static PotionEffect stringToPotionEffect(String string) {
        String[] split = string.split("//POTIONSPLITTER//");
        return new PotionEffect(PotionEffectType.getByName(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    private static JSONObject fireworkEffectToJSONObject(FireworkEffect effect) {
        JSONObject object = new JSONObject();

        object.put("type", effect.getType().name());
        object.put("flicker", effect.hasFlicker());
        object.put("trail", effect.hasTrail());

        if (effect.getColors() != null) {
            JSONArray colors_array = new JSONArray();

            for (Color color : effect.getColors())
                colors_array.add(colorToString(color));

            object.put("colors", colors_array);
        }

        if (effect.getFadeColors() != null) {
            JSONArray fade_colors_array = new JSONArray();

            for (Color color : effect.getFadeColors())
                fade_colors_array.add(colorToString(color));

            object.put("fade_colors", fade_colors_array);
        }

        return object;
    }

    public static FireworkEffect JSONObjectToFireworkEffect(JSONObject object) {
        Builder builder = FireworkEffect.builder();

        builder.with(FireworkEffect.Type.valueOf((String) object.get("type")));

        if ((Boolean) object.get("flicker"))
            builder.withFlicker();

        if ((Boolean) object.get("trail"))
            builder.withTrail();

        if (object.containsKey("colors"))
            for (Object color_string : (JSONArray) object.get("colors"))
                builder.withColor(stringToColor((String) color_string));

        if (object.containsKey("fade_colors"))
            for (Object color_string : (JSONArray) object.get("fade_colors"))
                builder.withFade(stringToColor((String) color_string));

        return builder.build();
    }

    private static String serializeChatColors(String string) {
        StringBuilder builder = new StringBuilder();
        for (char cha : string.toCharArray()) {
            if (cha == ChatColor.COLOR_CHAR) {
                builder.append('&');
            } else {
                builder.append(cha);
            }
        }
        return builder.toString();
    }

    private static String deserializeChatColors(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}