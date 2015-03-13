package us.supremeprison.kitpvp.modules.Economy;

import org.bukkit.Material;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.module.modifiers.Immutable;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;

import java.util.HashMap;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
@SuppressWarnings("unused")
@Immutable(from = Immutable.From.ALL)
@ModuleDependency
public class Economy extends Module {

    @ConfigOption(configuration_section = "ITEM-WORTH")
    private HashMap<String, Double> item_worth = new HashMap<String, Double>() {
        {
            put(Material.IRON_INGOT.toString(), 25000.0);
            put(Material.GOLD_INGOT.toString(), 100000.0);
            put(Material.DIAMOND.toString(), 500000.0);
            put(Material.EMERALD.toString(), 1000000.0);
            put(Material.NETHER_STAR.toString(), 100000000.0);
        }
    };

    @Override
    public void onEnable() {
        //Attach economy modules to players
    }
}
