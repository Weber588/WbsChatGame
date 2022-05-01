package wbs.chatgame.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.data.PlayerRecord;
import wbs.chatgame.data.TrackedPeriod;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatsCommand extends WbsSubcommand {
    public StatsCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "stats");
        addAlias("points");
        addAlias("info");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        String usernameOrUUID;
        if (args.length > 1) {
            usernameOrUUID = args[1];
        } else {
            if (!(sender instanceof Player player)) {
                sendMessage("Non-players must specify a username.", sender);
                return true;
            }

            usernameOrUUID = player.getUniqueId().toString();
        }
        UUID checkUUID = null;
        try {
            checkUUID = UUID.fromString(usernameOrUUID);
        } catch (IllegalArgumentException ignored) {}

        String username = usernameOrUUID;
        if (checkUUID != null) {
            ChatGameDB.getPlayerManager().getAsync(checkUUID, record -> {
                if (record != null) {
                    showStats(record, sender, args);
                } else {
                    ChatGameDB.getPlayerManager().getUUIDsAsync(username, uuids -> tryAfterUsername(uuids, sender, args));
                }
            });
        } else {
            ChatGameDB.getPlayerManager().getUUIDsAsync(username, uuids -> tryAfterUsername(uuids, sender, args));
        }

        return true;
    }

    private void tryAfterUsername(List<UUID> uuids, CommandSender sender, String[] args) {
        if (uuids.isEmpty()) {
            sendMessage("User not found!", sender);
        } else if (uuids.size() > 1) {
            sendMessage("Multiple users found! Please choose from the following UUIDs: ",
                    sender);
            int index = 1;
            for (UUID uuid : uuids) {
                plugin.buildMessage("&h" + index + ") " + uuid.toString())
                        .addHoverText("&hClick to view stats!")
                        .addClickCommand("/chatgame stats " + uuid)
                        .send();
                index++;
            }
        } else {
            ChatGameDB.getPlayerManager().getAsync(uuids.get(0), record -> showStats(record, sender, args));
        }
    }

    private void showStats(PlayerRecord player, CommandSender sender, String[] args) {
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
            sendMessageNoPrefix("&h" + player.getName() + "&r's stats in &h" + game.getGameName() + "&r:", sender);

            plugin.buildMessageNoPrefix("Total points: &h" + player.getPoints(game, TrackedPeriod.TOTAL))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.TOTAL))
                    .send(sender);

            plugin.buildMessageNoPrefix("Points this month: &h" + player.getPoints(game, TrackedPeriod.MONTHLY))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.MONTHLY))
                    .send(sender);

            plugin.buildMessageNoPrefix("Points this week: &h" + player.getPoints(game, TrackedPeriod.WEEKLY))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.WEEKLY))
                    .send(sender);
        } else {
            sendMessageNoPrefix("&h" + player.getName() + "&r's stats:", sender);

            plugin.buildMessageNoPrefix("Total points: &h" + player.getPoints(TrackedPeriod.TOTAL))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.TOTAL))
                    .send(sender);

            plugin.buildMessageNoPrefix("Points this month: &h" + player.getPoints(TrackedPeriod.MONTHLY))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.MONTHLY))
                    .send(sender);

            plugin.buildMessageNoPrefix("Points this week: &h" + player.getPoints(TrackedPeriod.WEEKLY))
                    .addHoverText(getStatsBreakdownString(player, TrackedPeriod.WEEKLY))
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
                    return "&6" + game.getGameName() + "&r: &h" + player.getPoints(game, period) + "&r (" + percent + "%)";
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
