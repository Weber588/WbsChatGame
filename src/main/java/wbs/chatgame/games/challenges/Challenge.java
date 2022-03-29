package wbs.chatgame.games.challenges;

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

    Class<T> getGameClass();
}
