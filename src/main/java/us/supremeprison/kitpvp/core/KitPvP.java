package us.supremeprison.kitpvp.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.util.ReflectionHandler;
import us.supremeprison.kitpvp.core.util.config.ClassConfig;

import java.util.HashMap;
import java.util.Set;

/**
 * @author Connor Hollasch
 * @since 3/9/2015
 */
public class KitPvP extends JavaPlugin {

    protected static KitPvP plugin_instance;

    private HashMap<Module, ClassConfig> modules = new HashMap<>();

    public void onEnable() {
        plugin_instance = this;

        ClassConfig.setWrapped_plugin(this);
        try {
            reloadPluginModules();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDisable() {

    }

    //====== INSTANTIATION ======
    protected void reloadPluginModules() throws Exception {
        if (modules.size() > 0) {
            for (Module key : modules.keySet()) {
                ClassConfig value = modules.get(key);
                value.saveAll();
                value.loadAll();

                key.onDisable();
                key.onEnable();
            }
        } else {
            Set<Class<? extends Module>> module_classes = new Reflections("us.supremeprison.kitpvp.modules")
                    .getSubTypesOf(Module.class);

            for (Class<? extends Module> module : module_classes) {
                Module instance = module.newInstance();
                instance.setModule_name(module.getSimpleName());

                ReflectionHandler.setDeclaredValue(instance, "parent_plugin", this);
                instance.onEnable();
                Bukkit.getPluginManager().registerEvents(instance, this);

                ClassConfig conf = new ClassConfig();
                conf.setWrapped_module(instance);
                conf.loadAll();

                this.modules.put(instance, conf);
            }
        }
    }
}
