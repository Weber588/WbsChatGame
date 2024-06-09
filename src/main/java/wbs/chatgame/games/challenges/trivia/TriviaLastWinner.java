package wbs.chatgame.games.challenges.trivia;

import org.bukkit.entity.Player;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.GameQuestion;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;

public class TriviaLastWinner extends TriviaQuestionChallenge {
    public TriviaLastWinner(TriviaGame parent) {
        super(parent);
    }

    @Override
    protected TriviaQuestion nextQuestion() {
        GameQuestion lastQuestion = GameController.getLastQuestion();
        assert lastQuestion != null;
        Player lastWinner = lastQuestion.getWinner();
        assert lastWinner != null;

        return new TriviaQuestion("custom",
                "Who won last round?",
                2,
                true,
                false,
                false,
                lastWinner.getName()
                ) {
            @Override
            public boolean checkGuess(String guess, Player player) {
                if (super.checkGuess(guess, player)) {
                    return true;
                } else {
                    if (lastWinner.equals(player)) {
                        return guess.equalsIgnoreCase("me");
                    }
                }
                return false;
            }
        };
    }

    @Override
    public boolean valid() {
        GameQuestion currentQuestion = GameController.getCurrentQuestion();
        return currentQuestion != null && currentQuestion.getWinner() != null;
    }
}
