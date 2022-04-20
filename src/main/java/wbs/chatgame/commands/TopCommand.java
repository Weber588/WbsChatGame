package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.data.GameStats;
import wbs.chatgame.data.LeaderboardEntry;
import wbs.chatgame.data.PlayerRecord;
import wbs.chatgame.data.StatsManager;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

        Game game = null;
        if (args.length > 2) {
            String gameName = args[2];
            game = GameManager.getGame(gameName);

            if (game == null) {
                sendMessage("Invalid game \"&h" + gameName + "&r\". Please choose from the following: " +
                        GameManager.getGames().stream()
                                .map(Game::getGameName)
                                .collect(Collectors.joining(", ")), sender);
                return true;
            }
        }

        GameStats.TrackedPeriod finalPeriod = period;
        Game finalGame = game;

        if (game == null) {
            StatsManager.getTopAsync(period, (top) -> showTop(top, finalPeriod, null, sender));
        } else {
            StatsManager.getTopAsync(period, finalGame, (top) -> showTop(top, finalPeriod, finalGame, sender));
        }

        return true;
    }


    private void showTop(List<LeaderboardEntry> top, GameStats.TrackedPeriod period, @Nullable Game game, CommandSender sender) {
        if (game != null) {
            sendMessage("Top " + top.size() + " players for &h" + game.getGameName() + "&r (" + WbsEnums.toPrettyString(period) + "):", sender);

            int i = 1;
            for (LeaderboardEntry entry : top) {
                plugin.buildMessage("&6" + i + ") &h" + entry.name() + "&r> &h" + entry.points())
                        .addHoverText("&hClick to view full stats for " + entry.name() + "!")
                        .addClickCommand("/chatgame stats " + entry.uuid() + " " + game.getGameName())
                        .send(sender);
                i++;
            }
        } else {
            sendMessage("Top " + top.size() + " players (" + WbsEnums.toPrettyString(period) + "):", sender);

            int i = 1;
            for (LeaderboardEntry entry : top) {
                plugin.buildMessage("&6" + i + ") &h" + entry.name() + "&r> &h" + entry.points())
                        .addHoverText("&hClick to view full stats for " + entry.name() + "!")
                        .addClickCommand("/chatgame stats " + entry.uuid())
                        .send(sender);
                i++;
            }
        }
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> choices = new LinkedList<>();

        switch (args.length - start + 1) {
            case 1 -> Arrays.stream(GameStats.TrackedPeriod.values())
                    .map(period -> WbsEnums.toPrettyString(period).toLowerCase())
                    .forEach(choices::add);
            case 2 -> GameManager.getGames().stream()
                    .map(Game::getGameName)
                    .forEach(choices::add);
        }

        return choices;
    }
}
