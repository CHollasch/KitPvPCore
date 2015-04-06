package us.supremeprison.kitpvp.core.util;

import org.bukkit.entity.Player;

/**
 * @author Connor Hollasch
 * @since 3/17/2015
 */
public interface Damager {

    public Player getDamager();

    public double getDamage();

    public String getDescription();

    public class Util {

        public static Damager createNewDamageEvenet(final Player damager, final double damage, final String description) {
            final Damager damager_event = new Damager() {
                @Override
                public Player getDamager() {
                    return damager;
                }

                @Override
                public double getDamage() {
                    return damage;
                }

                @Override
                public String getDescription() {
                    return description;
                }

                @Override
                public String toString() {
                    if (damager == null)
                        return damage + " dealt (" + description + ")";

                    return damager.getName() + " dealt " + damage + " damage (" + description + ")";
                }
            };
            return damager_event;
        }

    }
}
