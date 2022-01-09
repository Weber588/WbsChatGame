package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.data.PlayerRecord;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class IgnoreCommand extends WbsSubcommand {
    public IgnoreCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "ignore");

        addAlias("listen");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("Command is only usable by players.", sender);
            return true;
        }

        ChatGameDB.getPlayerManager().getAsync(((Player) sender).getUniqueId(), record -> updateListening(record, sender));

        return true;
    }

    private void updateListening(PlayerRecord record, CommandSender sender) {
        boolean listening = !record.isListening();
        record.setListening(listening);
        if (listening) {
            sendMessage("You are now listening to the game.", sender);
        } else {
            sendMessage("You are no longer listening to the game.", sender);
        }
    }
}
