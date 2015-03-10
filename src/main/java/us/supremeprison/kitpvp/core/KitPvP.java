package us.supremeprison.kitpvp.core;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import us.supremeprison.kitpvp.core.database.MySQLConnectionPool;
import us.supremeprison.kitpvp.core.database.MySQLDatabaseInformation;
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
    @Getter
    private MySQLConnectionPool connection_pool;

    public void onEnable() {
        plugin_instance = this;

        saveDefaultConfig();
        reloadConfig();

        ClassConfig.setWrapped_plugin(this);
        try {
            reloadPluginModules();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MySQLDatabaseInformation info = new MySQLDatabaseInformation();
        ClassConfig.loadAll(info);

        connection_pool = new MySQLConnectionPool(this, info);
    }

    public void onDisable() {
        connection_pool.closeConnections();

        if (modules.size() > 0) {
            for (Module key : modules.keySet()) {
                ClassConfig value = modules.get(key);
                value.saveAll();
                key.onDisable();
            }
        }

        reloadConfig();
    }

    public void logMessage(String message) {
        getServer().getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&', "&7[&6KitPvP&7] [&3" + getDescription().getVersion() + "&7] &e" + message));
    }

    public void logMessage(Module module, String message) {
        logMessage(ChatColor.translateAlternateColorCodes('&', "&r&7[&a" + module.getModule_name() + "&7] &e" + message));
    }

    //====== INSTANTIATION ======
    protected void reloadPluginModules() throws Exception {
        if (modules.size() > 0) {
            for (Module key : modules.keySet()) {
                ClassConfig value = modules.get(key);
                value.saveAll();
                value.loadAll();

                key.onEnable();
            }
        } else {
            Set<Class<? extends Module>> module_classes = new Reflections("us.supremeprison.kitpvp.modules")
                    .getSubTypesOf(Module.class);

            for (Class<? extends Module> module : module_classes) {
                Module instance = module.newInstance();
                instance.setModule_name(module.getSimpleName());

                instance.parent_plugin = this;
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
