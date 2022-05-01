package wbs.chatgame.games.challenges;

import org.jetbrains.annotations.Nullable;
import wbs.chatgame.data.*;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;

import java.util.List;
import java.util.Random;

public class TriviaPosition extends TriviaQuestionChallenge {
    private static int MAX_POSITION = 5;

    public TriviaPosition(TriviaGame parent) {
        super(parent);
    }

    @Override
    protected TriviaQuestion nextQuestion() {
        Game game = getGame();
        Leaderboard top = StatsManager.getCachedTop(TrackedPeriod.TOTAL, game);

        int position = new Random().nextInt(Math.min(MAX_POSITION, top.size() - 1));

        LeaderboardEntry entry = top.get(position);

        String gameString = "";
        if (game != null) {
            gameString = " for " + game.getGameName();
        }

        return new TriviaQuestion("custom",
                "Who is at rank " + (position + 1) + gameString + " in ChatGame?",
                2,
                true,
                false,
                false,
                entry.name() + ""
        );
    }

    @Nullable
    public Game getGame() {
        return null;
    }

    @Override
    public boolean valid() {
        return StatsManager.getCachedTop(TrackedPeriod.TOTAL, getGame()).size() >= 5;
    }
}
