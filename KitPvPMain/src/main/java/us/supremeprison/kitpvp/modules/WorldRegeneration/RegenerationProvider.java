package us.supremeprison.kitpvp.modules.WorldRegeneration;

import org.bukkit.Location;
import org.bukkit.block.Block;
import us.supremeprison.kitpvp.core.util.ParticleEffect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Connor Hollasch
 * @since 5/28/2015
 */
public class RegenerationProvider {

    protected static Object lock = new Object();

    public static int RESET_PER_SECOND = 100;
    private static transient HashMap<Integer, BlockRegenSet> blockData = new HashMap<>();

    private static Set<BlockRegenSet> beganResetting = new HashSet<>();

    public static void scheduleForRegeneration(Block block, int secondsFromNow) {
        synchronized (lock) {
            BlockInfo info = new BlockInfo(block.getType(), block.getData(), block.getLocation());

            int time = getTime(secondsFromNow);

            if (blockData.containsKey(time)) {
                BlockRegenSet set = blockData.remove(time);
                set.add(info);
                blockData.put(time, set);

                return;
            }

            BlockRegenSet set = new BlockRegenSet();
            set.add(info);
            blockData.put(time, set);
        }
    }

    public static void scheduleForRegeneration(int secondsFromNow, Block... blocks) {
        for (Block block : blocks) {
            scheduleForRegeneration(block, secondsFromNow);
        }
    }

    protected static void particles(Location location) {
        ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(location.getBlock().getType(),
                location.getBlock().getData()), 0.25f, 0.25f, 0.25f, 1, 30, location.clone().add(0.5, 0.5, 0.5));
    }

    protected static void reset(int time) {
        synchronized (lock) {
            resetOld();

            if (!blockData.containsKey(time))
                return;

            BlockRegenSet set = blockData.remove(time);
            if (set.size() > RESET_PER_SECOND) {
                beganResetting.add(set);
            }

            set.regenerate(RESET_PER_SECOND);
        }
    }

    private static void resetOld() {
        Set<BlockRegenSet> remove = new HashSet<>();

        for (BlockRegenSet set : beganResetting) {
            if (set.size() <= RESET_PER_SECOND) {
                remove.add(set);
            }

            set.regenerate(RESET_PER_SECOND);
        }

        beganResetting.removeAll(remove);
    }

    protected static void resetAll() {
        for (BlockRegenSet all : blockData.values()) {
            all.regenerate(all.size());
        }
    }

    private static int getTime(int seconds) {
        return Regeneration.currentTime + seconds;
    }
}
