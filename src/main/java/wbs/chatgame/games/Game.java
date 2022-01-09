package wbs.chatgame.games;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.GameController;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.challenges.Challenge;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Game {

    protected final WbsChatGame plugin;
    protected final ChatGameSettings settings;

    public Game(String gameName, ConfigurationSection section, String directory) {
        plugin = WbsChatGame.getInstance();
        settings = plugin.settings;
        this.gameName = gameName;

        duration = (int) (section.getDouble("duration", 120) * 20);
        challengeChance = section.getDouble("challenge-chance", 0);

        ConfigurationSection challengesSection = section.getConfigurationSection("challenges");
        if (challengesSection != null) {
            for (String key : challengesSection.getKeys(false)) {
                String chanceString = challengesSection.getString(key);
                assert chanceString != null;

                double chance;
                try {
                    chance = Double.parseDouble(chanceString);
                } catch (NumberFormatException e) {
                    settings.logError(key + " must be a number. Invalid value: " + challengesSection.get(key), directory + "/challenges/" + key);
                }

                // TODO
            }
        }

    }

    protected final String gameName;
    private final double challengeChance;
    private final int duration;
    private final Map<Challenge, Double> challengesWithChance = new HashMap<>();

    private List<String> options;

    private String currentQuestion;
    protected int currentPoints;

    protected void broadcastQuestion(String currentQuestion) {
        this.currentQuestion = currentQuestion;
        GameController.broadcast(currentQuestion);
    }

    public final void startGame() {
        if (WbsMath.chance(challengeChance) && !challengesWithChance.isEmpty()) {
            boolean challengeStarted = WbsCollectionUtil.getRandomWeighted(challengesWithChance).startChallenge();
            if (!challengeStarted) {
                start();
            }
        } else {
            start();
        }
    }

    /**
     * Start this game with a given set of options.
     * @param options The options to use.
     * @return Any errors that occurred during the start, or
     * null if the game started properly.
     */
    public String startWithOptions(@NotNull List<String> options) {
        startGame();
        return null;
    }

    /**
     * Checks if a string guess was correct.
     * @param guess Checks if a given guess is correct.
     * @return Whether or not the guess is a valid answer.
     */
    public abstract boolean checkGuess(String guess);

    protected abstract void start();

    /**
     * Called when the game ends without a winner, usually by the round
     * being skipped or stopped.
     */
    public abstract void endNoWinner();

    /**
     * Called when the game is won by a given player.
     * Points are handled in the game controller,
     * so usually this method only needs to send the round end
     * message.
     * @param player The player who guessed successfully
     * @param guess The guess that was triggered the player to win
     */
    public abstract void endWinner(Player player, String guess);

    public int getDuration() {
        return duration;
    }

    public String getGameName() {
        return gameName;
    }

    public abstract List<String> getAnswers();

    public String getCurrentQuestion() {
        return currentQuestion;
    }

    public int getPoints() {
        return currentPoints;
    }
}
