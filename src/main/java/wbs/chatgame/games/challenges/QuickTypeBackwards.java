package wbs.chatgame.games.challenges;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.word.QuickTypeGame;
import wbs.chatgame.games.word.Word;

import static wbs.chatgame.WordUtil.reverseString;

public class QuickTypeBackwards extends QuickTypeGame implements Challenge<QuickTypeGame> {
    public QuickTypeBackwards(QuickTypeGame parent) {
        super(parent.getGameName(), 0, parent.getDuration());
    }

    @Override
    protected Game startGame(Word wordToGuess) {
        setCurrentWord(new Word(reverseString(wordToGuess.word), wordToGuess.getPoints() + 1, wordToGuess.generator));

        broadcastQuestion("Quick! Type \"&h" + wordToGuess.word + "&r\" &obackwards&r for "
                + GameController.pointsDisplay(getPoints()) + "!");

        return this;
    }

    @Override
    public Class<QuickTypeGame> getGameClass() {
        return QuickTypeGame.class;
    }

    private String challengeId;

    @Override
    public final void setId(@NotNull String id) {
        challengeId = id;
    }

    @Override
    public final @NotNull String getId() {
        return challengeId;
    }
}
