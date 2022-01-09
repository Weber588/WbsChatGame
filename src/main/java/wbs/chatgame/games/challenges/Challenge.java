package wbs.chatgame.games.challenges;

import wbs.chatgame.games.Game;

public abstract class Challenge {

    protected final Game game;
    public Challenge(Game game) {
        this.game = game;
    }

    /**
     * Start the challenge.
     * @return Whether or not the challenge was successful. False = failed to start challenge.
     */
    public abstract boolean startChallenge();

}
