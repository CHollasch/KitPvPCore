package us.supremeprison.kitpvp.core.module;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Listener;
import us.supremeprison.kitpvp.core.KitPvP;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
public abstract class Module implements Listener {

    public KitPvP parent_plugin;

    @Getter
    @Setter
    protected String module_name;

    @Getter
    @Setter
    private boolean isEnabled = false;

    public void onEnable() {}
    public void onDisable() {}
}
