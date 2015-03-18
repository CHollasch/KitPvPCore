package us.supremeprison.kitpvp.modules;

import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
@SuppressWarnings("unused")
@ModuleDependency
public class GameRules extends Module {

    @ConfigOption(configuration_section = "GAME-RULES")
    private List<String> game_rules = new ArrayList<String>() {
        {
            add("doMobSpawning:false");
        }
    };

    @Override
    public void onEnable() {
        for (String game_rule : game_rules) {
            for (World world : parent_plugin.getServer().getWorlds()) {
                world.setGameRuleValue(game_rule.split(":")[0], game_rule.split(":")[1]);
                parent_plugin.logMessage(this, "Set game rule &d" + game_rule.split(":")[0] + "&e to &d" + game_rule.split(":")[1] + " &efor world &6" + world.getName());
            }
        }
    }
}
