package wbs.chatgame.games.challenges;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.word.QuickTypeGame;
import wbs.chatgame.games.word.Word;
import wbs.utils.util.plugin.WbsMessage;

import static wbs.chatgame.WordUtil.reverseString;

public class QuickTypeBackwards extends QuickTypeGame implements Challenge<QuickTypeGame> {
    public QuickTypeBackwards(QuickTypeGame parent) {
        super(parent);
    }

    @Override
    @NotNull
    protected Game startGame(Word wordToGuess) {
        setCurrentWord(new Word(reverseString(wordToGuess.word), wordToGuess.getPoints() + 1, true));

        WbsMessage message = plugin.buildMessage("Quick! Type \"")
                .append(Component.text(wordToGuess.word).color(plugin.getTextHighlightColour()))
                .append("\" &obackwards&r for "
                        + GameController.pointsDisplay(getPoints()) + "!")
                .build();

        broadcastQuestion(message);
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
