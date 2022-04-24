package wbs.chatgame.games.word;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import wbs.chatgame.GameController;
import wbs.chatgame.WordUtil;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.challenges.Challenge;
import wbs.chatgame.games.challenges.QuickTypeBackwards;
import wbs.chatgame.games.challenges.QuickTypeHover;
import wbs.utils.util.plugin.WbsMessage;

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
    protected Game startGame(Word wordToGuess) {
        WbsMessage message = plugin.buildMessage("Quick! Type \"")
                .appendRaw(wordToGuess.word)
                    .setFormatting("&h")
                .append("\" for "
                        + GameController.pointsDisplay(getPoints()) + "!")
                .build();

        broadcastQuestion(message);
        return this;
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
    public boolean checkGuess(String guess, Player guesser) {
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
    protected int calculateDefaultPoints(String word) {
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
        register("hover", QuickTypeHover.class);
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
