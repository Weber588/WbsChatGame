package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.data.PlayerRecord;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;
import java.util.UUID;

/**
 * Represents a command whose first arg is a username or UUID
 */
public abstract class AbstractLookupCommand extends WbsSubcommand {
    public AbstractLookupCommand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected final boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        String usernameOrUUID;
        if (args.length > 1) {
            usernameOrUUID = args[1];
        } else {
            if (!(sender instanceof Player player)) {
                sendMessage("Usage: /" + label + " " + args[0] + " <username/uuid>", sender);
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
                    afterLookup(record, sender, args);
                } else {
                    ChatGameDB.getPlayerManager().getUUIDsAsync(username, uuids -> tryAfterUsername(uuids, label, sender, args));
                }
            });
        } else {
            ChatGameDB.getPlayerManager().getUUIDsAsync(username, uuids -> tryAfterUsername(uuids, label, sender, args));
        }

        return true;
    }

    private void tryAfterUsername(List<UUID> uuids, String label, CommandSender sender, String[] args) {
        if (uuids.isEmpty()) {
            sendMessage("User not found!", sender);
        } else if (uuids.size() > 1) {
            sendMessage("Multiple users found! Please choose from the following UUIDs: ",
                    sender);
            int index = 1;
            for (UUID uuid : uuids) {
                plugin.buildMessage("&h" + index + ") " + uuid.toString())
                        .addHoverText("&hClick to choose!")
                        .addClickCommand("/" + label + " " + args[0] + " " + uuid)
                        .send();
                index++;
            }
        } else {
            ChatGameDB.getPlayerManager().getAsync(uuids.get(0), record -> afterLookup(record, sender, args));
        }
    }

    protected abstract void afterLookup(PlayerRecord player, CommandSender sender, String[] args);
}
