package us.supremeprison.kitpvp.core;

import lombok.Getter;
import net.minecraft.util.io.netty.util.internal.ConcurrentSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import us.supremeprison.kitpvp.core.database.MySQLConnectionPool;
import us.supremeprison.kitpvp.core.database.MySQLDatabaseInformation;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.Depend;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.util.ReflectionHandler;
import us.supremeprison.kitpvp.core.util.config.ClassConfig;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Connor Hollasch
 * @since 3/9/2015
 */
public class KitPvP extends JavaPlugin {

    @Getter
    protected static KitPvP plugin_instance;

    @Getter
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

        User.createUserListener();

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

        User.setOnline_user_map(new ConcurrentHashMap<String, User>());

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
            List<Class<Module>> module_classes = ReflectionHandler.getClassesInPackage("us.supremeprison.kitpvp.modules", Module.class);
            ConcurrentSet<Class<Module>> concurrent_modules = new ConcurrentSet<>();
            concurrent_modules.addAll(module_classes);
            loadExcessModules(concurrent_modules);
        }
    }

    private void loadExcessModules(ConcurrentSet<Class<Module>> module_classes) throws Exception {
        for (Class<? extends Module> module : module_classes) {
            Depend dependencies = module.getAnnotation(Depend.class);
            if (dependencies != null) {
                String[] all = dependencies.depends_on();
                for (String dependency : all) {
                    if (dependency.equalsIgnoreCase(module.getSimpleName()))
                        break;

                    boolean found = false;
                    forModule: for (Module loaded : this.modules.keySet()) {
                        if (loaded.getModule_name().toLowerCase().equals(dependency.toLowerCase())) {
                            found = true;
                            break forModule;
                        }
                    }

                    if (!found)
                        continue;
                }
            }

            Module instance = module.newInstance();
            instance.setModule_name(module.getSimpleName());

            logMessage("Created module: &5" + module.getSimpleName());

            instance.parent_plugin = this;
            instance.onEnable();
            Bukkit.getPluginManager().registerEvents(instance, this);

            ClassConfig conf = new ClassConfig();
            conf.setWrapped_module(instance);
            conf.loadAll();

            this.modules.put(instance, conf);
            module_classes.remove(module);
        }

        if (module_classes.size() == 0)
            return;
        else
            loadExcessModules(module_classes);
    }
}
