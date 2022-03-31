package wbs.chatgame.games.challenges;

import org.bukkit.entity.Player;
import wbs.chatgame.GameController;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;

public class TriviaLastWinner extends TriviaQuestionChallenge {
    public TriviaLastWinner(TriviaGame parent) {
        super(parent);
    }

    @Override
    protected TriviaQuestion nextQuestion() {
        return new TriviaQuestion("custom",
                "Who won last round?",
                2,
                true,
                false,
                GameController.getLastWinner().getName()
                ) {
            @Override
            public boolean checkGuess(String guess, Player player) {
                if (super.checkGuess(guess, player)) {
                    return true;
                } else {
                    if (GameController.getLastWinner().equals(player)) {
                        return guess.equalsIgnoreCase("me");
                    }
                }
                return false;
            }
        };
    }

    @Override
    public boolean valid() {
        return GameController.getLastWinner() != null;
    }
}
