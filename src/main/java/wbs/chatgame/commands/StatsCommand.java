package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.data.GameStats;
import wbs.chatgame.data.PlayerManager;
import wbs.chatgame.data.PlayerRecord;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatsCommand extends WbsSubcommand {
    public StatsCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "stats");
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
        String username = null;
        try {
            checkUUID = UUID.fromString(usernameOrUUID);
        } catch (IllegalArgumentException e) {
            username = usernameOrUUID;
        }

        if (checkUUID != null) {
            ChatGameDB.getPlayerManager().getAsync(checkUUID, record -> showStats(record, sender));
        } else {
            ChatGameDB.getPlayerManager().getUUIDsAsync(username, uuids -> tryAfterUsername(uuids, sender));
        }

        return true;
    }

    private void tryAfterUsername(List<UUID> uuids, CommandSender sender) {
        if (uuids.isEmpty()) {
            sendMessage("User not found!", sender);
        } else if (uuids.size() > 1) {
            sendMessage("Multiple users found! Please choose from the following UUIDs: " +
                    uuids.stream().map(UUID::toString).collect(Collectors.joining(", ")),
                    sender);
        } else {
            ChatGameDB.getPlayerManager().getAsync(uuids.get(0), record -> showStats(record, sender));
        }
    }

    private void showStats(PlayerRecord player, CommandSender sender) {
        sendMessage("&h" + player.getName() + "&r's stats:", sender);
        sendMessage("Total points: &h" + player.getPoints(), sender);
        // TODO: Make it configurable for if month/week is tracked & shown
        sendMessage("Points this month: &h" + player.getPoints(GameStats.TrackedPeriod.MONTHLY), sender);
        sendMessage("Points this week: &h" + player.getPoints(GameStats.TrackedPeriod.WEEKLY), sender);
    }
}
