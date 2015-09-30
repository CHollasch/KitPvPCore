package us.supremeprison.kitpvp.modules.Throwables;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.supremeprison.kitpvp.core.command.CommandModule;
import us.supremeprison.kitpvp.core.command.DynamicCommandRegistry;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.Todo;
import us.supremeprison.kitpvp.core.util.messages.Form;
import us.supremeprison.kitpvp.modules.Killstreak.Killstreak;
import us.supremeprison.kitpvp.modules.Throwables.modules.ArrowGrenade;
import us.supremeprison.kitpvp.modules.Throwables.modules.StarfieldBomb;

import java.util.HashMap;

/**
 * @author Connor Hollasch
 * @since 4/6/2015
 */
@SuppressWarnings("unused")
@ModuleDependency(depends_on = {"modulemanager", "killstreak"})
@Todo("More \"perks\" or throwables please")
public class Throwables extends Module {

    private HashMap<Material, ThrowableItem> throwables = new HashMap<Material, ThrowableItem>() {
        {
            put(Material.NETHER_STAR, new StarfieldBomb());
            put(Material.SUGAR, new ArrowGrenade());
        }
    };

    @Override
    public void onEnable() {
        Killstreak.getModule_instance().addKillstreakReward(new StarfieldBomb.StarfieldBombReward());
        Killstreak.getModule_instance().addKillstreakReward(new ArrowGrenade.ArrowGrenadeReward());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack holding = player.getItemInHand();

        if (holding == null)
            return;

        if (throwables.containsKey(holding.getType())) {
            if (!throwables.get(holding.getType()).canCreate()) {
                Form.at(player, "error", "You cannot use this item right now.");
                return;
            }

            //Is throwable material
            Common.removeOneInHand(player);

            ItemStack stack = holding.clone();
            stack.setAmount(1);

            Item item = player.getWorld().dropItem(player.getEyeLocation(), stack);
            item.setPickupDelay(20 * 3600);
            item.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(1.3));

            throwables.get(holding.getType()).onCreate(player, item);
        }
    }
}
