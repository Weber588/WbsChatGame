package wbs.chatgame.games.challenges;

import wbs.chatgame.GameController;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;

public class TriviaLastAnswer extends TriviaQuestionChallenge {
    public TriviaLastAnswer(TriviaGame parent) {
        super(parent);
    }

    @Override
    protected TriviaQuestion nextQuestion() {
        return new TriviaQuestion("custom",
                "What was the answer to the last question?",
                2,
                true,
                false,
                false,
                GameController.getLastAnswer()
        );
    }

    @Override
    public boolean valid() {
        return GameController.getLastAnswer() != null;
    }
}
