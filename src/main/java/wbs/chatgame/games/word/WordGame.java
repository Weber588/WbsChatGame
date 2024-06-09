package wbs.chatgame.games.word;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.math.ConditionalPointsCalculator;
import wbs.chatgame.games.math.EquationGenerator;
import wbs.chatgame.games.word.generator.GeneratorManager;
import wbs.chatgame.games.word.generator.WordGenerator;
import wbs.utils.exceptions.InvalidConfigurationException;

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

                customWords.add(new Word(word, points));
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

                    wordGenerators.put(generator, chance);
                }
            }
        }

        if (wordGenerators.isEmpty() && generationChance > 0) {
            plugin.logger.info("No valid generators set for " + gameName + "; generation disabled.");
            generationChance = 0;
        }

        if (generationChance >= 100 && wordGenerators.isEmpty()) {
            settings.logError("Custom list disabled with no generators set. Disabling game " + gameName, directory);
            throw new InvalidConfigurationException();
        } else if (generationChance <= 0 && customWords.isEmpty()) {
            settings.logError("Generators disabled with no custom words set. Disabling game " + gameName, directory);
            throw new InvalidConfigurationException();
        } else if (wordGenerators.isEmpty() && customWords.isEmpty()) {
            settings.logError("No generators or custom words specified. Disabling game " + gameName, directory);
            throw new InvalidConfigurationException();
        }

        plugin.logger.info("Loaded " + customWords.size() + " custom words and " + wordGenerators.size() + " generators for " + gameName);
    }

    protected WordGame(WordGame copy) {
        super(copy);

        customWords.addAll(copy.customWords);
        history.addAll(copy.history);

        wordGenerators.putAll(copy.wordGenerators);
        generationChance = copy.generationChance;
        capitalizeFirstThreshold = copy.capitalizeFirstThreshold;
        capitalizeAllThreshold = copy.capitalizeAllThreshold;

        pointsCalculator = copy.pointsCalculator;
    }

    private final List<Word> customWords = new LinkedList<>();
    // Track history to prevent repetition
    private final List<Word> history = new LinkedList<>();

    private final Map<WordGenerator, Double> wordGenerators = new HashMap<>();
    private double generationChance;
    private int capitalizeFirstThreshold = 0;
    private int capitalizeAllThreshold = 0;

    private final ConditionalPointsCalculator pointsCalculator;


    public ConditionalPointsCalculator getPointsCalculator() {
        return pointsCalculator;
    }

    @Override
    public List<String> getOptionCompletions() {
        return GeneratorManager.getIds();
    }

    public int getCapitalizeFirstThreshold() {
        return capitalizeFirstThreshold;
    }

    public int getCapitalizeAllThreshold() {
        return capitalizeAllThreshold;
    }

    public Map<WordGenerator, Double> getWordGenerators() {
        return new HashMap<>(wordGenerators);
    }

    public double getGenerationChance() {
        return generationChance;
    }

    @NotNull
    public final List<Word> getCustomWords() {
        return new LinkedList<>(customWords);
    }

    @NotNull
    public final List<Word> getHistory() {
        return new LinkedList<>(history);
    }
}
