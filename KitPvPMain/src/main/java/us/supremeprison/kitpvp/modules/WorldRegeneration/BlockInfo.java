package us.supremeprison.kitpvp.modules.WorldRegeneration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Arrays;

/**
 * @author Connor Hollasch
 * @since 5/28/2015
 */
public class BlockInfo {

    private World world;
    private int[] xyz;

    private Material type;
    private byte data;

    public BlockInfo(Material type, byte data, Location location) {
        this.type = type;
        this.data = data;

        world = location.getWorld();
        xyz = new int[3];

        xyz[0] = location.getBlockX();
        xyz[1] = location.getBlockY();
        xyz[2] = location.getBlockZ();
    }

    public Material getType() {
        return type;
    }

    public byte getData() {
        return data;
    }

    public World getWorld() {
        return world;
    }

    public Location getLocation() {
        return new Location(world, xyz[0], xyz[1], xyz[2]);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;

        if (!(other instanceof BlockInfo))
            return false;

        BlockInfo obj = (BlockInfo) other;

        boolean worldName = world.getName().equals(obj.getWorld().getName());
        boolean loc = xyz.equals(obj.xyz);
        boolean type = obj.type.equals(this.type) && obj.data == data;

        return (worldName && loc && type);
    }

    @Override
    public String toString() {
        return Arrays.toString(xyz) + " -> (" + type.toString() + ", " + data + ")";
    }
}
