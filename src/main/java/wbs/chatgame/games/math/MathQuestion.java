package wbs.chatgame.games.math;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WordUtil;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.controller.GameMessenger;
import wbs.chatgame.games.GameQuestion;
import wbs.utils.util.WbsMath;
import wbs.utils.util.plugin.WbsMessage;
import wbs.utils.util.plugin.WbsMessageBuilder;

import java.util.Collections;
import java.util.List;

public class MathQuestion extends GameQuestion {
    @NotNull
    protected ViewableEquation equation;
    @NotNull
    protected Solution solution;

    public MathQuestion(@NotNull MathGame parent, @NotNull ViewableEquation equation) {
        super(parent, parent.getDuration());

        this.equation = equation;
        solution = setEquation(equation);
    }

    public Solution setEquation(@NotNull ViewableEquation equation) {
        this.equation = equation;

        try {
            solution = equation.equation().solve(true);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        currentPoints = solution.points();

        return solution;
    }

    @Override
    public void start() {
        currentDisplay = getDisplay(equation);

        broadcastQuestion(currentDisplay);
    }

    public WbsMessage getDisplay(ViewableEquation currentEquation) {
        String equationString = currentEquation.asString();

        WbsMessageBuilder builder;
        if (currentEquation.customEquation()) {
            builder= plugin.buildMessage("")
                    .appendRaw(equationString)
                    .append(" &h(" + GameController.pointsDisplay(currentPoints) + ")");
        } else {
            builder = plugin.buildMessage("Solve \"&h")
                    .appendRaw(equationString)
                    .append("&r\" for " + GameController.pointsDisplay(currentPoints) + "!");
        }

        return builder.build();
    }

    @Override
    protected void onRoundEnd(@Nullable Player winner, @Nullable String guess, @Nullable Double finalDuration) {
        if (winner == null) {
            GameMessenger.broadcast("Nobody answered in time. The answer was: &h"
                    + formatAnswer());
        } else {
            GameMessenger.broadcast(winner.getName() + " won in " + GameController.getLastRoundStartedString() + "! The answer was: &h" + formatAnswer());
        }
    }

    @Override
    public boolean checkGuess(String guess, Player guesser) {
        double numberGuess;
        try {
            numberGuess = Double.parseDouble(guess);
        } catch (NumberFormatException e) {
            return false;
        }

        return checkAnswer(numberGuess);
    }

    public boolean checkAnswer(double guess) {
        return WbsMath.roundTo(guess, 2) == WbsMath.roundTo(solution.value(), 2);
    }

    protected String formatAnswer() {
        double rounded = WbsMath.roundTo(solution.value(), 2);
        if (solution.value() == rounded) {
            return (int) rounded + "";
        }
        return rounded + "";
    }

    @Override
    public List<String> getExampleAnswers() {
        return Collections.singletonList(formatAnswer());
    }
}
