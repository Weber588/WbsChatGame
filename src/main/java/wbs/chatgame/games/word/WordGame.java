package wbs.chatgame.games.word;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.controller.GameController;
import wbs.chatgame.controller.GameMessenger;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.math.ConditionalPointsCalculator;
import wbs.chatgame.games.math.EquationGenerator;
import wbs.chatgame.games.math.OperationSet;
import wbs.chatgame.games.word.generator.GeneratedWord;
import wbs.chatgame.games.word.generator.GeneratorManager;
import wbs.chatgame.games.word.generator.WordGenerator;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;
import wbs.utils.util.string.WbsStrings;

import java.util.*;

public abstract class WordGame extends Game {
    public WordGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        String pointsEquation = section.getString("points");
        if (pointsEquation != null) {
            pointsCalculator = new ConditionalPointsCalculator(new EquationGenerator(pointsEquation));
        } else {
            pointsCalculator = null;
        }

        ConfigurationSection formattingSection = section.getConfigurationSection("generator-formatting");
        if (formattingSection != null) {
            capitalizeFirstThreshold = formattingSection.getInt("capitalize-first", capitalizeFirstThreshold);
            capitalizeAllThreshold = formattingSection.getInt("capitalize-all", capitalizeAllThreshold);
        }

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

    protected WordGame(WordGame copy) {
        super(copy);

        customWords.addAll(copy.customWords);
        history.addAll(copy.history);

        generators.putAll(copy.generators);
        generationChance = copy.generationChance;
        capitalizeFirstThreshold = copy.capitalizeFirstThreshold;
        capitalizeAllThreshold = copy.capitalizeAllThreshold;

        pointsCalculator = copy.pointsCalculator;
    }

    private final List<Word> customWords = new LinkedList<>();
    // Track history to prevent repetition
    private final List<Word> history = new LinkedList<>();

    private final Map<WordGenerator, Double> generators = new HashMap<>();
    private double generationChance;
    private int capitalizeFirstThreshold = 0;
    private int capitalizeAllThreshold = 0;

    private final ConditionalPointsCalculator pointsCalculator;

    private Word currentWord;
    private String unformattedWord = null;

    @Override
    @NotNull
    protected final Game start() {
        setCurrentWord(getWord());
        currentPoints = currentWord.getPoints();
        return startGame(currentWord);
    }

    @Override
    @NotNull
    public Game startWithOptions(@NotNull List<String> options) {
        if (options.isEmpty()) {
            return start();
        }

        String id = options.get(0);

        WordGenerator generator = GeneratorManager.getGenerator(id);
        if (generator != null) {
            setCurrentWord(getGeneratedWord(generator));
        } else {
            String lastArg = options.get(options.size() - 1);
            int customPoints = Integer.MIN_VALUE;
            if (options.size() > 1) {
                try {
                    customPoints = Integer.parseInt(lastArg);
                } catch (NumberFormatException ignored) {}
            }

            String customWord;
            if (customPoints == Integer.MIN_VALUE) {
                customWord = String.join(" ", options);
                setCurrentWord(new Word(customWord, calculatePoints(customWord), null));
            } else {
                customWord = String.join(" ", options.subList(0, options.size() - 1));
                setCurrentWord(new Word(customWord, customPoints, null));
            }
        }

        currentPoints = currentWord.getPoints();
        return startGame(currentWord);
    }

    @Override
    public List<String> getAnswers() {
        return Collections.singletonList(unformattedWord);
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

    protected String formatWord(String word) {
        word = word.toLowerCase();

        if (word.length() >= capitalizeFirstThreshold) {
            word = WbsStrings.capitalize(word);
        }

        if (word.length() >= capitalizeAllThreshold) {
            word = WbsStrings.capitalizeAll(word);
        }

        return word;
    }

    @Override
    public boolean checkGuess(String guess, Player guesser) {
        return guess.equalsIgnoreCase(currentWord.word);
    }

    @NotNull
    protected abstract Game startGame(Word wordToGuess);

    protected int calculatePoints(String word) {
        int points;
        if (pointsCalculator != null) {
            Map<String, Double> placeholders = new HashMap<>();
            placeholders.put("length", (double) word.length());
            int numOfSpaces = word.length() - word.replace(" ", "").length();
            placeholders.put("spaces", (double) numOfSpaces);
            points = pointsCalculator.getPoints(placeholders, OperationSet.getDefaultSet());
        } else {
            points = calculateDefaultPoints(word);
        }

        return Math.max(1, points);
    }

    protected abstract int calculateDefaultPoints(String word);

    protected Word getCurrentWord() {
        return currentWord;
    }
    protected void setCurrentWord(Word word) {
        this.currentWord = word;
        unformattedWord = word.word;

        if (this.currentWord instanceof GeneratedWord generatedWord
                && !generatedWord.isFormatted()) {
            WordGenerator generator = generatedWord.getGenerator();

            currentWord = new GeneratedWord(formatWord(word.word), generator, generatedWord.getHint());
            currentWord.setPoints(word.getPoints());
        }

        currentPoints = word.getPoints();
    }

    @Override
    public void endWinner(Player player, String guess) {
        GameMessenger.broadcast(player.getName() + " won in " + GameController.getLastRoundStartedString() + "! The answer was: &h" + unformattedWord);
    }

    @Override
    public void endNoWinner() {
        GameMessenger.broadcast("Nobody got the word in time! The word was: &h" + unformattedWord);
    }

    @Override
    public List<String> getOptionCompletions() {
        return GeneratorManager.getIds();
    }
}
