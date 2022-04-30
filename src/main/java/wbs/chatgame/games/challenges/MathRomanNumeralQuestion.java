package wbs.chatgame.games.challenges;

import wbs.chatgame.games.math.MathGame;

public class MathRomanNumeralQuestion extends MathRomanNumeralBoth {
    public MathRomanNumeralQuestion(MathGame parent) {
        super(parent);

        answerInRN = false;
        questionInRN = true;
    }
}
