package wbs.chatgame.games.challenges;

import wbs.chatgame.games.math.MathGame;

public class MathRomanNumeralAnswer extends MathRomanNumeralBoth {
    public MathRomanNumeralAnswer(MathGame parent) {
        super(parent);

        answerInRN = true;
        questionInRN = false;
    }
}
