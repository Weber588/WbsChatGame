package wbs.chatgame.games.challenges.trivia;

import wbs.chatgame.LangUtil;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;

public class AdvancementTriviaQuestion extends TriviaQuestionChallenge {

    public AdvancementTriviaQuestion(TriviaGame parent) {
        super(parent);
    }

    @Override
    protected TriviaQuestion nextQuestion() {
        return null;
    }

    @Override
    public boolean valid() {
        return !LangUtil.getLangConfig().isEmpty();
    }
}
