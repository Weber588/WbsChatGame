package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.data.ChatGameDB;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class SaveCacheCommand extends WbsSubcommand {
    public SaveCacheCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "savecache");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        // TODO: Auto save at intervals OR after wins
        ChatGameDB.getPlayerManager().saveCache();

      //  ChatGameDB.getPlayerManager().get(((Player) sender).getUniqueId()).toRecord().upsert(ChatGameDB.playerTable);

        return true;
    }
}
