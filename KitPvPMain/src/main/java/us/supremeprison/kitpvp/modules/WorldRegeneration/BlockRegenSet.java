package us.supremeprison.kitpvp.modules.WorldRegeneration;

import org.bukkit.Location;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Connor Hollasch
 * @since 5/29/2015
 */
public class BlockRegenSet {

    private List<BlockInfo> blocksToReset = new ArrayList<>();

    public void add(BlockInfo info) {
        synchronized (RegenerationProvider.lock) {
            blocksToReset.add(info);
        }
    }

    public void remove(BlockInfo info) {
        blocksToReset.remove(info);
    }

    public BlockInfo remove(int index) {
        return blocksToReset.remove(index);
    }

    public BlockInfo get(int index) {
        return blocksToReset.get(index);
    }

    public void regenerate(int amount) {
        synchronized (RegenerationProvider.lock) {
            if (blocksToReset.size() == 0) {
                return;
            }

            int min = Math.min(amount, blocksToReset.size() - 1);

            for (int i = min; i >= 0; i--) {
                synchronized (blocksToReset) {
                    BlockInfo next = remove(i);

                    Location location = next.getLocation();

                    if (!location.getChunk().isLoaded()) {
                        location.getChunk().load();
                    }

                    location.getBlock().setType(next.getType());
                    location.getBlock().setData(next.getData());

                    RegenerationProvider.particles(location);
                    location.getWorld().playSound(location, Sound.STEP_STONE, 0.6f, 2f);
                }
            }
        }
    }

    public int size() {
        return blocksToReset.size();
    }

    @Override
    public int hashCode() {
        return blocksToReset.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof BlockRegenSet)) {
            return false;
        }

        BlockRegenSet other = (BlockRegenSet) obj;
        if (other.blocksToReset.size() != blocksToReset.size()) {
            return false;
        }

        for (int i = 0; i < blocksToReset.size(); i++) {
            if (!(blocksToReset.get(i).equals(other.blocksToReset.get(i)))) {
                return false;
            }
        }

        return true;
    }
}
