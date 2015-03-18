package kitpvp.core.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import kitpvp.core.user.User;

/**
 * @author Connor Hollasch
 * @since 3/11/2015
 */
public class UserInitializeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private User user;

    public UserInitializeEvent(User user) {
        this.user = user;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
