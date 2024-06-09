package wbs.chatgame.games.challenges;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.QuestionGenerator;

import java.util.*;

public final class ChallengeManager {
    private ChallengeManager() {}

    private static final Map<Class<?>, GameChallenges<?>> registered = new HashMap<>();

    @NotNull
    public static GameChallenges getGameChallenges(Game game) {
        GameChallenges challenges = registered.get(game.getClass());

        if (challenges == null) {
            challenges = new GameChallenges<>(game);
            registered.put(game.getClass(), challenges);
        }

        return challenges;
    }


    public static void registerChallenge(Game game, QuestionGenerator<?> challenge) {
        GameChallenges challenges = getGameChallenges(game);

        challenges.challenges.put(WordUtil.stripSyntax(challenge.getId()), challenge);
    }

    @Nullable
    public static QuestionGenerator<?> getChallenge(String id, Game game) {
        id = WordUtil.stripSyntax(id);

        GameChallenges challenges = getGameChallenges(game);

        return challenges.challenges.get(id);
    }

    public static class GameChallenges {
        public final Game game;
        private final Map<String, QuestionGenerator<?>> challenges = new HashMap<>();

        private GameChallenges(Game game) {
            this.game = game;
        }

        public Set<String> getIds() {
            return challenges.keySet();
        }

        public Collection<QuestionGenerator<?>> getGenerators() {
            return challenges.values();
        }
    }
}
