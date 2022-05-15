package wbs.chatgame.games.challenges;

import org.bukkit.entity.Player;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;

import java.util.List;

public class TriviaLastAnswer extends TriviaQuestionChallenge {
    public TriviaLastAnswer(TriviaGame parent) {
        super(parent);
    }

    @Override
    protected TriviaQuestion nextQuestion() {
        Game lastGame = GameController.getLastGame();
        assert lastGame != null;
        return new TriviaQuestion("custom",
                "What was the answer to the last question?",
                2,
                true,
                false,
                false,
                lastGame.getAnswers().toArray(new String[0])
        ) {
            @Override
            public boolean checkGuess(String guess, Player player) {
                return lastGame.checkGuess(guess, player);
            }

            @Override
            public String[] answers() {
                return lastGame.getAnswers().toArray(new String[0]);
            }
        };
    }

    @Override
    public boolean valid() {
        Game lastGame = GameController.getLastGame();
        // Avoid recursion - don't allow TriviaLastAnswer challenges to chain
        return lastGame != null && !(lastGame instanceof TriviaLastAnswer);
    }
}
