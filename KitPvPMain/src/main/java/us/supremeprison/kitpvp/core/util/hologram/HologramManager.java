package us.supremeprison.kitpvp.core.util.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import lombok.Getter;
import org.bukkit.Location;
import us.supremeprison.kitpvp.core.KitPvP;

import java.util.HashSet;

/**
 * @author Connor Hollasch
 * @since 3/12/2015
 */
public class HologramManager {

    @Getter
    private static HashSet<Hologram> holograms = new HashSet<>();

    public static Hologram buildHologram(Location location, String... lines) {
        Hologram holo = HologramsAPI.createHologram(KitPvP.getPlugin_instance(), location);
        for (String line : lines) {
            holo.appendTextLine(line);
        }
        holograms.add(holo);
        return holo;
    }

    public static void removeHologram(Hologram hologram) {
        hologram.delete();
        holograms.remove(hologram);
    }

    public static void disable() {
        for (Hologram h : holograms) {
            h.delete();
        }

        holograms.clear();
    }
}
