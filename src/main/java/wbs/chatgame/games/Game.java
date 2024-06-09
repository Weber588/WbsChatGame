package wbs.chatgame.games;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.challenges.ChallengeGenerator;
import wbs.chatgame.games.challenges.ChallengeManager;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Game {

    @NotNull
    protected final WbsChatGame plugin;
    @NotNull
    protected final ChatGameSettings settings;

    @NotNull
    protected final String gameName;
    private double challengeChance;

    private final QuestionGenerator<?> defaultGenerator;

    /** Duration in ticks */
    private final int duration;

    @NotNull
    private final List<String> challengeHistory = new LinkedList<>();

    /** A map of challenge ids to the chance */
    @NotNull
    private final Map<String, Double> challengesWithChance = new HashMap<>();

    public Game(@NotNull String gameName, ConfigurationSection section, String directory) {
        plugin = WbsChatGame.getInstance();
        settings = plugin.settings;
        this.gameName = gameName;

        int defaultDuration = 120 * 20;
        int duration = (int) (section.getDouble("duration", defaultDuration / 20.0) * 20);

        if (duration <= 0) {
            settings.logError("Invalid duration: " + duration + ". Duration must be positive.", directory + "/duration");
            duration = defaultDuration;
        }

        this.duration = duration;

        challengeChance = WbsMath.clamp(0, 100, section.getDouble("challenge-chance", 0));

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

        defaultGenerator = getDefaultGenerator();
    }

    protected Game(Game copy) {
        this.plugin = copy.plugin;
        this.settings = copy.settings;
        this.gameName = copy.gameName;
        this.challengeChance = copy.challengeChance;
        this.duration = copy.duration;
        defaultGenerator = getDefaultGenerator();
    }

    /**
     * Start the game, or a challenge, and return the game that was started.
     * @return A game question, or null if the question failed to generate.
     */
    @Nullable
    public final GameQuestion getNewQuestion() {
        QuestionGenerator<?> toUse = defaultGenerator;

        if (WbsMath.chance(challengeChance) && !challengesWithChance.isEmpty()) {
            QuestionGenerator<?> challenge;
            String id = WbsCollectionUtil.pseudoRandomAvoidRepeats(challengesWithChance, challengeHistory, 2);
            challenge = ChallengeManager.getChallenge(id, this);

            if (challenge == null) {
                plugin.logger.info("Invalid challenge in game " + gameName + ": " + id);
            }

            if (challenge != null) {
                if (challenge.valid()) {
                    toUse = challenge;
                }
            }
        }

        return toUse.generateQuestion();
    }

    @NotNull
    protected abstract QuestionGenerator<?> getDefaultGenerator();

    public GameQuestion generateQuestion() {
        return defaultGenerator.generateQuestion();
    }

    public final GameQuestion generateWithOptionsOrChallenge(@NotNull List<String> options) throws IllegalArgumentException {
        if (options.size() > 0) {
            String checkForChallenge = options.get(0);
            if (checkForChallenge.equalsIgnoreCase("-c")) {
                if (options.size() == 1) {
                    List<String> challengeIds = ChallengeManager.getGameChallenges(this)
                            .getGenerators()
                            .stream()
                            .filter(QuestionGenerator::valid)
                            .map(QuestionGenerator::getId)
                            .collect(Collectors.toList());
                    if (challengeIds.isEmpty()) {
                        throw new IllegalArgumentException("There are currently no valid challenges for this game type.");
                    } else {
                        throw new IllegalArgumentException("Provide a challenge: " + String.join(", ", challengeIds));
                    }
                }

                String challengeString = options.get(1);

                QuestionGenerator<?> nextChallenge = ChallengeManager.getChallenge(challengeString, this);
                if (nextChallenge == null) {
                    throw new IllegalArgumentException("Invalid challenge: " + challengeString + ". Valid challenges: " +
                            ChallengeManager.getGameChallenges(this)
                                    .getGenerators()
                                    .stream()
                                    .filter(QuestionGenerator::valid)
                                    .map(QuestionGenerator::getId)
                                    .collect(Collectors.joining(", ")));
                } else {
                    if (!nextChallenge.valid()) {
                        throw new IllegalArgumentException("That challenge isn't available at the moment.");
                    } else {
                        return nextChallenge.generateWithOptions(options.subList(2, options.size()));
                    }
                }
            }
        }

        return defaultGenerator.generateWithOptions(options);
    }

    public int getDuration() {
        return duration;
    }

    @NotNull
    public String getGameName() {
        return gameName;
    }

    /**
     * Returns a list of strings to use as suggestions when entering options for
     * the given game type.
     * @return The strings to suggest when entering options for this game type.
     */
    public List<String> getOptionCompletions() {
        return new LinkedList<>();
    }

    @Override
    public String toString() {
        return getGameName();
    }

    public void setChallengeChance(double challengeChance) {
        this.challengeChance = challengeChance;
    }
}
