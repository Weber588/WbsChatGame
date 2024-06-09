package wbs.chatgame.games.challenges.math;

import wbs.chatgame.games.math.MathGame;
import wbs.chatgame.games.math.ViewableEquation;

public class MathRomanNumeralQuestion extends MathRomanNumeralBoth {
    public MathRomanNumeralQuestion(MathGame parent, ViewableEquation equation) {
        super(parent, equation);
    }

    @Override
    protected boolean answerInRN() {
        return false;
    }

    @Override
    protected boolean questionInRN() {
        return true;
    }
}
