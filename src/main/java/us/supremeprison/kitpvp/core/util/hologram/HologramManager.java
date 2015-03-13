package us.supremeprison.kitpvp.core.util.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import lombok.Getter;

import java.util.HashSet;

/**
 * @author Connor Hollasch
 * @since 3/12/2015
 */
public class HologramManager {

    @Getter
    private static HashSet<Hologram> holograms = new HashSet<>();
}
