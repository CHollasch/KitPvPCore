package us.supremeprison.kitpvp.modules.Perks.modules;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.modules.Perks.Perk;

/**
 * @author Connor Hollasch
 * @since 3/18/2015
 */
public class StarfieldBomb extends Perk {

    @Override
    public void onCreate() {
        
    }

    @Override
    public void onDestroy() {

    }

    public ItemStack getPerkItem() {
        return Common.craftItem(Material.NETHER_STAR, "&f&l< &7Starfield Bomb &f&l>");
    }
}
