package wbs.chatgame.games;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.controller.GameMessenger;
import wbs.utils.util.plugin.WbsMessage;
import wbs.utils.util.plugin.WbsMessageBuilder;

import java.util.List;

/**
 * Represents an instance of a question being asked.
 * Game Questions are stateful and track winner details, as well as any state changes
 * defined by extending classes (such as whether hints were given, what stage the question
 * was in)
 */
public abstract class GameQuestion {

    @NotNull
    protected final Game parent;
    @NotNull
    protected final WbsChatGame plugin;
    @NotNull
    protected final ChatGameSettings settings;

    protected WbsMessage currentDisplay;
    protected int currentPoints;
    protected int duration;

    @Nullable
    protected Player winner;
    @Nullable
    protected String winningGuess;
    protected double finalDuration;

    public GameQuestion(QuestionGenerator<?> generator, int duration) {
        this.parent = generator.parent;
        plugin = parent.plugin;
        settings = parent.settings;
        this.duration = duration;
    }

    public abstract void start();

    public void broadcastQuestion(@NotNull String currentQuestion) {
        WbsMessageBuilder builder = plugin.buildMessage(currentQuestion);
        broadcastQuestion(builder.build());
    }

    public void broadcastQuestion(WbsMessage currentDisplay) {
        if (settings.debugMode) {
            String lineBreak = "___________________________________________________________";
            plugin.logger.info(lineBreak);
            currentDisplay.send(Bukkit.getConsoleSender());
            plugin.logger.info("Answers: ");
            for (String answer : getExampleAnswers()) {
                plugin.logger.info(" - " + answer);
            }
            plugin.logger.info(lineBreak);
        }

        this.currentDisplay = currentDisplay;
        GameMessenger.broadcast(currentDisplay);
    }

    public abstract List<String> getExampleAnswers();

    /**
     * Checks if a string guess was correct.
     * @param guess Checks if a given guess is correct.
     * @param guesser The player making the guess. Rewards should not be applied,
     *                but this allows player specific guesses.
     * @return Whether or not the guess is a valid answer.
     */
    public abstract boolean checkGuess(String guess, Player guesser);

    /**
     * Called when the game ends without a winner, usually by the round
     * being skipped or stopped.
     */
    public final void endNoWinner() {
        onRoundEnd(null, null, null);
    }

    /**
     * Called when the game is won by a given player.
     * Points are handled in the game controller,
     * so usually this method only needs to send the round end
     * message.
     * @param player The player who guessed successfully
     * @param guess The guess that was triggered the player to win
     */
    public final void endWinner(@NotNull Player player, @NotNull String guess, @NotNull Double finalDuration) {
        winner = player;
        winningGuess = guess;
        this.finalDuration = finalDuration;

        onRoundEnd(player, guess, finalDuration);
    }

    protected abstract void onRoundEnd(@Nullable Player winner, @Nullable String guess, @Nullable Double finalDuration);

    public int getPoints() {
        return currentPoints;
    }

    public int getDuration() {
        return duration;
    }

    public Game getGame() {
        return parent;
    }

    @Nullable
    public Player getWinner() {
        return winner;
    }
}
