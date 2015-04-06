package us.supremeprison.kitpvp.modules.Kits;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.supremeprison.kitpvp.modules.Rank.Rank;

/**
 * @author Connor Hollasch
 * @since 3/25/2015
 */
public class Kit {

    @Getter
    private ItemStack[] contents;

    @Getter
    private ItemStack[] armor;

    @Getter
    private Integer required_rank;

    @Getter
    private String required_permission;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private double cost;

    private Kit(ItemStack[] contents, ItemStack[] armor) {
        this.contents = contents;
        this.armor = armor;
    }

    public Kit(ItemStack[] contents, ItemStack[] armor, int required_rank) {
        this(contents, armor);
        this.required_rank = required_rank;
    }

    public Kit(ItemStack[] contents, ItemStack[] armor, String required_permission) {
        this(contents, armor);
        this.required_permission = required_permission;
    }

    public boolean canUse(Player player) {
        if (required_permission != null) {
            //Permission based
            return player.hasPermission(required_permission);
        } else if (required_rank != null) {
            //Rank based
            return Rank.getRank(player) >= required_rank;
        } else {
            //Unknown
            return false;
        }
    }
}
