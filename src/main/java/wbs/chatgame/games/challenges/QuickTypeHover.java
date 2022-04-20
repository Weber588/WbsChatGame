package wbs.chatgame.games.challenges;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.word.QuickTypeGame;
import wbs.chatgame.games.word.Word;
import wbs.utils.util.plugin.WbsMessage;

public class QuickTypeHover extends QuickTypeGame implements Challenge<QuickTypeGame> {
    public QuickTypeHover(QuickTypeGame parent) {
        super(parent.getGameName(), 0, parent.getDuration());
    }

    @Override
    protected Game startGame(Word wordToGuess) {
        setCurrentWord(new Word(wordToGuess.word, wordToGuess.getPoints() + 1, wordToGuess.generator));

        WbsMessage message = plugin.buildMessage("Quick! &hHover&r over this message to see the word to type! ("
                        + GameController.pointsDisplay(getPoints()) + ")")
                .addHoverText("&h" + wordToGuess.word)
                .build();

        broadcastQuestion(message);
        return this;
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

    @Override
    public Class<QuickTypeGame> getGameClass() {
        return QuickTypeGame.class;
    }
}
