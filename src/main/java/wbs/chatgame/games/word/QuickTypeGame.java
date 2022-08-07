package wbs.chatgame.games.word;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.WordUtil;
import wbs.chatgame.controller.GameMessenger;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.challenges.ChallengeManager;
import wbs.chatgame.games.challenges.QuickTypeBackwards;
import wbs.chatgame.games.challenges.QuickTypeHover;
import wbs.utils.util.plugin.WbsMessage;

public class QuickTypeGame extends WordGame {
    @SuppressWarnings("unused") // Accessed reflectively
    public QuickTypeGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        scramble = section.getBoolean("scramble", true);
        matchCase = section.getBoolean("match-case", true);
    }

    public QuickTypeGame(QuickTypeGame copy) {
        super(copy);

        scramble = copy.scramble;
        matchCase = copy.matchCase;
    }

    private final boolean scramble;
    private final boolean matchCase;

    @Override
    @NotNull
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
        GameMessenger.broadcast(player.getName() + " won in " + GameController.getLastRoundStartedString() + "!");
    }

    @Override
    public void endNoWinner() {
        GameMessenger.broadcast("Nobody answered in time!");
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
            return new Word(scrambled, word.getPoints());
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
        ChallengeManager.buildAndRegisterChallenge("backwards", this, QuickTypeBackwards.class);
        ChallengeManager.buildAndRegisterChallenge("hover", this, QuickTypeHover.class);
    }
}
