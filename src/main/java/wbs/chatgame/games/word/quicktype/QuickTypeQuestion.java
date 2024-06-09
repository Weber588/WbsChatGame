package wbs.chatgame.games.word.quicktype;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.controller.GameMessenger;
import wbs.chatgame.games.word.Word;
import wbs.chatgame.games.word.WordGameQuestion;

public class QuickTypeQuestion extends WordGameQuestion {

    protected final boolean matchCase;

    public QuickTypeQuestion(QuickTypeGame parent, Word word, int duration) {
        super(parent, word, duration);

        this.matchCase = parent.matchCase;
    }

    @Override
    public void start() {
        currentDisplay = plugin.buildMessage("Quick! Type \"")
                .appendRaw(currentWord.word)
                .setFormatting("&h")
                .append("\" for "
                        + GameController.pointsDisplay(getPoints()) + "!")
                .build();

        broadcastQuestion(currentDisplay);
    }
    @Override
    public boolean checkGuess(String guess, Player guesser) {
        if (matchCase()) {
            return guess.equals(currentWord.word);
        } else {
            return guess.equalsIgnoreCase(currentWord.word);
        }
    }

    protected boolean matchCase() {
        return matchCase;
    }

    @Override
    protected void onRoundEnd(@Nullable Player winner, @Nullable String guess, @Nullable Double finalDuration) {
        if (winner == null) {
            GameMessenger.broadcast("Nobody answered in time!");
        } else {
            GameMessenger.broadcast(winner.getName() + " won in " + GameController.getLastRoundStartedString() + "!");
        }
    }
}
