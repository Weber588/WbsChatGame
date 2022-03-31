package wbs.chatgame.games.word;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.GameController;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.challenges.Challenge;
import wbs.chatgame.games.word.generator.GeneratedWord;
import wbs.chatgame.games.word.generator.GeneratorManager;
import wbs.chatgame.games.word.generator.WordGenerator;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;

import java.util.*;

public abstract class WordGame extends Game {
    public WordGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        generationChance = section.getDouble("generation-chance", 0);

        if (generationChance < 100) {
            for (String wordFormat : section.getStringList("custom")) {
                String wordDirectory = directory + "/custom/" + wordFormat;

                int split = wordFormat.lastIndexOf(':');
                if (split == -1) {
                    settings.logError(
                            "Invalid format: " + wordFormat + ". Use [word]:[points]. " +
                                    "For example, minecraft:1 will be the word \"minecraft\" worth 1 point for a correct guess.",
                            wordDirectory);
                    continue;
                }

                String word = wordFormat.substring(0, split);

                int points;
                String pointsString = wordFormat.substring(split + 1);
                try {
                    points = Integer.parseInt(pointsString);
                } catch (NumberFormatException e) {
                    settings.logError("Invalid point value: " + pointsString + ". Use an integer.", wordDirectory);
                    continue;
                }

                customWords.add(new Word(word, points, null));
            }
        }

        if (generationChance > 0) {
            ConfigurationSection generatorSection = section.getConfigurationSection("generators");
            if (generatorSection != null) {
                for (String generatorString : generatorSection.getKeys(false)) {
                    String genDirectory = directory + "/generators/" + generatorString;
                    WordGenerator generator = GeneratorManager.getGenerator(generatorString);

                    if (generator == null) {
                        settings.logError("Invalid generator: " + generatorString + ". " +
                                        "Please choose from the following: "
                                        + String.join(", ", GeneratorManager.getIds()),
                                genDirectory);
                        continue;
                    }

                    double chance = generatorSection.getDouble(generatorString);

                    if (chance <= 0) {
                        settings.logError("Chance must be greater than 0.", genDirectory);
                        continue;
                    }

                    generators.put(generator, chance);
                }
            }
        }

        if (generators.isEmpty() && generationChance > 0) {
            plugin.logger.info("No valid generators set for " + gameName + "; generation disabled.");
            generationChance = 0;
        }

        if (generationChance >= 100 && generators.isEmpty()) {
            settings.logError("Custom list disabled with no generators set. Disabling game " + gameName, directory);
            throw new InvalidConfigurationException();
        } else if (generationChance <= 0 && customWords.isEmpty()) {
            settings.logError("Generators disabled with no custom words set. Disabling game " + gameName, directory);
            throw new InvalidConfigurationException();
        } else if (generators.isEmpty() && customWords.isEmpty()) {
            settings.logError("No generators or custom words specified. Disabling game " + gameName, directory);
            throw new InvalidConfigurationException();
        }

        plugin.logger.info("Loaded " + customWords.size() + " custom words and " + generators.size() + " generators for " + gameName);
    }

    public WordGame(String gameName, double challengeChance, int duration) {
        super(gameName, challengeChance, duration);
    }

    private final List<Word> customWords = new LinkedList<>();
    private final Map<WordGenerator, Double> generators = new HashMap<>();

    // Track history to prevent repetition
    private List<Word> history = new LinkedList<>();

    private double generationChance;

    private Word currentWord;

    @Override
    protected final void start() {
        currentWord = getWord();
        currentPoints = currentWord.getPoints();
        startGame(currentWord);
    }

    @Override
    public @NotNull Game startWithOptions(@NotNull List<String> options) {
        if (options.isEmpty()) {
            start();
            return this;
        }

        String id = options.get(0);

        WordGenerator generator = GeneratorManager.getGenerator(id);
        if (generator != null) {
            currentWord = getGeneratedWord(generator);
        } else {
            String lastArg = options.get(options.size() - 1);
            int customPoints = Integer.MIN_VALUE;
            try {
                customPoints = Integer.parseInt(lastArg);
            } catch (NumberFormatException ignored) {}

            String customWord;
            if (customPoints == Integer.MIN_VALUE) {
                customWord = String.join(" ", options);
                currentWord = new Word(customWord, calculatePoints(customWord), null);
            } else {
                customWord = String.join(" ", options.subList(0, options.size() - 1));
                currentWord = new Word(customWord, customPoints, null);
            }
        }

        currentPoints = currentWord.getPoints();
        startGame(currentWord);

        return this;
    }

    @Override
    public List<String> getAnswers() {
        return Collections.singletonList(currentWord.word);
    }

    protected Word getWord() {
        Word word;

        if (WbsMath.chance(generationChance)) {
            word = generateWord();
        } else {
            word = getCustomWord();
        }

        return word;
    }

    protected Word getCustomWord() {
        return WbsCollectionUtil.getAvoidRepeats(
                () -> WbsCollectionUtil.getRandom(customWords),
                customWords.size(),
                history,
                2);
    }

    /**
     * Generate a word from the specified generators.
     * @return The generated word, or null if no generators were specified.
     */
    protected Word generateWord() {
        if (generators.isEmpty()) {
            return null;
        }
        WordGenerator generator = WbsCollectionUtil.getRandomWeighted(generators);

        return getGeneratedWord(generator);
    }

    protected GeneratedWord getGeneratedWord(@NotNull WordGenerator generator) {
        GeneratedWord word = generator.getNext();

        int points = Math.max(1, calculatePoints(word.word) + generator.getPointsModifier());
        word.setPoints(points);

        return word;
    }

    @Override
    public boolean checkGuess(String guess, Player guesser) {
        return guess.equalsIgnoreCase(currentWord.word);
    }

    protected abstract void startGame(Word wordToGuess);

    protected abstract int calculatePoints(String word);

    protected Word getCurrentWord() {
        return currentWord;
    }
    protected void setCurrentWord(Word word) {
        this.currentWord = word;
    }

    @Override
    public void endWinner(Player player, String guess) {
        GameController.broadcast(player.getName() + " won in " + GameController.getLastRoundStartedString() + "! The answer was: &h" + currentWord.word);
    }

    @Override
    public void endNoWinner() {
        GameController.broadcast("Nobody got the word in time! The word was: &h" + currentWord.word);
    }

    @Override
    protected void configure(Challenge<?> challenge) {
        super.configure(challenge);

        if (challenge instanceof WordGame other) {
            other.generators.putAll(this.generators);
            other.customWords.addAll(customWords);
            other.history = history; // Set by reference, so the two histories are synchronized.
            other.generationChance = generationChance;
        }
    }

    @Override
    public List<String> getOptionCompletions() {
        return GeneratorManager.getIds();
    }
}
