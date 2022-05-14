package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.controller.GameQueue;
import wbs.chatgame.games.Game;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;

public class NextCommand extends AbstractNextCommand {
    public NextCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "next");
        addAlias("custom");

        isGameMandatory = false;
    }

    @Override
    protected void noGameSpecified(CommandSender sender, String label, String[] args) {
        GameQueue.RunnableInstance nextGame = GameController.getGameQueue().getQueue().peek();
        if (nextGame != null) {
            sendMessage("The next game will be " + nextGame.game() + ".", sender);
        }
    }

    @Override
    protected void afterNext(CommandSender sender, String label, String[] args, Game game, List<String> options) {
        if (game != null) {
            sendMessage("The next round will be " + game.getGameName() + ".", sender);
        }
    }
}
