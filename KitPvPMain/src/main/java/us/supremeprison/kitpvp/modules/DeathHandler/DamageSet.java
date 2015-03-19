package us.supremeprison.kitpvp.modules.DeathHandler;

import org.bukkit.entity.Player;
import us.supremeprison.kitpvp.core.util.Damager;

import java.util.*;

/**
 * @author Connor Hollasch
 * @since 3/18/2015
 */
public class DamageSet {

    private LinkedList<Damager> all_dealt_damage = new LinkedList<>();

    public void addDamage(Damager damager) {

        //Max of 20 recent damage causes
        if (all_dealt_damage.size() > 20) {
            while (all_dealt_damage.size() > 20)
                all_dealt_damage.pop();
        }

        all_dealt_damage.add(damager);
    }

    public Collection<Damager> getAllDamageCauses() {
        return all_dealt_damage;
    }

    public Player getKiller() {
        return all_dealt_damage.peek().getDamager();
    }

    public HashMap<Player, Double> getAssistors() {
        HashMap<Player, Double> assists = new HashMap<>();
        Iterator<Damager> damage_iterator = all_dealt_damage.iterator();

        while (damage_iterator.hasNext()) {
            Damager next = damage_iterator.next();

            if (assists.containsKey(next.getDamager())) {
                double total = assists.remove(next.getDamager());
                total += next.getDamage();

                assists.put(next.getDamager(), total);
            }
            else
                assists.put(next.getDamager(), next.getDamage());
        }

        return assists;
    }
}
