package us.supremeprison.kitpvp.core.module;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
public abstract class Module implements Listener {

    protected Plugin parent_plugin;

    @Getter
    @Setter
    protected String module_name;

    @Getter
    @Setter
    private boolean isEnabled = false;

    public void onEnable() {}
    public void onDisable() {}
}
