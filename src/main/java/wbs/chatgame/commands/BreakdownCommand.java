package wbs.chatgame.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.data.PlayerRecord;
import wbs.chatgame.data.TrackedPeriod;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.string.WbsStrings;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BreakdownCommand extends AbstractLookupCommand {
    public BreakdownCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "breakdown");
        addAlias("fullstats");
    }

    @Override
    protected void afterLookup(PlayerRecord player, CommandSender sender, String[] args) {
        TrackedPeriod period = TrackedPeriod.TOTAL;

        if (args.length > 2) {
            String periodString = args[2];
            period = WbsEnums.getEnumFromString(TrackedPeriod.class, periodString);
            if (period == null) {
                sendMessage("Invalid period \"&h" + periodString + "&r\". Please choose from the following: " +
                        Arrays.stream(TrackedPeriod.values())
                                .map(found -> WbsEnums.toPrettyString(found).toLowerCase())
                                .collect(Collectors.joining(", ")), sender);
                return;
            }
        }

        switch (period) {
            case TOTAL -> sendMessage("Breakdown of &h" + player.getName() + "&r:", sender);
            case MONTHLY -> sendMessage("Breakdown of &h" + player.getName() + "&r (this month):", sender);
            case WEEKLY -> sendMessage("Breakdown of &h" + player.getName() + "&r (this week):", sender);
        }

        sendMessageNoPrefix("Total points: &h" + player.getPoints(period), sender);

        int bestGamePoints = Integer.MIN_VALUE;
        List<Game> bestGames = new LinkedList<>();
        for (Game game : GameManager.getGames()) {
            int gamePoints = player.getPoints(game, period);
            if (gamePoints == bestGamePoints) {
                bestGames.add(game);
            } else if (gamePoints > bestGamePoints) {
                bestGamePoints = gamePoints;
                bestGames.clear();
                bestGames.add(game);
            }
        }

        if (bestGames.size() == 1) {
            sendMessageNoPrefix("Best game: &h" + WbsStrings.capitalizeAll(bestGames.get(0).getGameName()), sender);
        } else if (bestGames.size() == GameManager.getGames().size()) {
            sendMessageNoPrefix("Best game: &hN/A", sender);
        } else {
            sendMessageNoPrefix("Best games: &h" +
                    bestGames.stream()
                            .map(Game::getGameName)
                            .map(WbsStrings::capitalizeAll)
                            .collect(Collectors.joining(", ")), sender);
        }

        double bestGameReaction = player.getFastestTime(period);
        sendMessageNoPrefix("Fastest reaction time: " + getReactionString(bestGameReaction), sender);

        sendMessageNoPrefix("&6Game stats:", sender);
        for (Game game : GameManager.getGames()) {
            sendMessageNoPrefix("  &r" + WbsStrings.capitalizeAll(game.getGameName()) + "&r: " + getGameDisplay(player, game, period), sender);
        }
    }

    private String getGameDisplay(PlayerRecord player, Game game, TrackedPeriod period) {
        int points = player.getPoints(game, period);
        double percent = (double) points / player.getPoints(period) * 100;
        percent = WbsMath.roundTo(percent, 2);
        return "&h" + points + "&7 (" + percent + "%)";
    }

    private String getReactionString(double speed) {
        String speedString;

        if (speed != Double.MAX_VALUE) {
            speedString = WbsStringify.toString(Duration.ofMillis((long) (speed * 1000)), false);
        } else {
            speedString = "N/A";
        }

        return "&h" + speedString;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> choices = new LinkedList<>();

        switch (args.length - start + 1) {
            case 1 -> Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).forEach(choices::add);
            case 2 -> Arrays.stream(TrackedPeriod.values())
                    .map(period -> WbsEnums.toPrettyString(period).toLowerCase())
                    .forEach(choices::add);
        }

        return choices;
    }
}
