package wbs.chatgame.games.trivia;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.trivia.TriviaQuestion;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.string.WbsStrings;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TriviaGame extends Game {

    public TriviaGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        ConfigurationSection questionsSection = section.getConfigurationSection("questions");
        if (questionsSection != null) {
            for (String key : questionsSection.getKeys(false)) {
                String qDir = directory + "/questions/" + key;

                ConfigurationSection qSection = questionsSection.getConfigurationSection(key);
                if (qSection == null) {
                    settings.logError("Questions must be defined as a section with the question, points, and a list of possible answers.", qDir);
                    continue;
                }

                List<String> answers = qSection.getStringList("answers");
                if (answers.isEmpty()) {
                    settings.logError("No answers provided.", qDir + "/answers");
                    continue;
                }

                String question = qSection.getString("question");
                if (question == null) {
                    settings.logError("Question is a required field.", qDir + "/answers");
                    continue;
                }


                int points = qSection.getInt("points");
                boolean showAnswer = qSection.getBoolean("show-answer", false);
                boolean useRegex = qSection.getBoolean("use-regex", false);

                questions.add(new TriviaQuestion(question, points, showAnswer, useRegex, answers.toArray(new String[0])));
            }
        }

        plugin.logger.info("Loaded " + questions.size() + " questions for " + gameName);
    }

    private final List<TriviaQuestion> questions = new LinkedList<>();
    private TriviaQuestion question;

    /**
     * Get the correct format for an answer from a successful guess
     * @param guess The successful guess
     * @return The correctly formatted answer string
     */
    private String formatAnswer(String guess) {
        if (question.useRegex()) {
            return WbsStrings.capitalizeAll(guess);
        }

        for (String answer : question.answers()) {
            if (guess.equalsIgnoreCase(answer)) {
                return answer;
            }
        }

        return guess;
    }

    @Override
    protected void start() {
        question = WbsCollectionUtil.getRandom(questions);
        currentPoints = question.points();
        broadcastQuestion(question.question() + "&h (" + GameController.pointsDisplay(question.points()) + ")");
    }

    @Override
    public void endWinner(Player player, String guess) {
        GameController.broadcast(player.getName() + " won in " + GameController.getLastRoundStartedString() + "! The answer was: &h" + formatAnswer(guess));
    }

    @Override
    public void endNoWinner() {
        String endMessage = "Nobody answered in time!";
        if (question.showAnswer()) {
            endMessage += " The answer was: &h" + question.answers()[0];
        }
        GameController.broadcast(endMessage);
    }

    @Override
    public boolean checkGuess(String guess) {
        for (String answer : question.answers()) {
            if (question.useRegex()) {
                if (guess.toLowerCase().matches(answer.toLowerCase())) return true;
            } else {
                if (guess.equalsIgnoreCase(answer)) return true;
            }
        }

        return false;
    }

    @Override
    public List<String> getAnswers() {
        return new LinkedList<>(Arrays.asList(question.answers()));
    }
}
