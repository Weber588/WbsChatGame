package wbs.chatgame.games.word.generator;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsCollectionUtil;

import java.util.*;

public abstract class WordGenerator {

    protected WordGenerator() {
        generated = new HashSet<>(generateWords());
    }

    protected abstract List<GeneratedWord> generateWords();

    private final Set<GeneratedWord> generated;
    private final List<GeneratedWord> history = new LinkedList<>();
    private int pointsModifier;

    @NotNull
    public GeneratedWord getRandomWord() {
        return WbsCollectionUtil.getRandom(generated);
    }

    @NotNull
    public GeneratedWord getNext() {
        Set<GeneratedWord> words = generated;

        GeneratedWord word;
        do {
            word = WbsCollectionUtil.getRandom(words);
        } while (history.contains(word));

        history.add(word);
        if (history.size() > words.size() / 2) {
            history.remove(0);
        }
        return word;
    }

    public int size() {
        return generated.size();
    }

    public void configure(ConfigurationSection genSection) {
        generated.clear();
        generated.addAll(generateWords());

        pointsModifier = genSection.getInt("points-modifier", 0);

        List<String> ignoreExact = genSection.getStringList("ignore-exact");
        List<String> ignoreContains = genSection.getStringList("ignore-contains");
        List<String> include = genSection.getStringList("include");

        List<GeneratedWord> toRemove = new LinkedList<>();

        for (GeneratedWord word : generated) {
            boolean removed = false;
            for (String ignore : ignoreExact) {
                if (word.word.equalsIgnoreCase(ignore)) {
                    toRemove.add(word);
                    removed = true;
                    break;
                }
            }

            if (removed) continue;

            for (String ignore : ignoreContains) {
                if (word.word.toLowerCase().contains(ignore.toLowerCase())) {
                    toRemove.add(word);
                //    removed = true;
                    break;
                }
            }
        }

        toRemove.forEach(generated::remove); // Faster, according to IntelliJ.
        include.forEach(word -> generated.add(new GeneratedWord(word, this)));
    }

    public int getPointsModifier() {
        return pointsModifier;
    }

    @NotNull
    public String getGenericHint() {
        return "This word is a type of " + GeneratorManager.getRegisteredId(this);
    }

    public Set<GeneratedWord> getAll() {
        return generated;
    }
}
