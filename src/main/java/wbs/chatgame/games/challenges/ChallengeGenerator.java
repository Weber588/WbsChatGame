package wbs.chatgame.games.challenges;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.QuestionGenerator;
import wbs.chatgame.games.GameQuestion;

public abstract class ChallengeGenerator<T extends Game> extends QuestionGenerator<T> {
    private final String challengeId;

    public ChallengeGenerator(T parent, String challengeId) {
        super(parent);
        this.challengeId = challengeId;
    }

    @NotNull
    public String getId() {
        return challengeId;
    }

    public abstract Class<T> getGameClass();
}
