package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.data.*;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
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
        Game game = null;
        TrackedPeriod period = null;
        int amount = 0;

        int nextArg = 1;

        if (args.length > nextArg) {
            game = GameManager.getGame(args[nextArg]);

            if (game != null) {
                nextArg++;
            }
        }

        if (args.length > nextArg) {
            period = WbsEnums.getEnumFromString(TrackedPeriod.class, args[nextArg]);
            if (period != null) {
                nextArg++;
            }
        }

        if (args.length > nextArg) {
            try {
                amount = Integer.parseInt(args[nextArg]);
            } catch (NumberFormatException e) {
                sendMessage("Invalid amount: " + args[nextArg] + ". " +
                        "Please use an integer.", sender);
                return true;
            }

            if (amount < 1 || amount > StatsManager.topListSize) {
                sendMessage("Invalid amount: " + args[nextArg] + ". " +
                        "Amount must be between 1 and " + StatsManager.topListSize, sender);
                return true;
            }
        }

        if (period == null) {
            period = TrackedPeriod.TOTAL;
        }

        if (amount <= 0) {
            amount = 5;
        }

        TrackedPeriod finalPeriod = period;
        Game finalGame = game;
        int finalAmount = amount;

        if (game == null) {
            StatsManager.getTopAsync(period, (top) -> showTop(top, finalPeriod, null, finalAmount, sender));
        } else {
            StatsManager.getTopAsync(period, finalGame, (top) -> showTop(top, finalPeriod, finalGame, finalAmount, sender));
        }

        return true;
    }


    private void showTop(Leaderboard top, TrackedPeriod period, @Nullable Game game, int amount, CommandSender sender) {
        if (game != null) {
            sendMessage("Top " + Math.min(amount, top.size()) + " players for &h" + game.getGameName() + "&r (" + WbsEnums.toPrettyString(period) + "):", sender);

            int i = 0;
            for (LeaderboardEntry entry : top) {
                if (i >= amount) break;
                i++;
                plugin.buildMessage("&6" + (entry.getPosition() + 1) + ") &h" + entry.name() + "&r> &h" + entry.points())
                        .addHoverText("&hClick to view full stats for " + entry.name() + "!")
                        .addClickCommand("/chatgame stats " + entry.uuid() + " " + game.getGameName())
                        .send(sender);
            }
        } else {
            sendMessage("Top " + Math.min(amount, top.size()) + " players (" + WbsEnums.toPrettyString(period) + "):", sender);

            int i = 0;
            for (LeaderboardEntry entry : top) {
                if (i >= amount) break;
                i++;
                plugin.buildMessage("&6" + (entry.getPosition() + 1) + ") &h" + entry.name() + "&r> &h" + entry.points())
                        .addHoverText("&hClick to view full stats for " + entry.name() + "!")
                        .addClickCommand("/chatgame stats " + entry.uuid())
                        .send(sender);
            }
        }
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> choices = new LinkedList<>();

        switch (args.length - start + 1) {
            case 1 -> GameManager.getGames().stream()
                    .map(Game::getGameName)
                    .forEach(choices::add);
            case 2 -> Arrays.stream(TrackedPeriod.values())
                    .map(period -> WbsEnums.toPrettyString(period).toLowerCase())
                    .forEach(choices::add);
        }

        return choices;
    }
}
