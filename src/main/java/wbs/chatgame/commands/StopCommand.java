package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.GameController;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class StopCommand extends WbsSubcommand {
    public StopCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "stop");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        if (GameController.stop()) {
            GameController.setForceStopped(true);
            sendMessage("Game stopped!", sender);
        } else {
            sendMessage("&wGame was not running!", sender);
        }

        return true;
    }
}
