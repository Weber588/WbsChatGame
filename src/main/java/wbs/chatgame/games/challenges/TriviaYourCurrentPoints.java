package wbs.chatgame.games.challenges;

import org.bukkit.entity.Player;
import wbs.chatgame.data.StatsManager;
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
                int guessInt;
                try {
                    guessInt = Integer.parseInt(guess);
                } catch (NumberFormatException e) {
                    return false;
                }
                return guessInt == StatsManager.getTotalCachedPoints(player.getUniqueId());
            }
        };
    }
}
