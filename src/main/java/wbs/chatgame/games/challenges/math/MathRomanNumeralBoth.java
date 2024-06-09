package wbs.chatgame.games.challenges.math;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.math.MathGame;
import wbs.chatgame.games.math.MathQuestion;
import wbs.chatgame.games.math.ViewableEquation;
import wbs.utils.util.plugin.WbsMessage;
import wbs.utils.util.string.RomanNumerals;

public class MathRomanNumeralBoth extends MathQuestion {
    private boolean enforceChallenge = true;

    public MathRomanNumeralBoth(@NotNull MathGame parent, @NotNull ViewableEquation equation) {
        super(parent, equation);


    }

    protected boolean questionInRN() {
        return true;
    }

    protected boolean answerInRN() {
        return true;
    }

    @Override
    public WbsMessage getDisplay(ViewableEquation currentEquation) {
        if (answerInRN() && solution.value() > 3999) {
            enforceChallenge = false;
            super.broadcastEquation(currentEquation);
            return;
        }
        enforceChallenge = true;

        String answer = formatAnswer();

        currentPoints += Math.max(1, (answer.length() + 2) / 3);

        String equationString = currentEquation.asString();

        String broadcastString;
        if (currentEquation.customEquation()) {
            broadcastString = equationString + " &h(" + GameController.pointsDisplay(currentPoints) + ")";
        } else {
            if (questionInRN) {
                currentPoints++;
                broadcastString = "Solve \"&h" + currentEquation.equation().toString(true) +
                        "&r\"";
                // Don't need to clarify roman numerals if it's telling them to answer it in RN anyway
                if (!answerInRN) {
                    broadcastString += " (roman numerals)";
                }

                broadcastString += " for " + GameController.pointsDisplay(currentPoints) + "!";
            } else {
                broadcastString = "Solve \"&h" + equationString + "&r\" for " + GameController.pointsDisplay(currentPoints) + "!";
            }
        }

        if (answerInRN) {
            broadcastString += "&r (Answer in roman numerals!)";
        }

        broadcastQuestion(broadcastString);
    }

    @Override
    public boolean checkGuess(String guess, Player guesser) {
        if (!enforceChallenge) return super.checkGuess(guess, guesser);

        double value;
        try {
            value = RomanNumerals.fromRomanNumeralsDecimal(guess);
        } catch (IllegalArgumentException e) {
            try {
                value = Double.parseDouble(guess);
            } catch (NumberFormatException ex) {
                return false;
            }

            if (checkAnswer(value)) {
                if (answerInRN) {
                    plugin.sendMessage("&wYou must answer in roman numerals!", guesser);
                    return false;
                } else {
                    return true;
                }
            }

            return false;
        }

        return checkAnswer(value);
    }

    @Override
    protected String formatAnswer() {
        if (answerInRN && enforceChallenge) {
            return RomanNumerals.toRoman(solution.value());
        } else {
            return super.formatAnswer();
        }
    }

    private String id;

    @Override
    public void setId(@NotNull String id) {
        this.id = id;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public Class<MathGame> getGameClass() {
        return MathGame.class;
    }
}
