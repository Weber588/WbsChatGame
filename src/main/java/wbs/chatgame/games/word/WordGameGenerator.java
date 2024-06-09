package wbs.chatgame.games.word;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.QuestionGenerator;
import wbs.chatgame.games.GameQuestion;
import wbs.chatgame.games.math.ConditionalPointsCalculator;
import wbs.chatgame.games.math.OperationSet;
import wbs.chatgame.games.word.generator.GeneratedWord;
import wbs.chatgame.games.word.generator.GeneratorManager;
import wbs.chatgame.games.word.generator.WordGenerator;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;
import wbs.utils.util.string.WbsStrings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class WordGameGenerator<T extends WordGame> extends QuestionGenerator<T> {
    protected ConditionalPointsCalculator pointsCalculator;

    private final Map<WordGenerator, Double> wordGenerators;
    private double generationChance;
    private int capitalizeFirstThreshold = 0;
    private int capitalizeAllThreshold = 0;

    public WordGameGenerator(T parent) {
        super(parent);

        pointsCalculator = parent.getPointsCalculator();
        wordGenerators = parent.getWordGenerators();
        generationChance = parent.getGenerationChance();
        capitalizeAllThreshold = parent.getCapitalizeAllThreshold();
        capitalizeFirstThreshold = parent.getCapitalizeFirstThreshold();
    }

    @Override
    public final WordGameQuestion generateQuestion() {
        return generateQuestion(getWord());
    }

    @Override
    public @Nullable WordGameQuestion generateWithOptions(@NotNull List<String> options) {
        if (options.isEmpty()) {
            return generateQuestion();
        }

        String id = options.get(0);

        Word wordToGuess;

        WordGenerator generator = GeneratorManager.getGenerator(id);
        if (generator != null) {
            wordToGuess = getGeneratedWord(generator);
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
                wordToGuess = new Word(customWord, calculatePoints(customWord));
            } else {
                customWord = String.join(" ", options.subList(0, options.size() - 1));
                wordToGuess = new Word(customWord, customPoints);
            }
        }

        return generateQuestion(wordToGuess);
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
        List<Word> customWords = parent.getCustomWords();
        return WbsCollectionUtil.getAvoidRepeats(
                () -> WbsCollectionUtil.getRandom(customWords),
                customWords.size(),
                parent.getHistory(),
                2);
    }



    /**
     * Generate a word from the specified generators.
     * @return The generated word, or null if no generators were specified.
     */
    protected Word generateWord() {
        if (wordGenerators.isEmpty()) {
            return null;
        }
        WordGenerator generator = WbsCollectionUtil.getRandomWeighted(wordGenerators);

        return getGeneratedWord(generator);
    }

    protected GeneratedWord getGeneratedWord(@NotNull WordGenerator generator) {
        return generator.getNext(this::calculatePoints);
    }

    protected abstract @NotNull WordGameQuestion generateQuestion(Word wordToGuess);

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


    protected String formatWord(GeneratedWord word) {
        String display = word.word.toLowerCase();

        if (display.length() >= parent.getCapitalizeFirstThreshold()) {
            if (word.isFormatted() && display.length() > 1) {
                display = word.word.charAt(0) + display.substring(1);
            } else {
                display = WbsStrings.capitalize(display);
            }
        }

        if (display.length() >= parent.getCapitalizeAllThreshold()) {
            if (word.isFormatted() && display.length() > 1) {
                // Also splitting on ' for things like "Jack o'Lantern"
                String[] words = word.word.split("[\s']");

                int index = 0;
                StringBuilder newWord = new StringBuilder();
                for (String subWord : words) {
                    newWord.append(word.word.charAt(index))
                            .append(subWord.substring(1));
                    index += subWord.length() + 1;

                    if (index - 1 > 0 && index - 1 < word.word.length()) {
                        newWord.append(word.word.charAt(index - 1));
                    }
                }

                display = newWord.toString();
            } else {
                display = WbsStrings.capitalizeAll(display);
            }
        }

        return display;
    }
}
