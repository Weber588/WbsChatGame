package wbs.chatgame.games.challenges;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import wbs.chatgame.games.trivia.TriviaGame;
import wbs.chatgame.games.trivia.TriviaQuestion;

public class TriviaPlayersOnline extends TriviaQuestionChallenge {
    public TriviaPlayersOnline(TriviaGame parent) {
        super(parent);
    }

    @Override
    protected TriviaQuestion nextQuestion() {
        return new TriviaQuestion("custom",
                "How many players are online right now?",
                2,
                true,
                false,
                false,
                Bukkit.getOnlinePlayers().size() + ""
        ) {
            @Override
            public boolean checkGuess(String guess, Player player) {
                return (Bukkit.getOnlinePlayers().size() + "").equalsIgnoreCase(guess);
            }

            @Override
            public String[] answers() {
                return new String[]{(Bukkit.getOnlinePlayers().size() + "")};
            }
        };
    }

    @Override
    public boolean valid() {
        return Bukkit.getOnlinePlayers().size() >= 3;
    }
}
