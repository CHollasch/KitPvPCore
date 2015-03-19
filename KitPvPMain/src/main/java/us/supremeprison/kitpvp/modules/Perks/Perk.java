package us.supremeprison.kitpvp.modules.Perks;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

/**
 * @author Connor Hollasch
 * @since 3/18/2015
 */
public abstract class Perk {

    protected Item thrown;

    public void onCreate() {}
    public void onDestroy() {}

    public abstract ItemStack getPerkItem();
}
