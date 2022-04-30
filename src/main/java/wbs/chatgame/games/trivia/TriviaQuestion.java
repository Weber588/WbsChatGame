package wbs.chatgame.games.trivia;

import org.bukkit.entity.Player;
import wbs.utils.util.pluginhooks.PlaceholderAPIWrapper;

import java.util.Arrays;
import java.util.Objects;

public class TriviaQuestion {
    private final String id;
    private final String question;
    private final int points;
    private final boolean showAnswer;
    private final boolean fillPlaceholders;
    private final boolean useRegex;
    private final String[] answers;

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
