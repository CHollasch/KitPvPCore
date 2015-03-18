package us.supremeprison.kitpvp.modules;

import org.bukkit.event.HandlerList;
import us.supremeprison.kitpvp.core.KitPvP;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.Immutable;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
@SuppressWarnings("unused")
@Immutable
public class ModuleManager extends Module {

    @ConfigOption(configuration_section = "DISABLED-MODULES")
    private List<String> disabled_modules = new ArrayList<>();

    public void onEnable() {
        schedule(new Runnable() {
            public void run() {
                for (Module module : KitPvP.getPlugin_instance().getModules().keySet()) {
                    if (disabled_modules.contains(module.getModule_name())) {
                        if (checkImmutable(module.getClass()))
                            continue;

                        HandlerList.unregisterAll(module);
                        module.onDisable();
                    }
                }
            }
        }, 1);

    }

    private static boolean checkImmutable(Class<? extends Module> module_class) {
        if (module_class.getAnnotation(Immutable.class) != null) {
            Immutable annotation = module_class.getAnnotation(Immutable.class);
            if (annotation.from().getImmutableLevel() >= 2)
                return true;
        }

        return false;
    }
}
