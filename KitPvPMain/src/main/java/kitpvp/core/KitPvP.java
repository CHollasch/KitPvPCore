package kitpvp.core;

import kitpvp.core.command.DynamicCommandRegistry;
import kitpvp.core.database.MySQLConnectionPool;
import kitpvp.core.database.MySQLDatabaseInformation;
import kitpvp.core.database.MySQLVars;
import kitpvp.core.module.Module;
import kitpvp.core.util.ReflectionHandler;
import kitpvp.core.util.Todo;
import kitpvp.core.util.config.ClassConfig;
import kitpvp.core.util.messages.Form;
import lombok.Getter;
import net.minecraft.util.io.netty.util.internal.ConcurrentSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import kitpvp.core.module.modifiers.ModuleDependency;
import kitpvp.core.user.User;

import java.util.*;
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
    @Getter
    private MySQLDatabaseInformation database_information;

    public void onEnable() {
        plugin_instance = this;

        saveDefaultConfig();
        reloadConfig();

        createMessageFormatting();

        ClassConfig.setWrapped_plugin(this);
        new DynamicCommandRegistry();

        try { reloadPluginModules(); } catch (Exception e) { e.printStackTrace(); }

        User.createUserListener();

        database_information = new MySQLDatabaseInformation();
        ClassConfig.loadAll(database_information);

        connection_pool = new MySQLConnectionPool(this, database_information);
        MySQLVars.CREATE_ATTACHMENT_TABLE.executeQuery();

        printTodos();
    }

    private void createMessageFormatting() {
        Form.insertTag("error", "&c&l(!) &7%msg%");
        Form.insertTag("rank", "&9&lRank&7: &e%msg%");
        Form.insertTag("eco", "&a&lEco&7: &e%msg%");
    }

    private void printTodos() {
        List<Class<Object>> all_classes = ReflectionHandler.getClassesInPackage("us.supremeprison.kitpvp", Object.class);

        Set<String> todos = new HashSet<>();
        for (Class<Object> clazz : all_classes) {
            if (clazz.getAnnotation(Todo.class) != null) {
                String[] messages = clazz.getAnnotation(Todo.class).value();
                for (String message : messages) {
                    todos.add("Remember to &d\"" + message + "\"&e in the &6" + clazz.getSimpleName() + "&e class!");
                }
            }
        }

        if (todos.size() != 0) {
            logMessage("");
            logMessage("------------- PROJECT TODOS -------------");
            int index = 1;
            for (String todo : todos) {
                logMessage("  &3" + index + ". &e" + todo);
                ++index;
            }
            logMessage("-----------------------------------------");
            logMessage("");
        }
    }

    public void onDisable() {
        for (User user : User.getOnline_user_map().values()) {
            user.save(false);
        }

        connection_pool.closeConnections();
        HandlerList.unregisterAll(this);

        if (modules.size() > 0) {
            for (Module key : modules.keySet()) {
                ClassConfig value = modules.get(key);
                value.saveAll();
                key.onDisable();
            }
        }

        User.setOnline_user_map(new ConcurrentHashMap<String, User>());
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
        forClassModule: for (Class<? extends Module> module : module_classes) {
            ModuleDependency dependencies = module.getAnnotation(ModuleDependency.class);
            if (dependencies != null) {
                String[] all = dependencies.depends_on();
                for (String dependency : all) {
                    if (dependency.equalsIgnoreCase(module.getSimpleName()))
                        continue;

                    boolean found = false;
                    forModule: for (Module loaded : this.modules.keySet()) {
                        if (loaded.getModule_name().toLowerCase().equals(dependency.toLowerCase())) {
                            found = true;
                            break forModule;
                        }
                    }

                    if (!found)
                        continue forClassModule;
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
