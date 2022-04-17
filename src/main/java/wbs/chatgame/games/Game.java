package wbs.chatgame.games;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.GameController;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.challenges.Challenge;
import wbs.chatgame.games.challenges.ChallengeManager;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Game {

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
                    continue;
                }

                challengesWithChance.put(key, chance);
            }

            if (!challengesWithChance.isEmpty()) {
                plugin.logger.info("Loaded " + challengesWithChance.size() + " challenges for " + getGameName());
            }
        }
    }

    public Game(String gameName, double challengeChance, int duration) {
        plugin = WbsChatGame.getInstance();
        settings = plugin.settings;
        this.gameName = gameName;
        this.challengeChance = challengeChance;
        this.duration = duration;
    }

    protected final WbsChatGame plugin;
    protected final ChatGameSettings settings;

    protected final String gameName;
    private final double challengeChance;

    /** Duration in ticks */
    private final int duration;

    private List<String> challengeHistory = new LinkedList<>();
    /** A map of challenge ids to the chance */
    private final Map<String, Double> challengesWithChance = new HashMap<>();

    private String currentQuestion;
    protected int currentPoints;

    @Nullable
    private Challenge<?> nextChallenge = null;

    public void broadcastQuestion(String currentQuestion) {
        if (settings.debugMode) {
            String lineBreak = "___________________________________________________________";
            plugin.logger.info(lineBreak);
            plugin.logger.info("Question: " + currentQuestion);
            plugin.logger.info("Answers: ");
            for (String answer : getAnswers()) {
                plugin.logger.info(" - " + answer);
            }
            plugin.logger.info(lineBreak);
        }

        this.currentQuestion = currentQuestion;
        GameController.broadcast(currentQuestion);
    }

    /**
     * Start the game, or a challenge, and return the game that was started.
     * @return The game that was started; either this object, or the challenge
     * that was started instead.
     */
    @NotNull
    public final Game startGame() {
        if (!(this instanceof Challenge)) {
            if (nextChallenge != null || (WbsMath.chance(challengeChance) && !challengesWithChance.isEmpty())) {
                Challenge<?> challenge;
                if (nextChallenge == null) {
                    String id = WbsCollectionUtil.pseudoRandomAvoidRepeats(challengesWithChance, challengeHistory, 2);
                    challenge = ChallengeManager.getChallenge(id, this);

                    if (challenge == null) {
                        plugin.logger.info("Invalid challenge in game " + gameName + ": " + id);
                    }
                } else {
                    challenge = nextChallenge;
                    nextChallenge = null;
                }

                if (challenge != null) {
                    if (challenge.valid()) {
                        Game started = challenge.startChallenge();
                        if (started != null) {
                            return started;
                        }
                    }
                }
            }
        }

        return start();
    }

    public final Game startWithOptionsOrChallenge(@NotNull List<String> options) throws IllegalArgumentException {
        if (options.size() > 0) {
            String checkForChallenge = options.get(0);
            if (checkForChallenge.equalsIgnoreCase("-c")) {
                if (options.size() == 1) {
                    List<String> challengeIds = ChallengeManager.listChallenges(this)
                            .stream()
                            .filter(Challenge::valid)
                            .map(Challenge::getId)
                            .collect(Collectors.toList());
                    if (challengeIds.isEmpty()) {
                        throw new IllegalArgumentException("There are currently no valid challenges for this game type.");
                    } else {
                        throw new IllegalArgumentException("Provide a challenge: " + String.join(", ", challengeIds));
                    }
                }

                String challengeString = options.get(1);

                nextChallenge = ChallengeManager.getChallenge(challengeString, this);
                if (nextChallenge == null) {
                    throw new IllegalArgumentException("Invalid challenge: " + challengeString + ". Valid challenges: " +
                            ChallengeManager.listChallenges(this)
                                    .stream()
                                    .filter(Challenge::valid)
                                    .map(Challenge::getId)
                                    .collect(Collectors.joining(", ")));
                } else {
                    if (!nextChallenge.valid()) {
                        nextChallenge = null;
                        throw new IllegalArgumentException("That challenge isn't available at the moment.");
                    } else {
                        return ((Game) nextChallenge).startWithOptions(options.subList(2, options.size()));
                    }
                }
            }
        }

        return startWithOptions(options);
    }

    /**
     * Start this game with a given set of options.
     * @param options The options to use.
     * @return The game itself, or null if the game failed to start.
     */
    @Nullable
    public Game startWithOptions(@NotNull List<String> options) throws IllegalArgumentException {
        return startGame();
    }

    /**
     * Checks if a string guess was correct.
     * @param guess Checks if a given guess is correct.
     * @param guesser The player making the guess. Rewards should not be applied,
     *                but this allows player specific guesses.
     * @return Whether or not the guess is a valid answer.
     */
    public abstract boolean checkGuess(String guess, Player guesser);

    protected abstract Game start();

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

    /**
     * Register this games' challenges with the {@link ChallengeManager}, if it has any,
     * using the {@link #register(String, Class)} method.
     */
    public void registerChallenges() {}

    protected final <T extends Game> void register(String id, Class<? extends Challenge<T>> challengeClass) {
        @SuppressWarnings("unchecked")
        Challenge<?> challenge = ChallengeManager.buildAndRegisterChallenge(id, (T) this, challengeClass);
        if (challenge != null) {
            try {
                configure(challenge);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    /**
     * Set any fields that are needed to ensure behaviour of the challenge matches the behaviour of the current
     * instance. For example, if the subclass of {@link Game} has a "hint" functionality, it should copy that functionality
     * over to the challenge that extends that subclass.
     * @param challenge The challenge, which should also extend the class of the current implementation of {@link Game}
     */
    protected void configure(Challenge<?> challenge) throws IllegalArgumentException {
        if (!getClass().isAssignableFrom(challenge.getClass())) {
            plugin.logger.severe("Failed to configure challenge. Challenge \"" +
                    challenge.getClass().getCanonicalName() + "\" is not an extension of " +
                    getClass().getCanonicalName());
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns a list of strings to use as suggestions when entering options for
     * the given game type.
     * @return The strings to suggest when entering options for this game type.
     */
    public List<String> getOptionCompletions() {
        return new LinkedList<>();
    }
}
