package us.supremeprison.kitpvp.modules.DeathHandler;

import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.Immutable;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;

import java.util.ArrayList;

/**
 * @author Connor Hollasch
 * @since 3/18/2015
 */
@Immutable(from = Immutable.From.ALL)
@ModuleDependency
public class DeathHandler extends Module {

    @ConfigOption("DEATH-MESSAGE")
    protected ArrayList<String> death_message_formatting = new ArrayList<String>() {
        {
            add("&f&m*---=&7&m----&8&m----------&f&m*-*&r &a&lYou Died! &f&m*-*&8&m----------&7&m----&f&m=---*");
            add("");
            add("{feed}");
            add("");
            add("&f&m*---=&7&m----&8&m----------&f&m*-*&r &a&lYou Died! &f&m*-*&8&m----------&7&m----&f&m=---*");
        }
    };

    @Override
    public void onEnable() {
        parent_plugin.getServer().getPluginManager().registerEvents(new DamageHandler(this), parent_plugin);
    }
}
