package wbs.chatgame.games.challenges;

import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.Game;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class ChallengeManager {
    private ChallengeManager() {}

    private static final Map<Game, Map<String, Challenge<?>>> registered = new HashMap<>();

    public static void registerChallenge(String id, Game game, Challenge<?> challenge) {
        id = WordUtil.stripSyntax(id);
        Map<String, Challenge<?>> gameClasses = registered.get(game);

        if (gameClasses == null) {
            gameClasses = new HashMap<>();
        }

        gameClasses.put(id, challenge);

        registered.put(game, gameClasses);
    }

    @Nullable
    public static Challenge<?> getChallenge(String id, Game game) {
        id = WordUtil.stripSyntax(id);

        Map<String, Challenge<?>> challenges = registered.get(game);

        if (challenges == null) {
            return null;
        }

        return challenges.get(id);
    }

    @Nullable
    public static <T extends Game> Challenge<?> buildAndRegisterChallenge(String id, T game, Class<? extends Challenge<T>> challengeClass) {
        id = WordUtil.stripSyntax(id);
        Challenge<T> challenge = buildChallenge(game, challengeClass);

        if (challenge != null) {
            registerChallenge(id, game, challenge);
        } else {
            WbsChatGame.getInstance().logger.severe("Failed to register challenge " + id
                    + " under game " + game.getGameName() + " using class " + challengeClass.getCanonicalName());
        }

        return challenge;
    }

    @Nullable
    public static <T extends Game> Challenge<T> buildChallenge(T game, Class<? extends Challenge<T>> challengeClass) {
        Class<? extends Game> gameClass = game.getClass();

        try {
            Constructor<? extends Challenge<T>> constructor = challengeClass.getConstructor(gameClass);

            return constructor.newInstance(gameClass.cast(game));
        } catch (NoSuchMethodException e) {
            WbsChatGame.getInstance().logger.severe("Invalid challenge constructor and/or registration: " +
                    challengeClass.getCanonicalName() + " was registered as a challenge for " +
                    gameClass.getCanonicalName() + ", but lacked a constructor for that class.");

            e.printStackTrace();
            return null;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            WbsChatGame.getInstance().logger.severe("An unknown error occurred while creating a challenge: " +
                    challengeClass.getCanonicalName());

            e.printStackTrace();
            return null;
        }
    }
}
