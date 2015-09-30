package us.supremeprison.kitpvp.core.util.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Modified to work with KitPvPCore.
 *
 * @author Connor Hollasch
 * @since 2/28/2015
 */
public class ClassConfig {

    @Setter
    private Plugin wrapped_plugin;

    private List<ConfigSerializable<?>> specials = new ArrayList<ConfigSerializable<?>>() {
        {
            add(locationSerializer = new LocationSerializer());
            add(new MaterialdataSeralizable());
        }
    };

    @Getter
    private static LocationSerializer locationSerializer;

    public void addConfigSerializer(ConfigSerializable<?> serializable) {
        specials.add(serializable);
    }

    //====== STATIC CONFIGURATION ======

    public void loadAll(Object class_container) {
        loadAll(class_container, "");
    }

    public void saveAll(Object class_container) {
        saveAll(class_container, "");
    }

    public void loadAll(Object class_container, String conf_start) {
        try {
            Class<?> clazz = class_container.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                ConfigOption option = field.getAnnotation(ConfigOption.class);
                if (option == null)
                    continue;

                String section = conf_start + option.value();
                if (wrapped_plugin.getConfig().contains(section)) {
                    boolean set = false;
                    Object find = wrapped_plugin.getConfig().get(section);
                    if (find instanceof MemorySection && field.get(class_container) instanceof HashMap) {
                        LinkedHashMap<Object, Object> nMap = new LinkedHashMap<>();
                        ConfigurationSection cfgSec = (ConfigurationSection) find;
                        for (String key : cfgSec.getKeys(false)) {
                            Object val = cfgSec.get(key);
                            nMap.put(key, val);
                        }
                        field.set(class_container, nMap);
                        set = true;
                    }

                    for (ConfigSerializable<?> special : specials) {
                        if (field.getType().isInstance(special.getWrappedType()) || field.getType().isAssignableFrom(special.getWrappedType())) {
                            field.set(class_container, special.load(find.toString()));
                            set = true;
                        }
                    }

                    if (!set)
                        field.set(class_container, find);
                } else {
                    if (field.get(class_container) != null) {
                        boolean set = false;
                        for (ConfigSerializable special : specials) {
                            if (field.getType().isInstance(special.getWrappedType()) || field.getType().isAssignableFrom(special.getWrappedType())) {
                                wrapped_plugin.getConfig().set(section, special.save(field.get(class_container)));
                                set = true;
                                break;
                            }
                        }

                        if (!set) {
                            wrapped_plugin.getConfig().set(section, field.get(class_container));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        wrapped_plugin.saveConfig();
        wrapped_plugin.reloadConfig();
    }

    public void saveAll(Object class_container, String conf_start) {
        try {
            Class<?> clazz = class_container.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                ConfigOption option = field.getAnnotation(ConfigOption.class);
                if (option == null)
                    continue;

                if (field.get(class_container) == null)
                    continue;

                boolean set = false;

                for (ConfigSerializable special : specials) {
                    if (field.getType().isInstance(special.getWrappedType()) || field.getType().isAssignableFrom(special.getWrappedType())) {
                        wrapped_plugin.getConfig().set(conf_start + option.value(), special.save(field.get(class_container)));
                        set = true;
                    }
                }

                if (!set)
                    wrapped_plugin.getConfig().set(conf_start + option.value(), field.get(class_container));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        wrapped_plugin.saveConfig();
    }
}
