package us.supremeprison.kitpvp.core.util.config;

import lombok.Setter;
import org.bukkit.plugin.Plugin;
import us.supremeprison.kitpvp.core.module.Module;

import java.lang.reflect.Field;

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
                section = "modules." + wrapped_module.getModule_name() + "." + section;
                if (wrapped_plugin.getConfig().contains(section)) {
                    Object find = wrapped_plugin.getConfig().get(section);
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
                section = "modules." + wrapped_module.getModule_name() + "." + section;

                wrapped_plugin.getConfig().set(section, field.get(wrapped_module));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        wrapped_plugin.saveConfig();
        wrapped_plugin.reloadConfig();
    }
}
