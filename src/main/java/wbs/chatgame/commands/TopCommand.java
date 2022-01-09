package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.data.GameStats;
import wbs.chatgame.data.PlayerRecord;
import wbs.chatgame.data.StatsManager;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TopCommand extends WbsSubcommand {
    public TopCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "top");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        GameStats.TrackedPeriod period = GameStats.TrackedPeriod.TOTAL;

        if (args.length > 1) {
            period = WbsEnums.getEnumFromString(GameStats.TrackedPeriod.class, args[1]);
        }

        if (period == null) {
            sendMessage("Invalid period: " + args[1]
                    + ". Please choose from the following: "
                    + WbsEnums.joiningPrettyStrings(GameStats.TrackedPeriod.class), sender);
            return true;
        }

        GameStats.TrackedPeriod finalPeriod = period;
        StatsManager.getTopAsync(period, (top) -> showTop(top, finalPeriod, sender));

        return true;
    }


    private void showTop(List<PlayerRecord> top, GameStats.TrackedPeriod period, CommandSender sender) {
        sendMessage("Top " + top.size() + " players (" + WbsEnums.toPrettyString(period) + "):", sender);

        int i = 1;
        for (PlayerRecord player : top) {
            sendMessage("&6" + i + ") &h" + player.getName() + "&r> &h" + player.getPoints(period), sender);
        }
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();

        if (args.length == 2) {
            Arrays.stream(GameStats.TrackedPeriod.values())
                    .map(period -> WbsEnums.toPrettyString(period).toLowerCase())
                    .forEach(choices::add);
        }

        return choices;
    }
}
