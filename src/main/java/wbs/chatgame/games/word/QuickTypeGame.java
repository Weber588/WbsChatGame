package wbs.chatgame.games.word;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import wbs.chatgame.GameController;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.challenges.Challenge;
import wbs.chatgame.games.challenges.QuickTypeBackwards;

public class QuickTypeGame extends WordGame {
    public QuickTypeGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        scramble = section.getBoolean("scramble", true);
        matchCase = section.getBoolean("match-case", true);
    }

    public QuickTypeGame(String gameName, double challengeChance, int duration) {
        super(gameName, challengeChance, duration);
    }

    private boolean scramble;
    private boolean matchCase;

    @Override
    protected void startGame(Word wordToGuess) {
        broadcastQuestion("Quick! Type \"&h" + wordToGuess.word + "&r\" for "
                + GameController.pointsDisplay(getPoints()) + "!");
    }

    @Override
    public void endWinner(Player player, String guess) {
        GameController.broadcast(player.getName() + " won in " + GameController.getLastRoundStartedString() + "!");
    }

    @Override
    public void endNoWinner() {
        GameController.broadcast("Nobody answered in time!");
    }

    @Override
    public boolean checkGuess(String guess) {
        if (matchCase) {
            return guess.equals(getCurrentWord().word);
        } else {
            return guess.equalsIgnoreCase(getCurrentWord().word);
        }
    }

    @Override
    protected Word generateWord() {
        return conditionalScramble(super.generateWord());
    }

    @Override
    protected Word getCustomWord() {
        return conditionalScramble(super.getCustomWord());
    }

    private Word conditionalScramble(Word word) {
        if (scramble) {
            String scrambled = WordUtil.scrambleString(word.word);
            return new Word(scrambled, word.getPoints(), word.generator);
        } else {
            return word;
        }
    }

    @Override
    protected int calculatePoints(String word) {
        return Math.max(1,
                (int) Math.round(
                        Math.log(word.length() / 3.0) / Math.log(2) // log_2(length/3)
                )
        );
    }

    @Override
    public void registerChallenges() {
        super.registerChallenges();
        register("backwards", QuickTypeBackwards.class);
    }

    @Override
    protected void configure(Challenge<?> challenge) {
        super.configure(challenge);

        if (challenge instanceof QuickTypeGame other) {
            other.scramble = scramble;
            other.matchCase = matchCase;
        }
    }
}
