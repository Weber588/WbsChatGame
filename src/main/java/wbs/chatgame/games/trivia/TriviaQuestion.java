package wbs.chatgame.games.trivia;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.GameQuestion;
import wbs.utils.util.VersionUtil;
import wbs.utils.util.pluginhooks.PlaceholderAPIWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TriviaQuestion extends GameQuestion {
    @Nullable
    public static TriviaQuestion fromConfig(ConfigurationSection section, String directory, String key) {
        ChatGameSettings settings = WbsChatGame.getInstance().settings;

        double minVersion = -1;
        String minVersionString = section.getString("required-version");
        minVersionString = section.getString("min-ver", minVersionString);
        minVersionString = section.getString("min-version", minVersionString);
        if (minVersionString != null) {
            if (minVersionString.startsWith("1.")) {
                minVersionString = minVersionString.substring(2);
            }

            try {
                minVersion = Double.parseDouble(minVersionString);
            } catch (NumberFormatException ignored) {}

            if (minVersion == -1) {
                settings.logError("Invalid min-version: \"" + minVersionString + "\". Skipping.",
                        directory + "/min-version");
                return null;
            }
        }

        double maxVersion = -1;
        String maxVersionString = section.getString("max-ver");
        maxVersionString = section.getString("max-version", maxVersionString);
        if (maxVersionString != null) {
            if (maxVersionString.startsWith("1.")) {
                maxVersionString = maxVersionString.substring(2);
            }

            try {
                maxVersion = Double.parseDouble(maxVersionString);
            } catch (NumberFormatException ignored) {}

            if (maxVersion == -1) {
                settings.logError("Invalid max-version: \"" + maxVersionString + "\". Skipping.",
                        directory + "/max-version");
                return null;
            }
        }

        List<String> answers = section.getStringList("answers");
        if (answers.isEmpty()) {
            settings.logError("No answers provided.", directory + "/answers");
            return null;
        }

        String question = section.getString("question");
        if (question == null) {
            settings.logError("Question is a required field.", directory + "/answers");
            return null;
        }

        int points = section.getInt("points");
        boolean showAnswer = section.getBoolean("show-answer", false);
        boolean useRegex = section.getBoolean("use-regex", false);
        boolean fillPlaceholders = section.getBoolean("fill-placeholders", false);

        TriviaQuestion questionObject = new TriviaQuestion(key, question, points, showAnswer, fillPlaceholders, useRegex, answers.toArray(new String[0]));
        questionObject.minVersion = minVersion;
        questionObject.maxVersion = maxVersion;
        return questionObject;
    }

    private final String id;
    private final String question;
    private final int points;
    private final boolean showAnswer;
    private final boolean fillPlaceholders;
    private final boolean useRegex;
    private double minVersion = -1;
    private double maxVersion = -1;
    private final String[] answers;

    private List<String> hints;
    private int hintPoints = -1;

    public TriviaQuestion(String id,
                          String question,
                          int points,
                          boolean showAnswer,
                          boolean fillPlaceholders,
                          boolean useRegex,
                          String ... answers) {
        this.id = id;
        this.question = question;
        this.points = points;
        this.showAnswer = showAnswer;
        this.fillPlaceholders = fillPlaceholders;
        this.useRegex = useRegex;
        this.answers = answers;
    }

    @Override
    public void start() {

    }

    @Override
    public List<String> getExampleAnswers() {
        return null;
    }

    public boolean checkGuess(String guess, Player player) {
        for (String answer : answers) {
            if (fillPlaceholders) {
                answer = PlaceholderAPIWrapper.setPlaceholders(player, answer);
            }
            if (useRegex) {
                if (guess.toLowerCase().matches(answer.toLowerCase())) return true;
            } else {
                if (guess.equalsIgnoreCase(answer)) return true;
            }
        }

        return false;
    }

    @Override
    protected void onRoundEnd(@Nullable Player winner, @Nullable String guess, @Nullable Double finalDuration) {

    }

    public String id() {
        return id;
    }

    public String question() {
        return question;
    }

    public int points() {
        return points;
    }

    public boolean showAnswer() {
        return showAnswer;
    }

    public boolean useRegex() {
        return useRegex;
    }

    public String[] answers() {
        return answers;
    }

    public double getMinVersion() {
        return minVersion;
    }

    public double getMaxVersion() {
        return maxVersion;
    }

    public boolean isVersionValid() {
        if (minVersion != -1 && minVersion > VersionUtil.getVersion()) {
            return false;
        }

        if (maxVersion != -1 && maxVersion < VersionUtil.getVersion()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TriviaQuestion) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.question, that.question) &&
                this.points == that.points &&
                this.showAnswer == that.showAnswer &&
                this.useRegex == that.useRegex &&
                Arrays.equals(this.answers, that.answers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, question, points, showAnswer, useRegex, Arrays.hashCode(answers));
    }

    @Override
    public String toString() {
        return "TriviaQuestion[" +
                "id=" + id + ", " +
                "question=" + question + ", " +
                "points=" + points + ", " +
                "showAnswer=" + showAnswer + ", " +
                "useRegex=" + useRegex + ", " +
                "answers=" + Arrays.toString(answers) + ']';
    }
}
