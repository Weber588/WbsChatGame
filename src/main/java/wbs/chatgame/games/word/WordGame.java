package wbs.chatgame.games.word;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import wbs.chatgame.GameController;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.word.generator.GeneratorManager;
import wbs.chatgame.games.word.generator.WordGenerator;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;

import java.util.*;

public abstract class WordGame extends Game {

    public WordGame(String gameName, ConfigurationSection section, String directory) {
        super(gameName, section, directory);

        ChatGameSettings settings = WbsChatGame.getInstance().settings;

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

    private final List<Word> customWords = new LinkedList<>();
    private final Map<WordGenerator, Double> generators = new HashMap<>();

    // Track history to prevent repetition
    private final List<Word> history = new LinkedList<>();

    private double generationChance;

    private Word word;

    @Override
    protected final void start() {
        word = getWord();

        startGame(word);
    }

    @Override
    public List<String> getAnswers() {
        return new LinkedList<>(Collections.singletonList(word.word));
    }

    protected Word getWord() {
        Word word;

        if (WbsMath.chance(generationChance)) {
            word = generateWord();
        } else {
            word = getCustomWord();
        }

        currentPoints = word.getPoints();

        return word;
    }

    protected Word getCustomWord() {
        Word word;
        do {
            word = WbsCollectionUtil.getRandom(customWords);
        } while (history.contains(word));

        history.add(word);
        if (history.size() > customWords.size() / 2) {
            history.remove(0);
        }
        return word;
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
        String generated = generator.getNext();

        Word word = new Word(generated, generator);
        int points = Math.max(1, calculatePoints(word) + generator.getPointsModifier());
        word.setPoints(points);

        return word;
    }

    @Override
    public boolean checkGuess(String guess) {
        return guess.equalsIgnoreCase(word.word);
    }

    protected abstract void startGame(Word wordToGuess);

    protected abstract int calculatePoints(Word word);

    protected Word getCurrentWord() {
        return word;
    }

    @Override
    public void endWinner(Player player, String guess) {
        GameController.broadcast(player.getName() + " won in " + GameController.getLastRoundStartedString() + "! The answer was: &h" + word.word);
    }

    @Override
    public void endNoWinner() {
        GameController.broadcast("Nobody got the word in time! The word was: &h" + word.word);
    }
}
