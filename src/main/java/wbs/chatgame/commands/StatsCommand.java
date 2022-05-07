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
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class StatsCommand extends AbstractLookupCommand {
    public StatsCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "stats");
        addAlias("points");
    }

    @Override
    protected void afterLookup(PlayerRecord player, CommandSender sender, String[] args) {
        Game game = null;
        if (args.length > 2) {
            String gameName = args[2];
            game = GameManager.getGame(gameName);
            if (game == null) {
                sendMessage("Invalid game \"&h" + gameName + "&r\". Please choose from the following: " +
                        GameManager.getGames().stream()
                                .map(Game::getGameName)
                                .collect(Collectors.joining(", ")), sender);
                return;
            }
        }

        if (game != null) {
            sendMessageNoPrefix("&h" + player.getName() + "&r's stats in &h" + WbsStrings.capitalizeAll(game.getGameName()) + "&r:", sender);

            plugin.buildMessageNoPrefix("Total points: &h" + player.getPoints(game, TrackedPeriod.TOTAL))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.TOTAL))
                    .addClickCommand("/chatgame breakdown " + player.getUUID() + " " + TrackedPeriod.TOTAL)
                    .send(sender);

            plugin.buildMessageNoPrefix("Points this month: &h" + player.getPoints(game, TrackedPeriod.MONTHLY))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.MONTHLY))
                    .addClickCommand("/chatgame breakdown " + player.getUUID() + " " + TrackedPeriod.MONTHLY)
                    .send(sender);

            plugin.buildMessageNoPrefix("Points this week: &h" + player.getPoints(game, TrackedPeriod.WEEKLY))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.WEEKLY))
                    .addClickCommand("/chatgame breakdown " + player.getUUID() + " " + TrackedPeriod.WEEKLY)
                    .send(sender);
        } else {
            sendMessageNoPrefix("&h" + player.getName() + "&r's stats:", sender);

            plugin.buildMessageNoPrefix("Total points: &h" + player.getPoints(TrackedPeriod.TOTAL))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.TOTAL))
                    .addClickCommand("/chatgame breakdown " + player.getUUID() + " " + TrackedPeriod.TOTAL)
                    .send(sender);

            plugin.buildMessageNoPrefix("Points this month: &h" + player.getPoints(TrackedPeriod.MONTHLY))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.MONTHLY))
                    .addClickCommand("/chatgame breakdown " + player.getUUID() + " " + TrackedPeriod.MONTHLY)
                    .send(sender);

            plugin.buildMessageNoPrefix("Points this week: &h" + player.getPoints(TrackedPeriod.WEEKLY))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.WEEKLY))
                    .addClickCommand("/chatgame breakdown " + player.getUUID() + " " + TrackedPeriod.WEEKLY)
                    .send(sender);
        }
    }

    private String getStatsBreakdownString(PlayerRecord player, TrackedPeriod period) {
        int totalPoints = player.getPoints(period);

        return "&h" + WbsEnums.toPrettyString(period) + "\n" + GameManager.getGames().stream()
                .map(game -> {
                    int points = player.getPoints(game, period);
                    double percent = (double) points / totalPoints * 100;
                    percent = WbsMath.roundTo(percent, 2);
                    return "&6" + WbsStrings.capitalizeAll(game.getGameName()) + "&r: &h" + player.getPoints(game, period) + "&r (" + percent + "%)";
                })
                .collect(Collectors.joining("\n"));
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> choices = new LinkedList<>();

        switch (args.length - start + 1) {
            case 1 -> Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).forEach(choices::add);
            case 2 -> GameManager.getGames().stream().map(Game::getGameName).forEach(choices::add);
        }

        return choices;
    }
}
