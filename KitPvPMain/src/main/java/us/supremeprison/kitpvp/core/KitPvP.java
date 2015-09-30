package us.supremeprison.kitpvp.core;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import us.supremeprison.kitpvp.core.command.CommandModule;
import us.supremeprison.kitpvp.core.command.DynamicCommandRegistry;
import us.supremeprison.kitpvp.core.database.MySQLConnectionPool;
import us.supremeprison.kitpvp.core.database.MySQLDatabaseInformation;
import us.supremeprison.kitpvp.core.database.MySQLVars;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.util.ReflectionHandler;
import us.supremeprison.kitpvp.core.util.Todo;
import us.supremeprison.kitpvp.core.util.config.ClassConfig;
import us.supremeprison.kitpvp.core.util.hologram.HologramManager;
import us.supremeprison.kitpvp.core.util.inventory.InventoryListener;
import us.supremeprison.kitpvp.core.util.inventory.OpenInventoryData;
import us.supremeprison.kitpvp.core.util.messages.Form;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Connor Hollasch
 * @since 3/9/2015
 */
public class KitPvP extends JavaPlugin {

    private static final String MODULE_START = "MODULES.%.";

    @Getter
    protected static KitPvP plugin_instance;

    @Getter
    private HashSet<Module> modules = new HashSet<>();

    @Getter
    private MySQLConnectionPool connection_pool;

    @Getter
    private MySQLDatabaseInformation database_information;

    @Getter
    private HashMap<String, OpenInventoryData> open_inventories = new HashMap<>();

    @Getter
    private ClassConfig configuration_manager = new ClassConfig();

    public void onEnable() {
        plugin_instance = this;

        saveDefaultConfig();
        reloadConfig();

        createMessageFormatting();

        configuration_manager.setWrapped_plugin(this);

        new DynamicCommandRegistry();
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);

        User.createUserListener();

        database_information = new MySQLDatabaseInformation();
        configuration_manager.loadAll(database_information);

        connection_pool = new MySQLConnectionPool(this, database_information);
        MySQLVars.CREATE_ATTACHMENT_TABLE.executeQuery();

        try {
            reloadPluginModules();
        } catch (Exception e) {
            e.printStackTrace();
        }

        printTodos();

        DynamicCommandRegistry.registerCommand(new CommandModule("kitreload", new String[]{"kitpvpreload", "kprl", "kitpvprl", "krl"}, true, "kitpvp.reload") {
            @Override
            public void onCommand(CommandSender sender, String[] args) {
                try {
                    reloadPluginModules();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aKitPvP&7] &fReloaded!"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createMessageFormatting() {
        Form.insertTag("error", "&c&l(!) &7%msg%");
        Form.insertTag("rank", "&9&lRank&7: &e%msg%");
        Form.insertTag("eco", "&a&lEco&7: &e%msg%");
        Form.insertTag("info", "&e&l(!) &7%msg%");
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

        HologramManager.disable();

        connection_pool.closeConnections();
        HandlerList.unregisterAll(this);

        if (modules.size() > 0) {
            for (Module key : modules) {
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
            for (Module key : modules) {
                key.onDisable();
                HandlerList.unregisterAll(key);
            }

            modules.clear();
        }

        List<Class<Module>> module_classes = ReflectionHandler.getClassesInPackage("us.supremeprison.kitpvp.modules", Module.class);
        CopyOnWriteArrayList<Class<Module>> concurrent_modules = new CopyOnWriteArrayList<>();
        concurrent_modules.addAll(module_classes);
        loadExcessModules(concurrent_modules);
    }

    private void loadExcessModules(CopyOnWriteArrayList<Class<Module>> module_classes) throws Exception {
        forClassModule:
        for (Class<? extends Module> module : module_classes) {
            ModuleDependency dependencies = module.getAnnotation(ModuleDependency.class);
            if (dependencies != null) {
                String[] all = dependencies.depends_on();
                for (String dependency : all) {
                    if (dependency.equalsIgnoreCase(module.getSimpleName()))
                        continue;

                    boolean found = false;
                    forModule:
                    for (Module loaded : this.modules) {
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

            logMessage(instance, "Module loading...");

            getConfiguration_manager().loadAll(instance, MODULE_START.replace("%", instance.getModule_name().toUpperCase()));

            instance.parent_plugin = this;
            instance.onEnable();
            Bukkit.getPluginManager().registerEvents(instance, this);

            this.modules.add(instance);
            module_classes.remove(module);

            logMessage(instance, "Module loaded!");
        }

        if (module_classes.size() == 0)
            return;
        else
            loadExcessModules(module_classes);
    }
}
