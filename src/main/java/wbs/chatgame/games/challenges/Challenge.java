package wbs.chatgame.games.challenges;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.Game;

public interface Challenge<T extends Game> {
    default Game startChallenge() {
        if (this instanceof Game) {
            return ((Game) this).startGame();
        } else {
            WbsChatGame.getInstance().logger
                    .severe("Challenge classes must extend the Game class. Class: " + getClass().getCanonicalName()
                            + ". Please report this to the developer.");
            return null;
        }
    }

    void setId(@NotNull String id);

    @NotNull
    String getId();

    Class<T> getGameClass();

    /**
     * Returns if the challenge is valid at the time of being called. If not, the game will be re-rolled.
     * <br/>
     * This allows challenges with preconditions to auto-skip, such as if they require a certain number of
     * players to be online, or require certain variables to be populated in the game controller.
     * @return True if the challenge may be run.
     */
    default boolean valid() {
        return true;
    }
}
