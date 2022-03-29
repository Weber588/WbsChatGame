package wbs.chatgame.games.challenges;

import wbs.chatgame.GameController;
import wbs.chatgame.games.word.QuickTypeGame;
import wbs.chatgame.games.word.Word;

import static wbs.chatgame.WordUtil.reverseString;

public class QuickTypeBackwards extends QuickTypeGame implements Challenge<QuickTypeGame> {
    public QuickTypeBackwards(QuickTypeGame parent) {
        super(parent.getGameName(), 0, parent.getDuration());
    }

    @Override
    protected Word getWord() {
        Word word = super.getWord();
        return new Word(reverseString(word.word), word.getPoints() + 1, word.generator);
    }

    @Override
    protected void startGame(Word wordToGuess) {
        // Rebuild word in it's "forward" form to display, but don't change the actual word to guess.
        broadcastQuestion("Quick! Type \"&h" + reverseString(wordToGuess.word) + "&r\" &obackwards&r for "
                + GameController.pointsDisplay(getPoints()) + "!");
    }

    @Override
    public Class<QuickTypeGame> getGameClass() {
        return QuickTypeGame.class;
    }
}
