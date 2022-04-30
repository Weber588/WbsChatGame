package wbs.chatgame.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.chatgame.games.challenges.Challenge;
import wbs.chatgame.games.challenges.ChallengeManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractNextCommand extends WbsSubcommand {
    public AbstractNextCommand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    protected boolean isGameMandatory = true;

    protected boolean beforeStart(CommandSender sender, String label, String[] args) {
        return false;
    }
    protected void sendMandatoryGameMessage(CommandSender sender, String label, String[] args) {
        sendMessage("Usage: &h/" + label + " " + args[0] + " <game> [options]", sender);
    }
    protected abstract void afterNext(CommandSender sender, String label, String[] args, Game game, List<String> options);

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (beforeStart(sender, label, args)) {
            return true;
        }

        Game game = null;

        if (args.length >= 2) {
            game = GameManager.getGame(args[1]);
            if (game == null) {
                String gameList = GameManager.getGames().stream().map(Game::getGameName).collect(Collectors.joining(", "));
                sendMessage("Game not found: &h" + args[1] + "&r. Please choose from the following: &h" + gameList, sender);
                return true;
            }
        } else {
            if (isGameMandatory) {
                sendMandatoryGameMessage(sender, label, args);
                return true;
            }
        }

        List<String> options = new LinkedList<>();
        if (args.length > 2) {
            options.addAll(Arrays.asList(Arrays.copyOfRange(args, 2, args.length)));
        }

        if (game != null) {
            GameController.setNext(game);
        }

        if (!options.isEmpty()) {
            GameController.setNext(options);
        }

        GameController.setLastNextSender(sender);

        afterNext(sender, label, args, game, options);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();

        if (args.length < 2) {
            return choices;
        }

        if (args.length == 2) {
            return GameManager.getGames().stream()
                    .map(Game::getGameName)
                    .collect(Collectors.toList());
        }

        Game game = GameManager.getGame(args[1]);

        if (game != null) {
            switch (args.length) {
                case 3 -> {
                    choices.addAll(game.getOptionCompletions());
                    suggestChallengeIfAnyValid(game, choices);
                }
                case 4 -> {
                    if (args[2].equalsIgnoreCase("-c")) {
                        choices.addAll(getValidChallengeList(game));
                    }
                }
                case 5 -> {
                    // Already specified challenge, now suggest options
                    if (args[2].equalsIgnoreCase("-c")) {
                        return game.getOptionCompletions();
                    } else if (args[3].equalsIgnoreCase("-c")) {
                        choices.addAll(getValidChallengeList(game));
                    }
                }
            }
        }

        return choices;
    }

    private List<String> getValidChallengeList(Game game) {
        return ChallengeManager.listChallenges(game)
                .stream()
                .filter(Challenge::valid)
                .map(Challenge::getId)
                .collect(Collectors.toList());
    }

    private void suggestChallengeIfAnyValid(Game game, List<String> choices) {
        if (ChallengeManager.listChallenges(game)
                .stream().anyMatch(Challenge::valid)) {
            choices.add("-c");
        }
    }

}
