package wbs.chatgame.games.challenges;

import org.jetbrains.annotations.Nullable;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.Game;

public final class ChallengeManager {
    private ChallengeManager() {}

    @Nullable
    public static Challenge getChallenge(String id, Game game) {
        Challenge challenge;
        switch (WordUtil.stripSyntax(id)) {
            case "randomplayer":
                challenge = new UnscrambleOnlinePlayer(game);
        }



        return null;
    }
}
