package us.supremeprison.kitpvp.modules.DeathHandler;

import lombok.Getter;
import org.bukkit.entity.Player;
import us.supremeprison.kitpvp.core.util.Damager;

import java.util.*;

/**
 * @author Connor Hollasch
 * @since 3/18/2015
 */
public class DamageSet {

    private LinkedList<Damager> all_dealt_damage = new LinkedList<>();

    @Getter
    private double healed = 0.0;

    @Getter
    private double damaged = 0.0;

    public void addDamage(Damager damager) {
        //Max of 20 recent damage causes
        try {
            if (all_dealt_damage.size() >= 15) {
                while (all_dealt_damage.size() >= 15)
                    all_dealt_damage.pop();
            }
        } catch (NoSuchElementException ex) {
            all_dealt_damage = new LinkedList<>();
        }

        all_dealt_damage.add(damager);
        damaged += damager.getDamage();
    }

    public void addHealed(double healed) {
        this.healed += healed;
    }

    public Collection<Damager> getAllDamageCauses() {
        return all_dealt_damage;
    }

    public Player getKiller() {
        int index = 1;
        while (index <= all_dealt_damage.size()) {
            Damager next = all_dealt_damage.get(all_dealt_damage.size() - (index++));
            if (next.getDamager() == null)
                continue;

            return next.getDamager();
        }
        return null;
    }

    public double getDamage(Player player) {
        double all = 0.0;
        for (Damager dm : all_dealt_damage) {
            if (dm.getDamager() == null && player == null)
                all += dm.getDamage();
            else if (dm.getDamager() != null && dm.getDamager().equals(player))
                all += dm.getDamage();
        }
        return all;
    }

    public HashMap<Player, Double> getAssistors() {
        Player killer = getKiller();

        HashMap<Player, Double> assists = new HashMap<>();
        Iterator<Damager> damage_iterator = all_dealt_damage.iterator();

        while (damage_iterator.hasNext()) {
            Damager next = damage_iterator.next();
            if (next.getDamager() == null
                    || (killer != null && next.getDamager().equals(killer)))
                continue;

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

    public String toString() {
        String all = "";
        for (Damager damager : all_dealt_damage) {
            all += damager.toString() + "\n";
        }
        return all;
    }
}
