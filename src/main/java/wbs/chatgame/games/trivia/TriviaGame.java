package wbs.chatgame.games.trivia;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.controller.GameMessenger;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.challenges.*;
import wbs.utils.util.VersionUtil;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.string.WbsStrings;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

                String minVersion = qSection.getString("required-version");
                minVersion = qSection.getString("min-ver", minVersion);
                minVersion = qSection.getString("min-version", minVersion);
                if (minVersion != null) {
                    if (minVersion.startsWith("1.")) {
                        minVersion = minVersion.substring(2);
                    }

                    double versionAsDouble = -1;
                    try {
                        versionAsDouble = Double.parseDouble(minVersion);
                    } catch (NumberFormatException ignored) {}

                    if (versionAsDouble != -1) {
                        if (versionAsDouble > VersionUtil.getVersion()) {
                            continue;
                        }
                    } else {
                        settings.logError("Invalid min-version: \"" + minVersion + "\". Skipping.",
                                qDir + "/min-version");
                        continue;
                    }
                }

                String maxVersion = qSection.getString("max-ver");
                maxVersion = qSection.getString("max-version", maxVersion);
                if (maxVersion != null) {
                    if (maxVersion.startsWith("1.")) {
                        maxVersion = maxVersion.substring(2);
                    }

                    double versionAsDouble = -1;
                    try {
                        versionAsDouble = Double.parseDouble(maxVersion);
                    } catch (NumberFormatException ignored) {}

                    if (versionAsDouble != -1) {
                        if (versionAsDouble < VersionUtil.getVersion()) {
                            continue;
                        }
                    } else {
                        settings.logError("Invalid max-version: \"" + maxVersion + "\". Skipping.",
                                qDir + "/max-version");
                        continue;
                    }
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
                boolean fillPlaceholders = qSection.getBoolean("fill-placeholders", false);

                questions.add(new TriviaQuestion(key, question, points, showAnswer, fillPlaceholders, useRegex, answers.toArray(new String[0])));
            }
        }

        plugin.logger.info("Loaded " + questions.size() + " questions for " + gameName);
    }

    public TriviaGame(TriviaGame copy) {
        super(copy);

        questions.addAll(copy.questions);
        history.addAll(copy.history);
    }

    private final List<TriviaQuestion> questions = new LinkedList<>();
    private final List<TriviaQuestion> history = new LinkedList<>();
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
    @NotNull
    protected Game start() {
        question = nextQuestion();
        currentPoints = question.points();
        broadcastQuestion(question.question() + "&h (" + GameController.pointsDisplay(question.points()) + ")");
        return this;
    }

    @Override
    public Game startWithOptions(@NotNull List<String> options) {
        if (options.isEmpty()) {
            return start();
        }

        String id = options.get(0);

        TriviaQuestion found = null;
        for (TriviaQuestion check : questions) {
            if (check.id().equalsIgnoreCase(id)) {
                found = check;
                break;
            }
        }

        if (found != null) {
            question = found;
        } else {
            String formatErrorString = "Custom trivia questions must be in the form " +
                    "&h\"<points> <question> -a <answer1>, [answer2], [answer3]\"&r.";

            int points;
            try {
                points = Integer.parseInt(options.get(0));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number: " + options.get(0) + ". " + formatErrorString);
            }

            String fullOptions = String.join(" ", options.subList(1, options.size()));

            String[] split = fullOptions.split("-a");

            if (split.length == 1) {
                throw new IllegalArgumentException(formatErrorString);
            }

            String questionString = split[0].trim();
            String answerString = split[1].trim();
            String[] answers = answerString.split(",");

            for (int i = 0; i < answers.length; i++) {
                answers[i] = answers[i].trim();
            }

            question = new TriviaQuestion("custom", questionString, points, false, false, false, answers);
        }

        currentPoints = question.points();
        broadcastQuestion(question.question() + "&h (" + GameController.pointsDisplay(question.points()) + ")");

        return this;
    }

    protected TriviaQuestion nextQuestion() {
        return WbsCollectionUtil.getAvoidRepeats(
                () -> WbsCollectionUtil.getRandom(questions),
                questions.size(),
                history,
                2);
    }

    @Override
    public void endWinner(Player player, String guess) {
        GameMessenger.broadcast(player.getName() + " won in " + GameController.getLastRoundStartedString() + "! The answer was: &h" + formatAnswer(guess));
    }

    @Override
    public void endNoWinner() {
        String endMessage = "Nobody answered in time!";
        if (question.showAnswer()) {
            endMessage += " The answer was: &h" + question.answers()[0];
        }
        GameMessenger.broadcast(endMessage);
    }

    @Override
    public boolean checkGuess(String guess, Player guesser) {
        return question.checkGuess(guess, guesser);
    }

    @Override
    public List<String> getAnswers() {
        return new LinkedList<>(Arrays.asList(question.answers()));
    }

    @Override
    public List<String> getOptionCompletions() {
        return questions.stream()
                .map(TriviaQuestion::id)
                .collect(Collectors.toList());
    }

    @Override
    public void registerChallenges() {
        super.registerChallenges();
        ChallengeManager.buildAndRegisterChallenge("won-last-round", this, TriviaLastWinner.class);
        ChallengeManager.buildAndRegisterChallenge("answer-last-round", this, TriviaLastAnswer.class);
        ChallengeManager.buildAndRegisterChallenge("players-online", this, TriviaPlayersOnline.class);
        ChallengeManager.buildAndRegisterChallenge("player-in-position", this, TriviaPosition.class);
        ChallengeManager.buildAndRegisterChallenge("player-in-game-position", this, TriviaGamePosition.class);
        ChallengeManager.buildAndRegisterChallenge("current-points", this, TriviaYourCurrentPoints.class);
        ChallengeManager.buildAndRegisterChallenge("crafting-recipe", this, TriviaCraftingAmount.class);
        ChallengeManager.buildAndRegisterChallenge("crafting-ingredient", this, TriviaRecipeIncluding.class);
    }
}
