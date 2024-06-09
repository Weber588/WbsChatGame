package wbs.chatgame.games.challenges.trivia;

import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.chatgame.games.trivia.TriviaGame;

public class TriviaGamePosition extends TriviaPosition {
    public TriviaGamePosition(TriviaGame parent) {
        super(parent);
    }

    @Override
    public @Nullable Game getGame() {
        return GameManager.getRandomGame();
    }
}
