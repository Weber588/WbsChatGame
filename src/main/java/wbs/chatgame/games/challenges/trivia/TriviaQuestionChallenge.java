package wbs.chatgame.games.challenges.trivia;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.games.challenges.ChallengeGenerator;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;

public abstract class TriviaQuestionChallenge extends TriviaGame implements ChallengeGenerator<TriviaGame> {
    public TriviaQuestionChallenge(TriviaGame parent) {
        super(parent);
    }

    @Override
    protected abstract TriviaQuestion nextQuestion();

    @Override
    public final Class<TriviaGame> getGameClass() {
        return TriviaGame.class;
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
