package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.rewards.RewardManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class RewardsCommand extends WbsSubcommand {
    public RewardsCommand(WbsPlugin plugin) {
        super(plugin, "reward");
        addAlias("rewards");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        plugin.sendMessage(RewardManager.rewardMessage, sender);
        return true;
    }
}
