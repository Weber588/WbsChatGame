package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.Game;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class CurrentCommand extends WbsSubcommand {
    public CurrentCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "current");
        addAlias("question");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Game currentGame = GameController.getCurrentGame();

        if (currentGame == null) {
            if (GameController.isRunning()) {
                sendMessage("There is no question pending right now.", sender);
                sendMessage("There are &h" + GameController.timeToNextRound() + "&r until the next round starts.", sender);
            } else {
                sendMessage("The game is not running right now.", sender);
            }
        } else {
            currentGame.getCurrentQuestion().send(sender);
        }

        return true;
    }
}
