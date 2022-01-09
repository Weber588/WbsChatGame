package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NextCommand extends WbsSubcommand {
    public NextCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "next");
        addAlias("custom");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Game game;

        if (args.length >= 2) {
            game = GameManager.getGame(args[1]);
            if (game == null) {
                String gameList = GameManager.getGames().stream().map(Game::getGameName).collect(Collectors.joining(", "));
                sendMessage("Game not found: &h" + args[1] + "&r. Please choose from the following: &h" + gameList, sender);
                return true;
            }
        } else {
            sendMessage("Usage: &h/" + label + " " + args[0] + " <game> [options]", sender);
            return true;
        }

        List<String> options = new LinkedList<>();
        if (args.length > 2) {
            options.addAll(Arrays.asList(Arrays.copyOfRange(args, 2, args.length)));
        }

        GameController.setNext(game);
        GameController.setLastNextSender(sender);
        if (!options.isEmpty()) {
            GameController.setNext(options);
        }

        sendMessage("The next round will be " + game.getGameName() + ".", sender);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 2) {
            return GameManager.getGames().stream().map(Game::getGameName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
