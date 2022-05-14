package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.controller.GameQueue;
import wbs.chatgame.games.Game;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;

public class GameLockCommand extends AbstractNextCommand {
    public GameLockCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "lock");
    }

    @Override
    protected boolean beforeStart(CommandSender sender, String label, String[] args) {
        GameQueue queue = GameController.getGameQueue();
        if (queue.isLocked()) {
            sendMessage("Game is already locked as &h" + queue.getLockedGame() + "&r. Use &h/" + label + " unlock&r to change it.", sender);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void sendMandatoryGameMessage(CommandSender sender, String label, String[] args) {
        sendMessage("Set the game type (and, optionally, additional options) for the next round and all future " +
                "rounds until &h/" + label + " unlock&r is used.", sender);
        sendMessage("Usage: &h/" + label + " " + args[0] + " <game> [options]", sender);
    }

    @Override
    protected void afterNext(CommandSender sender, String label, String[] args, Game game, List<String> options) {
        GameController.getGameQueue().lockNext();
        sendMessage("All rounds will be &h" + game.getGameName() + "&r until &h/" + label + " unlock&r is used.", sender);
    }
}
