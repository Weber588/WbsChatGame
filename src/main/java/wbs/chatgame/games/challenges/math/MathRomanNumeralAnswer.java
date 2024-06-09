package wbs.chatgame.games.challenges.math;

import wbs.chatgame.games.math.MathGame;
import wbs.chatgame.games.math.ViewableEquation;

public class MathRomanNumeralAnswer extends MathRomanNumeralBoth {
    public MathRomanNumeralAnswer(MathGame parent, ViewableEquation equation) {
        super(parent, equation);
    }

    @Override
    protected boolean questionInRN() {
        return false;
    }

    @Override
    protected boolean answerInRN() {
        return true;
    }
}
