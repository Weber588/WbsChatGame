package wbs.chatgame.games.challenges.trivia;

import org.bukkit.entity.Player;
import wbs.chatgame.data.ChatGameDB;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;

public class TriviaYourCurrentPoints extends TriviaQuestionChallenge {
    public TriviaYourCurrentPoints(TriviaGame parent) {
        super(parent);
    }

    @Override
    protected TriviaQuestion nextQuestion() {
        return new TriviaQuestion("custom",
                "How many points do you have?",
                2,
                false,
                false,
                false,
                "") {
            @Override
            public boolean checkGuess(String guess, Player player) {
                double guessVal;
                try {
                    guessVal = Double.parseDouble(guess);
                } catch (NumberFormatException e) {
                    return false;
                }
                return guessVal == ChatGameDB.getPlayerManager().getOnlinePlayer(player.getUniqueId()).getPoints();
            }
        };
    }
}
