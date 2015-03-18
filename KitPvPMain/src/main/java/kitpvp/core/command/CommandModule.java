package kitpvp.core.command;

import lombok.Getter;
import org.bukkit.command.CommandSender;

/**
 * @author Connor Hollasch
 * @since 3/12/2015
 */
public abstract class CommandModule {

    @Getter
    private String command;

    @Getter
    private String[] aliases;

    @Getter
    private boolean can_sender_be_console = true;

    @Getter
    private String permission;

    public CommandModule(String command) {
        this(command, new String[0]);
    }

    public CommandModule(String command, String[] aliases) {
        this(command, aliases, false);
    }

    public CommandModule(String command, String[] aliases, boolean can_sender_be_console) {
        this(command, aliases, can_sender_be_console, "kitpvp.user");
    }

    public CommandModule(String command, String[] aliases, boolean can_sender_be_console, String permission) {
        this.command = command;
        this.aliases = aliases;
        this.can_sender_be_console = can_sender_be_console;
        this.permission = permission;
    }

    public abstract void onCommand(CommandSender sender, String[] args);
}
