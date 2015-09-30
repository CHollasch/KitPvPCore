package us.supremeprison.kitpvp.core.util;

/**
 * @author Connor Hollasch
 * @since 3/17/2015
 */
public interface Damager {

    public String getDamager();

    public double getDamage();

    public String getDescription();

    public class Util {

        public static Damager createNewDamageEvenet(final String damager, final double damage, final String description) {
            final Damager damager_event = new Damager() {
                @Override
                public String getDamager() {
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

                    return damager + " dealt " + damage + " damage (" + description + ")";
                }
            };
            return damager_event;
        }

    }
}
