package us.supremeprison.kitpvp.modules.Killstreak;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.supremeprison.kitpvp.core.command.CommandModule;
import us.supremeprison.kitpvp.core.command.DynamicCommandRegistry;
import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.module.modifiers.ModuleDependency;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.user.attachment.Attachment;
import us.supremeprison.kitpvp.core.util.Common;
import us.supremeprison.kitpvp.core.util.ReflectionHandler;

import java.util.*;

/**
 * @author Connor Hollasch
 * @since 4/7/2015
 */
@ModuleDependency
public class Killstreak extends Module {

    private static final String KS_REWARD_ATTACHMENT = "killstreak_rewards";

    @Getter
    private static Killstreak module_instance;

    @Getter
    private LinkedHashMap<KillstreakReward, Integer> killstreak_rewards_sorted = new LinkedHashMap<>();

    @Override
    public void onEnable() {
        module_instance = this;

        Attachment<List<KillstreakReward>> killstreak_rewards_attachment = new Attachment<List<KillstreakReward>>(KS_REWARD_ATTACHMENT, new ArrayList<KillstreakReward>()) {
            @Override
            public String serialize(List<KillstreakReward> value) {
                String compile = "";

                for (KillstreakReward key : value) {
                    if (key == null)
                        continue;

                    compile += (key.getName() + ",");
                }

                if (compile.length() == 0)
                    return compile;

                if (compile.endsWith(","))
                    compile = compile.substring(0, compile.length()-1);

                return compile;
            }

            @Override
            public List<KillstreakReward> deserialize(String in) {
                List<KillstreakReward> rewards = Lists.newArrayList();
                for (String key : in.split(",")) {
                    rewards.add(getKillstreak(key));
                }
                return rewards;
            }
        };
        User.getAttachments_manager().put(killstreak_rewards_attachment);

        DynamicCommandRegistry.registerCommand(new CommandModule("killstreak", new String[]{"ks", "kstreak"}, false) {
            public void onCommand(CommandSender sender, String[] args) {
                Player player = (Player) sender;
                new KillstreakSelectorInventory(player).openInventory();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lKillstreak selector opened!"));
            }
        });
    }

    public void addKillstreakReward(KillstreakReward reward) {
        killstreak_rewards_sorted.put(reward, reward.getKills());
        killstreak_rewards_sorted = Common.sortHashMapByValues(killstreak_rewards_sorted);

        System.out.println(killstreak_rewards_sorted);
    }

    public HashMap<KillstreakReward, Integer> getPlayer_killstreak_rewards(Player player) {
        HashMap<KillstreakReward, Integer> rewards = new HashMap<>();
        for (KillstreakReward reward : (List<KillstreakReward>) User.fromPlayer(player).getAttachments().getAttachment(KS_REWARD_ATTACHMENT)) {
            rewards.put(reward, killstreak_rewards_sorted.get(reward));
        }
        return rewards;
    }

    public void addKill(Player player) {
        User user = User.fromPlayer(player);
        verifyUserdata(user);
        int ks = (int) user.getUserdata().get("killstreak") + 1;

        user.getUserdata().put("killstreak", ks);

        KillstreakReward reward = getExactReward(ks);

        List<KillstreakReward> user_rewards = user.getAttachments().getAttachment(KS_REWARD_ATTACHMENT);
        if (user_rewards.contains(reward)) {
            reward.giveToPlayer(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lKillstreak&7: &fYou received a &e" + reward.getName() + "&f!"));
        }
    }

    public void reset(Player player) {
        User.fromPlayer(player).getUserdata().put("killstreak", 0);
    }

    public int getKillstreak(Player player) {
        User user = User.fromPlayer(player);
        verifyUserdata(user);
        return user.getUserdata().get("killstreak");
    }

    public Collection<KillstreakReward> getAllAbove(int level) {
        List<KillstreakReward> rewards = Lists.newArrayList();
        for (KillstreakReward key : killstreak_rewards_sorted.keySet()) {
            int value = killstreak_rewards_sorted.get(key);
            if (value >= level)
                rewards.add(key);
        }
        return rewards;
    }

    public KillstreakReward getKillstreak(String name) {
        for (KillstreakReward reward : killstreak_rewards_sorted.keySet()) {
            if (reward.getName().equals(name))
                return reward;
        }
        return null;
    }

    public KillstreakReward getExactReward(int level) {
        for (KillstreakReward key : killstreak_rewards_sorted.keySet()) {
            int value = killstreak_rewards_sorted.get(key);
            if (value == level)
                return key;
        }
        return null;
    }

    private void verifyUserdata(User user) {
        if (!user.getUserdata().contains("killstreak")) {
            user.getUserdata().put("killstreak", 0);
        }
    }
}
