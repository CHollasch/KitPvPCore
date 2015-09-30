package us.supremeprison.kitpvp.modules.WorldRegeneration;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import us.supremeprison.kitpvp.core.KitPvP;
import us.supremeprison.kitpvp.core.command.CommandModule;
import us.supremeprison.kitpvp.core.command.DynamicCommandRegistry;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.Immutable;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.util.Todo;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;
import us.supremeprison.kitpvp.core.util.messages.Form;

/**
 * @author Connor Hollasch
 * @since 5/27/2015
 */
@ModuleDependency
@Immutable
@Todo("Add regions people can break by hand, etc...")
public class Regeneration extends Module {

    private static final String REGEN_META_TAG = "bypassregen";

    @ConfigOption("REGEN-TIME")
    public static int REGEN_TIME = 10;

    protected static Regeneration instance;
    protected static int currentTime = 0;
    private static int taskId;

    @Override
    public void onEnable() {
        instance = this;

        Runnable regenTask = new Runnable() {
            @Override
            public void run() {
                ++currentTime;
                RegenerationProvider.reset(currentTime);
            }
        };
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(KitPvP.getPlugin_instance(), regenTask, 20, 20);

        CommandModule command = new CommandModule("bypassregen", new String[]{"regenbypass", "byregen", "bregen", "bre", "brr"}, false, "kitpvp.bypass.regen") {
            @Override
            public void onCommand(CommandSender sender, String[] args) {
                User user = User.fromPlayer((Player) sender);

                if (user.getUserdata().contains(REGEN_META_TAG)) {
                    user.getUserdata().remove(REGEN_META_TAG);
                    Form.at(sender, "info", "You are no longer bypassing regeneration");
                    return;
                }

                user.getUserdata().put(REGEN_META_TAG, true);
                Form.at(sender, "info", "You are now bypassing world regeneration");
            }
        };
        DynamicCommandRegistry.registerCommand(command);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(taskId);
        RegenerationProvider.resetAll();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (User.fromPlayer(e.getPlayer()).getUserdata().contains(REGEN_META_TAG))
            return;

        e.setCancelled(true);

        RegenerationProvider.particles(e.getBlock().getLocation());
        RegenerationProvider.scheduleForRegeneration(e.getBlock(), REGEN_TIME);

        e.getBlock().setType(Material.AIR);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (User.fromPlayer(e.getPlayer()).getUserdata().contains(REGEN_META_TAG))
            return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        RegenerationProvider.scheduleForRegeneration(REGEN_TIME, event.blockList().toArray(new Block[0]));
    }
}
