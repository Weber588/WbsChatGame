package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class StartCommand extends WbsSubcommand {
    public StartCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "start");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        if (GameController.start()) {
            sendMessage("Game started!", sender);
        } else {
            sendMessage("&wGame was already running!", sender);
        }

        return true;
    }
}
