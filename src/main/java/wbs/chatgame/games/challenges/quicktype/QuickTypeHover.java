package wbs.chatgame.games.challenges.quicktype;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.challenges.ChallengeGenerator;
import wbs.chatgame.games.word.quicktype.QuickTypeGame;
import wbs.chatgame.games.word.Word;
import wbs.chatgame.games.word.WordGameQuestion;
import wbs.utils.util.plugin.WbsMessage;

public class QuickTypeHover extends QuickTypeGame implements ChallengeGenerator<QuickTypeGame> {
    public QuickTypeHover(QuickTypeGame parent) {
        super(parent);
    }

    @Override
    protected @NotNull WordGameQuestion generateQuestion(Word wordToGuess) {
        setCurrentWord(new Word(wordToGuess.word, wordToGuess.getPoints() + 1, wordToGuess.isFormatted()));

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
