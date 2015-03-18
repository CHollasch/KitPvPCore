package kitpvp.core.util.config;

import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.Plugin;
import kitpvp.core.module.Module;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Modified to work with KitPvPCore.
 *
 * @author Connor Hollasch
 * @since 2/28/2015
 */
public class ClassConfig {

    @Setter
    private static Plugin wrapped_plugin;

    @Setter
    private Module wrapped_module;

    public void loadAll() {
        try {
            Class<?> clazz = wrapped_module.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                ConfigOption option = field.getDeclaredAnnotation(ConfigOption.class);
                if (option == null)
                    continue;

                String section = option.configuration_section();
                section = "MODULES." + wrapped_module.getModule_name().toUpperCase() + "." + section;
                if (wrapped_plugin.getConfig().contains(section)) {
                    Object find = wrapped_plugin.getConfig().get(section);
                    if (find instanceof MemorySection && field.get(wrapped_module) instanceof HashMap) {
                        Map<Object, Object> nMap = new HashMap<>();
                        ConfigurationSection cfgSec = (ConfigurationSection) find;
                        for (String key : cfgSec.getKeys(false)) {
                            Object val = cfgSec.get(key);
                            nMap.put(key, val);
                        }
                        field.set(wrapped_module, nMap);
                    } else if (field.get(wrapped_module) instanceof Location) {
                        field.set(wrapped_module, fromString(find.toString()));
                    } else
                        field.set(wrapped_module, find);
                } else {
                    if (field.get(wrapped_module) != null) {
                        wrapped_plugin.getConfig().set(section, field.get(wrapped_module));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        wrapped_plugin.saveConfig();
        wrapped_plugin.reloadConfig();
    }

    public void saveAll() {
        try {
            Class<?> clazz = wrapped_module.getClass();
            for (Field field : clazz.getFields()) {
                field.setAccessible(true);

                ConfigOption option = field.getAnnotation(ConfigOption.class);
                if (option == null)
                    continue;

                if (field.get(wrapped_module) == null)
                    continue;

                String section = option.configuration_section();
                section = "MODULES." + wrapped_module.getModule_name().toUpperCase() + "." + section;

                if (field.get(wrapped_module) instanceof Location) {
                    wrapped_plugin.getConfig().set(section, fromLocation((Location) field.get(wrapped_module)));
                } else
                    wrapped_plugin.getConfig().set(section, field.get(wrapped_module));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        wrapped_plugin.saveConfig();
        wrapped_plugin.reloadConfig();
    }

    //====== STATIC CONFIGURATION ======

    public static void loadAll(Object class_container) {
        try {
            Class<?> clazz = class_container.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                ConfigOption option = field.getDeclaredAnnotation(ConfigOption.class);
                if (option == null)
                    continue;

                String section = option.configuration_section();
                if (wrapped_plugin.getConfig().contains(section)) {
                    Object find = wrapped_plugin.getConfig().get(section);
                    if (find instanceof MemorySection && field.get(class_container) instanceof HashMap) {
                        Map<Object, Object> nMap = new HashMap<>();
                        ConfigurationSection cfgSec = (ConfigurationSection) find;
                        for (String key : cfgSec.getKeys(false)) {
                            Object val = cfgSec.get(key);
                            nMap.put(key, val);
                        }
                        field.set(class_container, nMap);
                    } else if (field.get(class_container) instanceof Location) {
                        field.set(class_container, fromString(find.toString()));
                    } else
                        field.set(class_container, find);
                } else {
                    if (field.get(class_container) != null) {
                        wrapped_plugin.getConfig().set(section, field.get(class_container));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        wrapped_plugin.saveConfig();
        wrapped_plugin.reloadConfig();
    }

    public static void saveAll(Object class_container) {
        try {
            Class<?> clazz = class_container.getClass();
            for (Field field : clazz.getFields()) {
                field.setAccessible(true);

                ConfigOption option = field.getAnnotation(ConfigOption.class);
                if (option == null)
                    continue;

                if (field.get(class_container) == null)
                    continue;

                if (field.get(class_container) instanceof Location) {
                    wrapped_plugin.getConfig().set(option.configuration_section(), fromLocation((Location) field.get(class_container)));
                } else
                    wrapped_plugin.getConfig().set(option.configuration_section(), field.get(class_container));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        wrapped_plugin.saveConfig();
        wrapped_plugin.reloadConfig();
    }

    public static Location fromString(String in) {
        String[] parts = in.split(",");
        World world = Bukkit.getWorld(parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static String fromLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }
}
