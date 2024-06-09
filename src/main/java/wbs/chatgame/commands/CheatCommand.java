package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameQuestion;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;

public class CheatCommand extends WbsSubcommand {
    public CheatCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "cheat");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        GameQuestion currentQuestion = GameController.getCurrentQuestion();
        if (currentQuestion != null) {
            List<String> answers = currentQuestion.getExampleAnswers();

            if (answers.isEmpty()) {
                sendMessage("&4Error: No answers defined. Please check console or report this issue.", sender);
                plugin.logger.severe("A running question had no answers defined: " + currentQuestion.getGame().getGameName() + ". Type: " + currentQuestion.getGame().getClass().getCanonicalName());
                return false;
            }

            sendMessage("Allowed answers (" + answers.size() + "):", sender);
            answers.forEach(answer -> sendMessageNoPrefix("&h    - " + answer, sender));
        } else {
            if (GameController.isRunning()) {
                sendMessage("There is no question pending right now.", sender);
                sendMessage("There are &h" + GameController.timeToNextRound() + "&r until the next round starts.", sender);
            } else {
                sendMessage("The game is not running right now.", sender);
            }
        }

        return true;
    }
}
